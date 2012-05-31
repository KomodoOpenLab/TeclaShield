/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teclaemulator;
import com.akdroid.emuwindow.EmuWindow;
/**
 * This is the TeclaEmulator for both Windows and Fedora
 * Requirements 
 * the bluecove dlls required in Windows JRE folder.
 * libbluetooth.so in "/usr/lib/"
 * @author Akhil
 */
public class TeclaEmulator {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        EmuWindow win=new EmuWindow();
        win.setTitle("TeclaEmulator");
        win.setVisible(true);
        // TODO code application logic here
    }
}
