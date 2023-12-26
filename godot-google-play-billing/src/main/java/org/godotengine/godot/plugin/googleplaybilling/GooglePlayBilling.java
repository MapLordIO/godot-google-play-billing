package org.godotengine.godot.plugin.googleplaybilling;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.googleplaybilling.helpers.BillingClientHelper;
import org.godotengine.godot.plugin.googleplaybilling.helpers.PurchasesHelper;
import org.godotengine.godot.plugin.googleplaybilling.utils.GooglePlayBillingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GooglePlayBilling {
    public static final int MAX_RETRIES_COUNT = 3;
    private final BillingClient billingClient;
    private final BillingClientHelper billingClientHelper;
    private final PurchasesHelper purchasesHelper;
    private final GooglePlayBillingPlugin googlePlayBillingPlugin;
    private final Godot godot;
    private final HashMap<String, ProductDetails> loadedProductDetails = new HashMap<>();

    private boolean autoRetryEnable;

    public GooglePlayBilling(final GooglePlayBillingPlugin googlePlayBillingPlugin,
                             final Godot godot) {

        this.googlePlayBillingPlugin = googlePlayBillingPlugin;
        this.godot = godot;

        this.billingClientHelper = new BillingClientHelper(this, this.godot);
        this.purchasesHelper = new PurchasesHelper(this, this.godot);

        Objects.requireNonNull(this.godot.getActivity().getApplicationContext());

        this.billingClient = BillingClient.newBuilder(this.godot.getActivity().getApplicationContext())
                .enablePendingPurchases()
                .setListener(this.purchasesHelper)
                .build();
    }

    public void queryProductDetails(List<QueryProductDetailsParams.Product> products) {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(products)
                        .build();

        ProductDetailsResponseListener productDetailsResponseListener = (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach(productDetails -> this.loadedProductDetails.put(productDetails.getProductId(), productDetails));

               GooglePlayBillingPlugin.getInstance().emitPluginSignal(
                        "queried_products",
                       (Object) GooglePlayBillingUtils.convertProductDetailsListToDictionaryObjectArray(productDetailsList)
                );
            } else {
                GooglePlayBillingPlugin.getInstance().emitPluginSignal(
                        "queried_products_error",
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        this.billingClient.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener);
    }

    public void queryPurchases(String productType) {
        QueryPurchasesParams queryPurchasesParams;

        PurchasesResponseListener purchasesQueryListener = (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                GooglePlayBillingPlugin.getInstance().emitPluginSignal(
                        "queried_purchases",
                        productType,
                        (Object) GooglePlayBillingUtils.convertPurchaseListToDictionaryObjectArray(list)
                );
            } else {
                GooglePlayBillingPlugin.getInstance().emitPluginSignal(
                        "queried_purchases_error",
                        productType,
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build();
        this.billingClient.queryPurchasesAsync(queryPurchasesParams, purchasesQueryListener);
    }

    public BillingClientHelper getBillingClientHelper() {
        return billingClientHelper;
    }

    public PurchasesHelper getPurchasesHelper() {
        return purchasesHelper;
    }

    public ProductDetails getLoadedProductDetails(String productId) {
        return loadedProductDetails.get(productId);
    }

    public void setAutoRetryEnable(boolean autoRetryEnable) {
        this.autoRetryEnable = autoRetryEnable;
    }

    public boolean isAutoRetryEnabled() {
        return this.autoRetryEnable;
    }

    public boolean isEnabled() {
        return this.billingClientHelper.isConnected();
    }

    public BillingClient getBillingClient() {
        return billingClient;
    }
}
