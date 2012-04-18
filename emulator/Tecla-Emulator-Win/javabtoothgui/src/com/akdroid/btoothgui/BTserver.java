package com.akdroid.btoothgui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.*;
import javax.microedition.io.*;
public class BTserver extends Thread implements Runnable {
	
	public  UUID uuid; //it can be generated randomly
    public  String name ;                       //the name of the service
    public  String url  ;
    LocalDevice local ;
    StreamConnectionNotifier server ;
    StreamConnection conn ;
    DataInputStream datain;
    DataOutputStream dataout;
	public BTserver(){
		uuid = new UUID(                              //the uid of the service, it has to be unique,
				"0000110100001000800000805F9B34FB", false);
		name = "SPP";                       //the name of the service
	    url  =  "btspp://localhost:" + uuid         //the service url
	                                + ";name=" + name 
	                                + ";authenticate=false;encrypt=false;";
		
	    System.out.println("Starting bluetooth device and making it discoverable");
		try {
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);
			try {
				server = (StreamConnectionNotifier)Connector.open(url);
				conn = server.acceptAndOpen();
	            System.out.println("Client Connected...");
	            datain=new DataInputStream(conn.openDataInputStream());
	            dataout=new DataOutputStream(conn.openDataOutputStream());
	           
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (BluetoothStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void ping(){
		Byte c;
		while(true){
			try {
				if((c = datain.readByte()) > 0){
					dataout.writeByte(c);
					//System.out.println(c);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void run(){
		ping();
	}
	public void send(Byte b){
		try {
			dataout.writeByte(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

