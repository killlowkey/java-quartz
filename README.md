# Java-quartz
Java-quartz 是一款任务定时调度框架，可以根据 cron、Duration、time string 运行。



## 特性

* cron trigger、run once trigger、simple trigger、time string trigger
* RunnableJob（无返回值的 task）、CallableJob（带有返回值的 task）
* context（保存 job 信息和每次运行状态、trigger 信息、task 返回值等等）
* 注解任务调度（@Schedule 注解）



## 配置

> Java-quartz  提供一个配置机制，用于更改 Java-quartz  scheduler 的线程数量，在 classpath resources 目录下创建 schedule.properties 即可，如以下配置 example 所示。

```properties
schedule.coreThreadNum=5
schedule.maxThreadNum=10
```



## examples
### cron trigger

> 根据 cron 表达式来定时触发任务

```java
public static void main(String[] args) {
    Scheduler scheduler = new StdScheduler();
    scheduler.start();
    
    Job job = JobFactory.newRunnableJob("cron trigger", () -> {
        System.out.println("cron trigger");
    });
    Trigger trigger = TriggerFactory.newCronTrigger("1/5 * * * * * *");
    scheduler.scheduleJob(job, trigger);
    scheduler.waitShutdown();
}
```



### run once Trigger

> 该触发器只会运行一次任务

```java
public static void main(String[] args) {
    Scheduler scheduler = new StdScheduler();
    scheduler.start();
    
    Job job = JobFactory.newRunnableJob("run once trigger", () -> {
        System.out.println("run once trigger");
    });
    // 该任务 5s 之后运行一次
    Trigger trigger = TriggerFactory.newRunOnceTrigger(Duration.ofSeconds(5));
    scheduler.scheduleJob(job, trigger);   
    scheduler.waitShutdown();
}
```



### simple Trigger

> 根据 Duration 定时触发任务

```java
public static void main(String[] args) {
    Scheduler scheduler = new StdScheduler();
    scheduler.start();
    
    Job job = JobFactory.newRunnableJob("simple trigger", () -> {
        System.out.println("simple trigger");
    });
    // 该任务每 10s 运行一次
    Trigger trigger = TriggerFactory.newSimpleTrigger(Duration.ofSeconds(10))
    scheduler.scheduleJob(job, trigger);   
    scheduler.waitShutdown();
}
```



### time string Trigger

> 根据传入的time string 进行解析，在指定的时间点运行，精确到毫秒，若当前时间超过了传入的时间则抛出异常；
>
> 该触发器只会运行一次

```java
public static void main(String[] args) {
    Scheduler scheduler = new StdScheduler();
    scheduler.start();
    
    Job job = JobFactory.newRunnableJob("time string trigger", () -> {
        System.out.println("time string trigger");
    });
    Trigger trigger = TriggerFactory.newTimeStrTrigger("2021-11-13 16:27:59.980");
    scheduler.scheduleJob(job, trigger);   
    scheduler.waitShutdown();
}
```



### 注解驱动

> 该设计借鉴了 spring ，只需在方法上标注 @Schedule 注解，并设置 cron 与 desc 字段，调用 Scheduler#register 方法注册到 Scheduler 即可。Scheduler 扫描之后，会注册一个 CronTrigger 与 RunnableJob 调度任务。该注解不支持私有、含参、带有返回值的方法。

```java
static class AnnotationTask {
    public AnnotationTask() {}

    @Schedule(cron = "1/5 * * * * * *", desc = "instance schedule")
    public void instanceJob() {
        System.out.println("instance annotation schedule task");
    }

    @Schedule(cron = "1/5 * * * * * *", desc = "static method schedule")
    public static void classJob() {
        System.out.println("static method annotation schedule task");
    }
}
public static void main(String[] args) {
    Scheduler scheduler = new StdScheduler();
    scheduler.register(AnnotationTask.class);
    scheduler.start();
    scheduler.waitShutdown();
}
```



context

> Job 接口包含一个 context 方法，该 context  与 trigger context 进行绑定，从而共享一些数据。当 task 运行之后，可以从 context 中获取运行信息（records）等其它信息。

```java
public static void main(String[] args) {
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
```



## 参考

> 该项目的 cron 解析与接口设计来自 [go-quartz](https://github.com/reugn/go-quartz)，特别感谢！！！

* https://github.com/reugn/go-quartz