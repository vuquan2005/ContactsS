package com.example.contactvip;

import android.app.Application;

import com.example.contactvip.utils.PreferenceUtils;

public class ContactApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        int mode = PreferenceUtils.getThemeMode(this);
        PreferenceUtils.applyTheme(mode);
    }
}
