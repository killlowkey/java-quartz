package com.xzc.trigger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.xzc.util.CommonUtil.getDefaultZoneOffset;
import static com.xzc.util.CommonUtil.isEmpty;

/**
 * @author Ray
 * @date created in 2021/11/7 10:46
 */
public class TimeStrTrigger extends AbstractTrigger {

    private final LocalDateTime localDateTime;
    private boolean expired;

    public TimeStrTrigger(String timeStr) {
        super("TimeStr Trigger");

        if (isEmpty(timeStr)) {
            throw new IllegalStateException("timeStr is empty");
        }

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        this.localDateTime = LocalDateTime.parse(timeStr, pattern);

        if (System.currentTimeMillis() > toMilli()) {
            throw new IllegalArgumentException(timeStr + " is expired");
        }
    }

    @Override
    public long nextFireTime(long prev) {
        if (!expired) {
            this.expired = true;
            return toMilli();
        }

        return -1;
    }

    public long toMilli() {
        return this.localDateTime.toInstant(getDefaultZoneOffset()).toEpochMilli();
    }
}
