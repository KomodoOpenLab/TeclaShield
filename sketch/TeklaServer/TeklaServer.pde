/*
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for Tekla 25 May 2010 by Jorge Silva,
 3 June 2010 by Zongyi Yang
 17 Feb 2011 by Jorge Silva
 
 */

// Variables will change:
byte switchStates = 0x0F;       // current switch states
byte extraState = 0x04;         // extra switch state helper
byte prevSwitchStates = 0x0F;   // previous state of the button
byte mByte;                     // used to store incoming serial port data

void setup() {
  // initialize inputs without changing the direction of pins we don't care about
  DDRB &= 0xE0;
  DDRD &= 0xFB;
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
    switchStates = PINB & 0x1F;  // read first 5 switches
    extraState = PIND & 0x04;    // read sixth switch 
    extraState <<= 3;            // shift from pos 2 to 5
    switchStates |= extraState;  // copy 6th switch to switchStates
    switchStates ^= 0x30;  // toggle bits 4 & 5 (extra switches) for compatibility with older versions
    // compare the switchState to its previous state
    if (switchStates != prevSwitchStates) {
      // if the state has changed, send to serial port
      Serial.write(switchStates);
      // save the current state as the last state for next time through the loop
      prevSwitchStates = switchStates;
    }
  }
}
