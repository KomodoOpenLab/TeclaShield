/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;

import java.util.EventObject;

/**
 * This class will define the events that occur 
 * for a bluetooth implemetation
 * e.g. on a byte received so that event driven model can be used
 * 
 * @author Akhil
 */
public class BluetoothEvent extends EventObject {
    public int event_id; 
    public static final int BLUETOOTH_CONNECT = 1;
    public static final int BLUETOOTH_DISCONNECT = 2;
    public static final int BLUETOOTH_SENT = 3;
    public static final int BLUETOOTH_RECEIVE = 4;
    public BluetoothEvent(Object source,int event){
        super(source);
        event_id=event;
    }
    
}
