/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.interfaces;

import java.util.EventListener;

/**
 * This interface is the event listener interface for Shield events
 * The button that is pressed can be obtained by calling 
 * e.getbutton() which will return any one of the constants
 * defined in the ShieldEvent class.
 * @author Akhil
 */
public interface ShieldEventListener extends EventListener {
    
    /*
     * onButtonPressed will be executed if a button is pressed 
     */
    
    public void onButtonPressed(ShieldEvent e); 
    
    /*
     * onButtonReleased will be executed when a button is released.
     */
    
    public void onButtonReleased(ShieldEvent e);
    
    /*
     * onButtonClick will be executed if a button is
     * pressed and released once. i.e. Single Click 
     */
    
    public void onButtonClick(ShieldEvent e);
    
    /*
     * onButtonDblClick will be executed if a button is 
     * double-clicked similar to a mouse.
     */
    
    public void onButtonDblClick(ShieldEvent e);
    
    /*
     * onLongPress will be executed if a button is pressed
     * for a large amount of time.
     */
    
    public void onLongPress(ShieldEvent e);    
    
}
