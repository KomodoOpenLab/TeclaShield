/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;

import java.util.EventListener;

/**
 *
 * @author akhil
 */
public interface WiFiEventListener extends EventListener {
    
    public void onClientFound();
    public void onClientConnected();
    public void onClientDisconnected();
    public void onNoClientFound();
    public void onReceived(String data);
    public void onSent();
    
}
