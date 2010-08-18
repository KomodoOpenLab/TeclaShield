package ca.idi.tekla.sep;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import ca.idi.tekla.Demo;
import ca.idi.tekla.R;

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
import android.widget.Toast;

public class SEPService extends Service implements Runnable {

    private NotificationManager mNM;
	private BluetoothAdapter btAdapter;
	private BluetoothSocket clientSocket;
    private InputStream inStream;
    private OutputStream outStream;
    
	//Constants
    public static final String INTENT = "ca.idi.tekla.sep.SEPService";
    public static final String FWD_ACTION = "ca.idi.tekla.sep.FWD_SWITCH_ACTION";
    public static final String BACK_ACTION = "ca.idi.tekla.sep.BACK_SWITCH_ACTION";
    public static final String RIGHT_ACTION = "ca.idi.tekla.sep.RIGHT_SWITCH_ACTION";
    public static final String LEFT_ACTION = "ca.idi.tekla.sep.LEFT_SWITCH_ACTION";
	//static final private String NONE_ACTION = "ca.idi.tekla.sep.NONE_SWITCH_ACTION";

    Intent fwdIntent = new Intent(FWD_ACTION);
    Intent backIntent = new Intent(BACK_ACTION);
    Intent rightIntent = new Intent(RIGHT_ACTION);
    Intent leftIntent = new Intent(LEFT_ACTION);
    //Intent noneIntent = new Intent(NONE_ACTION);
    
	// hard-code hardware address and UUID here
	private String server_address = "00:06:66:02:CB:75"; // BlueSMiRF 1
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
    	SEPService getService() {
            return SEPService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
	
    @Override
	public void onCreate() {
        //Intents & Intent Filters
        IntentFilter btStateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    	registerReceiver(btStateReceiver, btStateFilter);

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}
	
    @Override
	public void onDestroy() {
        /* Call this from the main Activity to shutdown the connection */
        try {
            // Close socket
        	clientSocket.close();
            // Cancel the persistent notification.
            mNM.cancel(R.string.sep_started);
        } catch (IOException e) {
        	e.printStackTrace();
        	showToast(e.getMessage());
        }

        // Tell the user we stopped.
        showToast(R.string.sep_stopped);
  }
    
    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Set up local bluetooth
		btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
            showToast("Device does not support Bluetooth");
        	stopSelf();
        } else {
	        if (!btAdapter.isEnabled()) {
	            // Bluetooth not enabled
	        	showToast("Bluetooth not enabled");
	            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            enableBTIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(enableBTIntent);
	        } else {
	        	if (connect2Server()) {
	        		startBroadcasting();
	        	} else {
	        		showToast("Failed to connect external input.");
	        	}
	        }
        }
        
        return Service.START_STICKY;
	}
	
	@Override
	public void run() {
		Looper.prepare();
		
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();

        try {
			inStream = clientSocket.getInputStream();
			outStream = clientSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while(true) {
	    	try {
	    		inStream = clientSocket.getInputStream();
	    		int b = inStream.read();
	    		switch (b) {
    			//case 0x0F: TODO: Do nothing
    			//case 0x07: ics.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DPAD_UP);
    			case 0x07: sendBroadcast(fwdIntent); break;
    			case 0x0B: sendBroadcast(backIntent); break;
    			case 0x0D: sendBroadcast(rightIntent); break;
    			case 0x0E: sendBroadcast(leftIntent); break;
    			//case 0x70: TODO: Send hard key event
    			//default: TODO: Do nothing
	    		}
	    	} catch (IOException e) {
				e.printStackTrace();
	    		break;
	    	}			
		}
	
	}
	
	// Bluetooth State Events will be processed here
	private BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			int state = extras.getInt(BluetoothAdapter.EXTRA_STATE);
			if (state == BluetoothAdapter.STATE_ON) {
	        	if (connect2Server()) {
	        		startBroadcasting();
	        	} else {
		            showToast("Failed to connect external input.");
	        	}
			}
		}
	};
	
	/**
	* Connects to bluetooth server.
	*/
	private boolean connect2Server() {
		Boolean success = false;
        try {
        	BluetoothDevice btServer = btAdapter.getRemoteDevice(server_address);
            clientSocket = btServer.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
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
	
    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.sep_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.meadl_status, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Demo.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.sep_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.sep_started, notification);
    }

    public void write(byte b) {
		try {
			outStream.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void showToast(int resid) {
		Toast.makeText(this, resid, Toast.LENGTH_SHORT).show();
	}

}
