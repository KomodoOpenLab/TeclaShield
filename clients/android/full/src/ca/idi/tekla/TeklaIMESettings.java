package ca.idi.tekla;

import ca.idi.tekla.R;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class TeklaIMESettings extends PreferenceActivity {

	private static final String PERSISTENT_KEYBOARD = "persistent_keyboard";
	private static final String CONNECT_SHIELD = "connect_shield";
	private static final String SHIELD_MAC = "shield_mac";
	
	private CheckBoxPreference mPersistentKeyboard, mConnectShield;
	private EditTextPreference mShieldMac;
	private BluetoothAdapter btAdapter;
	
	@Override
	protected void onCreate(Bundle icicle) {
	    super.onCreate(icicle);

	    addPreferencesFromResource(R.xml.preferences);
	    mPersistentKeyboard = (CheckBoxPreference) findPreference(PERSISTENT_KEYBOARD);
	    mConnectShield = (CheckBoxPreference) findPreference(CONNECT_SHIELD);
	    mShieldMac = (EditTextPreference) findPreference(SHIELD_MAC);
		mShieldMac.setEnabled(false);
	    refreshContent();
	    
        //Intents & Intent Filters
    	registerReceiver(btActionReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    	registerReceiver(btActionReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
	}

	@Override
	public boolean  onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference) {
		Boolean handled = true;
		
		refreshContent();
		if (preference.getKey().equals(CONNECT_SHIELD)) {
			btAdapter = BluetoothAdapter.getDefaultAdapter();
			if (btAdapter == null) {
			    // Device does not support Bluetooth
			} else {
				if (!btAdapter.isEnabled()) {
					// Bluetooth not enabled
				} else {
					// Bluetooth supported and enabled
					if (mConnectShield.isChecked()) {
						btAdapter.cancelDiscovery();
						btAdapter.startDiscovery();
						mConnectShield.setSummary("Looking for a Tekla shield...");
					} else {
						btAdapter.cancelDiscovery();
					}
				}
			}
		}
		return handled;
	}

	// Bluetooth intents will be processed here
	private BroadcastReceiver btActionReceiver = new BroadcastReceiver() {
		
		Boolean shieldFound = false;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if (dev.getName().startsWith("FireFly")) {
					// Got it!
					mShieldMac.setText(dev.getAddress());
					showToast("Found " + dev.getName());
					shieldFound = true;
				}
			}
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (!shieldFound) {
					mConnectShield.setChecked(false);
					showToast("No Tekla shields in range");
				}
				refreshContent();
			}
		}
	};
	
	private void refreshContent() {
		if (mPersistentKeyboard.isChecked())
			mPersistentKeyboard.setSummary(R.string.persistent_checked_summary);
		else
			mPersistentKeyboard.setSummary(R.string.persistent_unchecked_summary);
		if (mConnectShield.isChecked())
			mConnectShield.setSummary(R.string.shield_checked_summary);
		else
			mConnectShield.setSummary(R.string.shield_unchecked_summary);
		if (mShieldMac.getText().equals(""))
			mShieldMac.setSummary("No shield has been paired");
		else {
			String summary = "Device address: " + mShieldMac.getText();
			mShieldMac.setSummary(summary);
		}
	}
	
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void showToast(int resid) {
		Toast.makeText(this, resid, Toast.LENGTH_SHORT).show();
	}

}
