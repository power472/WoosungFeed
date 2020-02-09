package com.woosung.messages;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.woosung.appDefault.AppDefaultActivity;

public class MessageActivity extends AppDefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    protected Fragment createInitialFragment() {
        return MessageFragment.newInstance();
    }
}
