package com.xzc.schedule;

import com.xzc.Context;
import com.xzc.Job;
import com.xzc.Scheduler;
import com.xzc.Trigger;
import com.xzc.annotation.ScheduleAnnotationScanner;
import com.xzc.job.JobFactory;
import com.xzc.queue.ScheduleQueue;
import com.xzc.trigger.TriggerFactory;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public StdScheduler() {
        this.taskQueue = new ScheduleQueue();
        this.config = ScheduleConfig.parseConfig();
    }

    @Override
    public void start() {
        this.executor = new ThreadPoolExecutor(config.getCoreThreadNum(), config.getMaxThreadNum(),
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        scanAnnotationSchedule();
        this.running = true;
        startScheduleThread();
        Logger.info("scheduler started successfully");
    }


    private void scanAnnotationSchedule() {
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

        // 获取下次运行时间
        long nextFireTime = trigger.nextFireTime(System.currentTimeMillis());
        Task task = new Task(job, trigger, nextFireTime, false);
        taskQueue.offer(task);
    }

    @Override
    public boolean deleteJob(int key) {
        AtomicBoolean result = new AtomicBoolean(false);

        findTask(key).ifPresent(task -> {
            task.setCancel(true);
            taskQueue.remove(task);
            Logger.info("remove {} task", task.getJob().description());
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
            while (!taskQueue.isEmpty()) {
                // 阻塞 peek，没有数据则进行等待
                Task task = taskQueue.peek();

                // 任务取消
                if (task.isCancel()) {
                    taskQueue.poll();
                    continue;
                }

                long timeMillis = System.currentTimeMillis();
                // task 运行时间大于当前时间，则无需继续处理
                if (timeMillis < task.getPriority()) {
                    break;
                }

                // 任务出队时，需要加锁处理
                try {
                    lock.lock();
                    executeTask(taskQueue.poll());
                } finally {
                    lock.unlock();
                }
            }

            sleep(Duration.ofMillis(50));
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

}
