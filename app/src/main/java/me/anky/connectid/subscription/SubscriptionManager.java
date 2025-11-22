package me.anky.connectid.subscription;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple wrapper around SharedPreferences to cache whether the user currently has
 * an active ad-free subscription.
 */
public class SubscriptionManager {

    private static final String PREFS_NAME = "subscription_prefs";
    private static final String KEY_AD_FREE = "key_ad_free";

    private final SharedPreferences prefs;

    public SubscriptionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAdFree() {
        return prefs.getBoolean(KEY_AD_FREE, false);
    }

    public void setAdFree(boolean adFree) {
        prefs.edit().putBoolean(KEY_AD_FREE, adFree).apply();
    }
}
