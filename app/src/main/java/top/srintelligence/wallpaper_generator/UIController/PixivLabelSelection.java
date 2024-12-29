package top.srintelligence.wallpaper_generator.UIController;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import top.srintelligence.wallpaper_generator.R;

public class PixivLabelSelection extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pixiv_label_selection, container, false);

        TextInputEditText editTextLabel = view.findViewById(R.id.editTextLabel);
        ChipGroup chipGroupLabels = view.findViewById(R.id.chipGroupLabels);

        editTextLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.toString().contains(" ")) {
                    String[] tags = s.toString().split(" ");
                    for (String tag : tags) {
                        if (!tag.isEmpty()) {
                            addChipToGroup(tag, chipGroupLabels);
                        }
                    }
                    editTextLabel.getText().clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
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