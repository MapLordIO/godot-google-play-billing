package org.godotengine.godot.plugin.googleplaybilling.helpers;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBilling;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBillingPlugin;

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
            GooglePlayBillingPlugin.getInstance().emitPluginSignal("disconnected", true);

            this.googlePlayBilling.getBillingClient().startConnection(this);
        } else {
            GooglePlayBillingPlugin.getInstance().emitPluginSignal("disconnected", false);
        }
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            this.connected = true;

            GooglePlayBillingPlugin.getInstance().emitPluginSignal("connected");
        } else {
            System.out.println("[IAP] Not Connected");

            if (this.googlePlayBilling.isAutoRetryEnabled() && retries < GooglePlayBilling.MAX_RETRIES_COUNT) {
                retryConnection();

                GooglePlayBillingPlugin.getInstance().emitPluginSignal("retrying_connection", billingResult.getResponseCode(), billingResult.getDebugMessage());
            } else {
                GooglePlayBillingPlugin.getInstance().emitPluginSignal("failed_connecting", billingResult.getResponseCode(), billingResult.getDebugMessage());
            }
        }
    }

    public void startConnection() {
        this.googlePlayBilling.getBillingClient().startConnection(this);
    }

    public boolean isConnected() {
        return connected;
    }

    private void retryConnection() {
        if (retries < 3) {
            retries++;

            this.googlePlayBilling.getBillingClient().startConnection(this);
        }
    }
}
