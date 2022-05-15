package com.xzc.corn;

import java.util.Arrays;
import java.util.Map;

import static com.xzc.util.CommonUtil.*;
import static java.lang.Integer.parseInt;

/**
 * cron expression parse
 * <p>
 * the following examples from https://github.com/reugn/go-quartz
 * "0 0 12 * * ?"           Fire at 12pm (noon) every day
 * "0 15 10 ? * *"          Fire at 10:15am every day
 * "0 15 10 * * ?"          Fire at 10:15am every day
 * "0 15 10 * * ? *"        Fire at 10:15am every day
 * "0 * 14 * * ?"           Fire every minute starting at 2pm and ending at 2:59pm, every day
 * "0 0/5 14 * * ?"         Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day
 * "0 0/5 14,18 * * ?"      Fire every 5 minutes starting at 2pm and ending at 2:55pm,
 * AND fire every 5 minutes starting at 6pm and ending at 6:55pm, every day
 * "0 0-5 14 * * ?"         Fire every minute starting at 2pm and ending at 2:05pm, every day
 * "0 10,44 14 ? 3 WED"     Fire at 2:10pm and at 2:44pm every Wednesday in the month of March.
 * "0 15 10 ? * MON-FRI"    Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday
 * "0 15 10 15 * ?"         Fire at 10:15am on the 15th day of every month
 *
 * @author Ray
 * @date created in 2021/11/9 15:09
 */
public class CronExpressionParser {

    static final Map<String, String> special = Map.of(
            "@yearly", "0 0 0 1 1 *",
            "@monthly", "0 0 0 1 * *",
            "@weekly", "0 0 0 * * 0",
            "@daily", "0 0 0 * * *",
            "@hourly", "0 0 * * * *"
    );
    static final String[] months = new String[]{"0", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct",
            "Nov", "Dec"};
    static final String[] days = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    static final int[] daysInMonth = new int[]{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public CronExpression parse(String expression) {
        CronField[] fields = validateCronExpression(expression);

        int lastDefined = -1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getValues().length > 0) {
                lastDefined = i;
            }
        }

        if (lastDefined == -1) {
            fields[0].setValues(fillRange(0, 59));
        }

        return new CronExpressionImpl(expression, fields, lastDefined);
    }

    private CronField[] validateCronExpression(String expression) {
        String[] tokens;

        String s = special.get(expression);
        if (s != null) {
            tokens = s.split(" ");
        } else {
            tokens = expression.split(" ");
        }

        int len = tokens.length;
        if (len < 6 || len > 7) {
            throw new CronParseException("Invalid expression length");
        }

        if (len == 6) {
            tokens = (String[]) append(tokens, " ");
        }

        if ((!tokens[3].equals("?") && !tokens[3].equals("*"))
                && (!tokens[5].equals("?") && !tokens[5].equals("*"))) {
            throw new CronParseException("Day field was set twice");
        }

        if (!tokens[6].equals("*")) {
            throw new CronParseException("Year field is not supported, use asterisk");
        }

        return buildCronField(tokens);
    }


    private CronField[] buildCronField(String[] tokens) {
        CronField[] fields = new CronField[7];
        fields[0] = parseField(tokens[0], 0, 59);
        fields[1] = parseField(tokens[1], 0, 59);
        fields[2] = parseField(tokens[2], 0, 23);
        fields[3] = parseField(tokens[3], 1, 31);
        fields[4] = parseField(tokens[4], 1, 12, months);
        fields[5] = parseField(tokens[5], 0, 6, days);
        fields[6] = parseField(tokens[6], 1970, 1970 * 2);
        return fields;
    }

    private CronField parseField(String field, int min, int max, String[]... translate) {
        String[] dict = null;
        if (translate.length > 0) {
            dict = translate[0];
        }

        // any value
        if (field.equals("*") || field.equals("?")) {
            return new CronField(new int[]{});
        }

        // single value
        try {
            int num = parseInt(field);
            if (inScope(num, min, max)) {
                return new CronField(new int[]{num});
            }

            throw new CronParseException("Single min/max validation error");
        } catch (NumberFormatException ignored) {
        }

        // list values
        if (field.contains(",")) {
            return parseListField(field, dict);
        }

        // range values
        if (field.contains("-")) {
            return parseRangeField(field, min, max, dict);
        }

        // step values
        if (field.contains("/")) {
            return parseStepField(field, min, max, dict);
        }

        if (dict != null) {
            int index = index(dict, field);
            if (index >= 0 && inScope(index, min, max)) {
                return new CronField(new int[]{index});
            }

            throw new CronParseException("Cron literal min/max validation error");
        }

        throw new CronParseException("Cron parse error");
    }

    private CronField parseStepField(String field, int min, int max, String[] translate) {
        int[] steps;

        String[] target = field.split("/");
        if (target.length != 2) {
            throw new CronParseException("Parse cron step error");
        }

        if (target[0].equals("*")) {
            // integer convert to string
            target[0] = String.valueOf(min);
        }

        int from = normalize(target[0], translate);
        int step = parseInt(target[1]);
        if (!inScope(from, min, max)) {
            throw new CronParseException("Cron step min/max validation error");
        }

        steps = fillStep(from, step, max);
        return new CronField(steps);
    }

    private CronField parseRangeField(String field, int min, int max, String[] translate) {

        int[] range;

        String[] target = field.split("-");
        if (target.length != 2) {
            throw new CronParseException("Parse cron range error");
        }

        int from = normalize(target[0], translate);
        int to = normalize(target[1], translate);
        if (!inScope(from, min, max) || !inScope(to, min, max)) {
            throw new CronParseException("Cron range min/max validation error");
        }


        range = fillRange(from, to);
        return new CronField(range);
    }

    private CronField parseListField(String field, String[] translate) {
        String[] target = field.split(",");
        int[] res;

        try {
            res = stringToInt(target);
        } catch (NumberFormatException ex) {
            res = indexes(target, translate);
        }

        Arrays.sort(res);
        return new CronField(res);
    }

}
