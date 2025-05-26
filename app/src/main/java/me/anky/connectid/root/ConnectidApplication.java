package me.anky.connectid.root;

import android.app.Application;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.analytics.FirebaseAnalytics;

import me.anky.connectid.data.SharedPrefsHelper;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

public class ConnectidApplication extends Application {
    private ApplicationComponent component;
    private FirebaseAnalytics mFirebaseAnalytics;
    static ConnectidApplication appInstance;

    public ConnectidApplication() {
        appInstance = this;
    }

    public static ConnectidApplication getAppInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); // Moved after component creation

        // needs to run once to generate it
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        
        // Apply the saved theme preference
        SharedPrefsHelper sharedPrefsHelper = component.getPreferenceHelper(); // Corrected method name
        applyTheme(sharedPrefsHelper.getThemePreference());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); // Initialized after component and theme

        // needs to run once to generate it
        // Allow to expose file uri when creating the csv file
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void applyTheme(int themePreference) {
        switch (themePreference) {
            case SharedPrefsHelper.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case SharedPrefsHelper.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default: // THEME_SYSTEM_DEFAULT or any other case
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public FirebaseAnalytics getAnalyticsInstance() {
        return mFirebaseAnalytics;
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }
}
