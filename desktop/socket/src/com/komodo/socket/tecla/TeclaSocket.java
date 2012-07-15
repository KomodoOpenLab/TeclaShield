/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    ArrayList<RemoteDevice> filtered_devices;
    ArrayList<ServiceRecord> filtered_services;
    String[] devicelist;
    String device_name=null;
    public boolean btstate=false;
    boolean last;
    // Constructor should be provided be a 128 bit 
    //UUID in string form without dashes
    
    public final Object service_search_lock = new Object();
    
   //Constructor for Socket as a Client 
    public TeclaSocket(String uuidname){
        /*
         * Client Sockets Constructor and doesn't search for devices.
         */
        initialize(uuidname);
        
    }
    public TeclaSocket(String uuidname,boolean search){
        /*
         * Construtor with an option to search
         * Should be used for Client Sockets
         */
        initialize(uuidname);
        if(search)
        scan_devices();
    }
    
    //Constructor for socket as a server
    
    public TeclaSocket(String uuidname,String name,boolean auth,boolean enc){
        /*
         * Constructor when making a server TeclaSocket.
         */
        initialize(uuidname);
        startserver(name,auth,enc);
    }
    
    
    //initilaize the desktop sockets
    private void initialize(String uuidname) {
        /*
         * Initializes the socket parameters
         */
            eventlist = new EventListenerList();
        try {
            local=LocalDevice.getLocalDevice();
            System.out.println("Local device found with address " + local.getBluetoothAddress()+ " "
                    + local.getFriendlyName());
            if(!local.isPowerOn()){
                System.out.println("Bluetooth switched off,Turn on Bluetooth");
                btstate=false;
                
            }else{
            btstate=true;    
            dagent=local.getDiscoveryAgent();
            uuid=new UUID(uuidname,false);
            }
        } catch (BluetoothStateException ex) {
            btstate=false;
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
        /*
         * Should be used to start the bluetooth server.
         * Will block the thread.
         * auth and encr should match with the client.
         * auth -> authorization
         * encr -> encryption
         */
        String url;
        url="btspp://localhost:"+uuid+";name="+name+";authenticate="+auth+";encrypt="+encr+";";
        try {
            server = (StreamConnectionNotifier)Connector.open(url);
            conn = server.acceptAndOpen();
            datain=new DataInputStream(conn.openDataInputStream());
	    dataout=new DataOutputStream(conn.openDataOutputStream());
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_CONNECT);
            fireevent(eve);
            connectionflag=true;
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    //send bytes to TeclaShield
    public void send(Byte b) {
        try {
            /*
             * Send a byte to the Connected RemoteDevice
             */
            dataout.writeByte(b);
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_SENT);
            fireevent(eve);
        } catch (IOException ex) {
            if(connectionflag)
            disconnect();
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //receives byte fromstream will wait till a byte is recieved...
    public void receive() {
        try {
            
            while((datain.available()==0)){
                ;
            }
            /*
             * Opens the socket for listening, if a data is available,
             * the data can be read from inputstream -> datain from onReceive 
             */
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_RECEIVE);
            fireevent(eve);
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //starts the inquiry
    public void scan_devices() {
        /*
         * Start Device inquiry and search for TeclaShields in range
         */
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
        /*
         * Executed when services are discovered
         * Further filters the device list separating the
         * devices with service available from the 
         * devices with service not available
         * stores them in filtered_services.
         */
        if(srs != null && srs.length>0){
            filtered_services.add(srs[0]);
        }
        else{
            System.out.println("No TeclaShield in Range");
            BluetoothEvent ev=new BluetoothEvent(this,BluetoothEvent.NO_SHIELD_FOUND);
            fireevent(ev);
        }
        synchronized(service_search_lock){
            service_search_lock.notify(); //release the synchronizing lock.
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void serviceSearchCompleted(int i, int i1) {
        /*
         * If a device was foundbut the seervice record wasn't available 
         * fire no shield found event.
         */
        if(i1!=DiscoveryListener.SERVICE_SEARCH_COMPLETED && last){
            BluetoothEvent ev=new BluetoothEvent(this,BluetoothEvent.NO_SHIELD_FOUND);
            fireevent(ev);
            last=false;
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void inquiryCompleted(int i) {
        /*
         * Executes when enquiry is completed.
         */
        System.out.println("Device Search Completed");
        connect_to_shield();  //connect to shield
        
    }
   public boolean connect_to_shield(){
       /*
        * Connects to TeclaShield
        */
        device_list=dagent.retrieveDevices(DiscoveryAgent.CACHED);
        //contains all the remote device recently inquired or found....
        int i;
        connectionflag=false;
        /*
         * A list to hold all the devices having TeclaShield or TeklaShield 
         * in their friendly name to distinguish from other devices.
         */
        filtered_devices=new ArrayList<RemoteDevice>(); 
        if(device_list != null){
        for(i=0;i<device_list.length;i++){
          RemoteDevice temp=device_list[i];
          
            try {
                String name = temp.getFriendlyName(true);
                if(name.contains("TeclaShield")||name.contains("TeklaShield"))
                    filtered_devices.add(temp);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                
            }
          
        }
        
        }
        if(filtered_devices.size()>0)
            /*
             * if TeclaShields are found connect to them f services are available.
             */
            connect(new UUID[]{uuid},filtered_devices);
            
        else{
            /*
             * If no device with names having TeclaShield or Teklashield in them
             * fire no shield found event.
             * Wait for next call to connect.
             */
            BluetoothEvent ev=new BluetoothEvent(this,BluetoothEvent.NO_SHIELD_FOUND);
            fireevent(ev);
            connectionflag=false;
        }
        return connectionflag;
   }
   /*  Adds EventListener for custom Bluetooth events
    *  thus enabling an event driven model
    *  events possible are receive,sent,connect and disconnect
    */
   public void addBluetoothEventListener(BluetoothEventListener btlistener){
       //attach an event listener for bluetooth events to the calling instance.
       eventlist.add(BluetoothEventListener.class,btlistener);
   }
   public void removeBluetoothEventListener(BluetoothEventListener btlistener){
       //remove an existing eventlistener for the given instance.
       eventlist.remove(BluetoothEventListener.class,btlistener);
   }
   private void fireevent(BluetoothEvent ev){
       /*
        * Fires a BluetoothEvent ev.
        */
       Object[] listeners =eventlist.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==BluetoothEventListener.class) {
                switch(ev.event_id)
                {
                    case BluetoothEvent.BLUETOOTH_CONNECT:
                        ((BluetoothEventListener)listeners[i+1]).onConnect(device_name);
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
                    case BluetoothEvent.MULTIPLE_SHIELDS_FOUND:
                        ((BluetoothEventListener)listeners[i+1]).onMultipleShieldsFound(devicelist);
                        break; 
                    case BluetoothEvent.NO_SHIELD_FOUND:
                        ((BluetoothEventListener)listeners[i+1]).onNoShieldFound();
                        break;
                }
            }
        }
     
   }
   public boolean testconnection(Byte testbyte){
       /*
        * Test the connection by sending a byte and update the status of connectionflag.
        */
       send(testbyte);
       return connectionflag;
   }
   public void close(){
       /*
        * Closes all the streams without firing onDisconnect event.
        */
        try {
            if(datain!=null)
            datain.close();
            if(dataout!=null)
            dataout.close();
            if(conn!=null)
            conn.close();
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   public void disconnect(){
       /*
        * Close the connection by closing all the streams.
        */
        try {
            if(datain!=null)
            datain.close();
            if(dataout!=null)
            dataout.close();
            if(conn!=null)
            conn.close();
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_DISCONNECT);
            fireevent(eve);
            connectionflag=false;
        } catch (IOException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
   }

   public void connect(UUID uuid){
        try {
            String connurl = dagent.selectService(uuid, ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            conn = (StreamConnection) Connector.open(connurl);
            datain= new DataInputStream(conn.openInputStream());
            dataout=new DataOutputStream(conn.openOutputStream());
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_CONNECT);
            fireevent(eve);
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
                Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
   }

   public void connect(UUID[] uuid_,ArrayList<RemoteDevice> btdevices ){
       filtered_services=new ArrayList<ServiceRecord>();
       for(int j=0;j<btdevices.size();j++)
           try {   
            if(j==btdevices.size()-1)last=true; 
            
            dagent.searchServices(null,uuid_,btdevices.get(j),this); //start a service search
            //wait for search to complete by putting the thrad to wait.
            synchronized(service_search_lock){
                try {
                    service_search_lock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       int size=filtered_services.size();
       //If a single device is found connect to it straight away.
       if(size==1){
           connect_to_service(filtered_services.get(0));
       }
       else if(filtered_services.size()>1){
           /*
            * If multiple Teclashields with available service records are found
            * fire the multiple shields found event and provide it with a list of
            * device names to populate the radio button group in ChooserDialog.
            * String array and filtered_services are mapped one to one.
            */
           devicelist=new String[size];
           for( int i = 0;i<size;i++){
                try {
                    devicelist[i]=filtered_services.get(i).getHostDevice().getFriendlyName(true);
                } catch (IOException ex) {
                    Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
           }
           BluetoothEvent ev=new BluetoothEvent(this,BluetoothEvent.MULTIPLE_SHIELDS_FOUND);
           fireevent(ev);
       }
   }
   public void connect_to_service(int index){
       /*
        * Shold be called for passing a service record from a list of service records 
        * with index pointing the desirable servie record to choose.
        * Used to pass service record  from the ChooserDialog
        */
       connect_to_service(filtered_services.get(index));
   }
   
   public void connect_to_service(ServiceRecord record){
       /*
        * Actually connect to the particular Service Record on a particular hostDevice. 
        */
       try {
           //Get the device name
            device_name=record.getHostDevice().getFriendlyName(true);
           //Open the connections and input-output streams
            String connurl = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            conn = (StreamConnection) Connector.open(connurl);
            datain= new DataInputStream(conn.openInputStream());
            dataout=new DataOutputStream(conn.openOutputStream());
            //fire onconnect event.
            BluetoothEvent eve=new BluetoothEvent(this,BluetoothEvent.BLUETOOTH_CONNECT);
            fireevent(eve);
            //update connectionflag;
            connectionflag=true;
        } catch (BluetoothStateException ex) {
            Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
                Logger.getLogger(TeclaSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
   }

}
