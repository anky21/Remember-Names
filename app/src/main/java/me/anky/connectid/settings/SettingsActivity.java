package me.anky.connectid.settings;

import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import javax.inject.Inject;

import me.anky.connectid.R;
import me.anky.connectid.data.SharedPrefsHelper;
import me.anky.connectid.root.ConnectidApplication;

public class SettingsActivity extends AppCompatActivity {

    @Inject
    SharedPrefsHelper sharedPrefsHelper;

    RadioGroup themeRadioGroup;
    RadioButton themeLightRb;
    RadioButton themeDarkRb;
    RadioButton themeSystemDefaultRb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this); // Dagger injection

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_settings);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        themeRadioGroup = findViewById(R.id.theme_radio_group);
        themeLightRb = findViewById(R.id.theme_light_rb);
        themeDarkRb = findViewById(R.id.theme_dark_rb);
        themeSystemDefaultRb = findViewById(R.id.theme_system_default_rb);

        loadCurrentTheme();
        setupThemeSelectionListener();
    }

    private void loadCurrentTheme() {
        int currentTheme = sharedPrefsHelper.getThemePreference();
        switch (currentTheme) {
            case SharedPrefsHelper.THEME_LIGHT:
                themeRadioGroup.check(R.id.theme_light_rb);
                break;
            case SharedPrefsHelper.THEME_DARK:
                themeRadioGroup.check(R.id.theme_dark_rb);
                break;
            case SharedPrefsHelper.THEME_SYSTEM_DEFAULT:
            default:
                themeRadioGroup.check(R.id.theme_system_default_rb);
                break;
        }
    }

    private void setupThemeSelectionListener() {
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedTheme = SharedPrefsHelper.THEME_SYSTEM_DEFAULT;
            int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.theme_light_rb) {
                selectedTheme = SharedPrefsHelper.THEME_LIGHT;
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.theme_dark_rb) {
                selectedTheme = SharedPrefsHelper.THEME_DARK;
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            // Default is already THEME_SYSTEM_DEFAULT and MODE_NIGHT_FOLLOW_SYSTEM

            sharedPrefsHelper.setThemePreference(selectedTheme);
            AppCompatDelegate.setDefaultNightMode(nightMode);
            // No need to recreate SettingsActivity, as AppCompatDelegate.setDefaultNightMode
            // should trigger a configuration change that redraws it if the theme context changes.
            // If not, a recreate() might be needed.
        });
    }

    // This is needed for Dagger to inject into SettingsActivity
    // It should be added to ApplicationComponent
    // void inject(SettingsActivity settingsActivity);
}
