package com.xzc.job;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Ray
 * @date created in 2021/11/7 9:31
 */
public class CallableJob<T> extends AbstractJob<T> {

    public final Callable<T> task;

    public CallableJob(String description, Callable<T> task) {
        super(description);

        Objects.requireNonNull(task, "task must not be null");
        this.task = task;
    }

    @Override
    public T runTask() throws Exception {
        return this.task.call();
    }

}
