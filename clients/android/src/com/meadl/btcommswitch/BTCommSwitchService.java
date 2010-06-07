package com.meadl.btcommswitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

public class BTCommSwitchService extends Service implements Runnable {

    private NotificationManager mNM;
	private BluetoothAdapter btAdapter;
	private BluetoothSocket clientSocket;
    private InputStream inStream;
    private OutputStream outStream;
    
	//Constants
	static final private String FWD_ACTION = "com.meadl.btcommswitch.FWD_SWITCH_ACTION";
	static final private String BACK_ACTION = "com.meadl.btcommswitch.BACK_SWITCH_ACTION";
	static final private String RIGHT_ACTION = "com.meadl.btcommswitch.RIGHT_SWITCH_ACTION";
	static final private String LEFT_ACTION = "com.meadl.btcommswitch.LEFT_SWITCH_ACTION";
	//static final private String NONE_ACTION = "com.meadl.btcommswitch.NONE_SWITCH_ACTION";

    Intent fwdIntent = new Intent(FWD_ACTION);
    Intent backIntent = new Intent(BACK_ACTION);
    Intent rightIntent = new Intent(RIGHT_ACTION);
    Intent leftIntent = new Intent(LEFT_ACTION);
    //Intent noneIntent = new Intent(NONE_ACTION);
    
	// hard-code hardware address and UUID here
	private String server_address = "00:06:66:02:CB:75"; // BlueSMiRF
	// Using "well-known" SPP UUID as specified at:
	// http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	BTCommSwitchService getService() {
            return BTCommSwitchService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
	
    @Override
	public void onCreate() {
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
        	Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Tell the user we stopped.
        Toast.makeText(this, R.string.sep_stopped, Toast.LENGTH_SHORT).show();
  }
    
    @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO Launch a background thread to do processing.
		if((flags & START_FLAG_RETRY) == 0) {
			// TODO If it�s a restart, do something.
			
		} else if((flags & START_FLAG_REDELIVERY) == 0) {
			
		} else {
			// TODO Alternative background process.
			
		}

        Thread thread = new Thread(this);
        thread.start();
		
        // Tell the user we started.
        //Toast.makeText(this, R.string.sep_started, Toast.LENGTH_SHORT).show();

        return Service.START_STICKY;
	}
	
	@Override
	public void run() {
		Looper.prepare();
		
		// set up local bluetooth
		btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "No Bluetooth Adaptor", Toast.LENGTH_SHORT).show();
        	stopSelf();
        }
        if (!btAdapter.isEnabled()) {
            // Bluetooth not enabled
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	//startActivityForResult(enableBtIntent, 0);
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
        	stopSelf();
        }
        
        // connect to server app
        try {
        	BluetoothDevice btServer = btAdapter.getRemoteDevice(server_address);
            clientSocket = btServer.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		
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
                new Intent(this, BTCommSwitch.class), 0);

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

}
