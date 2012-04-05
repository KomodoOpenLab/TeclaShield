/*
created  27 Sep 2005
modified 30 Dec 2009 by Tom Igoe

modified for Tekla
25 May 2010 by Jorge Silva,
3 June 2010 by Zongyi Yang
3 May 2011 by Jorge Silva

*/

#define ZERO (uint8_t) 0x00
#define KEYPRESSDELAY 25

#define DEBOUNCECOUNT 8
#define TIMEOUT1 110
#define TIMEOUT2 250
#define TIMEOUT3 400
#define TIMEOUT4 550
#define TIMEOUT5 800
#define SCANTIMEOUTMAX 120
#define SCANTIMEOUTMIN 25
#define POKETIMEOUT 200

#define SP1MASK 0x10
#define SP2MASK 0x20
#define ECU1MASK 0x01
#define ECU2MASK 0x02
#define ECU3MASK 0x04
#define ECU4MASK 0x08

// Key Scan Codes
#define TOGGLEKEYBOARD 0x08
#define KEY_ESC 0x29
#define KEY_NEXT 0x4F
#define KEY_PREVIOUS 0x50
#define KEY_UP 0x52
#define KEY_DOWN 0x51
#define KEY_H 0x0B
#define KEY_S 0x16
#define MOD_VO 0x05

byte scanTimeout;
byte scanCounter, debounceCounter, pokeCounter;
byte switchState, debounceSwitchState, prevSwitchState, stateChange;
byte extraState;
boolean goingNext, pauseScan, first;
boolean skipT1, skipT2, skipT3, skipT4, skipT5;
byte SP1Released, SP2Released, ECU1Released, ECU2Released, ECU3Released, ECU4Released;
int SP1PresdCounter, SP2PresdCounter, ECU1PresdCounter;
int ECU2PresdCounter, ECU3PresdCounter, ECU4PresdCounter;

void stopScanning() {
	pauseScan = true;
	scanCounter = 0;
}

void startScanning() {
	scanTimeout = SCANTIMEOUTMAX;
	pauseScan = false;
}

void clearSkipTs() {
	skipT1 = false;
	skipT2 = false;
	skipT3 = false;
	skipT4 = false;
	skipT5 = false;
}

void reduceScanTimeout() {
	scanTimeout--;
	scanTimeout--;
	scanTimeout--;
	scanTimeout--;
}

void writeByte(byte which, byte how_many_times) {
	byte i = 0;
	do {
		Serial.write(which);
		i++;
	} while (i < how_many_times);
}

/**
Send HID record for up to two simultaneous key press and release events
mod: modifiers (e.g., Alt, Ctrl, Shift)
scan_code1: First key pressed
scan_code2: Second key pressed
**/
void keyDownUp(byte mod, byte scan_code1, byte scan_code2) {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	Serial.write(0x09);
	Serial.write(0x01);
	Serial.write(mod);
	Serial.write(ZERO);
	Serial.write(scan_code1);
	Serial.write(scan_code2);
	writeByte(ZERO,4);
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	Serial.write(0x09);
	Serial.write(0x01);
	Serial.write(mod);
	writeByte(ZERO,7);
}

/**
Send a consumer HID record (e.g., volume up/down, toggle keyboard)
loByte: Least significant byte of consumer record
hiByte: Most significant byte of consumer record
**/
void consumerDownUp(byte loByte, byte hiByte) {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	writeByte(0x03,2);
	Serial.write(loByte);
	Serial.write(hiByte);
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	writeByte(0x03,2);
	writeByte(ZERO,2);
}

/**
Send a null mouse event to keep bluetooth radio awake
**/
void pokeBluetooth() {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	Serial.write(0x05);
	Serial.write(0x02);
	Serial.write(ZERO);
	Serial.write(0x01);
	writeByte(ZERO,2);
}

/**
Read the state of the switches connected to the Shield
**/
void getSwitchState () {
	switchState = PINC & 0x0F;  // read joystick switches
	extraState = PIND & 0x0C;   // read extra switches
	extraState <<= 2;           // shift extra switches to most significant nibble
	switchState |= extraState;  // copy extra switches to switchState
}

/**
Initialize all variables and peripherals
**/
void setup() {
	// initialize inputs without changing the direction of pins we don't care about
	// 0 = input, 1 = output
	DDRC &= 0xF0; // Joystick inputs
	DDRD &= 0xF3; // Extra inputs
	// initialize serial communication
	Serial.begin(115200);
	scanCounter = 0;
	debounceCounter = 0;
	pokeCounter = 0;
	switchState = 0x3F;
	debounceSwitchState = switchState;
	prevSwitchState = switchState;
	extraState = 0x0C;
	first = true;
	goingNext = false;
	pauseScan = true;
	clearSkipTs();
	SP1PresdCounter = 0; SP2PresdCounter = 0;
	ECU1PresdCounter = 0; ECU2PresdCounter = 0;
	ECU3PresdCounter = 0; ECU4PresdCounter = 0;
	scanTimeout = SCANTIMEOUTMAX;
}

