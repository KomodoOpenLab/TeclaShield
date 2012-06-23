/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import java.util.ArrayList;

/**
 *
 * @author Akhil
 */
public class ComEvent {
    public int eventno;
    public int device;
    public ArrayList<Integer> values;
    public int dx;
    public int dy;
    public ComEvent(){
        
    }
    public ComEvent(int eventtype,String value,int event_no,int options[]){
        device=eventtype;
        values=new ArrayList<Integer>();
        int start=0,end;
        while(start<value.length()){
            end=value.indexOf(',');
            System.out.println(Integer.parseInt(value.substring(start, end)));
            values.add(Integer.parseInt(value.substring(start, end)));
            start=end+1;
        }
        dx=options[0];
        dy=options[1];
        eventno=event_no;
    }
}
