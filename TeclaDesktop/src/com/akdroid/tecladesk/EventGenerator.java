/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
/**
 *
 * @author Akhil
 */
public class EventGenerator {
     Robot robot;
     File config;
     String Filelocation="";
     public EventGenerator(String filelocation){
        Filelocation=filelocation;
        config=new File(filelocation);
        try {
            robot=new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(EventGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     public void enter_delay(int ms){
         robot.delay(ms);
     }
     public void generate_event(int device){
         
     }
}
