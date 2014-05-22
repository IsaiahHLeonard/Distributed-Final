/*
 Simple Arduino server to model/test functionality for a multi-state device.
 We turn on 0 leds for off, 1 led for low, 2 for medium, 3 for high.
 
 */

#include <SPI.h>
#include <Ethernet.h>

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = {
  0xAA, 0x00, 0x00, 0x00, 0x00, 0x16 };

// Initialize the Ethernet server library
// with the IP address and port you want to use
// (port 80 is default for HTTP):
EthernetServer server(23);
//Possible device states
char possStates[]="Off;Low;Medium;High";

//Lights to model device states
int led1 = 7;
int led2 = 5;
int led3 = 6;

void setup() {
  
  //Setup pins and set leds off to start
  pinMode(led1, OUTPUT);
  pinMode(led2, OUTPUT);
  pinMode(led3, OUTPUT);
  digitalWrite(led1, LOW);
  digitalWrite(led2, LOW);
  digitalWrite(led3, LOW);

//setup server
  Serial.begin(9600);
  while (!Serial){ ; }
  Ethernet.begin(mac);
  server.begin();
  Serial.print("Server is at ");
  Serial.println(Ethernet.localIP());
}

void loop() {

  // listen for incoming clients
  EthernetClient client = server.available();
  if (client) {
    Serial.println("new client");
    boolean currentLineIsBlank = true;
    String incoming = "";
    
    //For client, read in incoming message until new line. Then process command and respond appropriately.
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        incoming += c;
        
        Serial.write(c);
        // if you've gotten to the end of the line (received a newline
        // character) and the line is blank
        //Process incoming command
        if (c == '\n') {
          if (incoming.startsWith("setup")){
            client.print("OK: ");
            client.println(possStates);
            Serial.println("SETUP");
          } else if (incoming.startsWith("Off")){
            client.print("OK");
            digitalWrite(led1,LOW);
            digitalWrite(led2,LOW);
            digitalWrite(led3, LOW);
                        Serial.println("Off");

          }else if (incoming.startsWith("Low")){
            client.println("OK");
            digitalWrite(led1,HIGH);
            digitalWrite(led2,LOW);
            digitalWrite(led3, LOW);
                        Serial.println("Low");

          }else if (incoming.startsWith("Medium")){
            client.println("OK");
            digitalWrite(led1,HIGH);
            digitalWrite(led2,HIGH);
            digitalWrite(led3, LOW);
                        Serial.println("Medium");

          } else if (incoming.startsWith("High")){
            client.println("OK");
            digitalWrite(led1,HIGH);
            digitalWrite(led2,HIGH);
            digitalWrite(led3, HIGH);
                        Serial.println("High");

          } else {
            Serial.println("Bad Command");
            client.println("Bad command");
          
          }
          incoming  = "";
          break;
        }
      }
    }
    // give the web browser time to receive the data
    delay(1);
    // close the connection:
    client.stop();
    Serial.println("client disonnected");
  }
}
