##Tecla Emulator version 1.1##

###Written and Submitted by Akhil Rao aka akdroid###

Tecla Emulator is built for emulation of Tecla Shield a device by Komodo Open Lab used for remote access.More details on [http://komodoopenlab.com/](http://komodoopenlab.com/)

The emulator is basically a python script and requires [Pybluez](http://code.google.com/p/pybluez/) for accessing bluetooth (NB: GNU/Linux and Windows Only).

###Important###
In order to use Tecla Emulator name your bluetooth device with TeclaShield prefix in order that TeclaAccess App can connect to the emulator.

The Emulator is known give issues with certain versions of bluez on Ubuntu.
The following versions are known to work :

bluez-4.99
obexd-0.45
bluez-hcidump-2.3 (Don't think if this is needed, but anyway )


###How to run:###

1. Download teclaemu.py
2. Open Terminal and cd to the directory where you downloaded the python script.
3. type the following command `python teclaemu.py`

###List of Commands###

Keys | Function
--- | -------------------------
w/W | Generate Event on Jumper 1
s/S | Generate Event on Jumper 2 
a/A | Generate Event on Jumper 3 
d/D | Generate Event on Jumper 4 
1   | Generate Event on Switch Port 1
2   | Generate Event on Switch Port 2
h/H | View possible commands
r/R | Generate Switch Release Event
t/T | Toggle Auto Switch Release Mode 
q/Q | Quit 

###Notes###

1. Developers can adapt this script by changing the values of keys:bytevalue in the python dictionary - keyvalue as per their requirement
2. The current set of values works as per the Tecla Access App available in the andorid market.
3. The Keyboard may not function properly as of now.
4. Auto Switch Release Mode is a mode in which switch release events are also generated after a switch event is triggered.
When turned off,release event will have to be inserted manually by command r/R.Default value is true(on).When true(on)
release event cannot be sent using r/R command.  
