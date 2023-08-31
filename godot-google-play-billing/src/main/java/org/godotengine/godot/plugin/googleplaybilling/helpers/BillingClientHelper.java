package org.godotengine.godot.plugin.googleplaybilling.helpers;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryPurchasesParams;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBilling;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBillingPlugin;
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils;

public class BillingClientHelper implements BillingClientStateListener {
    private final GooglePlayBilling googlePlayBilling;
    private final Godot godot;

    private boolean connected;
    private int retries;

    public BillingClientHelper(GooglePlayBilling googlePlayBilling, Godot godot) {
        this.googlePlayBilling = googlePlayBilling;
        this.godot = godot;
    }

    @Override
    public void onBillingServiceDisconnected() {
        if (this.googlePlayBilling.isAutoRetryEnabled()) {
            GooglePlayBillingPlugin.getInstance().emitSignal("disconnected", true);

            this.googlePlayBilling.getBillingClient().startConnection(this);
        } else {
            GooglePlayBillingPlugin.getInstance().emitSignal("disconnected", false);
        }
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            this.connected = true;

            GooglePlayBillingPlugin.getInstance().emitSignal("connected");

            this.queryPurchases();
        } else {
            if (this.googlePlayBilling.isAutoRetryEnabled() && retries < GooglePlayBilling.MAX_RETRIES_COUNT) {
                retryConnection();

                GooglePlayBillingPlugin.getInstance().emitSignal("retrying_connection", billingResult.getResponseCode(), billingResult.getDebugMessage());
            } else {
                GooglePlayBillingPlugin.getInstance().emitSignal("failed_connecting", billingResult.getResponseCode(), billingResult.getDebugMessage());
            }
        }
    }

    public void startConnection() {
        this.googlePlayBilling.getBillingClient().startConnection(this);
    }

    public boolean isConnected() {
        return connected;
    }

    private void queryPurchases() {
        QueryPurchasesParams queryPurchasesParams;

        PurchasesResponseListener purchasesQueryListener = (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "queried_purchases",
                        BillingClient.BillingResponseCode.OK,
                        GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray(list)
                );
            } else {
                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "query_purchases_error",
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        this.googlePlayBilling.getBillingClient().queryPurchasesAsync(queryPurchasesParams, purchasesQueryListener);

        queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();
        this.googlePlayBilling.getBillingClient().queryPurchasesAsync(queryPurchasesParams, purchasesQueryListener);
    }

    private void retryConnection() {
        if (retries < 3) {
            retries++;

            this.googlePlayBilling.getBillingClient().startConnection(this);
        }
    }
}
