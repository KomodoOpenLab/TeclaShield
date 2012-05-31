/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teclaemulator;
import com.akdroid.emuwindow.EmuWindow;
import com.akdroid.teclasocket.*;
/**
 *
 * @author Akhil
 */
public class server extends Thread implements Runnable{
    TeclaSocket tm;
    public static final String uuidstring="0000110100001000800000805F9B34FB";
    public static final String name="SPP";
    EmuWindow win;
    Pinger pin;
    public server(EmuWindow w){
        tm=null;
        win =w;
        
    }
    
    public TeclaSocket getSocket(){
        return tm;
    }

    @Override
    public void run() {
        win.setStatus("Waiting for a TeclaShield to connect");
        win.setState("0x3f");
        tm=new TeclaSocket(uuidstring,name,true,false);
        win.setsocket(tm);
        win.setStatus("Connected To TeclaShield ");
        pin = new Pinger(tm);
        pin.start();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
