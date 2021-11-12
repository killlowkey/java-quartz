package com.xzc;

/**
 * @author Ray
 * @date created in 2021/11/7 9:02
 */
public interface Scheduler {

    void start();

    boolean isRunning();

    void scheduleJob(Job job, Trigger trigger);

    boolean deleteJob(int key);

    void stop();

    void waitShutdown() throws InterruptedException;

    void register(Class<?> clazz);

}
