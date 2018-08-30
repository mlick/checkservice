package com.mlick.checkss;

import com.mlick.checkss.quartz.QuartzManager;
import com.mlick.checkss.quartz.job.CheckJob;
import org.apache.log4j.Logger;

import java.util.Arrays;


public class Main {
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("检测的url地址有:\n" + Arrays.toString(CheckJob.urls));
        logger.info("每十分钟进行一次检测，如有异常将以短信形式发送");

        // 每十分钟 检测一次
        QuartzManager.addJob("check", "url", CheckJob.class, "0 0/10 * * * ?");
        QuartzManager.schedulerStart();


    }
}
