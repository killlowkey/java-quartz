package com.xzc;

import com.xzc.schedule.ContextImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ray
 * @date created in 2021/11/7 15:21
 */
public interface Context {

    Context set(String key, Object value);

    List<String> keys();

    <T> T value(String key, Class<T> type);

    default Context merge(Context context) {
        if (context != null) {
            context.keys().forEach(key -> set(key, value(key, Object.class)));
        }
        return this;
    }

    static Context create() {
        return new ContextImpl();
    }

    static Context create(Map<String, Object> data) {
        Objects.requireNonNull(data, "data must not be null");
        return new ContextImpl(data);
    }

    static Context of(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        Context context = create();
        context.set(key, value);
        return context;
    }


}
