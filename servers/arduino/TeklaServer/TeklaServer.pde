/*
 This example shows how to detect when a button changes from off to on
 and on to off.
 	
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for mEADL 25 May 2010 by Jorge Silva,
 3 June 2010 by Zongyi Yang
 19 Nov 2010 by Jorge Silva
 
 */

// Variables will change:
int switchState = 0x0F;      // current state of the button
int lastSwitchState = 0x0F;  // previous state of the button
int flipped;                 // helper to flip extra switch bit
int inByte;

void setup() {
  // initialize pins 0 to 4 as inputs
  // without changing the direction of pins we don't care about
  DDRB = DDRB & 0xE0;
  // initialize serial communication:
  Serial.begin(115200);
}


void loop() {
  // if incoming byte
  if (Serial.available() > 0)
  {
    // get incoming byte:
    inByte = Serial.read();
     Serial.write(inByte);
  }
  
  // read the switch states (5 switches):
  switchState = PINB & 0x1F;
  // flipping 5th switch for compatibility with previous versions
  flipped = !switchState | 0x0F;
  switchState = switchState & flipped;

  // compare the switchState to its previous state
  if (switchState != lastSwitchState) {
    // if the state has changed, and no incoming byte, send to serial port
    if (Serial.available() <= 0)
    {
      Serial.write(switchState);
      // save the current state as the last state, 
      //for next time through the loop
      lastSwitchState = switchState;
    }
  }
}
