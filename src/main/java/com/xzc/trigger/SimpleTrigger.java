package com.xzc.trigger;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Ray
 * @date created in 2021/11/7 10:44
 */
public class SimpleTrigger extends AbstractTrigger {

    private final Duration interval;

    public SimpleTrigger(Duration interval) {
        super("Simple Interval Trigger");

        Objects.requireNonNull(interval, "interval must not be null");
        this.interval = interval;
    }

    @Override
    public long nextFireTime(long prev) {
        return prev + interval.toMillis();
    }

}
