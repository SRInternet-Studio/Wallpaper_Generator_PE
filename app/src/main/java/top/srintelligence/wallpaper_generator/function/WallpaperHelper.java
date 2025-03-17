package top.srintelligence.wallpaper_generator.function;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;

import java.io.File;
import java.io.FileOutputStream;

public class WallpaperHelper {
    private final Context context;

    public WallpaperHelper(Context context) {
        this.context = context;
    }

    public void showSetWallpaperDialog(String imageUrl) {
        Toast.makeText(context, "准备设置壁纸...", Toast.LENGTH_SHORT).show();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("选择壁纸设置方式")
                .setItems(new String[]{"AOSP裁剪设置", "直接设置(无调整)", "自定义设置(锁屏/桌面)"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            setWallpaperWithSystemCrop(imageUrl);
                            break;
                        case 1:
                            setWallpaperDirectly(imageUrl);
                            break;
                        case 2:
                            setWallpaperWithCustomOptions(imageUrl);
                            break;
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void setWallpaperWithSystemCrop(String imageUrl) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            File wallpaperFile = new File(context.getCacheDir(), "temp_wallpaper.jpg");
                            FileOutputStream fos = new FileOutputStream(wallpaperFile);
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();

                            Uri contentUri = FileProvider.getUriForFile(
                                    context,
                                    "top.srintelligence.wallpaper_generator.fileprovider",
                                    wallpaperFile);

                            Intent cropIntent = new Intent("com.android.camera.action.CROP");
                            cropIntent.setDataAndType(contentUri, "image/*");
                            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cropIntent.putExtra("crop", "true");
                            cropIntent.putExtra("scale", true);
                            cropIntent.putExtra("return-data", false);
                            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                            cropIntent.putExtra("noFaceDetection", true);

                            if (cropIntent.resolveActivity(context.getPackageManager()) != null) {
                                context.startActivity(cropIntent);
                            } else {
                                Intent wallpaperIntent = new Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER);
                                wallpaperIntent.setDataAndType(contentUri, "image/*");
                                wallpaperIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                if (wallpaperIntent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(wallpaperIntent);
                                } else {
                                    Toast.makeText(context, "系统裁剪功能不可用，请尝试直接设置选项", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "壁纸设置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            ExceptionHandler.handleException(e);
                        }
                    }
                });
    }

    private void setWallpaperDirectly(String imageUrl) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        try {
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                            wallpaperManager.setBitmap(resource);
                            Toast.makeText(context, "壁纸设置成功", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "壁纸设置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            ExceptionHandler.handleException(e);
                        }
                    }
                });
    }

    private void setWallpaperWithCustomOptions(String imageUrl) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                            builder.setTitle("设置壁纸位置")
                                    .setItems(new String[]{"仅桌面", "仅锁屏", "桌面和锁屏"}, (dialog, which) -> {
                                        try {
                                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                                            switch (which) {
                                                case 0:
                                                    wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM);
                                                    break;
                                                case 1:
                                                    wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK);
                                                    break;
                                                case 2:
                                                    wallpaperManager.setBitmap(resource);
                                                    break;
                                            }
                                            Toast.makeText(context, "壁纸设置成功", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(context, "壁纸设置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            ExceptionHandler.handleException(e);
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create()
                                    .show();
                        }
                    });
        } else {
            Toast.makeText(context, "您的设备不支持分别设置锁屏和桌面壁纸", Toast.LENGTH_SHORT).show();
            setWallpaperDirectly(imageUrl);
        }
    }
}