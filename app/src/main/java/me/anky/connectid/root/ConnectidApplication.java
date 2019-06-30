package me.anky.connectid.root;

import android.app.Application;
import android.os.StrictMode;

import com.google.firebase.analytics.FirebaseAnalytics;

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

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // needs to run once to generate it
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        // Allow to expose file uri when creating the csv file
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public FirebaseAnalytics getAnalyticsInstance() {
        return mFirebaseAnalytics;
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }
}
