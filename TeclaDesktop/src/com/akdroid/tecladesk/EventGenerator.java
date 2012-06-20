
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Possible event parameters
 * type of event mouse/keyboard
 * event value as in keyvalue or mouse action
 * minimum displacement in case of mouse movement or keyboard keypresses
 * -1 will be the value if not applicable or not assigned
 * @author Akhil
 */
public class EventGenerator {
     Robot robot;
     File config;
     String Filelocation="";
     int screenwidth=0,screenheight=0;

     public EventGenerator(){
        try {
            robot=new Robot();
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            Dimension dim=toolkit.getScreenSize();
            screenwidth=dim.width;
            screenheight=dim.height;        
            
        } catch (AWTException ex) {
            Logger.getLogger(EventGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     public void enter_delay(int ms){
         robot.delay(ms);
     }
     public void generate_event(int device){
         
     }
     public void saveconfig(){

     }
     public void defaultconfig(){
         
     }
     public void interpret(ComEvent ev){
         if(ev.device==EventConstant.MOUSE){
             switch(ev.values.get(0)){
                 case EventConstant.MOUSECLICK:
                     fireMouseClick();
                     break;
                 case EventConstant.DBLCLICK:
                     fireDblClick();
                     break;
                 case EventConstant.MOUSEMOVE:
                     fireMouseMove(ev.dx,ev.dy);
                     break;
                 case EventConstant.RIGHTCLICK:
                     fireRightClick();
                     break;
                 case EventConstant.SCROLL:
                     fireMouseScroll(ev.dx);
                     break;
             }
         }
         else if(ev.device==EventConstant.KEYBOARD){
             fireKeyPress(ev.values);
         }
         
     }
     public void fireMouseClick(){
         robot.mousePress(MouseEvent.BUTTON1);
         robot.mouseRelease(MouseEvent.BUTTON1);
     }
     public void fireRightClick(){
         robot.mousePress(MouseEvent.BUTTON2);
         robot.mouseRelease(MouseEvent.BUTTON2);
     }
     public void fireDblClick(){
         fireMouseClick();
         fireMouseClick();
     }
     public void fireMouseMove(int dx,int dy){
         int x=MouseInfo.getPointerInfo().getLocation().x;
         int y=MouseInfo.getPointerInfo().getLocation().y;
         if(x+dx>0&&x+dx<=screenwidth)x=x+dx;
         if(y+dy>0&&y+dy<=screenheight)y=y+dy;
         robot.mouseMove(x,y);
     }
     public void fireMouseScroll(int units){
         robot.mouseWheel(units);
     }
     public void fireKeyPress(ArrayList<Integer> keyvalues){
         int i=0;
         for(i=0;i<keyvalues.size();i++){
             robot.keyPress(keyvalues.get(i));
         }
         for(i=keyvalues.size()-1;i>=0;i--){
             robot.keyRelease(keyvalues.get(i));
         }
     }
     
}
