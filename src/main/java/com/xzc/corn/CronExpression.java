package com.xzc.corn;

import java.util.Date;
import java.util.List;

/**
 * @author Ray
 * @date created in 2021/11/9 15:08
 */
public interface CronExpression {

    Date nextTime();

    List<Date> nextTime(int num);

}
