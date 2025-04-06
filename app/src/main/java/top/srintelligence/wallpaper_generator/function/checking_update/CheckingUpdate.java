package top.srintelligence.wallpaper_generator.function.checking_update;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.srintelligence.wallpaper_generator.MainActivity;
import top.srintelligence.wallpaper_generator.R;

public class CheckingUpdate {
    public void checkForUpdate() {
        System.out.println("启动检查更新");
        OkHttpClient client = new OkHttpClient(); // 创建 OkHttp 客户端
        Request request = new Request.Builder()
                .url(MainActivity.getInstance().getString(R.string.git_check_point) + "/releases")
                .addHeader(MainActivity.getInstance().getString(R.string.GitHeaderName), MainActivity.getInstance().getString(R.string.GitPassport)) // 添加 Authorization 标头
                .build();

        client.newCall(request).enqueue(new Callback() { // 异步发送 HTTP 请求
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        System.out.println(responseData);
                        JSONArray releasesArray = new JSONArray(responseData);
                        if (releasesArray.length() > 0) {
                            JSONObject latestRelease = releasesArray.getJSONObject(0); // 获取最新发布的版本
                            String latestVersion = latestRelease.getString("tag_name"); // 获取最新版本号
                            String Text = latestRelease.getString("body"); // 获取最新版本描述
                            String html_url = latestRelease.getString("html_url"); // 获取最新版本仓库链接

                            String browserDownloadUrl = null;
                            JSONArray assets = latestRelease.getJSONArray("assets"); // 获取最新版本下载链接

                            if (assets.length() > 0) {
                                // 遍历 assets 数组，筛选出目标文件的 browser_download_url
                                for (int i = 0; i < assets.length(); i++) {
                                    JSONObject asset = assets.getJSONObject(i);
                                    browserDownloadUrl = asset.getString("browser_download_url");
                                    // 进行其他操作，比如打印或存储
                                    System.out.println("目标文件的 browser_download_url: " + browserDownloadUrl);
                                }
                            } else {
                                System.out.println("没有找到目标文件。");
                            }

                            compareVersions(latestVersion, Text, html_url, browserDownloadUrl); // 比较版本号

                        } else {
                            // 没有发布的版本
                            new Handler(Looper.getMainLooper()).post(() -> {
                                androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(MainActivity.getInstance())
                                        .setTitle("找不到版本")
                                        .setMessage("请稍后重试，或向开发者反馈。")
                                        .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> dialogInterface.dismiss())
                                        .setCancelable(false) // 不可取消对话框
                                        .create();
                                dialog.show();
                            });

                        }
                    } catch (JSONException e) {
                        ExceptionHandler.handleDebug(e.getMessage());
                    }
                } else {
                    // 请求失败
                    new Handler(Looper.getMainLooper()).post(() -> {
                        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(MainActivity.getInstance())
                                .setTitle("已连接，但是请求失败")
                                .setMessage("请稍后重试，或前往Github仓库手动检查更新。\n您可以在“关于”中查看Github仓库信息。")
                                .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> dialogInterface.dismiss())
                                .setCancelable(false) // 不可取消对话框
                                .create();
                        dialog.show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 网络请求失败
                new Handler(Looper.getMainLooper()).post(() -> {
                    androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(MainActivity.getInstance())
                            .setTitle("无网络，或连接失败")
                            .setMessage("无法连接到服务器，请检查设备的网络状态，以及程序的网络请求权限是否已经给予，并在稍后重试。")
                            .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> dialogInterface.dismiss())
                            .setCancelable(false) // 不可取消对话框
                            .create();
                    dialog.show();
                });

            }
        });
    }

    private void openHTMLInBrowser(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        MainActivity.getInstance().startActivity(intent);
    }

    public void compareVersions(String latestVersion, String Text, String html_url, String browserDownloadUrl) {
        String currentVersion = MainActivity.getInstance().getString(R.string.VersionNumber);  // 替换成你实际的版本获取方式
        currentVersion = currentVersion.replaceAll("[^\\d.]+", "");
        System.out.println(currentVersion);

        latestVersion = latestVersion.replaceAll("[^\\d.]+", "");

        androidx.appcompat.app.AlertDialog.Builder builder = new MaterialAlertDialogBuilder(MainActivity.getInstance());
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        View customView = inflater.inflate(R.layout.update_dialog, null);
        builder.setView(customView);

        ImageView icon = customView.findViewById(R.id.imageView10);

        icon.setImageResource(R.drawable.ic_have_update);

        Button button1 = customView.findViewById(R.id.button_positive);
        Button button2 = customView.findViewById(R.id.button_negative);
        Button button3 = customView.findViewById(R.id.button_update);

        TextView DialogTitle = customView.findViewById(R.id.dialog_title);
        TextView DialogMessage = customView.findViewById(R.id.dialog_message);
        TextView DialogMessage2 = customView.findViewById(R.id.dialog_message2);

        DialogMessage2.setText("    当前版本：" + currentVersion);

        // 进行版本对比，判断是否需要更新
        if (latestVersion.compareTo(currentVersion) > 0) {
            // 需要更新，弹出更新对话框或其他相应操作

            DialogTitle.setText("发现新版本！");
            DialogMessage.setText("    最新版本：" + latestVersion + "\n\n" + Text);

            Looper.prepare(); // 添加此行

            final androidx.appcompat.app.AlertDialog dialog = builder.create();
            new Handler(Looper.getMainLooper()).post(dialog::show);;

            button1.setOnClickListener(v -> {
                // 要打开的HTML的URL
                openHTMLInBrowser(html_url);
                new Handler(Looper.getMainLooper()).post(dialog::dismiss);
            });

            button3.setOnClickListener(v -> {
                // 要打开的HTML的URL
                openHTMLInBrowser(browserDownloadUrl);
                new Handler(Looper.getMainLooper()).post(dialog::dismiss);
            });

            button2.setOnClickListener(v -> new Handler(Looper.getMainLooper()).post(dialog::dismiss));

            Looper.loop(); // 添加此行


        } else {
            // 已是最新版本
            if (Objects.equals(Text, "")) {
                DialogTitle.setText("有新版本准备中➪");
                DialogMessage.setText("有一个更新正在排队等待发布。请稍作等待，新的更新或将在几天之内正式发布。\n若要在此时获取新的更新，您可以加入我们的开发者联系或加入交流群参与内测哦\nヾ(≧∪≦*)ノ〃");
            } else {
                DialogTitle.setText("无更新版本");
                DialogMessage.setText("本版本更新信息：\n" + Text);
            }

            button3.setTextColor(Color.parseColor("#808080"));
            button2.setText("确定");

            Looper.prepare(); // 添加此行

            button1.setOnClickListener(v -> {
                // 要打开的HTML的URL
                openHTMLInBrowser(html_url);
            });

            button3.setOnClickListener(v -> {
                //Nothing there
            });

            final androidx.appcompat.app.AlertDialog dialog = builder.create();
            new Handler(Looper.getMainLooper()).post(dialog::show);

            button2.setOnClickListener(v -> new Handler(Looper.getMainLooper()).post(dialog::dismiss));

            Looper.loop(); // 添加此行
        }
    }
}
