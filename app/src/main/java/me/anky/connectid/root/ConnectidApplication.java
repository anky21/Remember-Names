package me.anky.connectid.root;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import me.anky.connectid.R;
import me.anky.connectid.subscription.SubscriptionManager;

/**
 * Created by Anky An on 10/07/2017.
 * anky25@gmail.com
 */

public class ConnectidApplication extends Application {
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
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                int color = ContextCompat.getColor(activity, R.color.colorPrimaryDark);
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
}
