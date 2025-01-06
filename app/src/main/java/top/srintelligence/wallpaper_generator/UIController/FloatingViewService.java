package top.srintelligence.wallpaper_generator.UIController;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import top.srintelligence.wallpaper_generator.R;
import top.srintelligence.wallpaper_generator.lookup_kernel.exception.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FloatingViewService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private TextView logTextView;
    private ScrollView scrollView;
    private Handler handler;
    private Thread logThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ExceptionHandler.handleDebug("FloatingViewService created");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
        scrollView = floatingView.findViewById(R.id.log_scrollView); // 初始化scrollView
        logTextView = floatingView.findViewById(R.id.log_text_view);
        logTextView.setText("FloatingViewService created");

        WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
        }

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        windowManager.addView(floatingView, params);

        handler = new Handler();
        startLogcatListener();
    }

    private void startLogcatListener() {
        logThread = new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("logcat");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final String logLine = line;
                    handler.post(() -> {
                        logTextView.append(logLine + "\n");
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    });
                }
            } catch (IOException e) {
                Log.e("FloatingViewService", "Error reading logcat output", e);
            }
        });
        logThread.start();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null; // 确保浮动视图被删除
        }
        if (logThread != null && logThread.isAlive()) {
            logThread.interrupt();
        }
        stopSelf(); // 停止服务
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null; // 释放资源
        }
        if (logThread != null && logThread.isAlive()) {
            logThread.interrupt();
        }
    }
}