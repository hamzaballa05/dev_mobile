package com.example.studentprofile;

public class ProfileLinearActivity extends BaseProfileActivity {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_profile_linear;
    }

    @Override
    protected String getScreenTitleText() {
        return getString(R.string.screen_title_linear);
    }
}
