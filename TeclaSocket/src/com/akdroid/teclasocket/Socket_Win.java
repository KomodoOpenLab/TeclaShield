/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;


import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * contains Windows Specific Socket Implementation
 * @author Akhil
 */
public class Socket_Win implements Communication,DiscoveryListener{
    public  UUID uuid; //it can be generated randomly
    public  String name ;                       //the name of the service
    public  String url  ;
    LocalDevice local ;
    DiscoveryAgent dagent;
    RemoteDevice main_list[];
    int currentDevice=0;
    StreamConnectionNotifier server ;
    StreamConnection conn ;
    DataInputStream datain;
    DataOutputStream dataout;
    boolean result=false;
    Socket_Win(String uid){
        try {
            local=LocalDevice.getLocalDevice();
            dagent=local.getDiscoveryAgent();
            uuid=new UUID(uid,false);
            search(uid);
        } catch (BluetoothStateException ex) {
            Logger.getLogger(Socket_Win.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean send(Character b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte recieve() {
        Byte ch = '0';
        try {
            while(datain.available()>0);
            ch=datain.readByte();
            System.out.println("In recieve");
            System.out.println(""+ch);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return ch;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean search(String UDID) {
        try {
             dagent.startInquiry(DiscoveryAgent.GIAC,this);
             
        } catch (BluetoothStateException ex) {
            Logger.getLogger(Socket_Win.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
//        throw new UnsupportedOperationException("Not supported yet.");
        
    }

    public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
        
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void servicesDiscovered(int i, ServiceRecord[] srs) {
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public void serviceSearchCompleted(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void inquiryCompleted(int i) {
        System.out.println("Device Search Completed");
        connect();
        // throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean connect(){
        main_list=dagent.retrieveDevices(DiscoveryAgent.CACHED);
        int i;
        result=false;
        String connstring;
        for(i=0;i<main_list.length;i++){
          RemoteDevice temp=main_list[i];
          
            try {
                String name = temp.getFriendlyName(true);
                System.out.println(temp.toString()+" "+name);
                if(name.contains("Tecla")||name.contains("Tekla"))
                {
                    connstring=dagent.selectService(uuid,ServiceRecord.AUTHENTICATE_ENCRYPT,false);
                    if(connstring != null)
                    {
                        conn = (StreamConnection)Connector.open(connstring);
                        dataout=conn.openDataOutputStream();
                        datain=conn.openDataInputStream();
                        result=true;
                        break;
                    }
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(Socket_Win.class.getName()).log(Level.SEVERE, null, ex);
            }
          
        }
        return result;
    }
    public boolean isConnected(){
        return result;
    }
}
