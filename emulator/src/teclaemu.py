# -*- coding: utf-8 -*-
import time
from bluetooth import *
from threading import Thread
#
# Tecla Emulator v0.2
# written by Akhil Rao
#

## {{{ http://code.activestate.com/recipes/134892/ (r2)
class _Getch:
    """Gets a single character from standard input.  Does not echo to the
screen."""
    def __init__(self):
        try:
            self.impl = _GetchWindows()
        except ImportError:
            self.impl = _GetchUnix()

    def __call__(self): return self.impl()


class _GetchUnix:
    def __init__(self):
        import tty, sys

    def __call__(self):
        import sys, tty, termios
        fd = sys.stdin.fileno()
        old_settings = termios.tcgetattr(fd)
        try:
            tty.setraw(sys.stdin.fileno())
            ch = sys.stdin.read(1)
        finally:
            termios.tcsetattr(fd, termios.TCSADRAIN, old_settings)
        return ch


class _GetchWindows:
    def __init__(self):
        import msvcrt

    def __call__(self):
        import msvcrt
        return msvcrt.getch()


getch = _Getch()
## end of http://code.activestate.com/recipes/134892/ }}}

def sendEvent(event):
	client_socket.send(chr(keyvalue[event]))

def listenkeys():
	auto_release = True;
	exitflag=False;
	while not exitflag :
		print ("Your switch action w/s/a/d/1/2/r?");
		c = getch();
		c = c.upper();
		if(c in "WSAD12QHRT"):
			#Valid key pressed
			if(c in "HTQ"):
				#Command key pressed
				if c == "H": #Help
					print helpstring
				if c == "T": #Help
					auto_release = not auto_release;
					if auto_release:
						print "Auto-release ON"
					else:
						print "Auto-release OFF"
				if c == "Q": #Help
					exitflag= True;
					print keymessage[c]
					
			if(c in "WSAD12R"):
				#Switch key pressed
				sendEvent(c)
				print keymessage[c];
				if auto_release:
					time.sleep(0.1) #Connection seems to have long latency
					sendEvent('R')
		else:
			#Invalid key pressed
			print "Invalid value"

version = "v0.2"
print 'Tecla - Emulator ' , version ;
print 'press h/H for list of possible commands'
print 'press q/Q to quit'
print 'Auto switch release mode set to True'

#setup the bluetooth RFComm socket i.e bind and start listening
server_socket = BluetoothSocket( RFCOMM )
server_socket.bind(('', 1))
server_socket.listen(1)

#uuid can be used if bluetooth client side program is written to minimize discovery and filtering time
uuid = "00001101-0000-1000-8000-00805F9B34FB"
advertise_service(
	server_socket,
	"SPP",
	uuid,
	service_classes=[SERIAL_PORT_CLASS],
	profiles=[SERIAL_PORT_PROFILE]
	)

print 'Waiting for a device ........'
client_socket, addr = server_socket.accept()
print 'Accepted connection from ', addr

#Keyboard 
#ECU1 -> Highlight next 
#ECU2 -> Highlight prev
#ECU3 -> Cancel
#ECU4 -> select highlighted
#
#SP1 -> Select highlighted
#SP2 -> Cancel

#keyvalue is the dictionary used for holding character input from keyboard and corresponding value of the byte to be sent....
keyvalue={
	"R":0x3F,		#Released all switches
	"W":0x3E,		#ECU1 pressed
	"S":0x3D,		#ECU2 pressed
	"A":0x3B,		#ECU3 pressed
	"D":0x37,		#ECU4 pressed
	"1":0x2F,		#Switch Port 1 pressed
	"2":0x1F,		#Switch Port 2 pressed
	"Q":0x88,		#Quit the emulator
	"H":0x77,		#Show commands list
	"T":0x99,		#Toggle release event mode
	}

keymessage= {
	"R":"All Switches Released ",
	"W":"ECU1 pressed",
	"S":"ECU2 pressed",
	"A":"ECU3 pressed",
	"D":"ECU4 pressed",
	"1":"Switch Port 1 pressed",
	"2":"Switch Port 2 pressed",
	"Q":"Quitting the emulator"
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

main_thread= Thread(target=listenkeys);
main_thread.start();
while main_thread.isAlive():
	data_in=client_socket.recv(1)
	if(len(data_in) > 0):
		client_socket.send(data_in)

client_socket.close()
server_socket.close()
print "Disconnected"
