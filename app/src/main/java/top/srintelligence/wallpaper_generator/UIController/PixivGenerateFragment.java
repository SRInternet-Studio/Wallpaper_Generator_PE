package top.srintelligence.wallpaper_generator.UIController;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import top.srintelligence.wallpaper_generator.R;

import java.util.Objects;

public class PixivGenerateFragment extends Fragment {
    String[] tags;
    Boolean excludeAI = false;


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
        numberPicker.setMaxValue(100);
        numberPicker.setWrapSelectorWheel(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button addButton = Objects.requireNonNull(getView()).findViewById(R.id.add_tag_button);
        CheckBox excludeAIBox = getView().findViewById(R.id.exclude_ai_checkbox);
        excludeAIBox.setOnCheckedChangeListener((buttonView, isChecked) -> excludeAI = isChecked);
        addButton.setOnClickListener(this::onAddTagButtonClick);
    }

    public void onAddTagButtonClick(View v) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PixivLabelSelection());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}