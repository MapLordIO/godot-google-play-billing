package org.godotengine.godot.plugin.googleplaybilling.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBilling;
import org.godotengine.godot.plugin.googleplaybilling.GooglePlayBillingPlugin;
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PurchasesHelper implements PurchasesUpdatedListener {
    private final GooglePlayBilling googlePlayBilling;
    private final Godot godot;

    private String obfuscatedAccountId;
    private String obfuscatedProfileId;

    public PurchasesHelper(GooglePlayBilling googlePlayBilling, Godot godot) {
        this.googlePlayBilling = googlePlayBilling;
        this.godot = godot;
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            GooglePlayBillingPlugin.getInstance().emitSignal(
                    "purchases_updated",
                    BillingClient.BillingResponseCode.OK,
                    GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray(purchases)
            );
        } else {
            GooglePlayBillingPlugin.getInstance().emitSignal(
                    "purchases_error",
                    billingResult.getResponseCode(),
                    billingResult.getDebugMessage()
            );
        }
    }

    public boolean purchaseInApp(@NonNull String productId,
                              boolean isPersonalized) {
        Objects.requireNonNull(this.godot.getActivity());

        ProductDetails productDetails = this.googlePlayBilling.getLoadedProductDetails(productId);

        if (productDetails == null) {
            return false;
        }

        if (!productDetails.getProductType().equals(BillingClient.ProductType.INAPP)) {
            return false;
        }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build());

        BillingFlowParams.Builder billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .setObfuscatedAccountId(this.obfuscatedAccountId)
                .setIsOfferPersonalized(isPersonalized);

        if (this.obfuscatedProfileId != null) billingFlowParams.setObfuscatedProfileId(this.obfuscatedProfileId);
        if (this.obfuscatedAccountId != null) billingFlowParams.setObfuscatedAccountId(this.obfuscatedAccountId);

        this.googlePlayBilling.getBillingClient().launchBillingFlow(this.godot.getActivity(), billingFlowParams.build());

        return true;
    }

    public boolean purchaseSubscription(@NonNull String productId,
                                     @NonNull String basePlanId,
                                     @Nullable String offerId,
                                     @Nullable BillingFlowParams.SubscriptionUpdateParams subscriptionUpdateParams,
                                     boolean isPersonalized) {
        Objects.requireNonNull(this.godot.getActivity());

        ProductDetails productDetails = this.googlePlayBilling.getLoadedProductDetails(productId);

        if (productDetails == null) {
            return false;
        }

        if (!productDetails.getProductType().equals(BillingClient.ProductType.SUBS)) {
            return false;
        }

        Optional<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails()
                .stream()
                .filter(details -> {
                    boolean basePlanMatches = details.getBasePlanId().equals(basePlanId);
                    boolean offerIdMatches = (offerId == null && details.getOfferId() == null) ||
                            (offerId != null && details.getOfferId() != null && offerId.equals(details.getOfferId()));
                    return basePlanMatches && offerIdMatches;
                })
                .findFirst();

        if (!subscriptionOfferDetails.isPresent()) {
            return false;
        }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(subscriptionOfferDetails.get().getOfferToken())
                .build());

        BillingFlowParams.Builder billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .setObfuscatedAccountId(this.obfuscatedAccountId)
                .setIsOfferPersonalized(isPersonalized);

        if (this.obfuscatedProfileId != null) billingFlowParams.setObfuscatedProfileId(this.obfuscatedProfileId);
        if (this.obfuscatedAccountId != null) billingFlowParams.setObfuscatedAccountId(this.obfuscatedAccountId);
        if (subscriptionUpdateParams != null) billingFlowParams.setSubscriptionUpdateParams(subscriptionUpdateParams);

        this.googlePlayBilling.getBillingClient().launchBillingFlow(this.godot.getActivity(), billingFlowParams.build());

        return true;
    }

    public void consumePurchase(String purchaseToken) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        ConsumeResponseListener consumeResponseListener = (billingResult, token) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                GooglePlayBillingPlugin.getInstance().emitSignal("consumed", token);
            } else {
                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "consumed",
                        token,
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        this.googlePlayBilling.getBillingClient().consumeAsync(consumeParams, consumeResponseListener);
    }

    public void acknowledgePurchase(@NonNull String purchaseToken) {
        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                GooglePlayBillingPlugin.getInstance().emitSignal("acknowledged", purchaseToken);
            } else {
                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "acknowledgement_error",
                        purchaseToken,
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        this.googlePlayBilling.getBillingClient().acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    public void setObfuscatedAccountId(String obfuscatedAccountId) {
        this.obfuscatedAccountId = obfuscatedAccountId;
    }

    public void setObfuscatedProfileId(String obfuscatedProfileId) {
        this.obfuscatedProfileId = obfuscatedProfileId;
    }
}
