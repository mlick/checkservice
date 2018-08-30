package com.mlick.checkss.quartz.job;

import com.github.kevinsawicki.http.HttpRequest;
import com.mlick.checkss.utils.EmailUtil;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * job类
 */
@DisallowConcurrentExecution
public class CheckJob implements Job {

    private static Logger logger = Logger.getLogger(CheckJob.class);

    public final static String[] urls;

    static {
        String content = null;
        try {
            //需要在项目根目录创建一个 dataurls.txt 文件中放入 格式: url地址#项目名称, 最后用逗号分隔
            String pathStr = System.getProperty("user.dir") + "/dataurls.txt";
            content = new String(Files.readAllBytes(Paths.get(pathStr)), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert content != null;

        urls = content.split(",");
    }


    private static Map<String, Integer> errorTimes = new HashMap<String, Integer>();

    public void execute(JobExecutionContext context) {

        for (String iurl : urls) {
            String[] us = iurl.split("#");
            String url = us[0];
            String name = us[1];
            int errorCounts = 0;
            if (errorTimes.containsKey(url)) {
                errorCounts = errorTimes.get(url);
            } else {
                errorTimes.put(url, errorCounts);
            }

            int code = -1;
            code = getCode(url, code, 1);
            if (code != HTTP_OK) {
                errorTimes.put(url, ++errorCounts);
            } else {
                errorTimes.put(url, errorCounts = 0);
            }

            logger.info("服务名称:" + name + "=>请求的URL:" + url + "=>状态:" + code);
            // 30m,1h,2h,5h,10h
            if (errorCounts == 3 || errorCounts == 6 || errorCounts == 12 || errorCounts == 30 || errorCounts == 60) {
                String msg = MessageFormat.format("({0})请求失败次数达到限制:{1}发送邮件", name, errorCounts);
                logger.error(msg);
                EmailUtil.sendMail(msg + url);
            }

        }
        logger.info("----------------------------------------------------------------");

    }

    /**
     * 重复三次请求 如果失败返回-3
     */
    private int getCode(String url, int code, int repeat) {
        try {
            if (repeat == 5) {//重复三次
                return -5;
            }
            code = HttpRequest.get(url, true).connectTimeout(15000).readTimeout(15000).code();
        } catch (HttpRequest.HttpRequestException ignored) {
            ignored.printStackTrace();
            return getCode(url, code, ++repeat);
        }
        return code;
    }
}
