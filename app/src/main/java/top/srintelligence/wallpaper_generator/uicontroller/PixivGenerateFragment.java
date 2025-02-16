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
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import top.srintelligence.wallpaper_generator.Image_Showcase_Activity;
import top.srintelligence.wallpaper_generator.MainActivity;
import top.srintelligence.wallpaper_generator.R;
import top.fireworkrocket.lookup_kernel.process.pixiv.Pixiv_Request_Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PixivGenerateFragment extends Fragment {
    String[] tags;
    Boolean excludeAI = false;
    int limit = 0;
    List<String> pixivURLs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generate_from_pixiv, container, false);
        TextView page_titleView = view.findViewById(R.id.generate_from_pixiv_page_title);
        TextView tag_View = view.findViewById(R.id.pixiv_Tag_Text_View);

        // 加粗
        SpannableString spannableString = new SpannableString("Pixiv\n图片筛选");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置文本
        page_titleView.setText(spannableString);
        page_titleView.setTextSize(20); // 文字大小

        getParentFragmentManager().setFragmentResultListener("pixiv_label_result", this, (requestKey, result) -> {
            String tagsString = String.join(", ", Objects.requireNonNull(result.getStringArray("tags")));
            tags = result.getStringArray("tags");
            tag_View.setText(tagsString);
        });

        NumberPicker numberPicker = view.findViewById(R.id.pixiv_input_number_picker); // 数量选择器
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> limit = newVal);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) { // 设置点击事件
        super.onActivityCreated(savedInstanceState);

        Button addButton = Objects.requireNonNull(getView()).findViewById(R.id.add_tag_button); // 添加标签按钮
        Button generateButton = getView().findViewById(R.id.pixiv_generate_button); // 生成按钮

        CheckBox excludeAIBox = getView().findViewById(R.id.exclude_ai_checkbox); // 排除AI
        excludeAIBox.setOnCheckedChangeListener((buttonView, isChecked) -> excludeAI = isChecked); // 排除AI

        generateButton.setOnClickListener(v -> new GeneratePixivTask().execute()); // 生成按钮
        addButton.setOnClickListener(this::onAddTagButtonClick); // 添加标签
    }

    private class GeneratePixivTask extends AsyncTask<Void, Void, Void> { // 异步任务
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
            Pixiv_Request_Builder builder = new Pixiv_Request_Builder();
            pixivURLs = builder
                    .setTags(tags)
                    .setLimit(limit)
                    .excludeAI(excludeAI)
                    .build();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // 后台任务完成
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            new Handler().post(() -> {
                Intent intent = new Intent(MainActivity.getInstance(), Image_Showcase_Activity.class); // 跳转到Image_Showcase_Activity
                intent.putStringArrayListExtra("imageURLs", (ArrayList<String>) pixivURLs); // 将图片URL传递给Image_Showcase_Activity
                startActivity(intent);
            });
        }
    }

    public void onAddTagButtonClick(View v) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PixivLabelSelection());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
