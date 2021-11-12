package com.xzc.job;

import java.util.Objects;

/**
 * @author Ray
 * @date created in 2021/11/7 9:28
 */
public class RunnableJob<T> extends AbstractJob<T> {

    private final Runnable task;

    public RunnableJob(String description, Runnable task) {
        super(description);

        Objects.requireNonNull(task, "task must not be null");
        this.task = task;
    }

    @Override
    public T runTask() throws Exception {
        this.task.run();
        return null;
    }

}
