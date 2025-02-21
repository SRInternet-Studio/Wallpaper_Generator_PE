package top.srintelligence.wallpaper_generator;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import org.jetbrains.annotations.NotNull;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.process.Download;

import java.util.ArrayList;
import java.util.Arrays;
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
                    downloadCurrentImage();
                }
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

        Glide.with(this)
                .load(imageURLs.get(currentIndex))
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.error_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)

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

    private void downloadCurrentImage() {
        if (currentIndex >= 0 && currentIndex < imageURLs.size()) {
            String currentImageUrl = imageURLs.get(currentIndex);
            String savePath;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                savePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            } else {
                savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            }
            new Thread(() -> {
                if (currentImageUrl.contains("sinaimg.")) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Referer", "https://weibo.com/");
                    Download.setCustomHeaders(headers);
                }
                Download.downLoadByUrlParallel(currentImageUrl, savePath, false); // 其他域名正常下载
                ExceptionHandler.handleDebug("Downloaded image: " + currentImageUrl + " to " + savePath);
                Looper.prepare();
                Toast.makeText(this, "Downloaded image to " + savePath, Toast.LENGTH_SHORT).show();
                Looper.loop();
                Download.clearCustomHeaders();
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