/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;

/**
 *
 * @author Akhil
 */
public interface Communication {
    public void send(Byte b);
    public void receive();
    public void scan_devices();
    public boolean isConnected();
}
