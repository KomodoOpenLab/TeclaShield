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
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.event.EventListenerList;

/**
 * Version 0.8
 * This class will be responsible for making and managing sockets
 * Bluetooth RFComm Sockets only
 * The socket will thus be used to connect to TeclaShield 
 * The current socket connects to the TeclaShield Emulator
 * in Windows and Fedora using same piece of code
 * A TeclaSocket can be made a server or a client 
 * This socket makes only RFComm Sockets i.e SPP profile
 * and cannot be used to make L2CAP sockets
 * @author Akhil
 */
public class TeclaSocket implements Communication,DiscoveryListener {
    public static final int OS_WINDOWS = 0;
    public static final int OS_LINUX = 1;
    public static final int OS_MAC = 2;
    public static final int TYPE_SERVER=1;
    public static final int TYPE_CLIENT=0;
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
    EventListenerList eventlist;
    StreamConnectionNotifier server ;
    // Constructor should be provided be a 128 bit 
    //UUID in string form without dashes
    
   //Constructor for Socket as a Client 
    
    public TeclaSocket(String uuidname){
        initialize(uuidname);
        scan_devices();
    }
    
    //Constructor for socket as a server
    
    public TeclaSocket(String uuidname,String name,boolean auth,boolean enc){
        initialize(uuidname);
        startserver(name,auth,enc);
    }
    
    
    //initilaize the desktop sockets
    private void initialize(String uuidname) {
            eventlist = new EventListenerList();
        try {
            local=LocalDevice.getLocalDevice();
            System.out.println("Local device found with address " + local.getBluetoothAddress()+ " "
                    + local.getFriendlyName());
            if(!local.isPowerOn())
                System.out.println("Bluetooth switched off,Turn on Bluetooth");
            dagent=local.getDiscoveryAgent();
            uuid=new UUID(uuidname,false);
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
     * Starts a server connection .Will block the thread till a conection is found
     *Should be started in a new thread.
     * on successful connection onConnect function of BluetoothEventListener will be 
     * executed for this particular socket.
     */
    
    public void startserver(String name,boolean auth,boolean encr){
        String url;
        url="btspp://localhost"+uuid+";name="+name+";authenticate="+auth+";encrypt="+encr+";";
        try {
            server = (StreamConnectionNotifier)Connector.open(url);
            conn = server.acceptAndOpen();
            datain=new DataInputStream(conn.openDataInputStream());
	    dataout=new DataOutputStream(conn.openDataOutputStream());
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_CONNECT);
            fireevent(eve);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    //send bytes to TeclaShield
    public void send(Character b) {
        try {
            dataout.write(b);
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_SENT);
            fireevent(eve);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //receives byte fromstream will wait till a byte is recieved...
    public void receive() {
        try {
            
            while((datain.available()!=0)); //wait till input stream contains a new byte 
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_RECEIVE);
            fireevent(eve);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                        BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_CONNECT);
                         fireevent(eve);
                        break;
                    }
                    
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                disconnect();
            }
          
        }
        return connectionflag;
   }
   /*  Adds EventListenr for custom Bluetooth events
    *  thus enabling an event driven model
    *  events possible are receive,sent,connect and disconnect
    */
   public void addBluetoothEventListener(BluetoothEventListener btlistener){
       eventlist.add(BluetoothEventListener.class,btlistener);
   }
   public void removeBluetoothEventListener(BluetoothEventListener btlistener){
       eventlist.remove(BluetoothEventListener.class,btlistener);
   }
   private void fireevent(BluetoothEvent ev){
       Object[] listeners =eventlist.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==BluetoothEventListener.class) {
                switch(ev.event_id)
                {
                    case BluetoothEvent.BLUETOOTH_CONNECT:
                        ((BluetoothEventListener)listeners[i+1]).onConnect();
                        break;
                    case BluetoothEvent.BLUETOOTH_DISCONNECT:
                        ((BluetoothEventListener)listeners[i+1]).onDisconnect();
                        break;
                    case BluetoothEvent.BLUETOOTH_RECEIVE:
                        ((BluetoothEventListener)listeners[i+1]).onReceive(datain);
                        break;
                    case BluetoothEvent.BLUETOOTH_SENT:
                        ((BluetoothEventListener)listeners[i+1]).onSent();
                        break;
                
                }
            }
        }
     
   }
   public void disconnect(){
        try {
            datain.close();
            dataout.close();
            conn.close();
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_DISCONNECT);
            fireevent(eve);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
   } 
}
