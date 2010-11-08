package ca.idi.tekla.sep;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import ca.idi.tekla.R;
import ca.idi.tekla.TeklaHelper;
import ca.idi.tekla.TeklaIMESettings;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

public class SwitchEventProvider extends Service implements Runnable {

	//Constants
	/**
	 * "Well-known" Serial Port Profile UUID as specified at:
	 * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	*/
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_MAC}
	 * must be provided to start the service.
	*/
    public static final String INTENT_START_SERVICE = "ca.idi.tekla.sep.SEPService";
    public static final String INTENT_STOP_SERVICE = "ca.idi.tekla.sep.SEPService";
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

    private static final long PING_DELAY = 5000;
    private static final int PING_TIMEOUT_COUNTER = 4;
    private boolean mKeepReconnecting;
	private int mPingCounter = 0;
	private Handler mHandler;
    private Thread mReconnectThread;
    
    private KeyguardManager mKeyguardManager;
    private KeyguardLock mKeyguardLock;

    private TeklaHelper mTeklaHelper = 
    	TeklaHelper.getInstance();

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

    	mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    	mKeyguardLock = mKeyguardManager.newKeyguardLock("");

		//Intents & Intent Filters
    	registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    	registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    	mSwitchEventIntent = new Intent(ACTION_SWITCH_EVENT_RECEIVED);
    	mBroadcastStartedIntent = new Intent(ACTION_SEP_BROADCAST_STARTED);
    	mBroadcastStoppedIntent = new Intent(ACTION_SEP_BROADCAST_STOPPED);

    	mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mBroadcastingThread = new Thread(this);
        mHandler = new Handler();
    	mIsBroadcasting = false;
    	mKeepReconnecting = false;
    	
    	if (mTeklaHelper.getPersistentKeyboard(this))
    		mTeklaHelper.forceShowTeklaIME(this);
	}
	
	@Override
	public void onDestroy() {
		stopReconnectThread();
		unregisterReceiver(mReceiver);
		disconnectShield();
		super.onDestroy();
	}

    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String shieldAddress = "";
		
		if (intent.hasExtra(EXTRA_SHIELD_ADDRESS))
			shieldAddress = intent.getExtras().getString(EXTRA_SHIELD_ADDRESS);
			
		if (!BluetoothAdapter.checkBluetoothAddress(shieldAddress))
			// MAC is invalid, try saved address
			shieldAddress = mTeklaHelper.getShieldAddress(this);
		if (!shieldAddress.equals("")) {
			restartReconnectThread();
		}
		
		return mKeepReconnecting? Service.START_STICKY:Service.START_NOT_STICKY;
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
		
		mHandler.postDelayed(mPingingRunnable, 1000);
		
		mIsBroadcasting = true;
		while(mIsBroadcasting) {
			try {
				mByte = mInStream.read();
			} catch (IOException e) {
				e.printStackTrace();
				showToast(e.getMessage());
				break;
			}			
	    	// Clean up intent
			mSwitchEventIntent.removeExtra(EXTRA_SWITCH_EVENT);
			switch (mByte) {
				case 0x07:
					handleSwitchEvent(SWITCH_FWD);
					break;
				case 0x0B:
					handleSwitchEvent(SWITCH_BACK);
					break;
				case 0x0E:
					handleSwitchEvent(SWITCH_RIGHT);
					break;
				case 0x0D:
					handleSwitchEvent(SWITCH_LEFT);
					break;
				case 0x70:
					mPingCounter--;
					break;
				default:
					break;
			}
		}
	}

	private void handleSwitchEvent(int switchEvent) {
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
				PowerManager.ON_AFTER_RELEASE, "");
		wl.acquire(1000);
		mKeyguardLock.disableKeyguard();
		// Poke the user activity timer
		pm.userActivity(SystemClock.uptimeMillis(), true);
		// Broadcast event
		mSwitchEventIntent.putExtra(EXTRA_SWITCH_EVENT, switchEvent);
		sendBroadcast(mSwitchEventIntent);
	}
	
	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				mKeyguardLock.reenableKeyguard();
			}
			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				Bundle extras = intent.getExtras();
				Integer state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
				if (state.equals(BluetoothAdapter.STATE_TURNING_OFF))
					stopSelf();
			}
		}
		
	};
	
	private void connectShield(String mShieldAddress) {
    	if (openSocket(mShieldAddress)) {
    		startBroadcasting();
    		mTeklaHelper.setShieldAddress(this, mShieldAddress);
    	}
	}
	/**
	* Connects to bluetooth server.
	*/
	private boolean openSocket(String shieldAddress) {
		Boolean success = false;
		BluetoothDevice teklaShield;
		
    	disconnectShield();
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

		success = connectSocket();
		
		if (!success) {
	    	// Second method to create Bluetooth socket
	    	try {
	            mBluetoothSocket = teklaShield.createRfcommSocketToServiceRecord(SPP_UUID);
			} catch (IOException e) {
				e.printStackTrace();
				showToast("CreateSocket: " + e.getMessage());
			}
			success = connectSocket();
		}
		return success;
	}
	
	private boolean connectSocket() {
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
	
	private void disconnectShield() {
		// Stop pinging
		mHandler.removeCallbacks(mPingingRunnable);
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
		if (mIsBroadcasting != null) {
			// Stop broadcasting thread
	    	mIsBroadcasting = false;
	    	while(mBroadcastingThread.isAlive()) {
	    		// Wait for the thread to die
	    		SystemClock.sleep(1);
	    	}
			// Reset if previously started
	    	if (mBroadcastingThread.getState() == Thread.State.TERMINATED) {
	    		mBroadcastingThread = new Thread(this);
	    		sendBroadcast(mBroadcastStoppedIntent);
	    	}
		}
    	cancelNotification();
	}

	private Runnable mPingingRunnable = new Runnable () {

		@Override
		public void run() {
			mPingCounter++;
			if (mPingCounter > PING_TIMEOUT_COUNTER) {
				// We lost connection, stop pinging
				mPingCounter = 0;
				disconnectShield();
				restartReconnectThread();
			} else {
				write2Shield((byte) 0x70);
				mHandler.postDelayed(this, PING_DELAY);
			}
		}
		
	};
	
	private Runnable mReconnectRunnable = new Runnable () {
		@Override
		public void run() {
			while(!mIsBroadcasting && mKeepReconnecting) {
				connectShield(getSavedShieldAddress());
				SystemClock.sleep(PING_DELAY);
			}
		}
	};
	
	private void restartReconnectThread() {
		stopReconnectThread();
		startReconnectThread();
	}

	
	private void startReconnectThread() {
		mKeepReconnecting = true;
        mReconnectThread = new Thread(mReconnectRunnable);
		mReconnectThread.start();
	};
	
	private void stopReconnectThread() {
		mKeepReconnecting = false;
		if (mReconnectThread != null) {
			// Stop reconnect thread
	    	while(mReconnectThread.isAlive()) {
	    		// Wait for the thread to die
	    		SystemClock.sleep(1);
	    	}
		}
	};
	
	private String getSavedShieldAddress() {
		return mTeklaHelper.getShieldAddress(this);
	}

    private void write2Shield(byte mByte) {
		try {
			mOutStream.write(mByte);
		} catch (IOException e) {
			e.printStackTrace();
			showToast(e.getMessage());
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

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
