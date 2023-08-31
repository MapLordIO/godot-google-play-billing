package org.godotengine.godot.plugin.googleplaybilling;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;

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

        Objects.requireNonNull(this.godot.getContext());

        this.billingClient = BillingClient.newBuilder(this.godot.getContext())
                .enablePendingPurchases()
                .setListener(this.purchasesHelper)
                .build();

        this.billingClientHelper.startConnection();
    }

    public void queryProductDetails(List<QueryProductDetailsParams.Product> products) {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(products)
                        .build();

        ProductDetailsResponseListener productDetailsResponseListener = (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach(productDetails -> this.loadedProductDetails.put(productDetails.getProductId(), productDetails));

                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "queried_products",
                        GooglePlayBillingUtils.convertProductDetailsListToDictionaryObjectArray(productDetailsList)
                );
            } else {
                GooglePlayBillingPlugin.getInstance().emitSignal(
                        "query_products_error",
                        billingResult.getResponseCode(),
                        billingResult.getDebugMessage()
                );
            }
        };

        this.billingClient.queryProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener);
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
