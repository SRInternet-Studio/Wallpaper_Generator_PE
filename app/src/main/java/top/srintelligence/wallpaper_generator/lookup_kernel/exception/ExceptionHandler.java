package top.srintelligence.wallpaper_generator.lookup_kernel.exception;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class ExceptionHandler {
    private static final int MAX_EXCEPTIONS = 100;
    private static final LinkedBlockingQueue<Throwable> EXCEPTIONS = new LinkedBlockingQueue<>(MAX_EXCEPTIONS);
    private static final String TAG = "ExceptionHandler";

    public static void handleException(Throwable e) {
        if (EXCEPTIONS.size() >= MAX_EXCEPTIONS) {
            EXCEPTIONS.poll();
        }
        EXCEPTIONS.offer(e);
        Log.e(TAG, logCaller(), e);
    }

    public static void handleException(String message, Throwable e) {
        Log.e(TAG, message, e);
    }

    public static void handleWarning(String message) {
        Log.w(TAG, message);
        logCaller();
    }

    public static void handleInfo(String message) {
        Log.i(TAG, message);
        logCaller();
    }

    public static void handleDebug(String message) {
        Log.d(TAG, message);
        logCaller();
    }

    public static void handleFatal(String message, Throwable e) {
        Log.wtf(TAG, message, e);
        logCaller();
    }

    public static void handleTrace(String message) {
        Log.v(TAG, message);
        logCaller();
    }

    private static String logCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return (caller.getClassName() + "." + caller.getMethodName() + ":");
        }
        return null;
    }
}