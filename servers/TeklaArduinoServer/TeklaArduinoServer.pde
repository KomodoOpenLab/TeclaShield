/*
 This example shows how to detect when a button changes from off to on
 and on to off.
 	
 created  27 Sep 2005
 modified 30 Dec 2009
 by Tom Igoe
 	
 modified for mEADL 25 May 2010
 by Jorge Silva, 3 June 2010 by Zongyi Yang
 
 */

int switchState;         // current state of the button
int prevSwitchState;     // previous state of the button
int inByte;              // input Byte from serial port
int filterCounter;       // Filter counter
boolean filterStarted;   // Did the switches state change?

void setup() {
  // initialize pins 0 to 3 as inputs
  // without changing the direction of pins we don't care about
  DDRB = DDRB & 0xF0;
  // Set switch state variables
  switchState = readSwitches();
  prevSwitchState = switchState;
  filterStarted = false;
  // Init serial communication:
  Serial.begin(115200);
}

int readSwitches() {
  return PINB & 0x0F;
}

void loop() {

  // if incoming byte
  if (Serial.available() > 0) {
    // get incoming byte:
    inByte = Serial.read();
    // echo incoming byte:
    Serial.write(inByte);
  }
  
  switchState = readSwitches();

  if (switchState != prevSwitchState) {
    // if the state has changed...
    // Reset filter counter
    filterStarted = true;
    filterCounter = 0;
    // Save the state for next loop cycle
    prevSwitchState = switchState;
  }
  
  if (filterStarted) {
    delay(1);
    filterCounter++;
    if (filterCounter == 75) {
      // If filter counter expired:
      if (Serial.available() <= 0) {
        Serial.write(switchState);
      }
      filterStarted = false;
    }
  }
}
