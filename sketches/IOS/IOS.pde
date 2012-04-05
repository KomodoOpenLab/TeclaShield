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

#define DEBOUNCECOUNT 6
#define TIMEOUT1 150
#define TIMEOUT2 300
#define TIMEOUT3 450
#define TIMEOUT4 600
#define TIMEOUT5 1600
#define SCANTIMEOUTMAX 120
#define SCANTIMEOUTMIN 25
#define POKETIMEOUT 200

#define E1MASK 0x10
#define E2MASK 0x20
#define J1MASK 0x01
#define J2MASK 0x02
#define J3MASK 0x04
#define J4MASK 0x08

// Key Scan Codes
#define TOGGLEKEYBOARD 0x08
//#define KEY_HOME 0x01
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
byte e1Released, e2Released, j1Released, j2Released, j3Released, j4Released;
int e1PresdCounter, e2PresdCounter, j1PresdCounter;
int j2PresdCounter, j3PresdCounter, j4PresdCounter;

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
Send HID record for up to two simultaneous key press events
mod: modifiers (e.g., Alt, Ctrl, Shift)
scan_code1: First key pressed
scan_code2: Second key pressed
**/
void keyDown(byte mod, byte scan_code1, byte scan_code2) {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	Serial.write(0x09);
	Serial.write(0x01);
	Serial.write(mod);
	Serial.write(ZERO);
	Serial.write(scan_code1);
	Serial.write(scan_code2);
	writeByte(ZERO,4);
}

/**
Send HID record for key release event
mod: modifiers (e.g., Alt, Ctrl, Shift)
**/
void keyUp(byte mod) {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	Serial.write(0x09);
	Serial.write(0x01);
	Serial.write(mod);
	writeByte(ZERO,7);
}

/**
Send HID record for up to two simultaneous key press and release events
mod: modifiers (e.g., Alt, Ctrl, Shift)
scan_code1: First key pressed
scan_code2: Second key pressed
**/
void keyDownUp(byte mod, byte scan_code1, byte scan_code2) {
	keyDown(mod, scan_code1, scan_code2);
	keyUp(mod);
}

/**
Send a consumer HID press event (e.g., volume up/down, toggle keyboard)
loByte: Least significant byte of consumer record
hiByte: Most significant byte of consumer record
**/
void consumerDown(byte hiByte, byte loByte) {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	writeByte(0x03,2);
	Serial.write(loByte);
	Serial.write(hiByte);
}

/**
Send a consumer HID release event (e.g., volume up/down, toggle keyboard)
**/
void consumerUp() {
	delay(KEYPRESSDELAY);
	Serial.write(0xFD);
	writeByte(0x03,2);
	writeByte(ZERO,2);
}

