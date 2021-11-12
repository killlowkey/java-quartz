package com.xzc.trigger;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Ray
 * @date created in 2021/11/7 10:30
 */
public class RunOnceTrigger extends AbstractTrigger {

    private boolean expired;
    private final Duration duration;

    public RunOnceTrigger(Duration duration) {
        super("Run Once Trigger");

        Objects.requireNonNull(duration, "timeUnit must not be null");
        this.duration = duration;
    }

    @Override
    public long nextFireTime(long prev) {
        if (!expired) {
            this.expired = true;
            return prev + this.duration.toMillis();
        }

        return -1;
    }


}