void loop() {

	// debounce switch input
	getSwitchState();
	if (first) {
		prevSwitchState = switchState;
		first = false;
	}
	if (switchState == debounceSwitchState) {
		debounceCounter++;
	} else {
		debounceCounter = 0;
		debounceSwitchState = switchState;
	}

	SP1Released = switchState & SP1MASK;
	SP2Released = switchState & SP2MASK;
	ECU1Released = switchState & ECU1MASK;
	ECU2Released = switchState & ECU2MASK;
	ECU3Released = switchState & ECU3MASK;
	ECU4Released = switchState & ECU4MASK;

	// process SP1 hold timers
	if (!SP1Released) {
		SP1PresdCounter++;
		if (!skipT1 && (SP1PresdCounter > TIMEOUT1)) {
			keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
			skipT1 = true;
		}
		if (!skipT2 && (SP1PresdCounter > TIMEOUT2)) {
			consumerDownUp(TOGGLEKEYBOARD,0x00); //Toggle keyboard
			skipT2 = true;
		}
		if (!skipT3 && (SP1PresdCounter > TIMEOUT3)) {
			keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			skipT3 = true;
		}
		if (!skipT4 && (SP1PresdCounter > TIMEOUT4)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); //Home
			skipT4 = true;
		}
		if (!skipT5 && (SP1PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process SP2 hold timers
	if (!SP2Released) {
		SP2PresdCounter++;
		if (!skipT1 && (SP2PresdCounter > TIMEOUT1)) {
			consumerDownUp(TOGGLEKEYBOARD,0x00); //Toggle keyboard
			skipT1 = true;
		}
		if (!skipT2 && (SP2PresdCounter > TIMEOUT2)) {
			keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			skipT2 = true;
		}
		if (!skipT3 && (SP2PresdCounter > TIMEOUT3)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); //Home
			skipT3 = true;
		}
		if (!skipT5 && (SP2PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process ECU1 hold timers
	if (!ECU1Released) {
		ECU1PresdCounter++;
		if (!skipT1 && (ECU1PresdCounter > TIMEOUT1)) {
			consumerDownUp(TOGGLEKEYBOARD,0x00); //Toggle keyboard
			skipT1 = true;
		}
		if (!skipT2 && (ECU1PresdCounter > TIMEOUT2)) {
			keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			skipT2 = true;
		}
		if (!skipT3 && (ECU1PresdCounter > TIMEOUT3)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); //Home
			skipT3 = true;
		}
		if (!skipT5 && (ECU1PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process ECU2 hold timers
	if (!ECU2Released) {
		ECU2PresdCounter++;
		if (!skipT2 && (ECU2PresdCounter > TIMEOUT2)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); //Home
			skipT2 = true;
		}
		if (!skipT4 && (ECU2PresdCounter > TIMEOUT4)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT4 = true;
		}
	}

	// process ECU4 hold timers
	if (!ECU4Released) {
		ECU4PresdCounter++;
		if (!skipT1 && (ECU4PresdCounter > TIMEOUT1)) {
			keyDownUp(MOD_VO,KEY_DOWN,ZERO); //Rotor next
			skipT1 = true;
		}
	}

	// process ECU3 hold timers
	if (!ECU3Released) {
		ECU3PresdCounter++;
		if (!skipT1 && (ECU3PresdCounter > TIMEOUT1)) {
			keyDownUp(MOD_VO,KEY_UP,ZERO); //Rotor previous
			skipT1 = true;
		}
	}

	// process state changes
	if ((debounceCounter > DEBOUNCECOUNT) && (switchState != prevSwitchState)) {

		// Switch state changed
		stateChange = switchState ^ prevSwitchState;

		if (!(SP1Released & SP2Released & ECU1Released & ECU2Released & ECU3Released & ECU4Released)) {
			// If anything is pressed
			stopScanning();
		}

		if (stateChange & SP1MASK) { // SP1 changed
			if (SP1Released) {
				if (!skipT1) { // was short press
					// Toggle scan direction
					if (goingNext) {
						goingNext = false;
						keyDownUp(MOD_VO, KEY_PREVIOUS, ZERO); //Focus previous
					} else {
						goingNext = true;
						keyDownUp(MOD_VO, KEY_NEXT, ZERO); //Focus next
					}
					startScanning();
				}
				clearSkipTs();
				SP1PresdCounter = 0;
			}
		}

		if ((stateChange & SP2MASK) || (stateChange & ECU1MASK)) { // SP2 or ECU1 changed
			if (SP2Released) {
				clearSkipTs();
			} else {
				//SP2 pressed
				SP2PresdCounter = 0;
				keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
			}
			if (ECU1Released) {
				clearSkipTs();
			} else {
				//ECU1 pressed
				ECU1PresdCounter = 0;
				keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
			}
		}

		if (stateChange & ECU2MASK) { // ECU2 changed
			if (ECU2Released) {
				clearSkipTs();
			} else {
				//ECU2 pressed
				ECU2PresdCounter = 0;
				keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			}
		}

		if (stateChange & ECU4MASK) { // ECU4 changed
			if (ECU4Released) {
				if (!skipT1) { // was short press
					keyDownUp(MOD_VO, KEY_NEXT, ZERO); //Focus next
				}
				clearSkipTs();
				ECU4PresdCounter = 0;
			}
		}

		if (stateChange & ECU3MASK) { // ECU3 changed
			if (ECU3Released) {
				if (!skipT1) { // was short press
					keyDownUp(MOD_VO, KEY_PREVIOUS, ZERO); //Focus previous
				}
				clearSkipTs();
				ECU3PresdCounter = 0;
			}
		}

		prevSwitchState = switchState; // save the current state as the last state for next time through the loop
	}

	// process scanning
	if (!pauseScan) scanCounter++;
	if (scanCounter > scanTimeout) {
	scanCounter = 0;
	if (goingNext) {
		keyDownUp(MOD_VO, KEY_NEXT, ZERO); //Focus next
	} else {
		keyDownUp(MOD_VO, KEY_PREVIOUS, ZERO); //Focus previous
	}
	reduceScanTimeout();
	if (scanTimeout < SCANTIMEOUTMIN) {
		scanTimeout = SCANTIMEOUTMIN;
	}
	}

	// process keep bluetooth alive
	pokeCounter++;
	if (pokeCounter > POKETIMEOUT) {
	pokeCounter = 0;
	pokeBluetooth();
	}

	delay(10);
}


