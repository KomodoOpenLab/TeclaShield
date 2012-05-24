/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;

/**
 * This class will be responsible for making and managing sockets
 * Bluetooth Socket or Wi-fi Socket
 * the socket will thus be used to connect to TeclaShield 
 * All the OS Specific code will thus go into project
 * @author Akhil
 */
public class TeclaSocket implements Communication {
    public static final int OS_WINDOWS = 0;
    public static final int OS_LINUX = 1;
    public static final int OS_MAC = 2;
    //UUID values
    public static final String uuidstring="00001101-0000-1000-8000-00805F9B34FB";
    //if a uuid list is required.
    public int system_os;
    Socket_Win win;
    Socket_Linux linux;
    Socket_Mac mac;
    
    
    public TeclaSocket(String uuidname){
        String os_name=System.getProperty("os.name");
        os_name=os_name.toUpperCase();
        if(os_name.contains("WINDOWS")){
            system_os=OS_WINDOWS;
            win=new Socket_Win(uuidname);
        }
        else if(os_name.contains("LINUX")){
            linux=new Socket_Linux();
            system_os=OS_LINUX;
        }
        else if(os_name.contains("MAC"))
        {
            system_os=OS_MAC;
            mac=new Socket_Mac();
        
        }
            
    }

    public boolean send(Character b) {
        switch(system_os){
            case OS_WINDOWS:
                win.send(b);
                break;
            case OS_LINUX:
                linux.send(b);
                break;
            case OS_MAC:
                mac.send(b);
                break;
        }
            
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte recieve() {
        Byte ch=0;
        switch(system_os){
            case OS_WINDOWS:
                ch=win.recieve();
                System.out.println("in win recieve");
                break;
            case OS_LINUX:
                ch=linux.recieve();
                break;
            case OS_MAC:
                ch=mac.recieve();
                break;
        }
        return ch;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean search(String UDID) {
        switch(system_os){
            case OS_WINDOWS:
                win.search(UDID);
                break;
            case OS_LINUX:
                linux.search(UDID);
                break;
            case OS_MAC:
                mac.search(UDID);
                break;
        }
        return false;
        // throw new UnsupportedOperationException("Not supported yet.");
    }
   public boolean isConnected(){
        switch(system_os){
            case OS_WINDOWS:
                return win.isConnected();
               
            case OS_LINUX:
               return  linux.isConnected();
               
            case OS_MAC:
               return  mac.isConnected();      
        }
        return false;
    }
   
}
