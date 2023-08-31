/*************************************************************************/
/*  GooglePlayBillingUtils.java                                                    */
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

package org.godotengine.godot.plugin.googleplaybilling.utils;

import org.godotengine.godot.Dictionary;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.List;

public class GooglePlayBillingUtils {
	public static Dictionary convertPurchaseToDictionary(Purchase purchase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("original_json", purchase.getOriginalJson());
		dictionary.put("order_id", purchase.getOrderId());
		dictionary.put("package_name", purchase.getPackageName());
		dictionary.put("purchase_state", purchase.getPurchaseState());
		dictionary.put("purchase_time", purchase.getPurchaseTime());
		dictionary.put("purchase_token", purchase.getPurchaseToken());
		dictionary.put("quantity", purchase.getQuantity());
		dictionary.put("signature", purchase.getSignature());
		dictionary.put("products", purchase.getProducts().toArray());
		dictionary.put("is_acknowledged", purchase.isAcknowledged());
		dictionary.put("is_auto_renewing", purchase.isAutoRenewing());

		if (purchase.getAccountIdentifiers() != null) {
			if (purchase.getAccountIdentifiers().getObfuscatedAccountId() != null)
				dictionary.put("account_id", purchase.getAccountIdentifiers().getObfuscatedAccountId());
			if (purchase.getAccountIdentifiers().getObfuscatedProfileId() != null) {
				dictionary.put("profile_id", purchase.getAccountIdentifiers().getObfuscatedProfileId());
			}
		}

		return dictionary;
	}

	public static Dictionary convertSubscriptionOfferDetailsToDictionary(ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("offer_id", subscriptionOfferDetails.getOfferId());
		dictionary.put("base_plan_id", subscriptionOfferDetails.getBasePlanId());
		dictionary.put("offer_tags", subscriptionOfferDetails.getOfferTags().toArray());

		List<Dictionary> pricingPhases = new ArrayList<>();
		subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().forEach(pricingPhase -> {
			Dictionary pricingPhaseDictionary = new Dictionary();
			pricingPhaseDictionary.put("billing_cycle_count", pricingPhase.getBillingCycleCount());
			pricingPhaseDictionary.put("billing_period", pricingPhase.getBillingPeriod());
			pricingPhaseDictionary.put("formatted_price", pricingPhase.getFormattedPrice());
			pricingPhaseDictionary.put("price_amount_micros", pricingPhase.getPriceAmountMicros());
			pricingPhaseDictionary.put("price_currency_code", pricingPhase.getPriceCurrencyCode());
			pricingPhaseDictionary.put("recurrence_mode", pricingPhase.getRecurrenceMode());

			pricingPhases.add(pricingPhaseDictionary);
		});

		dictionary.put("pricing_phases", pricingPhases.toArray());

		return dictionary;
	}

	public static Dictionary convertProductDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("product_id", details.getProductId());
		dictionary.put("name", details.getName()	);
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("type", details.getProductType());

		if (details.getProductType().equals(BillingClient.ProductType.INAPP)
				&& details.getOneTimePurchaseOfferDetails() != null) {
			ProductDetails.OneTimePurchaseOfferDetails purchaseOfferDetails = details.getOneTimePurchaseOfferDetails();
			dictionary.put("price", purchaseOfferDetails.getFormattedPrice());
			dictionary.put("price_currency_code", purchaseOfferDetails.getPriceCurrencyCode());
			dictionary.put("price_amount_micros", purchaseOfferDetails.getPriceAmountMicros());
		} else if (details.getSubscriptionOfferDetails() != null) {
			dictionary.put("subscription_offer_details", convertSubscriptionOfferDetailsListToDictionaryObjectArray(details.getSubscriptionOfferDetails()));
		}

		return dictionary;
	}

	public static Object[] convertSubscriptionOfferDetailsListToDictionaryObjectArray(List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetailsList) {
		List<Dictionary> subscriptionOfferDetailsDictionary = new ArrayList<>();

		subscriptionOfferDetailsList.forEach(subscriptionOfferDetails ->
				subscriptionOfferDetailsDictionary.add(convertSubscriptionOfferDetailsToDictionary(subscriptionOfferDetails)));

		return subscriptionOfferDetailsDictionary.toArray();
	}

	public static Object[] convertPurchaseListToDictionaryObjectArray(List<Purchase> purchases) {
		List<Dictionary> purchaseDictionaries = new ArrayList<>();

		purchases.forEach(purchase
				-> purchaseDictionaries.add(convertPurchaseToDictionary(purchase)));

		return purchaseDictionaries.toArray();
	}

	public static Object[] convertProductDetailsListToDictionaryObjectArray(List<ProductDetails> productDetailsList) {
		List<Dictionary> productDetailsDictionary = new ArrayList<>();

		productDetailsList.forEach(productDetails
				-> productDetailsDictionary.add(convertProductDetailsToDictionary(productDetails)));

		return productDetailsDictionary.toArray();
	}
}
