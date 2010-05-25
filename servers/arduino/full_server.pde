/*
 This example shows how to detect when a button changes from off to on
 and on to off.
 	
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for mEADL 25 May 2010
 by Jorge Silva
 
 */

// Variables will change:
int switchState = 0x0F;         // current state of the button
int lastSwitchState = 0x0F;     // previous state of the button

void setup() {
  // initialize pins 0 to 3 as inputs
  // without changing the direction of pins we don't care about
  DDRB = DDRB & 0xF0;
  // initialize serial communication:
  Serial.begin(115200);
}


void loop() {
  // read the pushbutton input pin:
  switchState = PINB & 0x0F;

  // compare the switchState to its previous state
  if (switchState != lastSwitchState) {
    // if the state has changed, send to serial port
    Serial.write(switchState);

    // save the current state as the last state, 
    //for next time through the loop
    lastSwitchState = switchState;
  }
  
}