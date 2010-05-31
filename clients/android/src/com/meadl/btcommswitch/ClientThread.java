package com.meadl.btcommswitch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.TextView;

public class ClientThread extends Thread {

	private BluetoothAdapter bluetooth;
	private BluetoothSocket client_socket;
    private InputStream inStream;
    private OutputStream outStream;
	
	// hard code hardware address and UUID here
	private String address = "00:06:66:02:CB:75";
	// Using "well-known" SPP UUID as specified at:
	// http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	String text = "";
	
	TextView txtStatus;
	
	public ClientThread(TextView tv) {
		super();
		txtStatus = tv;
		
		this.updateTextView("Creating thread ...");
	}
	
	private void updateTextView(String s) {
		text += "\n" + s;
		int start = 0, end = text.length(), index = 0, len = text.length();		
		char buffer[] = new char[128];
		text.getChars(start, end, buffer, index);
		txtStatus.setText(buffer, start, len);		
	}
	
	public void run() {
		this.updateTextView("Running thread ...");
		
		// set up local bluetooth
		bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            // Device does not support Bluetooth
        	this.updateTextView("Device does not support Bluetooth. Thread ended.");
        	return;
        }
        if (!bluetooth.isEnabled()) {
        	this.updateTextView("Bluetooth is not enabled. Thread ended.");
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
			this.updateTextView("Failed to connect. Thread ended.");
			return;
		}
		
		try {
			inStream = client_socket.getInputStream();
			outStream = client_socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			this.updateTextView("Failed to get IO streams. Thread ended.");
			return;
		}

		while(true) {
	    	try {
	    		inStream = client_socket.getInputStream();
	    		int b = inStream.read();
	    		this.updateTextView("Received byte '" + b + "'");
	    		switch (b) {
    			case 0x0F: this.updateTextView("Switch released");
    			case 0x07: this.updateTextView("Forward switch on");
    			case 0x0B: this.updateTextView("Back switch on");
    			case 0x0D: this.updateTextView("Left switch on");
    			case 0x0E: this.updateTextView("Right switch on");
    			case 0x70: this.updateTextView("Echo...");
    			default: this.updateTextView("Unknown byte received");
	    		}
	    	} catch (IOException e) {
	    		this.updateTextView("Read error.");	    		
	    		break;
	    	}			
		}
		this.updateTextView("Thread ended.");		
	}
	
	public void write(byte b) {
		try {
			outStream.write(b);
			this.updateTextView("Sent byte '" + b + "'");
		} catch (IOException e) {
			e.printStackTrace();
    		this.updateTextView("Write error.");
		}
	}
    
    /* Call this from the main Activity to shutdown the connection */
    public void cancel() {
        try {
            client_socket.close();
    		this.updateTextView("Closed client socket.");
        } catch (IOException e) { }
    }
}
