package com.xzc.schedule;

import com.xzc.Context;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ray
 * @date created in 2021/11/7 15:26
 */
public class ContextImpl implements Context {

    private final Map<String, Object> data;
    private final ReentrantLock lock = new ReentrantLock();

    public ContextImpl() {
        this(new HashMap<>());
    }

    public ContextImpl(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public Context set(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        this.lock.lock();
        this.data.put(key, value);
        this.lock.unlock();

        return this;
    }


    @Override
    public List<String> keys() {
        return new ArrayList<>(this.data.keySet());
    }

    @Override
    public <T> T value(String key, Class<T> type) {
        Object res = this.data.get(key);
        if (res == null) {
            return null;
        }

        return type.cast(res);
    }
}
