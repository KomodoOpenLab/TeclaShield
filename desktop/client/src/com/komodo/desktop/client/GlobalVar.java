/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ClientMain;

/**
 * GlobalVar is a class that can be used to give function access to certain variables
 * globally.
 * @author akhil
 */
public class GlobalVar {
    /*
     * GlobalVar contains static global vaariables ehich can be used in all functions
     * irrespective of the scope of the variable.
     * Mainly added for simplicity in code.
     */
    
    static public ClientMain client_window_global=null;//should contain the instance of main ClientWindow
    
    static public BluetoothClient btclient_global=null;//should contain the instance of main bluetoothclient
   
   static public AndroidServer android_server=null;
    
    static public PreferencesHandler handler=null;
    public static void setMainWindow(ClientMain client){
       /*
        * sets the client_window_global field with client
        * which should be the instance of client window.
        * It should be called right after initializing ClientMain instance.
        */
       client_window_global=client;
   }
   public static void setbluetoothclient(BluetoothClient bcl){
       /*
        * sets the btclient_global field with client
        * which should be the instance of BluetoothClient.
        * It should be called right after initializing BluetoothClient instance.
        */
       btclient_global=bcl;
   }
   public static void setAndroidServer(AndroidServer server){
       /*
        * sets the client_window_global field with client
        * which should be the instance of client window.
        * It should be called right after initializing ClientMain instance.
        */
       android_server=server;
   }
   public static void setPreferences(PreferencesHandler prefs){
       /*
        * sets the client_window_global field with client
        * which should be the instance of client window.
        * It should be called right after initializing ClientMain instance.
        */
       handler=prefs;
   }
}
