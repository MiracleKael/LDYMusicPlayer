package com.ldy.ldymusicplayer.utils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by ${jiaojing} on 2017/10/8.
 * Desc：把毫秒时间转化成00:00:00的样式
 */

public class TimeToString {
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    public TimeToString() {
        // 转换成字符串的时间
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public String translate(int timeMs) {
        int totalSeconds = (timeMs / 1000);
        int seconds = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
