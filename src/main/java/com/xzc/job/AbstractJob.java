package com.xzc.job;

import com.xzc.Context;
import com.xzc.Job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.xzc.util.CommonUtil.isEmpty;

/**
 * @author Ray
 * @date created in 2021/11/7 9:06
 */
public abstract class AbstractJob<T> implements Job {

    private final String description;
    private final List<Record<T>> records;
    private final Context context;

    AbstractJob(String description) {
        if (isEmpty(description)) {
            throw new IllegalStateException("description is empty");
        }

        this.description = description;
        this.records = new ArrayList<>();
        this.context = Context.of("job", this).set("records", records);
    }

    @Override
    public void execute() {
        Record<T> record = initRecord();

        try {
            T result = runTask();
            record.setResult(result);
            record.setStatus(JobStatus.OK);
        } catch (Exception ex) {
            record.setEx(ex);
            record.setStatus(JobStatus.FAILURE);
        } finally {
            record.setEndTime(new Date());
            records.add(record);
        }

    }

    private Record<T> initRecord() {
        Record<T> record = new Record<>();
        record.setStartTime(new Date());
        record.setStatus(JobStatus.NA);
        record.setThreadName(Thread.currentThread().getName());
        return record;
    }


    public abstract T runTask() throws Exception;

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public int key() {
        return this.description.hashCode();
    }

    @Override
    public Context context() {
        return this.context;
    }
}
