TeclaClient v1.0

Summary:
 
	TeclaClient is an open source desktop accessibility application which allows a TeclaShield user to use desktop computers running Windows and Linux. The application is distributed under GPL and MITL.The TeclaShield users can control their Windows PC or Linux Box using their TeclaShield when an instance of this application is running on the desktop.The application provides for a Graphical User Interface (GUI) which allows the user to assign a particular Computer event to a particular TeclaShield switch event.
This application can connect to a TeclaShield over Bluetooth or through an TeclaShield connected android device over WiFi.

Features:

1)Generate Keyboard and Mouse events like mouse click or keypress using Teclashield allowing full control over desktop.
2)Can be completely configured by user.
3)Dictate text on the desktop using Google Voice Actions o the android phone.

Authors:

1) Akhil Rao
2) Ankit Daftery
3) Jorge Silva

License: 

1)General Public License 
2)MIT License
3)The complete license can be found in the same directory as "Licensing.txt"

Installation:

Windows: 

Requirements: 

1)JRE 1.6 and above

2)If Bluetooth is used , use WidComm stack (the driver that windows installs automatically).This application doesn't work with BlueSoleil bluetooth stack.

3)Disable the virtualbox host as this will prevent android device from connecting to the desktop.


Installation procedure:

1)Run the TeclaClient-v1.0-setup.exe. You can download it from here.


Linux:

Requirements: 

1) bluez-libs v3.7 or later 
	(a file or symlink to libbluetooth.so.3 or libbluetooth.so.2 called "libbluetooth.so" in "/usr/lcoal" directory)
2) JRE 1.6 or above

Installation procedure:

//To be added
//


Uninstallation instructions:

1) "Start->TeclaClient->Uninstall TeclaClient" will uninstall TeclaClient in Windows.

2) //"Linux to be added"


Important Instructions:

1) Allow the application to communicate over firewall. Add a rule to allow both udp and tcp ports 28195-28197 to send and receive.

2) VirtualBox Host should not be enabled to allow connecting to android device in windows.

3) In order to connect to an android device the password should match the password on the android device.You can set the password by selecting the set password button on the GUI and on the android device by opening "TeclaAccess -> Desktop Settings -> Set Password".

It is not recommended to change the password.This is to be used only when multiple servers i.e multiple desktops are running this application and to differentiate between these desktops this password can be used.(May not work reliably).

4) The switch events on the GUI are not mutually exclusive. e.g Pressing and Releasing a switch results in the events onPress , onRelease and onClick.











