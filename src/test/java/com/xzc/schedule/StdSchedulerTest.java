package com.xzc.schedule;

import com.xzc.Context;
import com.xzc.Job;
import com.xzc.Scheduler;
import com.xzc.Trigger;
import com.xzc.annotation.Schedule;
import com.xzc.job.JobFactory;
import com.xzc.job.Record;
import com.xzc.trigger.TriggerFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

/**
 * @author Ray
 * @date created in 2021/11/7 11:07
 */
public class StdSchedulerTest {

    Scheduler scheduler;
    int count;

    @Before
    public void init() {
        System.setProperty("log4j.defaultInitOverride", "true");
        scheduler = new StdScheduler();
        scheduler.start();
    }

    @Test
    public void testScheduleTask() throws Exception {
//        Job job1 = JobFactory.newRunnableJob("job1", () -> {
//            System.out.println("job1: hello world");
//        });

        Job job2 = JobFactory.newRunnableJob("job2", () -> {
            System.out.println("job2: hello world");
        });

//        Job job3 = JobFactory.newRunnableJob("job3", () -> {
//            count++;
//            if (count == 5) {
//                scheduler.deleteJob("job3".hashCode());
////                scheduler.stop();
//            }
//            System.out.println(Thread.currentThread().getName() + " job3: hello world");
//        });

//        Trigger trigger1 = TriggerFactory.newTimeStrTrigger("2021-11-07 16:27:59.980");
        Trigger trigger2 = TriggerFactory.newRunOnceTrigger(Duration.ofSeconds(5));
        Trigger trigger3 = TriggerFactory.newSimpleTrigger(Duration.ofSeconds(3));

//        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(job2, trigger2);
//        scheduler.scheduleJob(job3, trigger3);

        scheduler.waitShutdown();
    }

    @Test
    public void testExpiredTrigger() throws InterruptedException {
        try {
            Job job = JobFactory.newRunnableJob("job1", () -> System.out.println("job1: hello world"));
            Trigger trigger = TriggerFactory.newTimeStrTrigger("2021-11-07 16:27:59.980");
            scheduler.scheduleJob(job, trigger);
        } catch (Exception ex) {
            if (ex.getClass() != IllegalArgumentException.class) {
                throw new AssertionError();
            }
        }

    }

    @Test
    public void testCronTrigger() throws Exception {
        Job job = JobFactory.newRunnableJob("cron", () -> {
            System.out.println("hello cron");
        });
        Trigger trigger = TriggerFactory.newCronTrigger("1/5 * * * * * *");
        scheduler.scheduleJob(job, trigger);
        scheduler.waitShutdown();
    }

    @Test
    public void testContext() throws Exception {
        Job job = JobFactory.newRunnableJob("context", () -> {
            System.out.println("hello context schedule");
            scheduler.stop();
        });
        Trigger trigger = TriggerFactory.newRunOnceTrigger(Duration.ofSeconds(3));
        scheduler.scheduleJob(job, trigger);
        scheduler.waitShutdown();

        Context context = job.context();
        context.keys().forEach(key -> {
            if (key.equals("records")) {
                List<Record<Void>> records = (List<Record<Void>>) context.value("records", List.class);
                System.out.println(records);
            } else if (key.equals("job")) {
                Job job1 = context.value("job", Job.class);
                System.out.println(job1);
            } else {
                System.out.printf("key=%s value=%s\n", key, context.value(key, Object.class));
            }
        });
    }

    @Test
    public void testAnnotationSchedule() throws Exception {
        scheduler.register(AnnotationTask.class);
        scheduler.start();
        scheduler.waitShutdown();
    }

    static class AnnotationTask {

        public AnnotationTask() {
        }

        @Schedule(cron = "1/5 * * * * * *", desc = "instance schedule")
        public void instanceJob() {
            System.out.println("instance annotation schedule task");
        }

        @Schedule(cron = "1/5 * * * * * *", desc = "static class schedule")
        public static void classJob() {
            System.out.println("static class annotation schedule task");
        }

    }
}