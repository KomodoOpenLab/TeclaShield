/*
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for Tekla
 25 May 2010
 by Jorge Silva,
 3 June 2010
 by Zongyi Yang
 7 Mar 2011
 by Jorge Silva
 
 */

// Variables will change:
byte switchState = 0x0F;       // current switch states
byte extraState = 0x0C;         // extra switch state helper
byte prevSwitchState = 0x0F;   // previous state of the button
byte mByte;                     // used to store incoming serial port data

void setup() {
  // initialize inputs without changing the direction of pins we don't care about
  DDRC &= 0xF0; // Joystick inputs
  DDRD &= 0xF3; // Extra inputs
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
    switchState = PINC & 0x0F;  // read joystick switches
    extraState = PIND & 0x0C;   // read extra switches
    extraState <<= 2;           // shift extra switches to most significant nibble
    switchState |= extraState;  // copy extra switches to switchState
    switchState ^= 0x30;        // toggle bits 4 & 5 (extra switches) for compatibility with older versions
    // compare the switchState to its previous state
    if (switchState != prevSwitchState) {
      // if the state has changed, send to serial port
      Serial.write(switchState);
      // save the current state as the last state for next time through the loop
      prevSwitchState = switchState;
    }
  }
}
