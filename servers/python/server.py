# -*- coding: utf-8 -*-
import time
from bluetooth import *

server_socket=BluetoothSocket( RFCOMM )

server_socket.bind(('', PORT_ANY))
server_socket.listen(1)

advertise_service(server_socket, "SPP", service_classes=[SERIAL_PORT_CLASS], profiles=[SERIAL_PORT_PROFILE])

client_socket, addr = server_socket.accept()

print 'Accepted connection from ', addr

c = 0
while True:
	client_socket.send(chr(c))
	time.sleep(1)
	c += 1
	if c == 256:
		c = 0
client_socket.close()
