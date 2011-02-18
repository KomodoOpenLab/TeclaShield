/*
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for Tekla 25 May 2010 by Jorge Silva,
 3 June 2010 by Zongyi Yang
 17 Feb 2011 by Jorge Silva
 
 */

// Variables will change:
int switchStates = 0x0F;       // current state of the button
int prevSwitchStates = 0x0F;   // previous state of the button
int mByte;                     // used to store incoming serial port data

void setup() {
  // initialize pins 0 to 5 as inputs without changing the direction of pins we don't care about
  DDRB = DDRB & 0xC0;
  // initialize serial communication
  Serial.begin(115200);
}


void loop() {
  // if incoming byte
  if (Serial.available() > 0) {
    // get incoming byte:
    mByte = Serial.read();
    // echo incoming byte:
    Serial.write(mByte);
  } else {
    // read the switch states (6 switches):
    switchStates = PINB & 0x3F;
    //switchStates ^= 0x30;  // toggle bits 4 and 5 (extra switches)
    // compare the switchState to its previous state
    if (switchStates != prevSwitchStates) {
      // if the state has changed, send to serial port
      Serial.write(switchStates);
      // save the current state as the last state for next time through the loop
      prevSwitchStates = switchStates;
    }
  }
}
