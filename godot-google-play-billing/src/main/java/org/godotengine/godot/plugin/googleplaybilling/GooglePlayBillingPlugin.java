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

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.Objects;
import java.util.Set;

public class GooglePlayBillingPlugin extends GodotPlugin {
	private static GooglePlayBillingPlugin instance;
	private final GooglePlayBilling googlePlayBilling;

	public GooglePlayBillingPlugin(Godot godot) {
		super(godot);

		instance = this;

		this.googlePlayBilling = new GooglePlayBilling(this, this.getGodot());
	}

	@Override
	public void onMainResume() {
		if (this.googlePlayBilling.isEnabled()) {
			//
		}
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

		signals.add(new SignalInfo("queried_purchases", Integer.class, Object[].class));
		signals.add(new SignalInfo("query_purchases_error", Integer.class, String.class));

		signals.add(new SignalInfo("purchases_updated", Integer.class, Object[].class));
		signals.add(new SignalInfo("purchases_update_error", Integer.class, String.class));

		signals.add(new SignalInfo("consumed", String.class));
		signals.add(new SignalInfo("consumption_error", String.class, Integer.class, String.class));

		signals.add(new SignalInfo("acknowledged", String.class));
		signals.add(new SignalInfo("acknowledgement_error", String.class, Integer.class, String.class));

		signals.add(new SignalInfo("queried_products", Integer.class, Object[].class));
		signals.add(new SignalInfo("query_products_error", Integer.class, String.class));

		return signals;
	}

	public void emitSignal(String signal, Object... args) {
		this.emitSignal(signal, args);
	}

	public static GooglePlayBillingPlugin getInstance() {
		return instance;
	}
}
