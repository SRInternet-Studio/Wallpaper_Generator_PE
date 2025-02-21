package top.srintelligence.wallpaper_generator.uicontroller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.srintelligence.wallpaper_generator.ImageShowcaseActivity;
import top.srintelligence.wallpaper_generator.MainActivity;
import top.srintelligence.wallpaper_generator.R;
import top.srintelligence.wallpaper_generator.api.acg.DefaultACGAPIConfig;
import top.srintelligence.wallpaper_generator.api.acg.MirlKoiAPISort;
import top.srintelligence.wallpaper_generator.function.acg.ACGRequestBuilder;

import java.util.ArrayList;
import java.util.List;

public class ACGGenerateFragment extends Fragment {

    private View view;
    List<String> acgURLs = new ArrayList<>();
    String type;
    int limit = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_acg_generate, container, false);
        TextView page_titleView = view.findViewById(R.id.generate_from_acg_page_title);

        // 加粗
        SpannableString spannableString = new SpannableString("ACG\n图片推荐");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置文本
        page_titleView.setText(spannableString);
        page_titleView.setTextSize(20); // 文字大小

        Spinner spinner = view.findViewById(R.id.acg_generate_type_spinner);

        // 创建一个数组适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.acg_generate_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 将适配器设置到Spinner
        spinner.setAdapter(adapter);

        NumberPicker numberPicker = view.findViewById(R.id.acg_input_number_picker); // 数量选择器
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> limit = newVal);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button generateButton = getView().findViewById(R.id.acg_generate_button); // 生成按钮
        generateButton.setOnClickListener(v -> {
            Spinner spinner = view.findViewById(R.id.acg_generate_type_spinner);
            type = spinner.getSelectedItem().toString();
            new GenerateACGTask().execute();
        });
    }


    private class GenerateACGTask extends AsyncTask<Void, Void, Void> { // 异步任务
        private AlertDialog progressDialog;

        @Override
        protected void onPreExecute() { // 加载框
            super.onPreExecute();
            Context context = MainActivity.getInstance();
            progressDialog = new MaterialAlertDialogBuilder(context) // 加载框
                    .setView(R.layout.progress_dialog)
                    .setCancelable(false)
                    .create();
            progressDialog.show(); // 显示加载框
        }

        @Override
        protected Void doInBackground(Void... voids) { // 后台任务
            ACGRequestBuilder acgRequestBuilder = new ACGRequestBuilder();
            if (type.equals("随机")) {
                acgRequestBuilder
                        .setSort(MirlKoiAPISort.TOP);
            } else if (type.equals("兽耳")) {
                acgRequestBuilder
                        .setSort(MirlKoiAPISort.CATGIRL);
            } else if (type.equals("银发")) {
                acgRequestBuilder
                        .setSort(MirlKoiAPISort.SILVERHAIR);
            } else if (type.equals("星空")) {
                acgRequestBuilder
                        .setSort(MirlKoiAPISort.STARRYSKY);
            }

            acgRequestBuilder
                    .setApi(DefaultACGAPIConfig.mirlKoiAPI)
                    .setNum(limit);

            acgURLs = acgRequestBuilder.build();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // 后台任务完成
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            try {
                new Handler().post(() -> {
                    Intent intent = new Intent(MainActivity.getInstance(), ImageShowcaseActivity.class);
                    intent.putStringArrayListExtra("imageURLs", (ArrayList<String>) acgURLs);
                    ExceptionHandler.handleDebug("ACG URL: " + acgURLs);
                    startActivity(intent); // 启动Activity
                    acgURLs.clear();
                });
            } catch (Exception e) {
                ExceptionHandler.handleException(e);
            }

        }
    }
}
