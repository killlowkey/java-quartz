package com.xzc.queue;

import com.xzc.schedule.Task;
import org.junit.Test;

/**
 * @author Ray
 * @date created in 2021/11/7 21:17
 */
public class ScheduleQueueTest {

    @Test
    public void testScheduleQueue() throws Exception{
        ScheduleQueue queue = new ScheduleQueue();
        new Thread(() -> {
            try {
                Thread.sleep(4000L);
                queue.offer(new Task());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println(queue.peek());
    }
}