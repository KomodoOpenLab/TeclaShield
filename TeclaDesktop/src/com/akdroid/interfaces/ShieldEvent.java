/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.interfaces;

import java.util.Date;
import java.util.EventObject;

/**
 *
 * @author Akhil
 */
public class ShieldEvent extends EventObject {
    
    //Constants depicting different buttons/ports possible from the TeclaShield
    
    //ECU Port
    public static final int ECU1 = 0;
    public static final int ECU2 = 1;
    public static final int ECU3 = 2;
    public static final int ECU4 = 3;
    
    //Switch Ports
    
    public static final int E1 = 4;
    public static final int E2 = 5;
    
    //Event Constants
    
    public static final int EVENT_PRESSED = 0;
    public static final int EVENT_RELEASED = 1;
    public static final int EVENT_CLICK = 2;
    public static final int EVENT_DOUBLECLICK = 3;
    public static final int EVENT_LONGPRESS = 4;
    
    public static final int DOUBLECLICK_DELAY=150;
    public static final int LONG_DELAY =2000;
    
    // button identification field
    int button_id ;
    //event identification field
    int event_id;
    
    long timestamp;
    public ShieldEvent(Object source,int id,int event_){
        super(source);
        button_id=id;
        event_id=event_;
        timestamp=new Date().getTime();
    }
    
    public int getbutton(){
        return button_id;
    }
    
    public int getevent(){
        return event_id;
    }
    public long gettimestamp(){
        return timestamp;
    }
    public boolean equals(ShieldEvent e){
        if(button_id==e.getbutton()&&event_id==e.getevent())
           return true;
        else
            return false;
    }
}
