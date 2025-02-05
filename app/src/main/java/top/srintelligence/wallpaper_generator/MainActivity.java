package top.srintelligence.wallpaper_generator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import top.fireworkrocket.lookup_kernel.config.DatabaseUtil;
import top.srintelligence.wallpaper_generator.uicontroller.*;
import org.jetbrains.annotations.NotNull;

import static top.fireworkrocket.lookup_kernel.config.DefaultConfig.backGroundfile;
import static top.fireworkrocket.lookup_kernel.config.DefaultConfig.debug;

public class MainActivity extends AppCompatActivity {

    public static final int NAV_HOME = R.id.nav_home;
    public static final int NAV_GENERATE = R.id.nav_generate;
    public static final int NAV_WATHERFALL = R.id.nav_waterfall;
    public static final int NAV_SETTINGS = R.id.nav_settings;
    private static MainActivity instance;
    private static DatabaseUtil databaseUtil;
    private boolean isChecked = debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用动态颜色支持
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        setContentView(R.layout.activity_main);

        ImageView backgroundImage = findViewById(R.id.background_image);
        View overlay = findViewById(R.id.overlay_view);
        String imageUrl = String.valueOf(backGroundfile);

        // 初始化悬浮窗开关
        if (isChecked) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, 0);
            } else {
                Log.d("MainActivity", "Starting FloatingViewService");
                startFloatingViewService();
            }
        } else {
            Log.d("MainActivity", "Stopping FloatingViewService");
            stopFloatingViewService();
        }

        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NotNull Target<Drawable> target, boolean isFirstResource) {
                        Log.e("MainActivity", "Image load failed", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NotNull Drawable resource, @NotNull Object model, Target<Drawable> target, @NotNull DataSource dataSource, boolean isFirstResource) {
                        Log.d("MainActivity", "Image loaded successfully");
                        new Thread(() -> {
                            BitmapDrawable drawable = (BitmapDrawable) resource;
                            Bitmap bitmap = drawable.getBitmap();
                            Bitmap blurredBitmap = blur(bitmap);
                            runOnUiThread(() -> {
                                backgroundImage.setImageBitmap(blurredBitmap);

                                // 根据当前主题模式设置遮罩颜色
                                int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                                if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                                    overlay.setBackgroundColor(0x80000000); // 暗色模式
                                } else {
                                    overlay.setBackgroundColor(0x80FFFFFF); // 亮色模式
                                }
                            });
                        }).start();
                        return true;
                    }
                })
                .into(backgroundImage);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);

            if (itemId == NAV_HOME && !(currentFragment instanceof HomeFragment)) {
                selectedFragment = new HomeFragment();
            } else if (itemId == NAV_WATHERFALL && !(currentFragment instanceof WaterfallFragment)) {
                selectedFragment = new WaterfallFragment();
            } else if (itemId == NAV_GENERATE && !(currentFragment instanceof GenerateFragment)) {
                selectedFragment = new GenerateFragment();
            } else if (itemId == NAV_SETTINGS && !(currentFragment instanceof SettingFragment)) {
                selectedFragment = new SettingFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
            }
            return true;
        });

        // 默认显示HomeFragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(NAV_HOME);
        }

        instance = this; // 保存当前实例
        databaseUtil = new DatabaseUtil(this); // 初始化数据库
    }

    private Bitmap blur(Bitmap image) {
        final float BLUR_RADIUS = 25f;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        RenderScript rs = RenderScript.create(this);
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, image);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        // 释放RenderScript对象
        rs.destroy();

        return outputBitmap;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void startFloatingViewService() {
        startService(new Intent(this, FloatingViewService.class));
    }

    private void stopFloatingViewService() {
        stopService(new Intent(this, FloatingViewService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingViewService();
            } else {
                isChecked = false;
            }
        }
    }

    public static DatabaseUtil getDatabaseUtil() {
        return databaseUtil;
    }
}