/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.teclasocket;

/**
 *
 * @author Akhil
 */
public class Socket_Linux implements Communication {
    public boolean result=false;
    Socket_Linux(){
        
    }
    public boolean isConnected(){
        return result;
    }
    public boolean send(Character b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte recieve() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean search(String UDID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
