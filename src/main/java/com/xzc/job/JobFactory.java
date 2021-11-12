package com.xzc.job;

import com.xzc.Job;

import java.util.concurrent.Callable;

/**
 * @author Ray
 * @date created in 2021/11/7 9:22
 */
public abstract class JobFactory {

    public static Job newRunnableJob(String description, Runnable task) {
        return new RunnableJob<>(description, task);
    }

    public static <T> Job newCallableJob(String description, Callable<T> task) {
        return new CallableJob<>(description, task);
    }
}
