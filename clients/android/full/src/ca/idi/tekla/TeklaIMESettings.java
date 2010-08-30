package ca.idi.tekla;

import ca.idi.tekla.R;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

public class TeklaIMESettings extends PreferenceActivity {

	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
	public static final String PREF_CONNECT_SHIELD = "connect_shield";
	public static final String PREF_SHIELD_MAC = "shield_mac";
	public static final String PREF_SHIELD_NAME = "shield_name";
	
	private CheckBoxPreference mPersistentKeyboard, mConnectShield;
	private Preference mShieldDetails;
	private BluetoothAdapter mBTAdapter;
	private Boolean mShieldFound;
	
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle icicle) {
	    super.onCreate(icicle);

	    addPreferencesFromResource(R.xml.preferences);
	    mPersistentKeyboard = (CheckBoxPreference) findPreference(PREF_PERSISTENT_KEYBOARD);
	    mConnectShield = (CheckBoxPreference) findPreference(PREF_CONNECT_SHIELD);
        mShieldDetails = (Preference) findPreference(PREF_SHIELD_MAC);
	    
        //Intents & Intent Filters
    	registerReceiver(mBTActionReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    	registerReceiver(mBTActionReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

		mShieldFound = false;
    	refreshContent();
	}

	@Override
	public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) {

		if (preference.getKey().equals(PREF_CONNECT_SHIELD)) {
			discoverShield();
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private void discoverShield() {
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null) {
		    // Device does not support Bluetooth
		} else {
			if (!mBTAdapter.isEnabled()) {
				// Bluetooth not enabled
			} else {
				// Bluetooth supported and enabled
				mBTAdapter.cancelDiscovery();
				if (mConnectShield.isChecked()) {
					mBTAdapter.startDiscovery();
					mShieldDetails.setTitle("Looking for a Tekla shield...");
			        mProgress = ProgressDialog.show(this, "Please wait...", 
		                      "Searching for Tekla shields...", true, true);
				}
			}
		}
	}
	
	// Bluetooth intents will be processed here
	private BroadcastReceiver mBTActionReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) && !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (dev.getName().startsWith("FireFly"))) {
					// Got it!
					saveShieldMac(dev.getAddress());
					saveShieldName(dev.getName());
					mPersistentKeyboard.setChecked(true);
					mShieldFound = true;
				}
			}
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (!mShieldFound) {
					mConnectShield.setChecked(false);
					showToast("No Tekla shields in range");
				}
				refreshContent();
				mProgress.dismiss();
			}
		}
	};
	
	private void refreshContent() {
		if (BluetoothAdapter.checkBluetoothAddress(retrieveShieldMac())) {
			mShieldDetails.setTitle(retrieveShieldName());
			String summary = "Device address: " + retrieveShieldMac();
			mShieldDetails.setSummary(summary);
		} else {
			mShieldDetails.setTitle("No shield found");
		}
		if (mConnectShield.isChecked() && mShieldFound) {
			mShieldDetails.setEnabled(true);
		} else {
			mShieldDetails.setEnabled(false);
		}
	}

	private void saveShieldMac(String mac) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_SHIELD_MAC, mac);
		editor.commit();
	}

	private void saveShieldName(String name) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_SHIELD_NAME, name);
		editor.commit();
	}

	private String retrieveShieldMac() {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		String mac = prefs.getString(PREF_SHIELD_MAC, "");
		if (!BluetoothAdapter.checkBluetoothAddress(mac))
			mac = "";
		return mac;
	}

	private String retrieveShieldName() {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		String name = prefs.getString(PREF_SHIELD_NAME, "");
		return name;
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
