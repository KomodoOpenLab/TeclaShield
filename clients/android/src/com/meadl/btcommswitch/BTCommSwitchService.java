package com.meadl.btcommswitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

public class BTCommSwitchService extends Service {


	private BluetoothAdapter bluetooth;
	private BluetoothSocket client_socket;
    private InputStream inStream;
    private OutputStream outStream;
	
	// hard code hardware address and UUID here
	private String address = "";
	private UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666");
	
	@Override
	public void onCreate() {
		//TODO: Actions to perform when service is created.
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO Launch a background thread to do processing.
		if((flags & START_FLAG_RETRY) == 0) {
			// TODO If it’s a restart, do something.
			
		} else if((flags & START_FLAG_REDELIVERY) == 0) {
			
		} else {
			// TODO Alternative background process.
			
		}

		Thread thread = new Thread(null, doBackgroundThreadProcessing, "Background");
		thread.start();
		
		return Service.START_STICKY;
	}
	
	//Runnable that executes the background processing method.
	private Runnable doBackgroundThreadProcessing = new Runnable() {
		public void run() {
			backgroundThreadProcessing();
			}};
			
	// bluetooth communication runs in the background here.
	private void backgroundThreadProcessing() {
		// set up local bluetooth
		bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            // Device does not support Bluetooth
        	//this.updateTextView("Device does not support Bluetooth. Thread ended.");
        	return;
        }
        if (!bluetooth.isEnabled()) {
        	//this.updateTextView("Bluetooth is not enabled. Thread ended.");
        	return;
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        
        // connect to server app
        try {
        	BluetoothDevice device = bluetooth.getRemoteDevice(address);
            client_socket = device.createRfcommSocketToServiceRecord(uuid);
            client_socket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			//this.updateTextView("Failed to connect. Thread ended.");
			return;
		}
		
		try {
			inStream = client_socket.getInputStream();
			outStream = client_socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			//this.updateTextView("Failed to get IO streams. Thread ended.");
			return;
		}

		int buf = -1;
		while(true) {
	    	try {
	    		inStream = client_socket.getInputStream();
	    		int b = inStream.read();
	    		//this.updateTextView("Received byte '" + buf + "'");
	    		if (buf != 1) {
	    			// edge detection
		    		if((buf>>0)%2 == 1 && (b>>0)%2 == 0) {
		    			//this.updateTextView("Switch #1 falling edge.");
		    		} else if ((buf>>0)%2 == 0 && (b>>0)%2 == 1) {
		    			//this.updateTextView("Switch #1 rising edge.");
		    		}
		    		if((buf>>1)%2 == 1 && (b>>1)%2 == 0) {
		    			//this.updateTextView("Switch #2 falling edge.");
		    		} else if ((buf>>1)%2 == 0 && (b>>1)%2 == 1) {
		    			//this.updateTextView("Switch #2 rising edge.");
		    		}
		    		if((buf>>2)%2 == 1 && (b>>2)%2 == 0) {
		    			//this.updateTextView("Switch #3 falling edge.");
		    		} else if ((buf>>2)%2 == 0 && (b>>2)%2 == 1) {
		    			//this.updateTextView("Switch #3 rising edge.");
		    		}
		    		if((buf>>3)%2 == 1 && (b>>3)%2 == 0) {
		    			//this.updateTextView("Switch #4 falling edge.");
		    		} else if ((buf>>3)%2 == 0 && (b>>3)%2 == 1) {
		    			//this.updateTextView("Switch #4 rising edge.");
		    		}
	    		}
	    		if((b>>7)%2 == 1) {
	    			//this.updateTextView("Echo ...");
	    		}		    		
	    		buf = b;	    		
	    	} catch (IOException e) {
	    		//this.updateTextView("Read error.");	    		
	    		break;
	    	}			
		}		
		//this.updateTextView("Thread ended.");
	}
	
	public void write(byte b) {
		try {
			outStream.write(b);
			//this.updateTextView("Sent byte '" + b + "'");
		} catch (IOException e) {
			e.printStackTrace();
    		//this.updateTextView("Write error.");
		}
	}
}
