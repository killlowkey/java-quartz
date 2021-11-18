package com.xzc.corn;

import lombok.Data;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xzc.corn.CronExpressionParser.*;
import static com.xzc.util.CommonUtil.index;
import static java.lang.Integer.parseInt;

/**
 * @author Ray
 * @date created in 2021/11/9 17:02
 */
@Data
public class CronExpressionImpl implements CronExpression {

    private String expression;
    private CronField[] fields;
    private int lastDefined;
    private int maxDays;
    private boolean done;

    private boolean minuteBump;
    private boolean hourBump;
    private boolean dayBump;
    private boolean monthBump;
    private boolean yearBump;

    public CronExpressionImpl(String expression, CronField[] fields, int lastDefined) {
        this.expression = expression;
        this.fields = fields;
        this.lastDefined = lastDefined;
    }

    @Override
    public Date nextTime() {
        return nextTime(new Date());
    }

    @Override
    public List<Date> nextTime(int num) {
        List<Date> res = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                res.add(nextTime());
            } else {
                Date prevDate = res.get(i - 1);
                res.add(nextTime(prevDate));
            }
        }
        return res;
    }


    private Date nextTime(Date date) {
        if (date == null) {
            date = new Date();
        }

        String[] ttok = date.toString().split(" ");
        String[] hms = ttok[3].split(":");
        this.maxDays = maxDays(monthIndex(ttok[1]), parseInt(ttok[5]));

        String second = nextSeconds(parseInt(hms[2]), fields[0]);
        String minute = nextMinutes(parseInt(hms[1]), fields[1]);
        String hour = nextHours(parseInt(hms[0]), fields[2]);
        String dayOfMonth = alignDigit(nextDay(index(days, ttok[0]), fields[5], parseInt(ttok[2]), fields[3]), "0");
        String month = nextMonth(ttok[1], fields[4]);
        String year = nextYear(ttok[5], fields[6]);

        String str = String.format("%s %s %s:%s:%s %s %s", month, dayOfMonth,
                hour, minute, second, getSimpleTimeZone(), year);

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd HH:mm:ss zzz yyyy", Locale.US);
            this.reset();
            return simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int monthIndex(String month) {
        Objects.requireNonNull(month, "month must not be null");

        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(month)) {
                return i;
            }
        }

        return -1;
    }

    private String nextSeconds(int prev, CronField field) {
        AtomicBoolean ab = new AtomicBoolean();
        int next = findNextValue(prev, field.values, ab);
        this.minuteBump = ab.get();
        this.setDone(Constant.SECOND);
        return alignDigit(next, "0");
    }

    private String nextMinutes(int prev, CronField field) {
        AtomicBoolean ab = new AtomicBoolean();

        if (field.isEmpty() && lastSet(Constant.MINUTE)) {
            if (minuteBump) {
                int next = bumpValue(prev, 59, 1, ab);
                this.hourBump = ab.get();
                return alignDigit(next, "0");
            }

            return alignDigit(prev, "0");
        }

        int next = findNextValue(prev, field.values, ab);
        this.hourBump = ab.get();
        this.setDone(Constant.MINUTE);
        return alignDigit(next, "0");
    }

    private String nextHours(int prev, CronField field) {
        AtomicBoolean ab = new AtomicBoolean();
        if (field.isEmpty() && lastSet(Constant.HOUR)) {
            if (this.hourBump) {
                int next = bumpValue(prev, 23, 1, ab);
                this.dayBump = ab.get();
                return alignDigit(next, "0");
            }
            return alignDigit(prev, "0");
        }

        int next = findNextValue(prev, field.values, ab);
        this.dayBump = ab.get();
        this.setDone(Constant.HOUR);
        return alignDigit(next, "0");
    }

    private int nextDay(int preWeek, CronField weekField, int prevMonth, CronField monthField) {
        AtomicBoolean ab = new AtomicBoolean();
        if (weekField.isEmpty() && monthField.isEmpty() && lastSet(Constant.DAY_OF_MONTH)) {
            if (dayBump) {
                int nexMonth = bumpValue(prevMonth, maxDays, 1, ab);
                this.monthBump = ab.get();
                return nexMonth;
            }

            return prevMonth;
        }

        if (monthField.getValues().length > 0) {
            int nextMonth = findNextValue(prevMonth, monthField.getValues(), ab);
            this.monthBump = ab.get();
            this.setDone(Constant.DAY_OF_MONTH);
            return nextMonth;
        } else if (weekField.getValues().length > 0) {
            int nextWeek = findNextValue(prevMonth, weekField.values, ab);
            this.setDone(Constant.DAY_OF_WEEK);
            int step;

            if (weekField.getValues().length == 1 && weekField.getValues()[0] < preWeek) {
                ab.set(false);
            }

            if (ab.get() && weekField.getValues().length == 1) {
                step = 7;
            } else {
                step = step(preWeek, nextWeek, 7);
            }
            int nextMonth = bumpValue(prevMonth, maxDays, step, ab);
            this.monthBump = ab.get();
            return nextMonth;
        }

        return prevMonth;
    }

    private String nextMonth(String prev, CronField field) {
        AtomicBoolean ab = new AtomicBoolean();

        if (field.isEmpty() && lastSet(Constant.DAY_OF_WEEK)) {
            if (monthBump) {
                int next = bumpLiteral(index(months, prev), 12, 1, ab);
                this.yearBump = ab.get();
                return months[next];
            }
            return prev;
        }

        int next = findNextValue(index(months, prev), field.values, ab);
        this.yearBump = ab.get();
        this.setDone(Constant.MONTH);
        return months[next];
    }

    private String nextYear(String prev, CronField field) {
        AtomicBoolean ab = new AtomicBoolean();
        if (field.isEmpty() && lastSet(Constant.YEAR)) {
            if (yearBump) {
                // int(^uint(0)>>1) = -1
                int next = bumpValue(prev, -1, 1, ab);
                return String.valueOf(next);
            }

            return prev;
        }

        int next = findNextValue(prev, field.getValues(), ab);
        if (ab.get()) {
            throw new CronParseException("Out of expression range error");
        }

        return String.valueOf(next);
    }

    private int maxDays(int month, int year) {
        if (month == 2 && isLeapYear(year)) {
            return 29;
        }

        return daysInMonth[month];
    }

    private boolean isLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        } else if (year % 100 != 0) {
            return true;
        } else {
            return year % 400 == 0;
        }
    }

    private int findNextValue(Object prev, int[] values, AtomicBoolean ab) {
        int iprev;

        if (prev instanceof String) {
            iprev = parseInt((String) prev);
        } else if (prev instanceof Integer) {
            iprev = (int) prev;
        } else {
            throw new CronParseException("Unknown type at findNextValue");
        }

        if (values.length == 0) {
            ab.set(false);
            return iprev;
        }

        // 从 values 中查找大于 prev 元素
        for (int value : values) {
            if (this.done) {
                if (value >= iprev) {
                    ab.set(false);
                    return value;
                }
            } else {
                if (value > iprev) {
                    ab.set(false);
                    this.done = true;
                    return value;
                }
            }
        }

        ab.set(true);
        return values[0];
    }

    private void setDone(Constant constant) {
        if (this.lastDefined == constant.getIndex()) {
            this.done = true;
        }
    }

    // 补齐两位
    private String alignDigit(int next, String prefix) {
        if (next < 10) {
            return prefix + next;
        }

        return String.valueOf(next);
    }

    private boolean lastSet(Constant constant) {
        return this.lastDefined <= constant.getIndex();
    }

    private int bumpValue(Object prev, int max, int step, AtomicBoolean ab) {
        int iprev;
        if (prev instanceof String) {
            iprev = parseInt((String) prev);
        } else if (prev instanceof Integer) {
            iprev = (int) prev;
        } else {
            throw new CronParseException("Unknown type at bumpValue");
        }

        int bumped = iprev + step;
        if (bumped > max) {
            ab.set(true);
            return bumped % max;
        }

        ab.set(false);
        return bumped;
    }

    private int step(int prev, int next, int max) {
        int diff = next - prev;
        if (diff < 0) {
            return diff + max;
        }

        return diff;
    }

    private int bumpLiteral(int iprev, int max, int step, AtomicBoolean ab) {
        int bumped = iprev + step;
        if (bumped > max) {
            if (bumped % max == 0) {
                ab.set(true);
                return iprev;
            }

            ab.set(true);
            return bumped % max;
        }

        ab.set(false);
        return bumped;
    }

    public String getSimpleTimeZone() {
        Calendar now = Calendar.getInstance();
        TimeZone timeZone = now.getTimeZone();
        return timeZone.getDisplayName(false, TimeZone.SHORT, Locale.US);
    }

    private void reset() {
        this.lastDefined = 0;
        this.maxDays = 0;
        this.done = false;
        this.minuteBump = false;
        this.hourBump = false;
        this.dayBump = false;
        this.monthBump = false;
        this.yearBump = false;
    }

    @Getter
    enum Constant {
        SECOND(0),
        MINUTE(1),
        HOUR(2),
        DAY_OF_MONTH(3),
        MONTH(4),
        DAY_OF_WEEK(5),
        YEAR(6);

        private final int index;

        Constant(int index) {
            this.index = index;
        }

    }
}
