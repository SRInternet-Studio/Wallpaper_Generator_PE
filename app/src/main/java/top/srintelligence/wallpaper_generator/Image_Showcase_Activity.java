package top.srintelligence.wallpaper_generator;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class Image_Showcase_Activity extends AppCompatActivity {

    private ArrayList<String> imageURLs;
    private int currentIndex = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_showcase);

        imageURLs = getIntent().getStringArrayListExtra("imageURLs");
        ImageView imageView = findViewById(R.id.image_show_view);
        preloadImages();

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
        });

        // 加载第一张图片
        loadImage(imageView);
    }

    // 将触摸事件交给手势检测器处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void loadImage(ImageView imageView) {
        Glide.with(this)
                .load(imageURLs.get(currentIndex))
                .into(imageView);
    }

    private void animateImageChange(ImageView imageView, boolean isNext) {
        float offset = 300f; // 控制滑动距离
        float start = 0f;
        float end = isNext ? offset : -offset;

        // 先向下(或上)移动并淡出
        imageView.animate()
                .translationY(end)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    loadImage(imageView); // 加载新图片
                    // 复位位置并设置透明度为0
                    imageView.setTranslationY(-end);
                    imageView.setAlpha(0f);

                    // 向上(或下)移回并淡入
                    imageView.animate()
                            .translationY(start)
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private void preloadImages() {
        for (String url : imageURLs) {
            Glide.with(this)
                    .load(url)
                    .preload();
        }
    }
}