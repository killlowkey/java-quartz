package com.xzc.schedule;

import com.xzc.Job;
import com.xzc.Trigger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ray
 * @date created in 2021/11/7 9:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Comparable<Task> {

    private Job job;
    private Trigger trigger;
    private long priority;
    private boolean cancel;

    @Override
    public int compareTo(Task other) {
        return (int) (this.priority - other.priority);
    }
}
