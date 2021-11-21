package com.xzc.schedule;

import com.xzc.Context;
import com.xzc.Job;
import com.xzc.Scheduler;
import com.xzc.Trigger;
import com.xzc.annotation.ScheduleAnnotationScanner;
import com.xzc.job.JobFactory;
import com.xzc.queue.ScheduleQueue;
import com.xzc.trigger.TriggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2021/11/7 9:51
 */
public class StdScheduler implements Scheduler, Runnable {

    private ThreadPoolExecutor executor;
    private final ScheduleQueue taskQueue;
    private boolean running;
    private final ScheduleConfig config;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Class<?>> clazzList = new ArrayList<>();
    private final BlockingQueue<Task> fireQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean fireThreadStatus = new AtomicBoolean(false);

    public StdScheduler() {
        this.taskQueue = new ScheduleQueue();
        this.config = ScheduleConfig.parseConfig();
    }

    @Override
    public void start() {
        this.executor = new ThreadPoolExecutor(config.getCoreThreadNum(), config.getMaxThreadNum(),
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        startAnnotationSchedule();
        this.running = true;
        startScheduleThread();
    }


    private void startAnnotationSchedule() {
        if (clazzList.isEmpty()) {
            return;
        }

        ScheduleAnnotationScanner scanner = new ScheduleAnnotationScanner();
        scanner.scan(clazzList)
                .stream()
                .flatMap(List::stream)
                .forEach(entry -> {
                    Job job = JobFactory.newRunnableJob(entry.getDesc(), entry::invoke);
                    Trigger trigger = TriggerFactory.newCronTrigger(entry.getCron());
                    scheduleJob(job, trigger);
                });
    }

    /**
     * start schedule daemon thread for submit task
     */
    private void startScheduleThread() {
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    @Override
    public boolean isRunning() {
        return !this.executor.isShutdown() && this.running;
    }

    @Override
    public void scheduleJob(Job job, Trigger trigger) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(trigger, "trigger must not be null");

        findTask(job.key()).ifPresent(ignore -> {
            throw new IllegalStateException(job.description() + " task is exist");
        });

        // merge job and trigger context
        Context context = job.context().merge(trigger.context());
        context.set("trigger", this);
        trigger.setContext(context);

        long priority = trigger.nextFireTime(System.currentTimeMillis());
        Task task = new Task(job, trigger, priority, false);
        taskQueue.offer(task);
    }

    @Override
    public boolean deleteJob(int key) {
        AtomicBoolean result = new AtomicBoolean(false);

        findTask(key).ifPresent(task -> {
            task.setCancel(true);
            taskQueue.remove(task);
            System.out.printf("remove %s task\n", task.getJob().description());
            result.set(true);
        });

        return result.get();
    }

    private Optional<Task> findTask(int key) {
        lock.lock();
        try {
            return taskQueue.stream()
                    .filter(task -> task.getJob().key() == key)
                    .findFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        this.running = false;
        this.executor.shutdown();
        this.countDownLatch.countDown();
    }

    @Override
    public void waitShutdown() throws InterruptedException {
        this.countDownLatch.await();
    }

    @Override
    public void register(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        this.clazzList.add(clazz);
    }

    @Override
    public void run() {
        while (running) {
            Task task = taskQueue.peek();
            Objects.requireNonNull(task, "task must not be null");
            if (task.isCancel()) {
                taskQueue.poll();
                continue;
            }

            // 不加锁的话，会导致 findTask 无法获取指定的 task，从而删除不了任务
            // 因为执行任务后，会 poll 任务，到后面的 offer task 时会有一个空档期
            // 如果在该空档期获取 task，会使得 task 在 queue 中无法找到
            lock.lock();
            boolean sleep = false;
            try {
                long timeMillis = System.currentTimeMillis();
                if (timeMillis > task.getPriority()) {
                    this.executeTask(taskQueue.poll());
                } else if (task.getPriority() - timeMillis <= 50) {
                    this.fireQueue.offer(Objects.requireNonNull(taskQueue.poll()));
                    if (!fireThreadStatus.get()) {
                        this.executor.submit(new FireRunnable(Duration.ofSeconds(60)));
                        this.fireThreadStatus.set(true);
                    }
                } else {
                    sleep = true;
                }
            } finally {
                this.lock.unlock();
            }

            if (sleep) {
                sleep(Duration.ofMillis(50));
            }
        }
    }

    private void executeTask(Task task) {
        if (task == null || task.isCancel()) {
            return;
        }

        this.executor.submit(() -> task.getJob().execute());
        // reschedule task
        long nextFireTime = task.getTrigger().nextFireTime(task.getPriority());
        if (nextFireTime != -1) {
            task.setPriority(nextFireTime);
            taskQueue.offer(task);
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class FireRunnable implements Runnable {

        private Date clock = new Date();
        private final Duration duration;

        public FireRunnable(Duration duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            while (!isTimeout()) {
                Task task = fireQueue.peek();
                if (task == null) {
                    continue;
                }

                if (System.currentTimeMillis() > task.getPriority()) {
                    executeTask(fireQueue.poll());
                    this.clock = new Date();
                }

                sleep(Duration.ofMillis(1));
            }

            fireThreadStatus.set(false);
        }

        private boolean isTimeout() {
            return clock.getTime() - System.currentTimeMillis()
                    > duration.toMillis();
        }
    }

}
