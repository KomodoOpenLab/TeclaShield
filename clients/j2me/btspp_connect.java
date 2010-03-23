/**********************************************************************

    OpenEADL is a collection of open source applications that may be
    used to extend the functionality of powered-wheelchairs and
    electronic aids for daily living (EADL).

    Copyright (C) 2008 by University of Toronto

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
    02110-1301, USA.

**********************************************************************/

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;


public class btspp_connect extends MIDlet implements CommandListener {
    
    private Display mDisplay;
    private Command exitCmd;
    private Command connectCmd = new Command("Connect", Command.ITEM, 1);
    private Form mForm;
    
    StreamConnection conn       = null;
    InputStream      is         = null;
    OutputStream     os         = null;
    String           message    = null;
    
    /*
     * initial a form to show the recieve data on the screen
     * we need push connect button to start the connection with microcontroller
     * 
     */
    public void startApp() {
        if ( mForm == null ) {
            mForm = new Form( "ArduinoBT connect!" );
            exitCmd = new Command( "Exit", Command.EXIT, 0 );
            mForm.addCommand( exitCmd );
            mForm.addCommand( connectCmd );
            mForm.setCommandListener( this );
            mDisplay = Display.getDisplay( this );
        }  
        mDisplay.setCurrent( mForm );
    }
    
    public void pauseApp() {
    }
    
    
    public void destroyApp( boolean unconditional ) {
    }
    
    //start the connection with microcontroller and record it
    public void commandAction( Command c, Displayable s ) {  
        if ( c == exitCmd ) {
            destroyApp( true );
            notifyDestroyed();
        } else {
            try {
              //build connection with microcontroller
              //000780821EE6 is microcontoller'mac address 	
              conn = (StreamConnection) Connector.open ("btspp://000780821EE6:1", 
            		  Connector.READ_WRITE);
              //infinite loop keep reading and sending data
              //because its inifinte loop so we can only terminate it
              //by pushing "back" button 3 seconds
              while(true){  
                is = conn.openInputStream();
                byte buffer[] = new byte[1];//a buffer save sending/recieving data
                is.read(buffer);
                message = new String(buffer);
                mForm.append(new StringItem(null, "\n" + message));
                os = conn.openOutputStream();
                os.write(buffer);
              }
                
              
            } catch (IOException io) {
                // handle exception
                mForm.append(new StringItem(null, "\n" + "Error"));
                
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (IOException ignored) {}
                }
                
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignored) {}
                }
                
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {}
                }
                
            }
            // --------------------------------------------------      
        }
    }  
}
 
