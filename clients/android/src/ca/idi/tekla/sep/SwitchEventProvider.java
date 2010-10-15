package ca.idi.tekla.sep;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import ca.idi.tekla.R;
import ca.idi.tekla.TeklaIMESettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class SwitchEventProvider extends Service implements Runnable {

	//Constants
	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_MAC}
	 * must be provided to start the service.
	*/
    public static final String INTENT_START_SERVICE = "ca.idi.tekla.sep.SEPService";
	/**
	 * Intent string used to broadcast switch events. The
	 * type of event will be packaged as an extra using
	 * the {@link #EXTRA_SWITCH_EVENT} string.
	*/
    public static final String ACTION_SEP_SERVICE_STARTED = "ca.idi.tekla.sep.action.SEP_SERVICE_STARTED";
    public static final String ACTION_SWITCH_EVENT_RECEIVED = "ca.idi.tekla.sep.action.SWITCH_EVENT_RECEIVED";
    public static final String EXTRA_SWITCH_EVENT = "ca.idi.tekla.sep.extra.SWITCH_EVENT";
	/**
	 * Refers to the MAC address sent with {@link #INTENT_START_SERVICE}
	 * to connect to the Tekla shield.
	*/
    public static final String EXTRA_SHIELD_MAC = "ca.idi.tekla.sep.extra.SHIELD_MAC";
    public static final int SWITCH_FWD = 10;
    public static final int SWITCH_BACK = 20;
    public static final int SWITCH_RIGHT = 40;
    public static final int SWITCH_LEFT = 80;
    public static final int SWITCH_RELEASE = 160;

	private BluetoothSocket mBluetoothSocket;
    private OutputStream outStream;
    private String mShieldAddress;

    private NotificationManager mNotificationManager;
    private Boolean mAlreadyBroadcasting;
    
    private Intent mSwitchEventIntent;
    private Intent mServiceStartedIntent;
    
	// hard-code hardware address and UUID here
	// private String mShieldAddress = "00:06:66:02:CB:75"; // BlueSMiRF 1
	// private String server_address = "00:06:66:04:13:01"; // BlueSMiRF 2
	// private String server_address = "00:16:41:89:C8:0A"; // jsilva-laptop
	// Using "well-known" SPP UUID as specified at:
	// http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	SwitchEventProvider getService() {
            return SwitchEventProvider.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
	
    @Override
	public void onCreate() {
        //Intents & Intent Filters
    	mSwitchEventIntent = new Intent(ACTION_SWITCH_EVENT_RECEIVED);
    	mServiceStartedIntent = new Intent(ACTION_SEP_SERVICE_STARTED);
    	mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	mAlreadyBroadcasting = false;
	}
	
	@Override
	public void onDestroy() {
		/* Call this from the main Activity to shutdown the connection */
		try {
			// Close socket
			if (mBluetoothSocket != null)
				mBluetoothSocket.close();
			cancelNotification();
    		mAlreadyBroadcasting = true;
		} catch (IOException e) {
			e.printStackTrace();
			showToast(e.getMessage());
		}
	}

    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!mAlreadyBroadcasting) {
			Bundle extras = intent.getExtras();
			mShieldAddress = extras.getString(EXTRA_SHIELD_MAC);
			if (BluetoothAdapter.checkBluetoothAddress(mShieldAddress)){
				// Assuming bluetooth is supported and enabled
	        	if (connect2Shield(mShieldAddress)) {
	        		startBroadcasting();
					showNotification();
	        		mAlreadyBroadcasting = true;
	        	} else {
					//TODO: Tekla - Add string to resources
	        		showToast("Failed to connect Tekla shield");
	        		stopSelf();
	                return Service.START_NOT_STICKY;
	        	}
		        
			} else {
				//TODO: Tekla - Add string to resources
        		showToast("Invalid MAC Address");
	    		stopSelf();
	            return Service.START_NOT_STICKY;
			}
		}
        return Service.START_STICKY;
	}
	
	@Override
	public void run() {
		Looper.prepare();

		InputStream inStream;

        try {
			inStream = mBluetoothSocket.getInputStream();
			outStream = mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		sendBroadcast(mServiceStartedIntent);
		while(true) {
			try {
				inStream = mBluetoothSocket.getInputStream();
				int b = inStream.read();
				mSwitchEventIntent.removeExtra(EXTRA_SWITCH_EVENT);
				switch (b) {
					case 0x07:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_FWD);
						sendBroadcast(mSwitchEventIntent);
						break;
					case 0x0B:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_BACK);
						sendBroadcast(mSwitchEventIntent);
						break;
					case 0x0E:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_RIGHT);
						sendBroadcast(mSwitchEventIntent);
						break;
					case 0x0D:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_LEFT);
						sendBroadcast(mSwitchEventIntent);
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}			
		}
	}
	
	/**
	* Connects to bluetooth server.
	*/
	private boolean connect2Shield(String shieldAddress) {
		Boolean success = false;
        try {
        	BluetoothDevice teklaShield = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(shieldAddress);
            mBluetoothSocket = teklaShield.createRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();
            success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	* Executes the run() thread.
	*/
	private void startBroadcasting() {
		Thread thread = new Thread(this);
        thread.start();
	}
	
    public void write(byte b) {
		try {
			outStream.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.sep_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.tekla_status, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TeklaIMESettings.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.sep_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(R.string.sep_started, notification);
    }

	private void cancelNotification() {
		// Cancel the persistent notification.
		mNotificationManager.cancel(R.string.sep_started);
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
