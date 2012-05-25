/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tecletest;
import com.akdroid.teclasocket.TeclaSocket;
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
        
        while (ch!=0x3E)  //press ECU1 for exit
        ch=sock.receive();
        
        System.out.println("Terminated Reception");
        
        
    }
}
