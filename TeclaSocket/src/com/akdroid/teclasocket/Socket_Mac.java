/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;

/**
 *
 * @author Akhil
 */
public class Socket_Mac implements Communication {
    public boolean result=false;;
    public Socket_Mac(){
        
    }

    public boolean send(Character b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public boolean isConnected(){
        return result;
    }
    public byte recieve() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean search(String UDID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
