#! /user/bin/python
# -*- coding: utf-8 -*-

# mEADL, mobile Electronic Aids for Daily Living
# Copyright (c) 2009, University of Toronto
# scyp@atrc.utoronto.ca
# http://scyp.atrc.utoronto.ca/projects/meadl

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#
# Authors: Jingqi Liu, Jorge Silva


import os
from bluetooth import *

### Use this code to scan for devices ###
#print "Looking for bluetooth devices in range..."
#nearby_devices = discover_devices(lookup_names = True)
#print "found %d devices" % len(nearby_devices)
#for name, addr in nearby_devices:
#	print "%s - %s" % (addr, name)
#########################################

def UI_Print(msg):
	print msg
	cmd = 'espeak -w temp.wav "' + msg + '"'
	os.system(cmd)
	os.system('mplayer temp.wav') # Better for full Linux distros
	#os.system('play-sound temp.wav')  # Best for Maemo

UI_Print('Connecting to bluetooth interface...')
client_socket = BluetoothSocket( RFCOMM )
#client_socket.connect(('00:07:80:82:1E:E6', 1)) #ArduinoBT
client_socket.connect(('00:06:66:02:CB:75', 1)) #BlueSMiRF
UI_Print('Bluetooth interface connection established.')

while 1:
	data = ord(client_socket.recv(1)) # Receive only one byte at a time
	print "0x%0x" % data

client_socket.close()
