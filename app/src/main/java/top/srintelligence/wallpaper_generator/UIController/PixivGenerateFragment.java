package top.srintelligence.wallpaper_generator.UIController;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import top.srintelligence.wallpaper_generator.MainActivity;
import top.srintelligence.wallpaper_generator.R;
import top.srintelligence.wallpaper_generator.lookup_kernel.process.pixiv.Pixiv_Request_Builder;

import java.util.Objects;

public class PixivGenerateFragment extends Fragment {
    String[] tags;
    Boolean excludeAI = false;
    int limit = 0;

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

        NumberPicker numberPicker = view.findViewById(R.id.pixiv_input_number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> limit = newVal);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button addButton = Objects.requireNonNull(getView()).findViewById(R.id.add_tag_button);
        Button generateButton = getView().findViewById(R.id.pixiv_generate_button);
        CheckBox excludeAIBox = getView().findViewById(R.id.exclude_ai_checkbox);
        excludeAIBox.setOnCheckedChangeListener((buttonView, isChecked) -> excludeAI = isChecked);
        generateButton.setOnClickListener(v -> new GeneratePixivTask().execute());
        addButton.setOnClickListener(this::onAddTagButtonClick);
    }

    private class GeneratePixivTask extends AsyncTask<Void, Void, Void> {
        private AlertDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context context = MainActivity.getInstance();
            progressDialog = new MaterialAlertDialogBuilder(context)
                    .setView(R.layout.progress_dialog) // 使用自定义布局
                    .setCancelable(false)
                    .create();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Pixiv_Request_Builder builder = new Pixiv_Request_Builder();
            builder
                    .setTags(tags)
                    .setLimit(limit)
                    .excludeAI(excludeAI)
                    .build();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // 更新UI
        }
    }

    public void onAddTagButtonClick(View v) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PixivLabelSelection());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
