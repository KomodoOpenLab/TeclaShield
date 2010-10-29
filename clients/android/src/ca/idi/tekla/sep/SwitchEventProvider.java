package ca.idi.tekla.sep;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.widget.Toast;

public class SwitchEventProvider extends Service implements Runnable {

	//Constants
	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_MAC}
	 * must be provided to start the service.
	*/
    public static final String INTENT_START_SERVICE = "ca.idi.tekla.sep.SEPService";
    public static final String ACTION_SEP_BROADCAST_STARTED = "ca.idi.tekla.sep.action.SEP_BROADCAST_STARTED";
    public static final String ACTION_SEP_BROADCAST_STOPPED = "ca.idi.tekla.sep.action.SEP_BROADCAST_STOPPED";
	/**
	 * Intent string used to broadcast switch events. The
	 * type of event will be packaged as an extra using
	 * the {@link #EXTRA_SWITCH_EVENT} string.
	*/
    public static final String ACTION_SWITCH_EVENT_RECEIVED = "ca.idi.tekla.sep.action.SWITCH_EVENT_RECEIVED";
    public static final String EXTRA_SWITCH_EVENT = "ca.idi.tekla.sep.extra.SWITCH_EVENT";
	/**
	 * Refers to the MAC address sent with {@link #INTENT_START_SERVICE}
	 * to connect to the Tekla shield.
	*/
    public static final String EXTRA_SHIELD_ADDRESS = "ca.idi.tekla.sep.extra.SHIELD_ADDRESS";
    public static final int SWITCH_FWD = 10;
    public static final int SWITCH_BACK = 20;
    public static final int SWITCH_RIGHT = 40;
    public static final int SWITCH_LEFT = 80;
    public static final int SWITCH_RELEASE = 160;

	public static final String SHIELD_ADDRESS_KEY = "shield_address";

	private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutStream;
	private InputStream mInStream;

    private NotificationManager mNotificationManager;
    private Boolean mIsBroadcasting;
    private Thread mBroadcastingThread;
    
    private Intent mBroadcastStartedIntent;
    private Intent mBroadcastStoppedIntent;
    private Intent mSwitchEventIntent;
    
	// hard-code hardware address
	// private String mShieldAddress = "00:06:66:02:CB:75"; // BlueSMiRF 1
	// private String server_address = "00:06:66:04:13:01"; // BlueSMiRF 2
	// private String server_address = "00:16:41:89:C8:0A"; // jsilva-laptop

    // Using "well-known" SPP UUID as specified at:
	// http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
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
		// Use the following line to debug IME service.
		android.os.Debug.waitForDebugger();

    	//Intents & Intent Filters
    	registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    	mSwitchEventIntent = new Intent(ACTION_SWITCH_EVENT_RECEIVED);
    	mBroadcastStartedIntent = new Intent(ACTION_SEP_BROADCAST_STARTED);
    	mBroadcastStoppedIntent = new Intent(ACTION_SEP_BROADCAST_STOPPED);
    	mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mBroadcastingThread = new Thread(this);
    	mIsBroadcasting = false;
	}
	
	@Override
	public void onDestroy() {
		/* Call this from the main Activity to shutdown the connection */
		stopBroadcasting();
	}

    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String mShieldAddress = "";
		
		if (intent.hasExtra(EXTRA_SHIELD_ADDRESS)) {
			mShieldAddress = intent.getExtras().getString(EXTRA_SHIELD_ADDRESS);
		}
			
		if (BluetoothAdapter.checkBluetoothAddress(mShieldAddress)) {
			// MAC is valid
			broadcastFromShield(mShieldAddress);
		} else {
			// MAC is invalid
			if (!mIsBroadcasting) {
				// Not broadcasting yet
				// Try with saved MAC address
				broadcastFromShield(retrieveSavedShieldAddress());
			} //else ignore (if already broadcasting)
		}
		
		if(!mIsBroadcasting) stopSelf();
		return mIsBroadcasting? Service.START_STICKY:Service.START_NOT_STICKY;
	}
	
	@Override
	public void run() {
		Looper.prepare();

		int mByte;
		
		try {
			mInStream = mBluetoothSocket.getInputStream();
			mOutStream = mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			showToast(e.getMessage());
			return;
		}

		sendBroadcast(mBroadcastStartedIntent);
		mIsBroadcasting = true;
		while(mIsBroadcasting) {
			try {
				mByte = mInStream.read();
		    	// Clean up intent
				mSwitchEventIntent.removeExtra(EXTRA_SWITCH_EVENT);
				switch (mByte) {
					case 0x07:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_FWD);
						break;
					case 0x0B:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_BACK);
						break;
					case 0x0E:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_RIGHT);
						break;
					case 0x0D:
						mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, SWITCH_LEFT);
						break;
					default:
						break;
				}
		    	// Poke the user activity timer
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				pm.userActivity(SystemClock.uptimeMillis(), true);
				sendBroadcast(mSwitchEventIntent);
			} catch (IOException e) {
				e.printStackTrace();
				showToast(e.getMessage());
				break;
			}			
		}
	}
	
	// All intents will be processed here
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			Integer state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
			if (state.equals(BluetoothAdapter.STATE_TURNING_OFF))
				stopBroadcasting();
		}
		
	};
	
	private void saveShieldAddress(String shieldAddress) {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SHIELD_ADDRESS_KEY, shieldAddress);
		editor.commit();
	}

	private String retrieveSavedShieldAddress() {

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		String mac = prefs.getString(SHIELD_ADDRESS_KEY, "");
		if (!BluetoothAdapter.checkBluetoothAddress(mac))
			mac = "";
		return mac;
	}

	private void broadcastFromShield(String mShieldAddress) {
    	if (connect2Shield(mShieldAddress)) {
    		startBroadcasting();
    		saveShieldAddress(mShieldAddress);
    	}
	}
	/**
	* Connects to bluetooth server.
	*/
	private boolean connect2Shield(String shieldAddress) {
		Boolean success = false;
		BluetoothDevice teklaShield;
		
    	stopBroadcasting();
    	teklaShield = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(shieldAddress);
    	
    	// First method to create Bluetooth socket
   		Method m = null;
		try {
			m = teklaShield.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			mBluetoothSocket = (BluetoothSocket) m.invoke(teklaShield, 1);
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		success = createSocket();
		
		if (!success) {
	    	// Second method to create Bluetooth socket
	    	try {
	            mBluetoothSocket = teklaShield.createRfcommSocketToServiceRecord(SPP_UUID);
			} catch (IOException e) {
				e.printStackTrace();
				showToast("CreateSocket: " + e.getMessage());
			}
			success = createSocket();
		}
		return success;
	}
	
	private boolean createSocket() {
		boolean success = false;
        try {
			mBluetoothSocket.connect();
            success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
            success = false;
			e.printStackTrace();
			//showToast(e.getMessage());
		}
		return success;
	}
	
	/**
	* Executes the run() thread.
	*/
	private void startBroadcasting() {
		mBroadcastingThread.start();
		showNotification();
	}
	
	private void stopBroadcasting() {
		// Close socket if it exists
		if (mBluetoothSocket != null) {
			try {
				mBluetoothSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showToast(e.getMessage());
			}
		}
		// Stop broadcasting thread
    	mIsBroadcasting = false;
    	while(mBroadcastingThread.isAlive()) {
    		// Wait for the thread to die
    		SystemClock.sleep(1);
    	}
    	if (mBroadcastingThread.getState() == Thread.State.TERMINATED) {
    		mBroadcastingThread = new Thread(this);
    		sendBroadcast(mBroadcastStoppedIntent);
    	}
    	cancelNotification();
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

        // Add sound and type.
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(R.string.sep_started, notification);
    }

	private void cancelNotification() {
		// Cancel the persistent notification.
		mNotificationManager.cancel(R.string.sep_started);
	}

    private void write(byte mByte) {
		try {
			mOutStream.write(mByte);
		} catch (IOException e) {
			e.printStackTrace();
			showToast(e.getMessage());
		}
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
