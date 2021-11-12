package com.xzc.trigger;

import com.xzc.Trigger;

import java.time.Duration;

/**
 * @author Ray
 * @date created in 2021/11/7 10:55
 */
public class TriggerFactory {

    public static Trigger newRunOnceTrigger(Duration duration) {
        return new RunOnceTrigger(duration);
    }

    public static Trigger newSimpleTrigger(Duration interval) {
        return new SimpleTrigger(interval);
    }

    public static Trigger newTimeStrTrigger(String timeStr) {
        return new TimeStrTrigger(timeStr);
    }

    public static Trigger newCronTrigger(String expression) {
        return new CronTrigger(expression);
    }

}
