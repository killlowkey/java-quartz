package com.xzc.trigger;


import com.xzc.corn.CronExpression;
import com.xzc.corn.CronExpressionParser;

/**
 * @author Ray
 * @date created in 2021/11/7 16:28
 */
public class CronTrigger extends AbstractTrigger {

    private final CronExpression cronExpression;
    private static final CronExpressionParser parser = new CronExpressionParser();

    public CronTrigger(String expression) {
        super("cron trigger");
        this.cronExpression = parser.parse(expression);
    }

    @Override
    public long nextFireTime(long prev) {
        return cronExpression.nextTime()
                .toInstant().toEpochMilli();
    }

}
