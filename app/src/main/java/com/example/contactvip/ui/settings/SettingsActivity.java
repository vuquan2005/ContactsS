package com.example.contactvip.ui.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contactvip.R;
import com.example.contactvip.databinding.ActivitySettingsBinding;
import com.example.contactvip.utils.PreferenceUtils;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Setup Theme
        int currentMode = PreferenceUtils.getThemeMode(this);
        if (currentMode == PreferenceUtils.THEME_LIGHT) {
            binding.rbLight.setChecked(true);
        } else if (currentMode == PreferenceUtils.THEME_DARK) {
            binding.rbDark.setChecked(true);
        } else {
            binding.rbSystem.setChecked(true);
        }

        binding.rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rb_light) {
                mode = PreferenceUtils.THEME_LIGHT;
            } else if (checkedId == R.id.rb_dark) {
                mode = PreferenceUtils.THEME_DARK;
            } else {
                mode = PreferenceUtils.THEME_SYSTEM;
            }
            PreferenceUtils.saveThemeMode(this, mode);
            PreferenceUtils.applyTheme(mode);
        });

        // Setup Language
        String currentLang = PreferenceUtils.getLanguage(this);
        if (PreferenceUtils.LANG_VI.equalsIgnoreCase(currentLang)) {
            binding.rbLangVi.setChecked(true);
        } else {
            binding.rbLangEn.setChecked(true);
        }

        binding.rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLang;
            if (checkedId == R.id.rb_lang_vi) {
                selectedLang = PreferenceUtils.LANG_VI;
            } else {
                selectedLang = PreferenceUtils.LANG_EN;
            }

            if (!selectedLang.equalsIgnoreCase(PreferenceUtils.getLanguage(this))) {
                PreferenceUtils.saveLanguage(this, selectedLang);
                PreferenceUtils.applyLanguage(selectedLang);
            }
        });
    }
}

