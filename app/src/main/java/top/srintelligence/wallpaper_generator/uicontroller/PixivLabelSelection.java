package top.srintelligence.wallpaper_generator.uicontroller;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import top.srintelligence.wallpaper_generator.R;

import java.util.Objects;

public class PixivLabelSelection extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pixiv_label_selection, container, false); // 加载布局
        TextInputEditText editTextLabel = view.findViewById(R.id.editTextLabel); // 获取输入框
        ChipGroup chipGroupLabels = view.findViewById(R.id.chip_group_labels); // 获取 ChipGroup
        Button returnButton = view.findViewById(R.id.pixiv_label_return_button); // 获取返回按钮
        Button addLabelButton = view.findViewById(R.id.tag_button); // 获取添加按钮

        editTextLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && (s.toString().contains(" ") || s.toString().contains("\n"))) { // 检测是否输入了空格或换行符
                    String[] tags = s.toString().split("[\\s&]+"); // 以空格或换行符分割
                    for (String tag : tags) { // 遍历所有标签
                        if (!tag.isEmpty()) {
                            addChipToGroup(tag, chipGroupLabels); // 添加到 ChipGroup
                        }
                    }
                    Objects.requireNonNull(editTextLabel.getText()).clear(); // 清空输入框
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addLabelButton.setOnClickListener(v -> {
            String tag = Objects.requireNonNull(editTextLabel.getText()).toString(); // 获取输入框内容
            if (!tag.isEmpty()) {
                addChipToGroup(tag, chipGroupLabels); // 添加到 ChipGroup
                editTextLabel.setText(""); // 清空输入框
            }
        });

        returnButton.setOnClickListener(v -> {
            String[] tags = new String[chipGroupLabels.getChildCount()];
            for (int i = 0; i < chipGroupLabels.getChildCount(); i++) { // 获取所有标签
                Chip chip = (Chip) chipGroupLabels.getChildAt(i);
                tags[i] = chip.getText().toString(); // 添加到数组
            }

            Bundle bundle = new Bundle();
            bundle.putStringArray("tags", tags);

            getParentFragmentManager().setFragmentResult("pixiv_label_result", bundle); // 传递数据
            getParentFragmentManager().popBackStack(); // 返回上一个 Fragment
        });

        return view;
    }

    private void addChipToGroup(String tag, ChipGroup chipGroup) {
        Chip chip = new Chip(getContext());
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        chipGroup.addView(chip);
    }
}