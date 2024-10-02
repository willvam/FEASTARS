package com.example.feastarfeed;

public class ThrottleUtils {
    private static long lastClickTime = 0;
    private static final long THROTTLE_DELAY = 1000; // 節流延遲時間,單位毫秒

    public static boolean skip() {
        long currentTime = System.currentTimeMillis();
        boolean skip = currentTime - lastClickTime < THROTTLE_DELAY;
        if (!skip) {
            lastClickTime = currentTime;
        }
        return skip;
    }
}