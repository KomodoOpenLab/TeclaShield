int ledPin = 13; // choose the pin for the LED
int inPin = 2;   // choose the input pin (for a pushbutton)
int val = 0;     // variable for reading the pin status
int preVal = 0;  // previous pin status
void LEDcontrol(int a) {
     digitalWrite(ledPin, !a);  // turn LED ON/OFF
     Serial.print(a);
}
void setup() {
  Serial.begin(115200);
  pinMode(ledPin, OUTPUT);  // declare LED as output
  pinMode(inPin, INPUT);    // declare pushbutton as input
}

void loop(){
  val = digitalRead(inPin);  // read input value
  if (val != preVal) {         // check if status changed(pushed or released)

   LEDcontrol(val);
  } 
  preVal = val;
}
