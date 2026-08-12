package me.anky.connectid.root;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.insets.ColorProtection;
import androidx.core.view.insets.ProtectionLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Collections;

import me.anky.connectid.R;
import me.anky.connectid.subscription.SubscriptionManager;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

public class ConnectidApplication extends Application {
    private static final String STATUS_BAR_PROTECTION_TAG = "status_bar_protection";
    private ApplicationComponent component;
    private FirebaseAnalytics mFirebaseAnalytics;
    static ConnectidApplication appInstance;

    private SubscriptionManager subscriptionManager;

    public ConnectidApplication() {
        appInstance = this;
    }

    public static ConnectidApplication getAppInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // needs to run once to generate it
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        // Allow to expose file uri when creating the csv file
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Force blue status bar on all activities
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                forceStatusBarColor(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                forceStatusBarColor(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                forceStatusBarColor(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    public FirebaseAnalytics getAnalyticsInstance() {
        return mFirebaseAnalytics;
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }

    public SubscriptionManager getSubscriptionManager() {
        if (subscriptionManager == null) {
            subscriptionManager = new SubscriptionManager(this);
        }
        return subscriptionManager;
    }

    private void forceStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Window window = activity.getWindow();
                int color = ContextCompat.getColor(activity, R.color.colorPrimaryDark);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    addStatusBarProtection(activity, window, color);
                    return;
                }

                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);

                // Force dark status bar icons on API 23+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decorView = window.getDecorView();
                    int flags = decorView.getSystemUiVisibility();
                    // Clear SYSTEM_UI_FLAG_LIGHT_STATUS_BAR to use light icons (for dark status bar)
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(flags);
                }
            } catch (Exception e) {
                android.util.Log.e("ConnectidApp", "Failed to set status bar color", e);
            }
        }
    }

    private void addStatusBarProtection(Activity activity, Window window, int color) {
        WindowCompat.getInsetsController(window, window.getDecorView())
                .setAppearanceLightStatusBars(false);

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.findViewWithTag(STATUS_BAR_PROTECTION_TAG) != null) {
            return;
        }

        ProtectionLayout protectionLayout = new ProtectionLayout(activity);
        protectionLayout.setTag(STATUS_BAR_PROTECTION_TAG);
        protectionLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        protectionLayout.setProtections(Collections.singletonList(
                new ColorProtection(WindowInsetsCompat.Side.TOP, color)
        ));
        content.addView(protectionLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        ViewCompat.requestApplyInsets(protectionLayout);
    }
}
