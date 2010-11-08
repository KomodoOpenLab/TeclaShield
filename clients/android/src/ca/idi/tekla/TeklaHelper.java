package ca.idi.tekla;

import ca.idi.tekla.ime.TeklaIME;
import ca.idi.tekla.sep.SwitchEventProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;

public class TeklaHelper {

	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
    public static final String PREF_SHIELD_CONNECT = "shield_connect";
	public static final String PREF_SHIELD_ADDRESS = "shield_address";


    private static TeklaHelper th = null;
	
	public static TeklaHelper getInstance() {
		if(th == null) {
			return new TeklaHelper();
		}
		return th;
	}
	
	public boolean getPersistentKeyboard(Context context) {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PREF_PERSISTENT_KEYBOARD, false);
	}
	
	public boolean getShieldConnect(Context context) {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PREF_SHIELD_CONNECT, false);
	}

	public String getShieldAddress(Context context) {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(context);
		String mac = prefs.getString(PREF_SHIELD_ADDRESS, "");
		if (!BluetoothAdapter.checkBluetoothAddress(mac))
			mac = "";
		return mac;
	}
	
	public void setShieldAddress(Context context, String shieldAddress) {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_SHIELD_ADDRESS, shieldAddress);
		editor.commit();
	}

    public void forceShowTeklaIME(Context context) {
    	makeIMEActive(context).sendAppPrivateCommand(null, TeklaIME.ACTION_SHOW_IME, null);
    }
    
    public void forceHideTeklaIME(Context context) {
    	makeIMEActive(context).sendAppPrivateCommand(null, TeklaIME.ACTION_HIDE_IME, null);
    }
    
    public InputMethodManager makeIMEActive(Context context) {
    	InputMethodManager imeManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	// Trying to force initialization of IME so it can respond
    	// to the commands below (if it is Tekla)
    	while (!imeManager.isActive()) {
	    	imeManager.toggleSoftInput(0, 0);
	    	imeManager.toggleSoftInput(0, 0);
    	}
    	return imeManager;
    }
    
	public boolean startSwitchEventProvider(Context context, String shieldAddress) {
		Intent sepIntent = new Intent(SwitchEventProvider.INTENT_START_SERVICE);
		sepIntent.putExtra(SwitchEventProvider.EXTRA_SHIELD_ADDRESS, shieldAddress);
		return context.startService(sepIntent) == null? false:true;
	}

}
