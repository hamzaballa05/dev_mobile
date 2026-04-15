package com.example.studentprofile;

public class MainActivity extends BaseProfileActivity {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected String getScreenTitleText() {
        return getString(R.string.screen_title);
    }
}
