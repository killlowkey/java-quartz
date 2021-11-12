package com.xzc.util;

import java.util.Date;

/**
 * @author Ray
 * @date created in 2021/11/8 7:34
 */
public class Logger {

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public void info(String msg, Object... args) {
        String message = String.format(msg, args);
        System.err.printf("%s [%s] %s %s: %s\n",
                CommonUtil.formatDate(new Date()), Thread.currentThread().getName(),
                "INFO", this.name, message);
    }

    public static void main(String[] args) {
        new Logger(Logger.class.getName()).info("hello");
    }


}
