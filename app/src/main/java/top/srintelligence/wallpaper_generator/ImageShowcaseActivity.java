package top.srintelligence.wallpaper_generator;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.process.Download;
import top.srintelligence.wallpaper_generator.function.WallpaperHelper;
import top.srintelligence.wallpaper_generator.uicontroller.ACGGenerateFragment;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageShowcaseActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private ArrayList<String> imageURLs;
    private int currentIndex = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_image_showcase);

            imageURLs = getIntent().getStringArrayListExtra("imageURLs");
            ImageView imageView = findViewById(R.id.image_show_view);
            preloadImages();

            requestPermissions();

            // 初始化手势检测器
            gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                private static final int SWIPE_THRESHOLD = 50; // 控制滑动最小距离

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                        if (diffY > 0) {
                            // 下滑 -> 下一张
                            if (currentIndex < imageURLs.size() - 1) {
                                currentIndex++;
                                animateImageChange(imageView, true);
                            }
                        } else {
                            // 上滑 -> 上一张
                            if (currentIndex > 0) {
                                currentIndex--;
                                animateImageChange(imageView, false);
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    showOptionsDialog();
                }
            });

            ImageButton upButton = findViewById(R.id.up_button); // 生成按钮
            upButton.setOnClickListener(v -> {
                if (currentIndex > 0) {
                    currentIndex--;
                    animateImageChange(imageView, false);
                }
            });

            ImageButton downButton = findViewById(R.id.down_button); // 生成按钮
            downButton.setOnClickListener(v -> {
                if (currentIndex < imageURLs.size() - 1) {
                    currentIndex++;
                    animateImageChange(imageView, true);
                }
            });

            ImageButton moreButton = findViewById(R.id.more_button); // 生成按钮
            moreButton.setOnClickListener(v -> {
                showOptionsDialog();
            });

            ImageButton backButton = findViewById(R.id.back_button); // 生成按钮
            backButton.setOnClickListener(v -> {
                finish();
            });

            loadImage(imageView);
        } catch (Exception e) {
            this.finish();
            ExceptionHandler.handleException(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void loadImage(ImageView imageView) throws IndexOutOfBoundsException {
        if (currentIndex < 0 || currentIndex >= imageURLs.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + currentIndex);
        }

        ProgressBar progressBar = findViewById(R.id.Loading_progress_bar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        TextView indicator = findViewById(R.id.Indicator);
        indicator.setText(String.format("第 %d/%d 张", currentIndex + 1, imageURLs.size()));

        Glide.with(this)
                .load(imageURLs.get(currentIndex))
                .centerInside() // 或使用 fitCenter()
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.error_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // 加载失败
                        progressBar.setVisibility(ProgressBar.GONE);
                        return false; // 返回 false，让 Glide 处理 error_placeholder
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // 加载成功
                        progressBar.setVisibility(ProgressBar.GONE);
                        return false; // 返回 false，让 Glide 显示图片
                    }
                })
                .into(imageView);
    }

    private void animateImageChange(ImageView imageView, boolean isNext) {
        float offset = 300f;
        float start = 0f;
        float end = isNext ? offset : -offset;

        imageView.animate()
                .translationY(end)
                .alpha(0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .withEndAction(() -> {
                    loadImage(imageView);
                    imageView.setTranslationY(-end);
                    imageView.setAlpha(0f);

                    imageView.animate()
                            .translationY(start)
                            .alpha(1f)
                            .setDuration(400)
                            .setInterpolator(new android.view.animation.OvershootInterpolator())
                            .start();
                })
                .start();
    }

    private void preloadImages() {
        if (imageURLs != null && !imageURLs.isEmpty()) {
            for (String url : imageURLs) {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.loading_placeholder)
                        .error(R.drawable.error_placeholder)
                        .preload();
            }
        }

    }

    private void showOptionsDialog() {
        if (currentIndex >= 0 && currentIndex < imageURLs.size()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("请选择操作")
                    .setItems(new String[]{"设置为壁纸", "下载图片"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                new WallpaperHelper(this).showSetWallpaperDialog(imageURLs.get(currentIndex));
                                break;
                            case 1:
                                downloadCurrentImage();
                                break;
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }
    }

    private void downloadCurrentImage() {
        if (currentIndex >= 0 && currentIndex < imageURLs.size()) {
            String currentImageUrl = imageURLs.get(currentIndex);
            Toast.makeText(this, "请稍后", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    // 司马微博
                    if (currentImageUrl.contains("sinaimg.")) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Referer", "https://weibo.com/");
                        Download.setCustomHeaders(headers);
                    }

                    // 下载图片并获取文件
                    String fileName = "wallpaper_" + System.currentTimeMillis() + ".jpg";
                    Bitmap bitmap;

                    // bitmap
                    bitmap = Glide.with(this)
                            .asBitmap()
                            .load(currentImageUrl)
                            .submit()
                            .get();

                    if (bitmap != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            // Android 10 MediaStore API
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallpapers");
                            values.put(MediaStore.Images.Media.IS_PENDING, 1);

                            ContentResolver resolver = getContentResolver();
                            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            if (imageUri != null) {
                                try (OutputStream os = resolver.openOutputStream(imageUri)) {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
                                }

                                // 完成后更新IS_PENDING
                                values.clear();
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                                resolver.update(imageUri, values, null, null);

                                Looper.prepare();
                                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } else {
                            // Android 9以下版本
                            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Wallpapers");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            File imageFile = new File(directory, fileName);
                            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                fos.flush();
                            }

                            // 通知图库更新
                            MediaScannerConnection.scanFile(
                                    this,
                                    new String[]{imageFile.getAbsolutePath()},
                                    new String[]{"image/jpeg"},
                                    null
                            );

                            Looper.prepare();
                            Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }

                    Download.clearCustomHeaders();
                } catch (Exception e) {
                    Looper.prepare();
                    Toast.makeText(this, "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    ExceptionHandler.handleException(e);
                    Looper.loop();
                }
            }).start();
        }
    }

    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // 安卓 13 请求媒体读取权限
            String[] permissions = {
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO
            };
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        } else {
            // 安卓 12 请求存储权限
            String[] permissions = {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                throw new RuntimeException("Permission denied");
            } else {
                ExceptionHandler.handleInfo("Permission granted");
            }
        }
    }
}