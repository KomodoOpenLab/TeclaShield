
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * EventGenerator generates Computer Events using the Robot class.
 * 
 * @author Akhil
 */
public class EventGenerator {
    
     Robot robot;
     int screenwidth=0,screenheight=0;

     public EventGenerator(){
         /*
          * Initializes the robot and obtains screen dimensions 
          * for limiting mouse movement.
          */
                 
        try {
            robot=new Robot(); //initialize robot
            
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            Dimension dim=toolkit.getScreenSize();
            
            screenwidth=dim.width;          //screen width
            screenheight=dim.height;        //screen height
            
        } catch (AWTException ex) {
            Logger.getLogger(EventGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     public void enter_delay(int ms_){
         /*
          * Introduces delay of ms_ units in ms .
          */
         robot.delay(ms_);
     }
     
     public void interpret(ComEvent ev){
         /*
          * Interprets ComEvent ev and accordingly 
          * fires the corresponding Computer Event 
          */
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
                 case EventConstant.MIDDLEBUTTON:
                     fireMiddleButton();
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
         /*
          * fires mouse left click
          */
         robot.mousePress(InputEvent.BUTTON1_MASK);
         robot.mouseRelease(InputEvent.BUTTON1_MASK);
     }
     public void fireRightClick(){
         /*
          * fires mouse Right click
          */
         robot.mousePress(InputEvent.BUTTON3_MASK);
         robot.mouseRelease(InputEvent.BUTTON3_MASK);
     }
     public void fireMiddleButton(){
         robot.mousePress(InputEvent.BUTTON2_MASK);
         robot.mouseRelease(InputEvent.BUTTON2_MASK);
     }
     public void fireDblClick(){
         /*
          * fires mouse left double click
          */
         fireMouseClick();
         fireMouseClick();
     }
     public void fireMouseMove(int dx,int dy){
         /*
          * moves the mouse pointer by dx and dy
          * within the screen limits.
          * dx = +ve -> Right
          * dx = -ve -> Left
          * dy = +ve -> Down
          * dy = -ve -> Up
          *   
          */
         //get curent mouse pointer location
         int x=MouseInfo.getPointerInfo().getLocation().x;
         int y=MouseInfo.getPointerInfo().getLocation().y;
         //move the pointer within the limits.
         if(x+dx>0&&x+dx<=screenwidth)x=x+dx;
         if(y+dy>0&&y+dy<=screenheight)y=y+dy;
         robot.mouseMove(x,y);
     }
     public void fireMouseScroll(int units){
         /*
          * generates mouse scroll for "units"
          * units = +ve scrollwheel away from the user.
          * units = -ve scrollwheel towards the user.
          */
         robot.mouseWheel(units);
     }
     public void fireKeyPress(ArrayList<Integer> keyvalues){
         /*
          * fires keypress and keyrelease of the values so as to
          * generate key combination events like Alt+Tab stored in 
          * the arraylist keyvalues.
          */
         int i;

         for(i=0;i<keyvalues.size();i++){
            if(keyvalues.get(i)!=0)
             robot.keyPress(keyvalues.get(i));
         }
         for(i=keyvalues.size()-1;i>=0;i--){
            if(keyvalues.get(i)!=0)
             robot.keyRelease(keyvalues.get(i));
         }
     }
     
}
