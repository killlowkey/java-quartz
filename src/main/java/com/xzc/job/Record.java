package com.xzc.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Ray
 * @date created in 2021/11/7 9:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record<T> {
    private Date startTime;
    private Date endTime;
    private String threadName;
    private Exception ex;
    private JobStatus status;
    private T result;
}
