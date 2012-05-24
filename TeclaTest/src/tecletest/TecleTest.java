/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tecletest;
import com.akdroid.teclasocket.*;
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
        
        while (ch!=0x3E)
        ch=sock.recieve();
        
        System.out.println("Terminated Reception");
        
        
    }
}
