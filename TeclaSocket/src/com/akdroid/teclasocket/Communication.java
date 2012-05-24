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
    public boolean send(Character b);
    public byte recieve();
    public boolean search(String UDID);
    public boolean isConnected();
}
