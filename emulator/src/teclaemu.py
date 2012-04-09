# -*- coding: utf-8 -*-
import time
from bluetooth import *
from threading import Thread
#
# Tecla Emulator v0.2
# written by Akhil Rao
#
#Keyboard 
#J1 -> Highlight next 
#J2 -> Highlight prev
#J3 -> Cancel
#J4 -> select highlighted
#
#SW1 ->Select highlighted
#SW2 ->Cancel
version = "v0.2"

print 'Tecla - Emulator ' , version ;
print 'press h/H for list of possible commands'
print 'press q/Q to quit'
print 'Auto switch release mode set to True'

#setup the bluetooth RFComm socket i.e bind and start listening
server_socket=BluetoothSocket( RFCOMM )
server_socket.bind(('', 1))
server_socket.listen(1)

#uuid can be used if bluetooth client side program is written to minimize discovery and filtering time
uuid = "00001101-0000-1000-8000-00805F9B34FB"
advertise_service(server_socket, "TeclaShield",uuid, service_classes=[SERIAL_PORT_CLASS], profiles=[SERIAL_PORT_PROFILE])

print 'Waiting for a device ........'

client_socket, addr = server_socket.accept()

print 'Accepted connection from ', addr

#keyvalue is the dictionary used for holding character input from keyboard and corresponding value of the byte to be sent....

keyvalue={ "w":0x01,		#Joystick 1 asserted
	    "W":0x3E,
	    "s":0x02,		#Joystick 2 asserted
	    "S":0x3D,
	    "a":0x04,		#Joystick 4 asserted
	    "A":0x0B,
	    "d":0x08,		#Joystick 3 asserted
	    "D":0x07,		
	    "1":0xC0,		#Switch 1 asserted
	    "2":0x20,		#Switch 2 asserted
	    "q":0x88, 
	    "Q":0x88,		#Quit the emulator
	    "h":0x77,
	    "H":0x77,		#Show command lists
	    "r":160,		
	    "R":160,		#Release switch event
	    "t":0x99,
	    "T":0x99,		#Toggle release event mode
	    }

keymessage= {
	    "w":"Event on Joystick 1 generated",		#Joystick 1 asserted
	    "W":"Event on Joystick 1 generated",
	    "s":"Event on Joystick 2 generated",		#Joystick 2 asserted
	    "S":"Event on Joystick 2 generated",
	    "d":"Event on Joystick 4 generated",		#Joystick 4 asserted
	    "D":"Event on Joystick 4 generated",
	    "a":"Event on Joystick 3 generated",		#Joystick 3 asserted
	    "A":"Event on Joystick 3 generated",		
	    "2":"Event on Switch Port 2 generated",		#Switch 2 asserted
	    "1":"Event on Switch Port 1 generated",		#Switch 1 asserted
	    "q":"Quitting the emulator", 
	    "Q":"Quitting the emulator",
	    "r":"Switch Released ",
	    "R":"Switch Released "				#Switch Released
	    }
	    
helpstring = "\nw/W => Generate Event on Joystick 1"
helpstring=helpstring  + "\ns/S => Generate Event on Joystick 2 " 
helpstring=helpstring  + "\na/A => Generate Event on Joystick 3 "
helpstring=helpstring  + "\nd/D => Generate Event on Joystick 4 "
helpstring=helpstring  + "\n1 => Generate Event on Switch Port 1" 
helpstring=helpstring  + "\n2 => Generate Event on Switch Port 2"
helpstring=helpstring  + "\nh/H => view possible commands"
helpstring=helpstring  + "\nr/R => Generate release switch event"
helpstring=helpstring  + "\nt/T => Toggle auto switch release event"
helpstring=helpstring  + "\nq/Q => Quit "
helpstring=helpstring  + "\n\n\n#####Auto switch release mode is a mode in which switch release events are inserted after every switch event \nDefault set to true.\nCan be turned on or off by command t/T"	     

def listenkeys():
    exitflag=False;
    auto_release_mode=True;
    while not exitflag :
	 c=raw_input("\n Your switch action w/s/a/d/1/2 ?");
	#if(c== "w" or c=="a" or c=="s" or c=="d" or c=="1" or c=="2" or c=="q" or c=="h" or c== "W" or c=="A" or c=="S" or c=="D" or c=="Q" or c=="H"):
	 if(len(c) == 1 and c in "wWsSaAdDqQhHrRtT12"): 
	    if(not c in "qQhHtTrR"):
	      
	      client_socket.send(chr(keyvalue[c]))
	      time.sleep(0.1)
	    #  client_socket.send(chr(0xFF - keyvalue[c]))
	   #   time.sleep(0.5)
	      print "\n", keymessage[c];
	      time.sleep(0.5)
	      if auto_release_mode :
		  if c == "1":
		    client_socket.send(chr(0x10))
		  else:
		    client_socket.send(chr(0xC0))	      
		  time.sleep(0.5)
	      else:
		time.sleep(1);
	    if (not auto_release_mode) and keyvalue[c]== 0xC0:
	      client_socket.send(chr(keyvalue[c]))
	      print "\n", keymessage[c];
	      time.sleep(1)
	    if keyvalue[c]== 0x77:
		print "\n ",helpstring ;
	    if keyvalue[c] == 0x88:
		exitflag= True;
		print "\n",keymessage[c];
	    else:
		exitflag = False  
	    if(keyvalue[c]== 0x99):
		auto_release_mode=not auto_release_mode;
		print "\n auto release mode set to ", auto_release_mode;
	 else:
	      print("\ninvalid value");
	
	  
thr= Thread(target=listenkeys);
thr.start();
while thr.isAlive():
	a=client_socket.recv(1000)
	if(len(a) == 1):
	  client_socket.send(a)
	  time.sleep(0.1)

print "Disconnected"
client_socket.close()
server_socket.close()
