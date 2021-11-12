package com.xzc.queue;

import com.xzc.schedule.Task;
import com.xzc.util.CommonUtil;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2021/11/7 21:05
 */
public class ScheduleQueue extends PriorityQueue<Task> {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition empty = lock.newCondition();

    @Override
    public boolean offer(Task task) {
        lock.lock();
        try {
            System.out.printf("【%s】下次运行时间：%s\n", task.getJob().description(),
                    CommonUtil.formatMillis(task.getPriority()));
            return super.offer(task);
        } finally {
            empty.signalAll();
            lock.unlock();
        }
    }

    @Override
    public Task peek() {
        lock.lock();
        try {
            while (isEmpty()) {
                empty.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return super.peek();
    }

    @Override
    public Task poll() {
        lock.lock();
        try {
            while (isEmpty()) {
                empty.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return super.poll();
    }
}
