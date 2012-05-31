/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.emuwindow;

import com.akdroid.teclasocket.TeclaSocket;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 *
 * @author Akhil
 */
public class TeclaEventListener implements MouseListener{
    Byte mask,state;
    String name;
    JLabel status,switchstate;
    TeclaSocket bt;
    public TeclaEventListener(Byte m,String nm,JLabel st,JLabel by,TeclaSocket ts){
        mask=m;
        name=nm;
        status=st;
        switchstate=by;
        bt=ts;
        state=0x3F;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        
       // throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void mousePressed(MouseEvent e) {
          state=(byte) (state & ~mask);
	  switchstate.setText("switchstate = "+Integer.toHexString(state));
	  status.setText(name + " Pressed ");
	  bt.send(state);      
        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        state=(byte) (state | mask);
	switchstate.setText("switchstate = "+Integer.toHexString(state));
      	status.setText(name + " Released ");
        bt.send(state);
      //  throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
      //  throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
