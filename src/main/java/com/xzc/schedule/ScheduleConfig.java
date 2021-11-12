package com.xzc.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ray
 * @date created in 2021/11/8 9:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleConfig {
    private int coreThreadNum;
    private int maxThreadNum;


    public static ScheduleConfig parseConfig() {
        InputStream is = ScheduleConfig.class.getClassLoader()
                .getResourceAsStream("schedule.properties");

        if (is == null) {
            return defaultConfig();
        }

        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            return defaultConfig();
        }

        int coreThreadNum = toInt(properties.getOrDefault("schedule.coreThreadNum", 5));
        int maxThreadNum = toInt(properties.getOrDefault("schedule.maxThreadNum", 20));
        return new ScheduleConfig(coreThreadNum, maxThreadNum);
    }

    static ScheduleConfig defaultConfig() {
        return new ScheduleConfig(5, 20);
    }

    static int toInt(Object value) {
        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            return 0;
        }

    }

}
