/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Akhil
 */
public class WiFiSocket {
    
    int type;
    Socket sock;
    ObjectInputStream in;
    ObjectOutputStream out;
    ServerSocket serversock;
    boolean connection_status;
    public static final String SERVER_NAME = "localhost";
    public static final int[] PORT_NUMBER = {4444,0}; //any free port is used
    public static final int SOCKET_SERVER = 0;
    public static final int SOCKET_CLIENT = 1;
    
    public WiFiSocket(int type_){
       type=type_;
       connection_status=false;
       if(type==SOCKET_SERVER){
            try {
                serversock=new ServerSocket(PORT_NUMBER[0]);
            } catch (IOException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       if(type==SOCKET_CLIENT){
            try {
                sock=new Socket(SERVER_NAME,PORT_NUMBER[0]);
            } catch (UnknownHostException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
    }
    public WiFiSocket(int type_,int portnum){
        type=type_;
        if(type==SOCKET_SERVER){
         try {
                serversock=new ServerSocket(portnum);
            } catch (IOException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       if(type==SOCKET_CLIENT){
            try {
                sock=new Socket(SERVER_NAME,portnum);
            } catch (UnknownHostException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
    public void connect(){
        
    }
    public void disconnect(){
        
    }
    public void server_connect(){
        if(type!=SOCKET_SERVER)return;
        try {
            sock=serversock.accept();
            in=new ObjectInputStream(sock.getInputStream());
            out=new ObjectOutputStream(sock.getOutputStream());
            
        } catch (IOException ex) {
            Logger.getLogger(WiFiSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void client_connect(int timeout_ms){
        if(type!=SOCKET_CLIENT)return;
        //sock.connect(sock.g, timeout_ms);
    }
}
