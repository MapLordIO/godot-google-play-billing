/*************************************************************************/
/*  GodotGooglePlayBilling.java                                                    */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2020 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2020 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/

package org.godotengine.godot.plugin.googleplaybilling;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class GooglePlayBillingPlugin extends GodotPlugin {
	private static GooglePlayBillingPlugin instance;
	private final GooglePlayBilling googlePlayBilling;

	public GooglePlayBillingPlugin(Godot godot) {
		super(godot);

		instance = this;

		this.emitPluginSignal("connected");

		this.googlePlayBilling = new GooglePlayBilling(this, this.getGodot());
	}

	@Override
	public void onMainResume() {
		if (this.googlePlayBilling.isEnabled())
			this.emitPluginSignal("resume");
	}

	@NonNull
	@Override
	public String getPluginName() {
		return "GooglePlayBilling";
	}

	@NonNull
	@Override
	public Set<SignalInfo> getPluginSignals() {
		Set<SignalInfo> signals = new ArraySet<>();

		signals.add(new SignalInfo("connected"));
		signals.add(new SignalInfo("disconnected", Boolean.class));
		signals.add(new SignalInfo("retrying_connection", Integer.class, String.class));
		signals.add(new SignalInfo("failed_connecting", Integer.class, String.class));

		signals.add(new SignalInfo("queried_purchases", String.class, Object[].class));
		signals.add(new SignalInfo("queried_purchases_error", String.class, Integer.class, String.class));

		signals.add(new SignalInfo("purchases_updated", Object[].class));
		signals.add(new SignalInfo("purchases_update_error", Integer.class, String.class));

		signals.add(new SignalInfo("consumed", String.class));
		signals.add(new SignalInfo("consumption_error", String.class, Integer.class, String.class));

		signals.add(new SignalInfo("acknowledged", String.class));
		signals.add(new SignalInfo("acknowledgement_error", String.class, Integer.class, String.class));

		signals.add(new SignalInfo("queried_products", Object[].class));
		signals.add(new SignalInfo("queried_products_error", Integer.class, String.class));

		signals.add(new SignalInfo("resume"));

		return signals;
	}

	@UsedByGodot
	public void start_connection() {
		this.googlePlayBilling.getBillingClientHelper().startConnection();
	}

	@UsedByGodot
	public boolean purchase_inapp(
			@NonNull String product_id,
			@NonNull boolean personalized_offer) {
		return this.googlePlayBilling.getPurchasesHelper().purchaseInApp(product_id, personalized_offer);
	}

	@UsedByGodot
	public boolean purchase_subscription(
			@NonNull String product_id,
			@NonNull String base_plan_id,
			@NonNull boolean personalized_offer) {
		return this.googlePlayBilling.getPurchasesHelper().purchaseSubscription(product_id, base_plan_id, null, null, personalized_offer);
	}

	@UsedByGodot
	public boolean purchase_subscription_with_offer(
			@NonNull String product_id,
			@NonNull boolean personalized_offer,
			@NonNull String base_plan_id,
			@NonNull String offer_id) {
		return this.googlePlayBilling.getPurchasesHelper().purchaseSubscription(product_id, base_plan_id, offer_id, null, personalized_offer);
	}

	@UsedByGodot
	public boolean purchase_subscription_with_update_params(
			@NonNull String product_id,
			@NonNull boolean personalized_offer,
			@NonNull String base_plan_id,
			@NonNull int subscription_replacement_mode,
			@Nullable String old_purchase_token,
			@Nullable String original_external_transaction_id) {
		return this.internalSubscriptionPurchase(product_id, personalized_offer, base_plan_id, null,
				subscription_replacement_mode, old_purchase_token, original_external_transaction_id);
	}

	@UsedByGodot
	public boolean purchase_subscription_with_offer_and_update_params(
			@NonNull String product_id,
			@NonNull boolean personalized_offer,
			@NonNull String base_plan_id,
			@NonNull String offer_id,
			@NonNull int subscription_replacement_mode,
			@Nullable String old_purchase_token,
			@Nullable String original_external_transaction_id) {
		return this.internalSubscriptionPurchase(product_id, personalized_offer, base_plan_id, offer_id,
				subscription_replacement_mode, old_purchase_token, original_external_transaction_id);
	}

	@UsedByGodot
	public void acknowledge_purchase(@NonNull String purchase_token) {
		this.googlePlayBilling.getPurchasesHelper().acknowledgePurchase(purchase_token);
	}

	@UsedByGodot
	public void consume_purchase(@NonNull String purchase_token) {
		this.googlePlayBilling.getPurchasesHelper().consumePurchase(purchase_token);
	}

	@UsedByGodot
	public boolean query_product_details(@NonNull String[] products, @NonNull String product_type) {
		if (Stream.of("subs", "inapp")
				.noneMatch(validType -> validType.equalsIgnoreCase(product_type))) return false;
		List<QueryProductDetailsParams.Product> queryProductDetailsList = new ArrayList<>();

		System.out.println(Arrays.toString(products));

		Arrays.stream(products).forEach(product_id -> {
			QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
					.setProductId(product_id)
					.setProductType(product_type.toLowerCase())
					.build();
			queryProductDetailsList.add(product);
		});

		this.googlePlayBilling.queryProductDetails(queryProductDetailsList);

		return true;
	}

	@UsedByGodot
	public boolean query_purchases(@NonNull String product_type) {
		if (Stream.of("subs", "inapp")
				.noneMatch(validType -> validType.equalsIgnoreCase(product_type))) return false;

		this.googlePlayBilling.queryPurchases(product_type);

		return true;
	}

	@UsedByGodot
	public void set_obfuscated_account_id(@NonNull String obfuscated_account_id) {
		this.googlePlayBilling.getPurchasesHelper().setObfuscatedAccountId(obfuscated_account_id);
	}

	@UsedByGodot
	public void set_obfuscated_profile_id(@NonNull String obfuscated_profile_id) {
		this.googlePlayBilling.getPurchasesHelper().setObfuscatedProfileId(obfuscated_profile_id);
	}

	@UsedByGodot
	public void set_auto_retry_enabled(@NonNull boolean auto_retry_enabled) {
		this.googlePlayBilling.setAutoRetryEnable(auto_retry_enabled);
	}

	private boolean internalSubscriptionPurchase(
			String product_id,
			boolean personalized_offer,
			String base_plan_id,
			String offer_id,
			int subscription_replacement_mode,
			String old_purchase_token,
			String original_external_transaction_id) {
		BillingFlowParams.SubscriptionUpdateParams.Builder subscriptionUpdateParamsBuilder =
				BillingFlowParams.SubscriptionUpdateParams.newBuilder()
						.setSubscriptionReplacementMode(subscription_replacement_mode);

		if (old_purchase_token != null)
			subscriptionUpdateParamsBuilder.setOldPurchaseToken(old_purchase_token);
		if (original_external_transaction_id != null)
			subscriptionUpdateParamsBuilder.setOriginalExternalTransactionId(original_external_transaction_id);

		BillingFlowParams.SubscriptionUpdateParams subscriptionUpdateParams = subscriptionUpdateParamsBuilder.build();

		return this.googlePlayBilling.getPurchasesHelper().purchaseSubscription(product_id, base_plan_id, offer_id, subscriptionUpdateParams, personalized_offer);
	}

	public void emitPluginSignal(String signal, Object... args) {
		this.emitSignal(signal, args);
	}

	public static GooglePlayBillingPlugin getInstance() {
		return instance;
	}
}
