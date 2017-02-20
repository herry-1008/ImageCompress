package com.herry.imagecompress.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by herry on 2016/11/22.
 */

public class CommonUtils {

    public static void silentClose(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            //
        }
    }

    /**
     * 判断字符串是否无效
     *
     * @param s
     * @return
     */
    public static boolean isNull(String s) {
        if (s == null || s.trim().length() <= 0) {
            return true;
        }
        return false;
    }

    public static String calcFileSize(String imagePath) {
        File f = new File(imagePath);
        long length = f.length();
        return formatSize(length);
    }


    private static String formatSize(long length) {
        if (length < 1024) { //1KB
            return length + "bytes";
        } else if (length < 1024 * 1024) { //1MB
            return length / 1024 + "KB";
        } else {
            return length / (1024 * 1024) + "MB";//1GB
        }
    }

    public static String formatTimeInterval(long timeInterval) {
        if (timeInterval < 1000) {
            return timeInterval + " ms";
        } else {
            long second = timeInterval / 1000;
            long ms = timeInterval % 1000;
            return second + " s " + ms + " ms";
        }
    }
}
