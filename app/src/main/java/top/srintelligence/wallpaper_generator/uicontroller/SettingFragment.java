package top.srintelligence.wallpaper_generator.uicontroller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import top.srintelligence.wallpaper_generator.AboutPage;
import top.srintelligence.wallpaper_generator.MainActivity;
import top.srintelligence.wallpaper_generator.function.checking_update.CheckingUpdate;
import top.srintelligence.wallpaper_generator.R;


public class SettingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) { // 设置点击事件
        super.onActivityCreated(savedInstanceState);

        Button about = requireView().findViewById(R.id.About);
        about.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.getInstance(), AboutPage.class);
            startActivity(intent);
        });

        Button ck = requireView().findViewById(R.id.CheckingUpdate);
        ck.setOnClickListener(v -> {
            CheckingUpdate ckf = new CheckingUpdate();
            Toast.makeText(MainActivity.getInstance(), "正在检查更新……", Toast.LENGTH_LONG).show();
            ckf.checkForUpdate();
        });
    }
}