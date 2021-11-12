package com.xzc.util;

import org.junit.Test;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Ray
 * @date created in 2021/11/9 19:46
 */
public class DateTest {

    @Test
    public void testTime() {
        System.out.println(new Date());
        Instant instant = new Date().toInstant();
        BigInteger bigInteger = new BigInteger(instant.toEpochMilli() + "000000");
        BigInteger bigInteger1 = new BigInteger(String.valueOf(System.nanoTime()));
        System.out.println(bigInteger.add(bigInteger1));

//        System.out.println(instant.toEpochMilli());
//        System.out.println(instant.getNano());
//        System.out.println( (instant.toEpochMilli() << 9) +instant.getNano());
    }

    @Test
    public void test() throws Exception{
//        String[] split = new Date().toString().split(" ");
//        System.out.println(split);

//        Calendar now = Calendar.getInstance();
//        TimeZone timeZone = now.getTimeZone();
//        String displayName = timeZone.getDisplayName(false, TimeZone.SHORT, Locale.US);
//        System.out.println(displayName);

        String string = new Date().toString();
//        System.out.println(string);
//        System.out.println(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(string));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
//        System.out.println(simpleDateFormat.parse(string));

//        System.out.println(DateTimeFormatter.ofPattern("EEE, MMM dd HH:mm:ss zzz yyyy").parse(string));

//        System.out.println(new Date());
//        Date date = new Date(string);
//        System.out.println(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        System.out.println(simpleDateFormat.parse(string).toInstant());
    }
}
