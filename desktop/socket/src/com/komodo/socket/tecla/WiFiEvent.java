/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;

import java.util.EventObject;

/**
 * This class describes a WiFi Event like connecting and disconnecting.
 * @author akhil
 */
public class WiFiEvent extends EventObject{

    public static final int CLIENT_FOUND=0; 
    public static final int CLIENT_CONNECTED=1; 
    public static final int CLIENT_DISCONNECTED=2; 
    public static final int CLIENT_RECEIVED=3;
    public static final int CLIENT_SENT=4;
    public static final int NO_CLIENT_FOUND=5;
    public int event_id;
    public WiFiEvent(Object source,int value){
        super(source);
        event_id=value;
    }
}
