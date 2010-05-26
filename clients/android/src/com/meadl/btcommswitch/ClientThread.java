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
	private String address = "";
	private UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666");
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

		int buf = -1;
		while(true) {
	    	try {
	    		inStream = client_socket.getInputStream();
	    		int b = inStream.read();
	    		this.updateTextView("Received byte '" + buf + "'");
	    		if (buf != 1) {
	    			// edge detection
		    		if((buf>>0)%2 == 1 && (b>>0)%2 == 0) {
		    			this.updateTextView("Switch #1 falling edge.");
		    		} else if ((buf>>0)%2 == 0 && (b>>0)%2 == 1) {
		    			this.updateTextView("Switch #1 rising edge.");
		    		}
		    		if((buf>>1)%2 == 1 && (b>>1)%2 == 0) {
		    			this.updateTextView("Switch #2 falling edge.");
		    		} else if ((buf>>1)%2 == 0 && (b>>1)%2 == 1) {
		    			this.updateTextView("Switch #2 rising edge.");
		    		}
		    		if((buf>>2)%2 == 1 && (b>>2)%2 == 0) {
		    			this.updateTextView("Switch #3 falling edge.");
		    		} else if ((buf>>2)%2 == 0 && (b>>2)%2 == 1) {
		    			this.updateTextView("Switch #3 rising edge.");
		    		}
		    		if((buf>>3)%2 == 1 && (b>>3)%2 == 0) {
		    			this.updateTextView("Switch #4 falling edge.");
		    		} else if ((buf>>3)%2 == 0 && (b>>3)%2 == 1) {
		    			this.updateTextView("Switch #4 rising edge.");
		    		}
	    		}
	    		if((b>>7)%2 == 1) {
	    			this.updateTextView("Echo ...");
	    		}		    		
	    		buf = b;	    		
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
