package com.mlick.checkss.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * 获取当前的时间的文件名称
     */
    public static String getFileName() {
        return new SimpleDateFormat("yyyyMMddHH").format(new Date()) + ".png";
    }

    public static String getToday() {
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        return df.format(new Date());
    }



}
