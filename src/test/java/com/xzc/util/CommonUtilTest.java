package com.xzc.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2021/11/7 9:44
 */
public class CommonUtilTest {

    @Test
    public void testFormatTime() {
        String res = CommonUtil.formatDate(new Date());
        System.out.println(res);
        assertNotNull(res);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(CommonUtil.isEmpty(""));
        assertTrue(CommonUtil.isEmpty(null));
        assertFalse(CommonUtil.isEmpty("hello world"));
    }

    @Test
    public void testTime() {
        System.out.println(System.currentTimeMillis());
        System.out.println(new Date().getTime());
        System.out.println(new Date().toInstant().toEpochMilli());
    }

}