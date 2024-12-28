package top.srintelligence.wallpaper_generator.UIController;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import top.srintelligence.wallpaper_generator.R;

import java.util.Objects;

public class PixivGenerateFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generate_from_pixiv, container, false);
        TextView textView = view.findViewById(R.id.generate_from_pixiv_page_title);
        // 创建加粗的Pixiv文字
        SpannableString spannableString = new SpannableString("Pixiv\n图片筛选");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置文本
        textView.setText(spannableString);
        textView.setTextSize(20); // 增大文字大小
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button addButton = Objects.requireNonNull(getView()).findViewById(R.id.add_tag_button);
        addButton.setOnClickListener(this::onAddTagButtonClick);
    }

    public void onAddTagButtonClick(View v) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PixivLabelSelection());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}