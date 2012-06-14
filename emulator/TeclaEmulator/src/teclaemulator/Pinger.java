/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teclaemulator;

import com.akdroid.teclasocket.TeclaSocket;

/**
 *
 * @author Akhil
 */
public class Pinger extends Thread implements Runnable{
    
    TeclaSocket sock;
    boolean flag;
    public Pinger(TeclaSocket t){
        sock=t;
        flag=true;
    }
    @Override
    public void run() {  //starts receiving in a separate thread
        while(flag)
            sock.receive();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void end(){   //end reception
        flag=false;
    }
    
}
