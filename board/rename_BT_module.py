#! /user/bin/python
# -*- coding: utf-8 -*-

# Tekla, tools for alternative access to mobile devices
# Copyright (c) 2009-2011, Inclusive Design Research Centre
# jsilva@ocad.ca
# http://scyp.idrc.ocad.ca/projects/tekla
# Authors: Jingqi Liu, Jorge Silva

import os
from bluetooth import *

version = "TeklaShieldP2"
print "Looking for FireFly module..."
nearby_devices = discover_devices(lookup_names = True)
n = len(nearby_devices)
print "found %d device(s):" % n
i = 0
shieldFound = False;
while ((not shieldFound) and (i < n)):
	candidate = nearby_devices[i]
	address = candidate[0]
	name = candidate[1]
	print str(i+1) + ": " + name
	if (name.find("FireFly") == 0):
		print "Tekla shield found!"
		shieldFound = True
	i = i + 1

if (shieldFound):
	print "Connecting to bluetooth interface..."
	client_socket = BluetoothSocket( RFCOMM )
	client_socket.connect((address, 1))
	print "Bluetooth interface connection established."

	# Enter configuration mode
	client_socket.send("$$$")
	print client_socket.recv(1024).rstrip("\n")

	# Get device name
	client_socket.send("GN\n")
	name = client_socket.recv(1024).rstrip("\n")
	print "Name: %s" % name
	
	# Set new device name
	print "Changing to %s" % name.replace("FireFly",version)
	command = "S-," + version + "\n"
	client_socket.send(command)
	print client_socket.recv(1024).rstrip("\n")
	
	# Exit configuration mode
	client_socket.send("---\n")
	print client_socket.recv(1024).rstrip("\n")

	client_socket.close()
else:
	print "Nothing to do."
