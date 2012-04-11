/**
 *
 * @author Rishabh
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.bluetooth.*;
import javax.microedition.io.*;

public class Emulator {
    
    private final UUID uuid = new UUID("27012f0c68af4fbf8dbe6bbaf7aa432a", false);    
    private final String name = "TeclaShield";
    private final String url = "btspp://localhost:" + uuid
                                + ";name=" + name
                                + ";authenticate=false;encrypt=false;";
    private LocalDevice localDevice = null;
    private StreamConnectionNotifier server = null;
    private StreamConnection conn = null;
    private InputStream din = null;
    private OutputStream dout = null;
    private Map<String,Integer> dict;
    private BufferedReader reader = null;
    private String helpString = "";
    private PingThread pinging;
    private boolean auto_release = true;

    public Emulator(){
        dict = new HashMap<String,Integer>();
        dict.put("W",0x3E);
        dict.put("S", 0x3D);
        dict.put("A", 0x3B);
        dict.put("D", 0x37);
        dict.put("R", 0x3F);
        pinging = new PingThread();
        auto_release = true;
        reader = new BufferedReader(new InputStreamReader(System.in));
        helpString += 
                "**********HELP**********\n"
                +"w/W : ECU1 pressed\n"
                +"s/S : ECU2 pressed\n"
                +"a/A : ECU3 pressed\n"
                +"d/D : ECU4 pressed\n"
                +"1 : Switch Port 1 pressed\n"
                +"2 : Switch port 2 pressed\n"
                +"h/H : View possible commands\n"
                +"r/R : Generate realease switch event\n"
                + "t/T : Toggle auto switch release event\n"
                + "q/Q : Quit\n"
                + "\n\n#####Auto switch release mode is a mode in which switch release events are inserted after every switch event\n"
                + "Default set to true.\n"
                + "Can be turned on or off by command t/T\n"
                +"************************";
        System.out.println(helpString);
    }
    
    private boolean discover(){
        try {
            localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
            server = (StreamConnectionNotifier) Connector.open(url);
            System.out.println("Waiting for connection...");
            conn = server.acceptAndOpen();
            System.out.println("Device connected...");
            din = conn.openInputStream();
            dout = conn.openOutputStream();
        } catch (Exception ex) {
            System.out.println("An error occured while connecting to the device");
            return false;
        }
        return true;
    }
    
    private boolean sendKeyEvent(int keyEvent){
        try {
            synchronized(dout){
                System.out.println("writing : " + keyEvent);
                dout.write(keyEvent);
                if(auto_release){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {}
                    dout.write(dict.get("R"));
                }
            }
        } catch (IOException ex) {
            System.out.println("An error occured while sending to the connected device");
            return false;
        }
        return true;
    }
    
    class PingThread extends Thread{

        private boolean end = false;

        @Override
        public void run() {
            end = false;
            while(!end){
                try {
                    try {
                        this.sleep(100);
                    } catch (InterruptedException ex) {}
                    int read = din.read();
                    if(read != -1)
                    synchronized(dout){
                        dout.write(read);
                    }
                } catch (IOException ex) {
                    System.out.println("An error occured while pinging the connected device, disconnecting");
                    disconnect();
                    System.exit(0);
                }
            }
        }
        
        public void end(){
            end = true;
        }

    };
    
    private void disconnect(){
        try {
            din.close();
        } catch (Exception ex) {}
        try {
            dout.close();
        } catch (Exception ex) {}
        try {
            server.close();
        } catch (Exception ex) {}
    }

    private void startEmulator(){
        if(!this.discover()){
            System.out.println("Closing the emulator");
            System.exit(0);
        }
        pinging.start();
        this.listenKeys();
    }
    
    private void listenKeys(){
        System.out.println("Enter the keystroke (h for help): ");
        while(true){
            char ans = 'h';
            try {
                String g = reader.readLine();
                ans = (g.equals(""))?' ':g.charAt(0);
            } catch (IOException ex) {
                System.out.println("An internal error occured, exiting");
                pinging.stop();
                disconnect();
                System.exit(0);
            }
            boolean sendKeyEvent = true;
            if((ans == 'w' || ans == 'W') && dict.containsKey("W")){
                sendKeyEvent = this.sendKeyEvent(dict.get("W"));
            }
            else if((ans == 's' || ans == 'S') && dict.containsKey("S")){
                sendKeyEvent = this.sendKeyEvent(dict.get("S"));
            }
            else if((ans == 'a' || ans == 'A') && dict.containsKey("A")){
                sendKeyEvent = this.sendKeyEvent(dict.get("A"));
            }
            else if((ans == 'd' || ans =='D') && dict.containsKey("D")){
                sendKeyEvent = this.sendKeyEvent(dict.get("D"));
            }
            else if((ans == 'r' || ans =='R') && dict.containsKey("R")){
                sendKeyEvent = this.sendKeyEvent(dict.get("R"));
            }
            else if(ans == '1' && dict.containsKey("1")){
                sendKeyEvent = this.sendKeyEvent(dict.get("1"));
            }
            else if(ans == '2' && dict.containsKey("2")){
                sendKeyEvent = this.sendKeyEvent(dict.get("2"));
            }
            else if(ans == 'h'|| ans =='H'){
                System.out.println(helpString);
            }
            else if(ans == 't' || ans =='T'){
                auto_release = !auto_release;
                if(auto_release)
                    System.out.println("Auto release is now on");
                else
                    System.out.println("Auto release is now off");
            }
            else if(ans == 'q' || ans =='Q'){
                pinging.stop();
                this.disconnect();
                System.exit(0);
            }
            if(!sendKeyEvent){
                System.out.println("An unknown error occured while sending this keystroke to the connected device, exiting");
                pinging.stop();
                this.disconnect();
                System.exit(0);                
            }
        }
    }
    
    public static void main(String[] args){
        Emulator emulator = new Emulator();
        emulator.startEmulator();
    }
}
