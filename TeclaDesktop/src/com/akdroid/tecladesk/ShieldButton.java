/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import org.w3c.dom.Element;

/**
 *
 * @author Akhil
 */
public class ShieldButton {
    
    public int buttonid;
    public ComEvent eventlist[];
    public ShieldButton(int number,Element button){
        buttonid=number;
        Element event=(Element)button.getFirstChild();
        int j=0;
        eventlist=new ComEvent[5];
        try{
        while(j<5){
            int event_no=Integer.parseInt(event.getAttribute("eventid"));
            int eventtype=Integer.parseInt(event.getAttribute("device"));
            String value=event.getAttribute("value");
            String option=event.getAttribute("options");
            //System.out.println(option);
            int[] options=new int [2];
            options[0]=Integer.parseInt(option.substring(0, option.indexOf(',')));
            options[1]=Integer.parseInt(option.substring( option.indexOf(',')+1));
            eventlist[j]=setComEvent(eventtype,value,event_no,options);
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
