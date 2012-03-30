# -*- coding: utf-8 -*-
import time
from bluetooth import *
from threading import Thread
#
# Tecla Emulator v1.0 
# written by Akhil Rao
#
version = "v1.0"

print 'Tecla - Emulator ' , version ;
print 'press h/H for list of possible commands'
print 'press q/Q to quit'
#setup the bluetooth RFComm socket i.e bind and start listening

server_socket=BluetoothSocket( RFCOMM )

# server_socket is the socket for bluetooth set to RFCOMM 

server_socket.bind(('', 1))

server_socket.listen(1)

uuid = "00001101-0000-1000-8000-00805F9B34FB"

#uuid can be used if bluetooth client side program is written to minimize discovery and filtering time

advertise_service(server_socket, "TeclaShield",uuid, service_classes=[SERIAL_PORT_CLASS], profiles=[SERIAL_PORT_PROFILE])

client_socket, addr = server_socket.accept()

print 'Accepted connection from ', addr

#keyvalue is the dictionary used for holding character input from keyboard and corresponding value of the byte to be sent....

keyvalue={ "w":0x01,		#Joystick 1 asserted
	    "W":0x01,
	    "s":0x02,		#Joystick 2 asserted
	    "S":0x02,
	    "d":0x08,		#Joystick 4 asserted
	    "D":0x08,
	    "a":0x04,		#Joystick 3 asserted
	    "A":0x04,		
	    "2":0x10,		#Switch 2 asserted
	    "1":0x20,		#Switch 1 asserted
	    "q":0x88, 
	    "Q":0x88,		#Quit the emulator
	    "h":0x77,
	    "H":0x77,		#Show command lists
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
	    "Q":"Quitting the emulator"	
	    }
	    
helpstring = "\nw/W => Generate Event on Joystick 1"
helpstring=helpstring  + "\ns/S => Generate Event on Joystick 2 " 
helpstring=helpstring  + "\na/A => Generate Event on Joystick 3 "
helpstring=helpstring  + "\nd/D => Generate Event on Joystick 4 "
helpstring=helpstring  + "\n1 => Generate Event on Switch Port 1" 
helpstring=helpstring  + "\n2 => Generate Event on Switch Port 2"
helpstring=helpstring  + "\nh/H => view possible commands"
helpstring=helpstring  + "\nq/Q => Quit "
	     
def listenkeys():
    exitflag=False;
    while not exitflag :
	c=raw_input("\n Your switch action w/s/a/d/1/2 ?");
	if len(c) > 1:
	      print("\ninvalid value");
	if(c== "w" or c=="a" or c=="s" or c=="d" or c=="1" or c=="2" or c=="q" or c=="h" or c== "W" or c=="A" or c=="S" or c=="D" or c=="Q" or c=="H"):
	    if(c != "q" and c !="h" and c!= "Q" and c!= "H"):
	      client_socket.send(chr(keyvalue[c]))
	      print "\n", keymessage[c];
	      time.sleep(1)
	    if(keyvalue[c] == 0x77):
		print helpstring
	    if keyvalue[c] == 0x88:
		exitflag= True;
		print "\n",keymessage[c];
	    else:
		exitflag = False  
	else:
	      print("\ninvalid value");
	
	  
thr= Thread(target=listenkeys);
thr.start();
while thr.isAlive():
	client_socket.send(chr(0x70))
	time.sleep(1)

client_socket.close()
