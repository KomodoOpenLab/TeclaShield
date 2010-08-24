package ca.idi.tekla.ime;

import ca.idi.tekla.R;
import ca.idi.tekla.TeklaIMESettings;
import ca.idi.tekla.sep.SwitchEventProvider;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class TeklaIMEHelper {

	private static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	
	private KeyboardView mKeyboardView;
    private NotificationManager mNotificationManager;
    private Context context;


	public TeklaIMEHelper (Context context) {
		this.context = context;
		mNotificationManager = (NotificationManager)
			this.context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private SharedPreferences retrievePreferences() {
		// Get the xml/preferences.xml preferences
		return PreferenceManager
		                .getDefaultSharedPreferences(this.context);
	}
	
	public String retrieveShieldMac() {
		SharedPreferences mSharedPreferences = retrievePreferences();
		return mSharedPreferences.getString(TeklaIMESettings.PREF_SHIELD_MAC, "");
	}
	
	public Boolean retrievePersistentKeyboard() {
		SharedPreferences mSharedPreferences = retrievePreferences();
		Boolean mPersistentKeyboard = mSharedPreferences.getBoolean(TeklaIMESettings.PREF_PERSISTENT_KEYBOARD, false); 
		return  mPersistentKeyboard;
	}
	
	public Boolean retrieveConnectShield() {
		SharedPreferences mSharedPreferences = retrievePreferences();
		return mSharedPreferences.getBoolean(TeklaIMESettings.PREF_CONNECT_SHIELD, false);
	}
	
	/**
     * Helper to determine if a given character code is alphabetic.
     */
    public boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
  
	/**
	* Helper method to perform UI drawing operations
	* using a handler.
	*/
	public void redrawSoftKeyboard (KeyboardView keyboardView) {
		mKeyboardView = keyboardView;
		// Should't mess with GUI from within a thread,
		// and threads call this method, so we'll use a
		// handler to take care of it.
		Message msg = Message.obtain();
		msg.what = TeklaIMEHelper.REDRAW_KEYBOARD;
		redrawHandler.sendMessage(msg);  
	}
	
	private Handler redrawHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case TeklaIMEHelper.REDRAW_KEYBOARD:
					mKeyboardView.invalidateAllKeys(); // Redraw keyboard
					break;          
				default:
					super.handleMessage(msg);
					break;          
			}
		}
	};
	 
}
