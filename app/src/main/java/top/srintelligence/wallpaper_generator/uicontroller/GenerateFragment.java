package top.srintelligence.wallpaper_generator.uicontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import top.srintelligence.wallpaper_generator.R;

public class GenerateFragment extends Fragment {

    private View _view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_generate, container, false);

        Button generateFromPixivButton = _view.findViewById(R.id.generate_from_pixiv_button);
        generateFromPixivButton.setOnClickListener(this::onGenerateFromPixivButtonClick);

        return _view;
    }

    public void onGenerateFromPixivButtonClick(View v) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PixivGenerateFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _view = null;
    }
}