/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ca.idi.tekla;

//FIXME: Tekla - Solve backup elsewhere
//import android.backup.BackupManager;
import ca.idi.tekla.R;
import ca.idi.tekla.sep.SwitchEventProvider;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.AutoText;
import android.widget.Toast;

public class TeklaIMESettings extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String QUICK_FIXES_KEY = "quick_fixes";
    private static final String SHOW_SUGGESTIONS_KEY = "show_suggestions";
    private static final String PREDICTION_SETTINGS_KEY = "prediction_settings";

    private CheckBoxPreference mQuickFixes;
    private CheckBoxPreference mShowSuggestions;

    //Tekla keys & variables
	public static final String CONNECT_SHIELD_KEY = "connect_shield";
	public static final String SHIELD_MAC_KEY = "shield_mac";
	public static final String SHIELD_NAME_KEY = "shield_name";
    
    private CheckBoxPreference mConnectShield;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mShieldFound;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

		// Use the following line to debug IME service.
		android.os.Debug.waitForDebugger();

        addPreferencesFromResource(R.xml.prefs);
        mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
        mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);

        // Check bluetooth state to determine if connect_shield preference
        // should be enabled
        mConnectShield = (CheckBoxPreference) findPreference(CONNECT_SHIELD_KEY);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			//TODO: Tekla - Add string to resources
			mConnectShield.setSummary("Device does not support Bluetooth");
		    mConnectShield.setEnabled(false);
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				//TODO: Tekla - Add string to resources
				mConnectShield.setSummary("Bluetooth is not enabled");
			    mConnectShield.setEnabled(false);
			} else {
				//TODO: Tekla - Add string to resources
				mConnectShield.setSummary("Check to connect to a nearby Tekla shield");
			}
		}
        //Tekla Intents & Intent Filters
    	registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    	registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    	registerReceiver(mBroadcastReceiver, new IntentFilter(SwitchEventProvider.ACTION_SEP_SERVICE_STARTED));
    	
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int autoTextSize = AutoText.getSize(getListView());
        if (autoTextSize < 1) {
            ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
                .removePreference(mQuickFixes);
        } else {
            mShowSuggestions.setDependency(QUICK_FIXES_KEY);
        }
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        super.onDestroy();
    }

	@Override
	public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) {

		if (preference.getKey().equals(CONNECT_SHIELD_KEY) &&
				mConnectShield.isChecked()) {
			mConnectShield.setChecked(false);
			discoverShield();
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private void discoverShield() {
		mShieldFound = false;
		if (mBluetoothAdapter.isDiscovering())
			mBluetoothAdapter.cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
		//TODO: Tekla - Add strings to resources
	    mProgressDialog = ProgressDialog.show(this, "Please wait...", 
	              "Searching for Tekla shields...", true, true);
	}
	
	// Bluetooth intents will be processed here
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) && !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (dev.getName().startsWith("FireFly"))) {
					// Got it!
					mShieldFound = true;
					if (mBluetoothAdapter.isDiscovering())
						mBluetoothAdapter.cancelDiscovery();
					String shieldAddress = dev.getAddress(); 
					String shieldName = dev.getName(); 
					saveShieldMac(shieldAddress);
					saveShieldName(shieldName);
					//TODO: Tekla - Add string to resources
					mProgressDialog.setMessage("Connecting to Tekla shield "
							+ shieldName + ": " + shieldAddress);
					if (!startSwitchEventProvider(shieldAddress)) {
						mProgressDialog.dismiss();
						//TODO: Tekla - Add string to resources
						showToast("Could not connect to Tekla shield");
					}
				}
			}

			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (!mShieldFound) {
					//TODO: Tekla - Add string to resources
					showToast("No Tekla shields in range");
					mProgressDialog.dismiss();
				}
			}
			
			if (intent.getAction().equals(SwitchEventProvider.ACTION_SEP_SERVICE_STARTED)) {
				mConnectShield.setChecked(true);
				mProgressDialog.dismiss();
			}

		}
	};
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
    	//showToast("debug: onSharedPreferenceChange called with key: " + key);
    	//FIXME: Tekla - Solve backup elsewhere
    	//(new BackupManager(this)).dataChanged();
    }

	private boolean startSwitchEventProvider(String shieldAddress) {
		Intent sepIntent = new Intent(SwitchEventProvider.INTENT_START_SERVICE);
		sepIntent.putExtra(SwitchEventProvider.EXTRA_SHIELD_MAC, shieldAddress);
		return startService(sepIntent) == null? false:true;
	}

	private void saveShieldMac(String mac) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SHIELD_MAC_KEY, mac);
		editor.commit();
	}

	private void saveShieldName(String name) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SHIELD_NAME_KEY, name);
		editor.commit();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
