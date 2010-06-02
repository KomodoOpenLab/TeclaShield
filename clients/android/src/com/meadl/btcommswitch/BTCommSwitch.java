package com.meadl.btcommswitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.widget.TextView;

public class BTCommSwitch extends Activity implements Runnable {

	private BluetoothAdapter btAdapter;
	private BluetoothSocket clientSocket;
    private InputStream inStream;
    private OutputStream outStream;
	private int REQUEST_ENABLE_BT = 0;
	private TextView logView, txByte, rxByte;	
	private String logString = "", rxByteString = "", txByteString = "";
	
	// hard-code hardware address and UUID here
	//private String server_address = "00:06:66:02:CB:75"; // BlueSMiRF
	private String server_address = "00:16:41:89:C8:0A"; // jsilva-laptop
	// Using "well-known" SPP UUID as specified at:
	// http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        logView = (TextView) findViewById(R.id.TextView01);
        rxByte = (TextView) findViewById(R.id.TextView03);
        txByte = (TextView) findViewById(R.id.TextView05);
        
        updateLogView("Creating thread ...");
        
        Thread thread = new Thread(this);
        thread.start();
    }

	@Override
	public void run() {
		updateLogView("Running thread...");
		Looper.prepare();
		
		// set up local bluetooth
		btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
        	updateLogView("Device does not support Bluetooth. Ending thread...");
        	return;
        }
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        
        // connect to server app
        try {
        	BluetoothDevice btServer = btAdapter.getRemoteDevice(server_address);
            clientSocket = btServer.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			updateLogView(e.getMessage());
			return;
		}
		
		try {
			inStream = clientSocket.getInputStream();
			outStream = clientSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			updateLogView("Failed to get IO streams. Thread ended.");
			return;
		}

		while(true) {
	    	try {
	    		inStream = clientSocket.getInputStream();
	    		int b = inStream.read();
	    		updateRXbyte("" + b);
	    		//switch (b) {
    			//case 0x0F: updateText("Switch released");
    			//case 0x07: updateText("Forward switch on");
    			//case 0x0B: updateText("Back switch on");
    			//case 0x0D: updateText("Left switch on");
    			//case 0x0E: updateText("Right switch on");
    			//case 0x70: updateText("Echo...");
    			//default: updateText("Unknown byte received");
	    		//}
	    	} catch (IOException e) {
	    		updateLogView(e.getMessage());	    		
	    		break;
	    	}			
		}
		updateLogView("Thread ended.");
		
	}

	private void updateLogView(String s) {
		logString += "\n" + s;
		logHandler.sendEmptyMessage(0);
	}

	private void updateRXbyte(String s) {
		rxByteString = s;
		logHandler.sendEmptyMessage(1);
	}

	private void updateTXbyte(String s) {
		txByteString = s;
		logHandler.sendEmptyMessage(2);
	}

	private Handler logHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: logView.setText(logString);
			case 1: rxByte.setText(rxByteString);
			case 2: txByte.setText(txByteString);
			}
		}
	};
    
	public void write(byte b) {
		try {
			outStream.write(b);
			updateLogView("Sent byte '" + b + "'");
		} catch (IOException e) {
			e.printStackTrace();
			updateLogView("Write error.");
		}
	}
    
    /* Call this from the main Activity to shutdown the connection */
    public void stopDetectingEvent() {
        try {
            clientSocket.close();
            updateLogView("Closed client socket.");
        } catch (IOException e) { }
    }
    
}