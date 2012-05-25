/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * This class will be responsible for making and managing sockets
 * Bluetooth Socket or Wi-fi Socket
 * the socket will thus be used to connect to TeclaShield 
 * The current socket connects to the TeclaShield Emulator
 * in Windows and Fedora using same piece of code
 * 
 * @author Akhil
 */
public class TeclaSocket implements Communication,DiscoveryListener {
    public static final int OS_WINDOWS = 0;
    public static final int OS_LINUX = 1;
    public static final int OS_MAC = 2;
    //UUID values
    //public static final String uuidstring="00001101-0000-1000-8000-00805F9B34FB";
    //if a uuid list is required.
    LocalDevice local;
    DiscoveryAgent dagent;
    RemoteDevice device_list[];
    StreamConnection conn;
    boolean connectionflag;
    public UUID uuid;
    DataInputStream datain;
    DataOutputStream dataout;
    
    // Constructor should be provided be a 128 bit 
    //UUID in string form without dashes
    
    public TeclaSocket(String uuidname){
        initialize(uuidname);
        scan_devices();
    }
    //initilaize the desktop client sockets
    private void initialize(String uuidname) {
        try {
            local=LocalDevice.getLocalDevice();
            System.out.println("Local device found with address " + local.getBluetoothAddress());
            dagent=local.getDiscoveryAgent();
            uuid=new UUID(uuidname,false);
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //send bytes to TeclaShield
    public void send(Character b) {
        try {
            dataout.write(b);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //receives byte fromstream will wait till a byte is recieved...
    public byte receive() {
        byte b='0';
        try {
           // while(!(datain.available()>0));
            b=datain.readByte();
            System.out.println(b);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }
    //starts the inquiry
    public void scan_devices() {
        try {
            dagent.startInquiry(DiscoveryAgent.GIAC,this);  
            //when completed inquiryCompleted is executed
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   public boolean isConnected(){  //provides state of connection
        return connectionflag;
    }

    public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void servicesDiscovered(int i, ServiceRecord[] srs) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void serviceSearchCompleted(int i, int i1) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void inquiryCompleted(int i) {
        System.out.println("Device Search Completed");
        boolean result=connect();  //connect to shield
        if(result) 
            System.out.println("Connected to TeclaShield");
        else
            System.out.println("TeclaShield not found in range");
    }
   public boolean connect(){
        device_list=dagent.retrieveDevices(DiscoveryAgent.CACHED);
        //contains all the remote device recently inquired or found....
        int i;
        connectionflag=false;
        String connstring;
        for(i=0;i<device_list.length;i++){
          RemoteDevice temp=device_list[i];
          
            try {
                String name = temp.getFriendlyName(true);
                System.out.println(temp.toString()+" "+name);
                // connect to the device with name Tecla and Tekla in its friendly name.
                if(name.contains("Tecla")||name.contains("Tekla"))
                {
                    connstring=dagent.selectService(uuid,ServiceRecord.NOAUTHENTICATE_NOENCRYPT,false);
                   //Provides the string required for connecting to TeclaShield service
                    if(connstring != null)
                    {
                        conn = (StreamConnection)Connector.open(connstring); //Connect
                        dataout=conn.openDataOutputStream(); //outputstream to write bytes into
                        datain=conn.openDataInputStream();   //inputstream to read data from
                        connectionflag=true;                 
                        break;
                    }
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
          
        }
        return connectionflag;
   }

    
}
