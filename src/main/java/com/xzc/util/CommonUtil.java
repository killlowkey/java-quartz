package com.xzc.util;

import com.xzc.corn.CronParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Ray
 * @date created in 2021/11/7 9:13
 */
public class CommonUtil {

    private CommonUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String formatDate(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(date);
    }

    public static String formatMillis(long millis) {
        return formatDate(new Date(millis));
    }

    public static ZoneOffset getDefaultZoneOffset() {
        OffsetDateTime odt = OffsetDateTime.now(ZoneId.systemDefault());
        return odt.getOffset();
    }

    // utils.go#intVal
    public static int index(String[] target, String search) {
        for (int i = 0; i < target.length; i++) {
            if (target[i].equals(search)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean inScope(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int[] stringToInt(String[] source) throws NumberFormatException {
        int[] target = new int[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = Integer.parseInt(source[i]);
        }

        return target;
    }

    public static int[] indexes(String[] search, String[] target) {
        int[] searchIndexes = new int[search.length];
        for (int i = 0; i < search.length; i++) {
            int index = index(target, search[i]);
            if (index == -1) {
                throw new CronParseException(String.format("Invalid cron field: %s", search[i]));
            }

            searchIndexes[i] = index;
        }

        return searchIndexes;
    }

    public static Object[] append(Object[] source, Object value) {
        Object[] result = new Object[source.length + 1];
        System.arraycopy(source, 0, result, 0, source.length);
        result[result.length - 1] = value;
        return result;
    }

    public static int normalize(String field, String[] tr) {
        try {
            return Integer.parseInt(field);
        } catch (NumberFormatException ignored) {
            return index(tr, field);
        }
    }

    public static int[] fillRange(int from, int to) {
        if (to < from) {
            throw new CronParseException("fillRange");
        }

        int len = (to - from) + 1;
        int[] arr = new int[len];

        for (int i = from, j = 0; i <= to; i = i + 1, j = j + 1) {
            arr[j] = i;
        }

        return arr;
    }

    public static int[] fillStep(int from, int step, int max) {
        if (max < from) {
            throw new CronParseException("fillStep");
        }

        int len = ((max - from) / step) + 1;
        int[] arr = new int[len];

        for (int i = from, j = 0; i <= max; i = i + step, j = j + 1) {
            arr[j] = i;
        }

        return arr;
    }

}
