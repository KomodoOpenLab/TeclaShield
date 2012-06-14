/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tecletest;
import com.akdroid.teclasocket.BluetoothEventListener;
import com.akdroid.teclasocket.TeclaSocket;
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
        TeclaSocket sock=new TeclaSocket(uuid);
        byte ch=0;
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
        
        
    }
}
