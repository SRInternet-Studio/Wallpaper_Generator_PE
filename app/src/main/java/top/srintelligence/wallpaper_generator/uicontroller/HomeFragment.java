package top.srintelligence.wallpaper_generator.uicontroller;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.srintelligence.wallpaper_generator.R;

import java.net.HttpURLConnection;
import java.net.URL;


public class HomeFragment extends Fragment {
    View view;
    private final Handler handler = new Handler(Looper.getMainLooper()); // 创建一个 Handler

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                URL url = new URL(this.getString(R.string.proxy));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                connection.disconnect();

                TextView textViewLatency = view.findViewById(R.id.text_view_latency);
                ImageView today = view.findViewById(R.id.today_image);

                // 使用 Handler 在主线程中更新 UI
                handler.post(() -> {
                    if (textViewLatency != null) {
                        textViewLatency.setText(latency + "ms 延迟");
                    }

                    if (today != null) {
                        Glide.with(this)
                                .load(R.drawable.ba)
                                .centerInside()
                                .into(today);
                    }
                });


            } catch (Exception ignored) {
                // ExceptionHandler.handleException(e);
            }
        }).start();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // 移除所有消息和回调
        view = null;
    }
}