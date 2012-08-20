/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;
import java.io.*;
import java.lang.Thread.State;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 * The socket implementation for connecting to android over WiFi 
 * This class also provides functions for reading,writing and is responsible for
 * firing events regarding the state of WiFi connection.
 * @author Akhil
 */
public class WiFiSocket{
      
    ObjectInputStream in;
    ObjectOutputStream out;
    ServerSocket serversock;
    Object lock;
    MulticastSocket multi_sock;
    DatagramPacket packet;
    Socket client;
    String passcode;
    InetAddress group;   
    String receiveddata;
    EventListenerList list;
    acceptorthread acceptor;
    
    public boolean connection_status;
    
    public static final String SERVER_NAME = "localhost";
    
    public static final int PORT_NUMBER = 28195;
    
    public static final int TIMEOUT=2*60000; //2 minutes
    
    
    public WiFiSocket(String passcode_) throws IOException{
        passcode=passcode_;
        group=InetAddress.getByName("225.0.0.0");
        list=new EventListenerList();
        connection_status=false;
        acceptor=new acceptorthread();
        lock=new Object();
    }
    
    public WiFiSocket(String passcode_,boolean open) throws IOException{
        /*
         * TeclaClient will act as wifi server socket
         */
        passcode=passcode_;
        serversock=new ServerSocket(PORT_NUMBER);
        multi_sock=new MulticastSocket(PORT_NUMBER);
        group=InetAddress.getByName("225.0.0.0");
        connection_status=false;
        lock=new Object();
        if(open)
            search_connections();
    }
    public WiFiSocket(String passcode_,boolean open,int portnumber) throws IOException{
        /*
         * TeclaClient will act as wifi server socket
         */
        passcode=passcode_;
        serversock=new ServerSocket(portnumber);
        connection_status=false;
        if(open)
            search_connections();
    }
    public void setPasscode(String passcode_){
        passcode=passcode_;
    }
    
    public boolean authenticate(String inputcode){
        /*
         * Authentication function
         */
        if(inputcode.equals(passcode))
            return true;        
        return false;
    }
    
    public void close() throws IOException{
        /*
         * Closes the socket without firing the disconnected event
         */
        if(in!=null)
            in.close();
        if(out!=null)
            out.close();
        if(client!=null)
            client.close();
        
        connection_status=false;
    }
    public void disconnect() throws IOException{
        /*
         * Disconnects with firing the appropriate event.
         */
        
        close();
        fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_DISCONNECTED));
    }
    public void read() throws IOException{
        /*
         *  Reads the input stream.
         *  Blocks the thread.Should be called from another thread.
         *  The result can be obtained in OnReceiveddata Method of WiFiEventListener Interface;
         */
        receiveddata=in.readUTF();
        fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_RECEIVED));
                
    }
    
    public void write(String input) throws IOException{
        /*
         * Writes an inputString to the OutputStream i.e.
         * Sends data to the android client.
         */
        out.writeUTF(input);
        out.flush(); //very important
        fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_SENT));
    }
    
    public void search_connections(){
        /*
         * Should be called on a separate thread to find android clients.
         * repeatedly sends the broadcast packets for the android client to make
         * the android client aware of the server's IP address.
         */
            
            if(acceptor.getState()==State.NEW)
                acceptor.start();
            else if(acceptor.getState()==State.TERMINATED){
                acceptor=new acceptorthread();
                acceptor.start();
            }
            try {
            /*
             * Broadcast alive packets multiple times as communication over
             * UDP is unreliable.
             */
            for(int j=0;j<10;j++)
                broadcast("alive");
            try {
                Thread.sleep(10000); //wait for 10 seconds before braodcasting again.
            } catch (InterruptedException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            fireevent(new WiFiEvent(this,WiFiEvent.NO_CLIENT_FOUND));
        } catch (IOException ex) {
            ex.printStackTrace();
            fireevent(new WiFiEvent(this,WiFiEvent.NO_CLIENT_FOUND));
        }
        
    }
    
    public String getPasscode(){
        return passcode;
    }
    public void broadcast(String message) throws IOException{
        /*
         * Writes the Multicast Socket with String messages and sends it to 
         * all the subscribers o fthe group i.e the android phones running 
         * TeclaAccess.
         */
        if(multi_sock==null){
            multi_sock=new MulticastSocket(PORT_NUMBER); 
            multi_sock.setBroadcast(true);       
            multi_sock.setSoTimeout(3000);
        }
        byte[] buf=message.getBytes(); 
        packet=new DatagramPacket(buf,buf.length,group,PORT_NUMBER);
        multi_sock.send(packet);
    }
       
    /*
     * WiFiEvent Firing code
     */
    public void addWiFiEventListener(WiFiEventListener wifilistener){
        // registers the WiFiEventListener for this object
        list.add(WiFiEventListener.class,wifilistener);
    }
    
    public void removeWiFiEventListener(WiFiEventListener wifilistener){
        // unregisters the WiFiEventListener for this object
        list.remove(WiFiEventListener.class,wifilistener);
    }
    
    public void fireevent(WiFiEvent ev){
        //Fires a WiFiEvent to be listened by a WiFiEventListener
        Object[] listeners =list.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==WiFiEventListener.class) {
                switch(ev.event_id)
                {
                    case WiFiEvent.CLIENT_FOUND:
                        ((WiFiEventListener)listeners[i+1]).onClientFound();
                        break;
                    case WiFiEvent.CLIENT_CONNECTED:
                        ((WiFiEventListener)listeners[i+1]).onClientConnected();
                        break;
                    case WiFiEvent.CLIENT_DISCONNECTED:
                        ((WiFiEventListener)listeners[i+1]).onClientDisconnected();
                        break;
                    case WiFiEvent.NO_CLIENT_FOUND:
                        ((WiFiEventListener)listeners[i+1]).onNoClientFound();
                        break;
                    case WiFiEvent.CLIENT_RECEIVED:
                        ((WiFiEventListener)listeners[i+1]).onReceived(receiveddata);
                        break; 
                    case WiFiEvent.CLIENT_SENT:
                        ((WiFiEventListener)listeners[i+1]).onSent();
                        break;
                }
            }
        }
        
        
        
    }
    
    public void closeserver(){
        //closes the server socket.
        if(serversock!=null)
            try {
            serversock.close();
        } catch (IOException ex) {
            Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class acceptorthread extends Thread{
        /*
         * A thread to wait for accepting a client
         */
        
        public acceptorthread(){
        
        }
        @Override
        public void run(){
            try {
                //Make a new server socket and put it to accept mode
                
                if(serversock==null || !serversock.isBound() || serversock.isClosed())
                serversock=new ServerSocket(PORT_NUMBER+2);
                     
                System.out.println("Opening server connection");
                
                client=serversock.accept();
                //accept a client and then close the serversocket
                serversock.close();
                
                if(client==null)
                        return;
                        
                System.out.println("connection to android established,authenticating");
                
                client.setSoTimeout(20000); //Set timeout value for client socket.
                //Initialize the input and putput streams
                
                out=new ObjectOutputStream(client.getOutputStream());
                
                out.flush();
                
                in=new ObjectInputStream(client.getInputStream());
            
            String received="";
            
            received=in.readUTF();
            
            /*
             * Read the sent Password and maintain the connection 
             * if authentication is successful otherwise close the connection.
             */
            if(authenticate(received)){
                out.writeUTF("Success");
                out.flush();
                fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_CONNECTED));
            }
            else
                close();
                
            } catch (IOException ex) {
                
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            //set the connection status to true
            connection_status=true;
            
        }
    }
   
}
