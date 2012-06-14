# -*- coding: utf-8 -*-
import time
from bluetooth import *
import threading

#
# Tecla Emulator v1.2
# written by Akhil Rao
# Extended by Ankit Daftery
#

## {{{ http://code.activestate.com/recipes/134892/ (r2)
## Modified to support non-blocking I/O
class _Getch:
	"""Gets a single character from standard input. Does not echo to the
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
		from select import select		# Using select for non-blocking I/o
		fd = sys.stdin.fileno()
		old_settings = termios.tcgetattr(fd)
		try:
			tty.setraw(sys.stdin.fileno())
			[i, o, e] = select([sys.stdin.fileno()], [], [], 5)		# Timeout in 5 seconds if no input
			if i: ch=sys.stdin.read(1)
			else: ch='.'		# Dummy character to avoid KeyError in sendEvent
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


class TeclaThread(threading.Thread):
	"""
	Threaded Class to handle sockets to the TeclaShields
	"""
	def __init__(self,client_socket,address):
		self.client_socket = client_socket
		self.address = address
		self.exitflag=False
		self.c="."								#Null character initialised to avoid transmitting 
		threading.Thread.__init__(self)
	
	def disconnect(self):
		"""
		Close the socket
		"""
		self.client_socket.close()
		self.exitflag=True
	
	def sendEvent(self,event):
		"""
		Transmit a Shield keyevent to TeclaAccess
		"""
		try:
			self.client_socket.send(chr(keyvalue[event]))
		except BluetoothError,err:
			print "Bluetooth Error in TeclaThread",err
	
	def listenkeys(self):
		"""
		Function to generate keyevents based on user input
		"""
		global flag		#Global is necessary to allow thread synchronisation
		self.auto_release = True;
		self.exitflag=False;
		while not self.exitflag :
			print ("Your switch action w/s/a/d/1/2/r?");
			self.c= getch()
			self.c = self.c.upper();
			if(self.c in "WSAD12QHRT"):
				#Valid key pressed
				if(self.c in "HTQ"):
					#Command key pressed
					if self.c == "H": #Help
						print helpstring
					if self.c == "T": #Help
						self.auto_release = not self.auto_release;
						if self.auto_release:
							print "Auto-release ON"
						else:
							print "Auto-release OFF"
					if self.c == "Q": #Hel
						self.disconnect()
						flag=True
						print keymessage[self.c]
				
				if(self.c in "WSAD12R"):
					#Switch key pressed
					self.sendEvent(self.c)
					print keymessage[self.c];
					if self.auto_release:
						time.sleep(0.1) #Connection seems to have long latency
						self.sendEvent('R')
			else:
				if('.' in self.c):
					print "No input received\n"
				#Invalid key pressed
				else:
					print "Invalid value %s"%(self.c)
	
	def run(self):
		"""
		Overriden run function
		"""
		print 'Accepted connection from ', self.address
		self.listenkeys()

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

#keymessage dictionary to notify users of action performed on keypress
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

#Help menu
helpstring = "\nw/W => Generate Event on Joystick 1"
helpstring=helpstring + "\ns/S => Generate Event on Joystick 2 "
helpstring=helpstring + "\na/A => Generate Event on Joystick 3 "
helpstring=helpstring + "\nd/D => Generate Event on Joystick 4 "
helpstring=helpstring + "\n1 => Generate Event on Switch Port 1"
helpstring=helpstring + "\n2 => Generate Event on Switch Port 2"
helpstring=helpstring + "\nh/H => view possible commands"
helpstring=helpstring + "\nr/R => Generate release switch event"
helpstring=helpstring + "\nt/T => Toggle auto switch release event"
helpstring=helpstring + "\nq/Q => Quit "
helpstring=helpstring + "\n\n\n#####Auto switch release mode is a mode in which switch release events are inserted after every switch event \nDefault set to true.\nCan be turned on or off by command t/T"


#Initial notifss
version = "v0.2"
print 'Tecla - Emulator ' , version ;
print 'press h/H for list of possible commands'
print 'press q/Q to quit'
print 'Auto switch release mode set to True'

#setup the bluetooth RFComm socket i.e bind and start listening
server_socket = BluetoothSocket( RFCOMM )
server_socket.bind(('', PORT_ANY))
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

#Global variable to check for quit
flag = False

while (not flag):
	print 'Waiting for a device ........'
	client_socket, address = server_socket.accept()
	t = TeclaThread(client_socket,address)
	t.start()
	while t.isAlive():
		time.sleep(0.1)
		try:
			data_in=t.client_socket.recv(128)
			if(len(data_in) > 0):
				t.client_socket.send(data_in)
		except BluetoothError:
			t.disconnect()
			t.join()
			break

#Clean up
server_socket.close()
client_socket.close()
print "Disconnected"
