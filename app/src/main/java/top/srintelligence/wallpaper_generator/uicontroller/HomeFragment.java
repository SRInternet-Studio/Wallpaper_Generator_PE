package top.srintelligence.wallpaper_generator.uicontroller;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.srintelligence.wallpaper_generator.R;

import java.net.HttpURLConnection;
import java.net.URL;


public class HomeFragment extends Fragment {
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                URL url = new URL("https://pixiv.re");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                connection.disconnect();

                TextView textViewLatency = view.findViewById(R.id.text_view_latency);
                getActivity().runOnUiThread(() -> textViewLatency.setText(latency + "ms 延迟"));

                connection.disconnect();
            } catch (Exception e) {
                ExceptionHandler.handleException(e);
            }
        }).start();

        return view;
    }
}