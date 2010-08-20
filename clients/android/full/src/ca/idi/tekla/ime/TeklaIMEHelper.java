package ca.idi.tekla.ime;

import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;

public class TeklaIMEHelper {

	public static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	
	private KeyboardView mKeyboardView;

	public TeklaIMEHelper () {
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
