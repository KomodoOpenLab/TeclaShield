/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tecletest;
import com.akdroid.teclasocket.*;
/**
 *
 * @author Akhil
 */
public class Receiver extends Thread implements Runnable {
    TeclaSocket sock;
    boolean runf;
    public Receiver(TeclaSocket t){
        sock=t;
        runf=true;
    }
    
    @Override
    public void run() {
        while(runf)
            sock.receive();
       // throw new UnsupportedOperationException("Not supported yet.");
    }
   
    public void end(){
        runf=false;
    }
}
