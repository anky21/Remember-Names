package me.anky.connectid.subscription;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal BillingClient wrapper to support a single subscription: ad_free_monthly.
 *
 * This does not try to be a full-featured billing abstraction; it focuses on:
 * - Connecting to Google Play Billing
 * - Querying whether the user is currently entitled to ad-free
 * - Launching the purchase flow for ad_free_monthly
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";
    private static final String PRODUCT_ID_AD_FREE = "ad_free_monthly";
    // Basic plan ID configured in Play Console for this subscription
    private static final String BASE_PLAN_ID_AD_FREE = "ad-free-monthly-plan";

    public interface BillingUpdatesListener {
        void onAdFreeStatusChanged(boolean isAdFree);
    }

    private final Context appContext;
    private final SubscriptionManager subscriptionManager;
    private BillingClient billingClient;
    private ProductDetails adFreeProductDetails;
    private BillingUpdatesListener updatesListener;

    public BillingManager(Context context, SubscriptionManager subscriptionManager) {
        this.appContext = context.getApplicationContext();
        this.subscriptionManager = subscriptionManager;
        buildBillingClient();
    }

    public void setBillingUpdatesListener(BillingUpdatesListener listener) {
        this.updatesListener = listener;
    }

    private void buildBillingClient() {
        billingClient = BillingClient.newBuilder(appContext)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }

    public void startConnection() {
        if (billingClient == null) {
            buildBillingClient();
        }
        if (billingClient.isReady()) {
            queryExistingSubscriptions();
            return;
        }
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished");
                    queryProductDetails();
                    queryExistingSubscriptions();
                } else {
                    Log.w(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                    Toast.makeText(appContext,
                            appContext.getString(me.anky.connectid.R.string.billing_setup_failed,
                                    billingResult.getDebugMessage()),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected");
                Toast.makeText(appContext,
                        appContext.getString(me.anky.connectid.R.string.billing_service_disconnected),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Query Play for existing subscriptions and update the local ad-free flag.
     */
    public void queryExistingSubscriptions() {
        if (billingClient == null || !billingClient.isReady()) return;

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchasesList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "queryPurchasesAsync failed: " + billingResult.getDebugMessage());
                Toast.makeText(appContext,
                        appContext.getString(me.anky.connectid.R.string.billing_query_purchases_failed,
                                billingResult.getDebugMessage()),
                        Toast.LENGTH_LONG).show();
                return;
            }
            boolean hasAdFree = false;
            if (purchasesList != null) {
                for (Purchase purchase : purchasesList) {
                    if (purchase.getProducts().contains(PRODUCT_ID_AD_FREE)
                            && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                            && !purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                        hasAdFree = true;
                    } else if (purchase.getProducts().contains(PRODUCT_ID_AD_FREE)
                            && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        hasAdFree = true;
                    }
                }
            }
            notifyAdFreeChanged(hasAdFree);
        });
    }

    private void queryProductDetails() {
        if (billingClient == null || !billingClient.isReady()) return;

        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        products.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_AD_FREE)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "queryProductDetailsAsync failed: " + billingResult.getDebugMessage());
                Toast.makeText(appContext,
                        appContext.getString(me.anky.connectid.R.string.billing_product_details_failed,
                                billingResult.getDebugMessage()),
                        Toast.LENGTH_LONG).show();
                return;
            }
            boolean found = false;
            if (productDetailsList != null) {
                for (ProductDetails details : productDetailsList) {
                    if (PRODUCT_ID_AD_FREE.equals(details.getProductId())) {
                        adFreeProductDetails = details;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Log.w(TAG, "Ad free product not found in Play: " + PRODUCT_ID_AD_FREE);
                Toast.makeText(appContext,
                        appContext.getString(me.anky.connectid.R.string.billing_product_not_found),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void launchAdFreePurchase(Activity activity) {
        // If billing is not yet ready, start the connection and let the user know
        if (billingClient == null || !billingClient.isReady()) {
            Toast.makeText(activity,
                    activity.getString(me.anky.connectid.R.string.billing_initializing),
                    Toast.LENGTH_SHORT).show();
            startConnection();
            return;
        }
        // If we don't have product details yet, query them and show a short message
        if (adFreeProductDetails == null) {
            Toast.makeText(activity,
                    activity.getString(me.anky.connectid.R.string.billing_loading_product),
                    Toast.LENGTH_SHORT).show();
            queryProductDetails();
            return;
        }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        ProductDetails.SubscriptionOfferDetails offer = null;
        if (adFreeProductDetails.getSubscriptionOfferDetails() != null
                && !adFreeProductDetails.getSubscriptionOfferDetails().isEmpty()) {
            // Prefer the offer that matches our base plan ID, if present
            for (ProductDetails.SubscriptionOfferDetails candidate : adFreeProductDetails.getSubscriptionOfferDetails()) {
                if (BASE_PLAN_ID_AD_FREE.equals(candidate.getBasePlanId())) {
                    offer = candidate;
                    break;
                }
            }
            // Fallback to first offer if no exact base-plan match
            if (offer == null) {
                offer = adFreeProductDetails.getSubscriptionOfferDetails().get(0);
            }
        }
        if (offer == null) {
            Log.w(TAG, "No subscription offer details for ad_free_monthly / " + BASE_PLAN_ID_AD_FREE);
            Toast.makeText(appContext,
                    appContext.getString(me.anky.connectid.R.string.billing_no_offer_details),
                    Toast.LENGTH_LONG).show();
            return;
        }

        productDetailsParamsList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(adFreeProductDetails)
                        .setOfferToken(offer.getOfferToken())
                        .build()
        );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getProducts().contains(PRODUCT_ID_AD_FREE)
                        && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase);
                    notifyAdFreeChanged(true);
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Purchase canceled by user");
        } else {
            Log.w(TAG, "Purchase failed: " + billingResult.getDebugMessage());
            Toast.makeText(appContext,
                    appContext.getString(me.anky.connectid.R.string.billing_purchase_failed,
                            billingResult.getDebugMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (billingClient == null || !billingClient.isReady()) return;
        if (purchase.isAcknowledged()) return;

        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(params, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged");
            } else {
                Log.w(TAG, "Acknowledge failed: " + billingResult.getDebugMessage());
                Toast.makeText(appContext,
                        appContext.getString(me.anky.connectid.R.string.billing_acknowledge_failed,
                                billingResult.getDebugMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void notifyAdFreeChanged(boolean hasAdFree) {
        subscriptionManager.setAdFree(hasAdFree);
        if (updatesListener != null) {
            new Handler(Looper.getMainLooper()).post(
                    () -> updatesListener.onAdFreeStatusChanged(hasAdFree));
        }
    }

    public void destroy() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
    }
}
