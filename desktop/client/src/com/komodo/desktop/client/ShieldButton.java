/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import org.w3c.dom.Element;

/**
 * ShieldButton represents one of the six buttons present on the TeclaShield.
 * it has an event list which contains the ComEvent for every ShieldEvent.
 * The array subscript denotes the ShieldEvent id.
 * @author Akhil
 */
public class ShieldButton {
    
    public int buttonid; //button number
    public ComEvent eventlist[]; //eventlist containing ComputerEvents info to be 
                                 //generated on the occurence of the event
    public ShieldButton(int number,Element button){
        /*
         * Create a shieldbutton and initialize all its ComEvents.
         */
        buttonid=number;
        
        Element event=(Element)button.getFirstChild(); //get first ShieldEvent
        int j=0;
        eventlist=new ComEvent[5];
        try{
        while(j<5){
            //Obtain event number and device
            int event_no=Integer.parseInt(event.getAttribute("eventid"));
            int eventtype=Integer.parseInt(event.getAttribute("device"));
            
            //The value of event to be fired
            String value=event.getAttribute("value");
            //The options of a ComEvent
            String option=event.getAttribute("options");
            
            int[] options=new int [2];
            //Pasring the options from String to Integer 
            options[0]=Integer.parseInt(option.substring(0, option.indexOf(',')));
            options[1]=Integer.parseInt(option.substring( option.indexOf(',')+1));
            
            //Initialize ComeEvent with the given event id
            eventlist[event_no]=setComEvent(eventtype,value,event_no,options);
            
            //Get next ShieldEvent.
            event=(Element)event.getNextSibling();
            j++;
        }
        }catch(NumberFormatException e){
            System.out.println(e.getLocalizedMessage());
        }
    }
    
    public ComEvent setComEvent(int eventtype,String value,int event_no,int options[]){
        return new ComEvent(eventtype,value,event_no,options);
    }
    
    
    
}
