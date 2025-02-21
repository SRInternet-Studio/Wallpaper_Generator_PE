package top.fireworkrocket.lookup_kernel.exception;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import top.srintelligence.wallpaper_generator.MainActivity;

import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class ExceptionHandler {
    private static final int MAX_EXCEPTIONS = 100;
    private static final LinkedBlockingQueue<Throwable> EXCEPTIONS = new LinkedBlockingQueue<>(MAX_EXCEPTIONS);
    private static final String TAG = "ExceptionHandler";
    private static final AtomicInteger openAlertCount = new AtomicInteger(0);

    public static void handleException(Throwable e) {
        if (EXCEPTIONS.size() >= MAX_EXCEPTIONS) {
            EXCEPTIONS.poll();
        }
        EXCEPTIONS.offer(e);
        Log.e(TAG, logCaller() + " Exception: " + e.getClass().getName(), e);
        showAlert("An error was encountered while processing the request", e.getMessage() + "\n" + Log.getStackTraceString(e));
    }

    public static void handleException(String message, Throwable e) {
        Log.e(TAG, message + " Exception: " + e.getClass().getName(), e);
        logCaller();
        showAlert("An error was encountered while processing the request", e.getMessage() + "\n" + Log.getStackTraceString(e));
    }

    public static void showAlert(String title, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (openAlertCount.get() >= 3) {
                Log.w(TAG, "Too many alerts open, skipping alert: " + title + " - " + message);
                return;
            }
            openAlertCount.incrementAndGet();
            Context context = MainActivity.getInstance();
            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> dialogInterface.dismiss())
                    .create();
            dialog.setOnDismissListener(d -> openAlertCount.decrementAndGet());
            dialog.show();
        });
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

    public static void captureMemorySnapshot() throws IOException {
        android.os.Debug.dumpHprofData(Environment.getExternalStorageDirectory().getPath());
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