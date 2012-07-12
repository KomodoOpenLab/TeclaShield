/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.interfaces;

import java.util.Date;
import java.util.EventObject;

/**
 * ShieldEvent represents the possible events that TeclaShield can generate
 * @author Akhil
 */
public class ShieldEvent extends EventObject {
    
    /*
     * ShieldEvents are fired as
     * Basic Events:
     * 
     * OnPress   -> Fired when any of the buttons pressed.
     * 
     * OnRelease -> Fired when any of the buttons released.
     * 
     * Special Events:
     * 
     * OnClick    -> fired when a button is pressed and released and if same event
     *               doesn't occur in DOUBLECLICK_DELAY,the event is fired
     * 
     * OnDblClick -> fired when a button is pressed twice and released in a row 
     *               with a delay less than DOUBLECLICK_DELAY
     * 
     * OnLongPress ->fired when a button is released after being pressed for a 
     *               time more than LONG_DELAY
     * 
     */
    
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
        timestamp=new Date().getTime();//Time at which event occured
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
