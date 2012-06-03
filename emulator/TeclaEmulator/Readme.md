#Tecla Emulator GUI  

The Tecla Emulator GUI is a GUI tool for emulation of TeclaShield for development purposes. The project has been developed in *Netbeans* IDE and also uses the TeclaSocket library to interface bluetooth on the device.

##Requirements

This emulator is known to run on Windows and Linux. The emulator has been tested on Fedora 16 and Windows 7.There may be problems when using it in Ubuntu.If someone can test,let me know the results or if possible edit this Readme.In all cases,for the emuator to connect to the Tecla Access app , you should (Bluetooth) name your device with a prefix **TeclaShield**.The android device (or test device) should be paired with your host computer which is running the emulator.


###Windows

* The bluetooth libraries require you to have few .dll files in the folder   
*C:/Program Files/Java/jrex/bin/* 

     the dll files can be downloaded from :

     [bluecovedlls_zip](http://github.com/AKdroid/Tecla-emu/downloads)

* You should install **default bluetooth drivers** and not **bluesoleil** as it is known to give problems.

* To change the device name  
  Device manager -> Bluetooth radios -> Adapter ->Change Properties -> Advanced   
and then change the name with a prefix **TeclaShield**

###Linux 

* The only requirement other than the prefix is libbluetooth.so to     be present in   
    */usr/lib/*
  
* If it is not present you can follow the instructions given here
    [bluecove-gpl] (http://bluecove.org//bluecove-gpl/index.html) 

##Usage Notes

1. TeclaShield Hardware Specs can be found here:   
    [TeclaShield Hardware Specs] (http://komodoopenlab.com/tecla/developers/tecla-for-device-manufacturers/)

2. All button names are self explanatory  
3. A Button press is equivalent to active state of that particular switch.
4. A Button Release is equivalent to inactive state i.e release event of that particular switch.  
5. Connect/Disconnect button has not been tested yet but should work properly.