/**
Send a full consumer HID event (e.g., volume up/down, toggle keyboard)
loByte: Least significant byte of consumer record
hiByte: Most significant byte of consumer record
**/
void consumerDownUp(byte hiByte, byte loByte) {
	consumerDown(hiByte, loByte);
	consumerUp();
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
	e1PresdCounter = 0; e2PresdCounter = 0;
	j1PresdCounter = 0; j2PresdCounter = 0;
	j3PresdCounter = 0; j4PresdCounter = 0;
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

	e1Released = switchState & E1MASK;
	e2Released = switchState & E2MASK;
	j1Released = switchState & J1MASK;
	j2Released = switchState & J2MASK;
	j3Released = switchState & J3MASK;
	j4Released = switchState & J4MASK;

	// process E1 hold timers
	if (!e1Released) {
		e1PresdCounter++;
		if (!skipT1 && (e1PresdCounter > TIMEOUT1)) {
			keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
			skipT1 = true;
		}
		if (!skipT2 && (e1PresdCounter > TIMEOUT2)) {
			consumerDownUp(0x00,TOGGLEKEYBOARD); //Toggle keyboard
			skipT2 = true;
		}
		if (!skipT3 && (e1PresdCounter > TIMEOUT3)) {
			keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			skipT3 = true;
		}
		if (!skipT4 && (e1PresdCounter > TIMEOUT4)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); // VoiceOver Home
			skipT4 = true;
		}
		if (!skipT5 && (e1PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process E2 hold timers
	if (!e2Released) {
		e2PresdCounter++;
		if (!skipT5 && (e2PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process J1 hold timers
	if (!j1Released) {
		j1PresdCounter++;
		if (!skipT1 && (j1PresdCounter > TIMEOUT1)) {
			consumerDownUp(0x00,TOGGLEKEYBOARD); //Toggle keyboard
			skipT1 = true;
		}
		if (!skipT2 && (j1PresdCounter > TIMEOUT2)) {
			keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
			skipT2 = true;
		}
		if (!skipT3 && (j1PresdCounter > TIMEOUT3)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); // VoiceOver Home
			skipT3 = true;
		}
		if (!skipT5 && (j1PresdCounter > TIMEOUT5)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT5 = true;
		}
	}

	// process J2 hold timers
	if (!j2Released) {
		j2PresdCounter++;
		if (!skipT2 && (j2PresdCounter > TIMEOUT2)) {
			keyDownUp(MOD_VO,KEY_H,ZERO); //Home
			skipT2 = true;
		}
		if (!skipT4 && (j2PresdCounter > TIMEOUT4)) {
			keyDownUp(MOD_VO,KEY_S,ZERO); //Toggle speech output
			skipT4 = true;
		}
	}

	// process J4 hold timers
	if (!j4Released) {
		j4PresdCounter++;
		if (!skipT1 && (j4PresdCounter > TIMEOUT1)) {
			keyDownUp(MOD_VO,KEY_DOWN,ZERO); //Rotor next
			skipT1 = true;
		}
	}

	// process J3 hold timers
	if (!j3Released) {
		j3PresdCounter++;
		if (!skipT1 && (j3PresdCounter > TIMEOUT1)) {
			keyDownUp(MOD_VO,KEY_UP,ZERO); //Rotor previous
			skipT1 = true;
		}
	}

	// process state changes
	if ((debounceCounter > DEBOUNCECOUNT) && (switchState != prevSwitchState)) {
		// Switch state changed

		stateChange = switchState ^ prevSwitchState;

		if (!(e1Released & e2Released & j1Released & j2Released & j3Released & j4Released)) {
			// If anything is pressed
			stopScanning();
		}

		if (stateChange & E1MASK) { // E1 changed
			if (e1Released) {
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
				e1PresdCounter = 0;
			}
		}

		if (stateChange & E2MASK) { // E2 changed
			if (e2Released) {
				if (!skipT1) { // was short press
					keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
				}
				clearSkipTs();
				e2PresdCounter = 0;
			}
		}

		if (stateChange & J1MASK) { // J1 changed
			if (j1Released) {
				if (!skipT1) { // was short press
					keyDownUp(ZERO, KEY_UP, KEY_DOWN); //Select
				}
				clearSkipTs();
				j1PresdCounter = 0;
			}
		}

		if (stateChange & J2MASK) { // J2 changed
			if (j2Released) {
				if (!skipT1) { // was short press
					keyDownUp(ZERO,KEY_ESC,ZERO); //Escape
				}
				clearSkipTs();
				j2PresdCounter = 0;
			}
		}

		if (stateChange & J4MASK) { // J4 changed
			if (j4Released) {
				if (!skipT1) { // was short press
					keyDownUp(MOD_VO, KEY_NEXT, ZERO); //Focus next
				}
				clearSkipTs();
				j4PresdCounter = 0;
			}
		}

		if (stateChange & J3MASK) { // J3 changed
			if (j3Released) {
				if (!skipT1) { // was short press
					keyDownUp(MOD_VO, KEY_PREVIOUS, ZERO); //Focus previous
				}
				clearSkipTs();
				j3PresdCounter = 0;
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


