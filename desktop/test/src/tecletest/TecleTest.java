/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tecletest;
import com.komodo.socket.tecla.BluetoothEventListener;
import com.komodo.socket.tecla.TeclaSocket;
import com.komodo.socket.tecla.WiFiEventListener;
import com.komodo.socket.tecla.WiFiSocket;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Akhil
 */
public class TecleTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String uuid="0000110100001000800000805F9B34FB";
        String dictate="dictate:ashhsak";
        System.out.println(dictate.substring(0,dictate.indexOf(":")));
        System.out.println(dictate.substring(dictate.indexOf(":")+1));
        try {
                Robot rob=new Robot();
                rob.keyPress(KeyEvent.VK_CONTROL);
                rob.keyPress(KeyEvent.VK_V);
                rob.keyRelease(KeyEvent.VK_V);
                rob.keyRelease(KeyEvent.VK_CONTROL);
                
            } catch (AWTException ex) {
                
            }
        
        try {
            final WiFiSocket sock=new WiFiSocket("Tecla123");
            sock.addWiFiEventListener(new WiFiEventListener(){

                @Override
                public void onClientFound() {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onClientConnected() {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onClientDisconnected() {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onNoClientFound() {
                    System.out.println("Not found");
                    sock.search_connections();
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onReceived(String data) {
                    System.out.println("received"+data);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onSent() {
                   // throw new UnsupportedOperationException("Not supported yet.");
                }
            });
            while(!sock.connection_status)
            {
                sock.search_connections();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TecleTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                sock.read();
            }
            /*byte ch=0;
            System.out.println("Connecting .....");
            while(!sock.isConnected());
            System.out.println("Connected...");
            Receiver r=new Receiver(sock);
            sock.addBluetoothEventListener(new BluetoothEventListener(){

                @Override
                public void onConnect() {
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onDisconnect() {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onReceive(DataInputStream datain) {
                    try {
                        Byte b=datain.readByte();
                        System.out.println("Byte= "+ b.toString());
                        //throw new UnsupportedOperationException("Not supported yet.");
                    } catch (IOException ex) {
                        Logger.getLogger(TecleTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void onSent() {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }
                
            });
            r.start();
            
            try {
                System.in.read();
            } catch (IOException ex) {
                Logger.getLogger(TecleTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            r.end();
            System.out.println("Terminated Reception");
            
            */
        } catch (IOException ex) {
            //Logger.getLogger(TecleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
