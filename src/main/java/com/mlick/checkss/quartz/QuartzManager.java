package com.mlick.checkss.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;


public class QuartzManager {

    private static SchedulerFactory gSchedulerFactory = null;

    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName  任务名
     * @param clz      任务
     * @param cronExpr 时间设置，参考quartz说明文档
     */
    public static void addJob(String jobGroup, String jobName, Class<? extends Job> clz, String cronExpr, Object... entity) {

//        System.out.println(">>>>>>" + entity.toString());


        try {
            if (gSchedulerFactory == null) {
                gSchedulerFactory = new StdSchedulerFactory("quartz.properties");
//                gSchedulerFactory = new StdSchedulerFactory();
            }
            Scheduler scheduler = gSchedulerFactory.getScheduler();
            if (isExitJobKey(jobGroup, jobName, scheduler)) {
                System.out.println("任务已存在!");
                return;
            }

//            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
//            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            JobDetail jobDetail = JobBuilder.newJob(clz).withIdentity(jobName, jobGroup).build();
            if (entity.length > 0) {
                jobDetail.getJobDataMap().put("entity", entity[0]);
                jobDetail.getJobDataMap().put("entity2", entity.length > 1 ? entity[1] : null);
                jobDetail.getJobDataMap().put("entity3", entity.length > 2 ? entity[2] : null);
            }

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpr);

            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static void deleteJob(String jobGroup, String jobName) {
//        logger.info("........................deleteJob...........................");
        try {
            if (gSchedulerFactory == null) {
                return;
            }
            Scheduler scheduler = gSchedulerFactory.getScheduler();
            if (isExitJobKey(jobGroup, jobName, scheduler)) {
                JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @return true表示存在  false表示不存在
     */
    private static boolean isExitJobKey(String jobGroup, String jobName, Scheduler scheduler) throws SchedulerException {
        if (scheduler == null) {
            return false;
        }
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        return scheduler.getTrigger(triggerKey) != null;
    }


    public static void schedulerStart() {
        try {
            if (gSchedulerFactory != null) {
                Scheduler scheduler = gSchedulerFactory.getScheduler();
                if (scheduler != null)
                    scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void schedulerShutdown() {
        try {
            if (gSchedulerFactory != null) {
                Scheduler scheduler = gSchedulerFactory.getScheduler();
                if (scheduler != null)
                    scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static boolean isShutdown() {
        try {
            if (gSchedulerFactory != null) {
                Scheduler scheduler = gSchedulerFactory.getScheduler();
                return scheduler.isShutdown();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void scheduler(boolean b) {
        if (b) {
            schedulerStart();
        } else {
            schedulerShutdown();
        }
    }
}
