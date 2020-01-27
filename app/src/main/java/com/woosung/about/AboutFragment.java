package com.woosung.about;

import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.woosung.R;
import com.woosung.appDefault.AppDefaultFragment;


public class AboutFragment extends AppDefaultFragment {

    private TextView mVersionTextView;
    private TextView contactMe;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVersionTextView = view.findViewById(R.id.aboutVersionTextView);
        mVersionTextView.setText(getResources().getString(R.string.app_version));

    }

    @LayoutRes
    protected int layoutRes() {
        return R.layout.fragment_about;
    }

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }
}
