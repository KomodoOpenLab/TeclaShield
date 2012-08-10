/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Akhil
 */
public class WiFiSocket implements Runnable{
    
    int type;
    
    ObjectInputStream in;
    ObjectOutputStream out;
    
    
    ServerSocket serversock;
    
    Object lock;
    
    MulticastSocket multi_sock;
    
    DatagramPacket packet;
    
    Socket client;
    
    public boolean connection_status;
    
    public static final String SERVER_NAME = "localhost";
    
    public static final int PORT_NUMBER = 28195;
    
    public static final int TIMEOUT=2*60000; //2 minutes
    String passcode;
    InetAddress group;
    TimeoutTimer timer;
    
    String receiveddata;
    
    EventListenerList list;
    acceptorthread acceptor;
    
    public WiFiSocket(String passcode_) throws IOException{
        passcode=passcode_;
        group=InetAddress.getByName("225.0.0.0");
        list=new EventListenerList();
        acceptor=new acceptorthread();           
        acceptor.start();
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
        out.flush();
        fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_SENT));
    }
    
    public void search_connections(){
        /*
         * Should be called on a separate thread to find android clients.
         * 
         */
        if(!acceptor.isAlive()){
            acceptor=new acceptorthread();
            acceptor.start();
        }
            try {
            /*
             * Broadcast alive packets multiple times as communication over
             * UDP is unreliable.
             */
            for(int j=0;j<20;j++)
                broadcast("alive");
            
            byte[] buf=new byte[256];
            
            packet=new DatagramPacket(buf,buf.length);
            
            System.out.println("TeclaShield".getBytes());
            
            //Receiver Multicast Socket to receive acknowledgement from android client.
            
            MulticastSocket multi_sock_receiver=new MulticastSocket(PORT_NUMBER+1);
            
            multi_sock_receiver.joinGroup(InetAddress.getByName("226.0.0.0"));
            
            multi_sock_receiver.setSoTimeout(30000);
            
            multi_sock_receiver.receive(packet);
            
            //Receive the Acknowledgement packet from the android client.
            
            byte[] buffer=packet.getData();
            
            String pr=new String(buffer);
            
            pr=pr.substring(0,pr.indexOf(0));
            
            if(pr.equals("TeclaShield"))
                //Acknowledgement successful;
                fireevent(new WiFiEvent(this,WiFiEvent.CLIENT_FOUND));
            
            System.out.println(pr);
            
            
                try {
                    synchronized(lock){
                    //Wait for the actual tcp connection to be completed and authenticated.    
                    lock.wait();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
                
            }
            
           
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
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
        list.add(WiFiEventListener.class,wifilistener);
    }
    
    public void removeWiFiEventListener(WiFiEventListener wifilistener){
        list.remove(WiFiEventListener.class,wifilistener);
    }
    
    public void fireevent(WiFiEvent ev){
        
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

    public void run() {
        //throw new UnsupportedOperationException("Not supported yet.");
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
                if(serversock==null)
                serversock=new ServerSocket(PORT_NUMBER+2);
                
                client=serversock.accept();
                
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
            
            synchronized(lock){
                //Notify the thread waiting in search_connections that the connection is complete.
                lock.notify();
            }
                
            } catch (IOException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            //set the connection status to true
            connection_status=true;
            
        }
    }
    
}
