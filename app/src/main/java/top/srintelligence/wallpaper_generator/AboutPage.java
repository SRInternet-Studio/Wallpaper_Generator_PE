package top.srintelligence.wallpaper_generator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_about_page);

            TextView gButton = findViewById(R.id.githubButton);
            gButton.setOnClickListener(v -> {
                    openHTMLInBrowser(MainActivity.getInstance().getString(R.string.git_point));
                });

            TextView sButton = findViewById(R.id.statementButton);
            sButton.setOnClickListener(v -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                            .setTitle("免责声明")
                            .setMessage(this.getString(R.string.statement))
                            .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                });
            });

            TextView aButton = findViewById(R.id.agreementButton);
            aButton.setOnClickListener(v -> {
                openHTMLInBrowser("https://github.com/SRInternet-Studio/Wallpaper_Generator_PE/blob/Development/LICENSE");
            });

            ImageButton backButton = findViewById(R.id.back_button); // 生成按钮
            backButton.setOnClickListener(v -> {
                finish();
            });

        } catch (Exception e) {
            this.finish();
            ExceptionHandler.handleException(e);
        }
    }

    private void openHTMLInBrowser(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(intent);

    }
}