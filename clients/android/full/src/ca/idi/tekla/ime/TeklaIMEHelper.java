package ca.idi.tekla.ime;

import ca.idi.tekla.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;

public class TeklaIMEHelper {

	public static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	
	private KeyboardView mKeyboardView;
    private NotificationManager mNotificationManager;
    private Context context;


    // Preferences
    public boolean alwaysShowKeyboard;
	public boolean connect2shield;

	public TeklaIMEHelper (Context context) {
		this.context = context;
		mNotificationManager = (NotificationManager)
			this.context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = context.getText(R.string.sep_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.meadl_status, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        //        new Intent(this, Demo.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, context.getText(R.string.sep_label),
                       text, null);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(R.string.sep_started, notification);
    }

	public void cancelNotification() {
		// Cancel the persistent notification.
		mNotificationManager.cancel(R.string.sep_started);
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
