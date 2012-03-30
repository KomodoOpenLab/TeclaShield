Tecla Emulator version 1.0
----------------------------------------------------------------------------------------------------------------------------------------------------
Written and Submitted by Akhil Rao aka akdroid

Tecla Emulator is built for emulation of Tecla Shield a device by Komodo Open Lab used for remote access.More details on http://komodoopenlab.com/
----------------------------------------------------------------------------------------------------------------------------------------------------
The emulator is basically a python script and requires Pybluez for accessing bluetooth.

****Important******
In order to use Tecla Emulator name your bluetooth device with TeclaShield prefix in order that TeclaAccess App can connect to the emulator.

How to run:

1)Download teclaemu.py
2)Open Terminal and cd to the directory where you downloaded the python script.
3)type the following command "python teclaemu.py"

List of Commands:

Keys => Function
------------------------------------------------------------------------
w/W => Generate Event on Jumper 1
s/S => Generate Event on Jumper 2 
a/A => Generate Event on Jumper 3 
d/D => Generate Event on Jumper 4 
1 => Generate Event on Switch Port 1
2 => Generate Event on Switch Port 2
h/H => view possible commands
q/Q => Quit 

Notes:

1)Developers can adapt this script by changing the values of keys:bytevalue in the python dictionary - keyvalue as per their requirement
2)The current set of values works as per the Tecla Access App available in the andorid market.
3)The Keyboard may not function properly as of now.
 



