int ledPin = 13; // choose the pin for the LED
int inPin = 2;   // choose the input pin (for a pushbutton)
int val = 1;     // variable for reading the pin status
int preVal = 1;  // previous pin status
char c;        // reading data from serial port
void LEDcontrol(int a) {
     Serial.print(a);
}
void setup() {
  Serial.begin(115200);
  pinMode(ledPin, OUTPUT);  // declare LED as output
  pinMode(inPin, INPUT);    // declare pushbutton as input
}

void loop(){
  val = digitalRead(inPin);  // read input value
  if (val != preVal) {         // check if the input status is changed)
   LEDcontrol(val);
  } 
  preVal = val;
  if(Serial.available()){//check serial port is available
    c=Serial.read();// read data from serial port and save in c
   //if recieve data is 0 then write hight to ledPin
  if(c=='0')
    digitalWrite(ledPin, HIGH);
  if(c=='1')
    digitalWrite(ledPin,LOW);
  }
}
