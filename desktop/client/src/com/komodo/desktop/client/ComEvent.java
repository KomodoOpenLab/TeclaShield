/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import java.util.ArrayList;

/**
 * ComEvent represents the computer event that needs to be fired.
 * An instance of this class will be given to the EventGenerator class 
 * which will then fire an appropriate event.
 * @author Akhil
 */
public class ComEvent {

    public int eventno; //ShieldEvent id.
    
    public int device;  //device event to fire none,mouse or keyboard
    
    public ArrayList<Integer> values; //The value if the event like the keycode
                                      //or the index of the mouseevents list
                                      //Arraylist so that keycombinations could be fired.
    
    public int dx;                    //Optional for many events,Horizontal change value in X 
    public int dy;                    // and Y for mouse change,dx is also used for storing 
                                      // scrolling amount.For other events its irrelevant. 
    
    public ComEvent(){
        
    }
    public ComEvent(int eventtype,String value,int event_no,int options[]){
        /*
         * initializes the ComEvent
         */
        device=eventtype;
        values=new ArrayList<Integer>();
        int start=0,end;
        
        String temp=value.concat("");
        while(start<temp.length()){
            temp=temp.substring(start);
            end=temp.indexOf(',');
            values.add(Integer.parseInt(temp.substring(0, end)));
            start=end+1;
            
        }
        dx=options[0];
        dy=options[1];
        eventno=event_no;
    }
}
