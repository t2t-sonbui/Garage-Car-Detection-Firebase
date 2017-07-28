#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <Ultrasonic.h>



#define LED     D0        // Led in NodeMCU at pin GPIO16 (D0).
// Set these to run example.
#define FIREBASE_HOST "garagecardetector.firebaseio.com"
#define FIREBASE_AUTH "TuJmJEc1YJRAOtEEiS1qYxYE2KiqRDaWNkGxyLhm"//change with your Database secrets
#define WIFI_SSID "TuHu"
#define WIFI_PASSWORD "tuhu123456"
#define NODE_NAME "LB Aventador"
#define NODE_KEY "aventador"
#define VEHICLE_RANG 150 //number cm from node to vehicle 
#define GMC_START "{\"time_to_live\":300,\"to\":\"/topics/garages-car-detector\",\"notification\":{\"title\":\"The car state change\",\"body\":\""//change"garages-car-detector" with your topic
#define GMC_END "\"}}"
//for GMC
char API_key[] = "AIzaSyDFfD5v6NSnjXZUdGx9anKc-DPZRKh9hJM";//change with server mgs key
const char* host = "fcm.googleapis.com";
const char* GMC_DATA = "{\"time_to_live\":300,\"to\":\"/topics/garages-car-detector\",\"notification\":{\"title\":\"The car state change\",\"body\":\"testcfsdf\"}}";//Change topic with yours

WiFiClient client;
//
long lastMsg = 0;
Ultrasonic ultrasonic(5, 4); // (Trig PIN,Echo PIN) D1(GPIO5) and D2(GPIO4) in Nodemcu 1.0
long distance;
bool inRange;
bool lastState;
StaticJsonBuffer<150> jsonBuffer;
JsonObject& timestamp = jsonBuffer.createObject();
JsonObject& updateData = jsonBuffer.createObject();
void wifi_smartconfig()
{
  int cnt = 0;
  WiFi.mode(WIFI_STA);
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(LED, LOW);          // turn the LED on.
    Serial.print(".");
    delay(250);
    digitalWrite(LED, HIGH);          // turn the LED off.
    delay(250);
    Serial.print(".");
    if (cnt++ >= 20) {
        digitalWrite(LED, LOW);          // turn the LED on.
        Serial.print(".");
        delay(150);
        digitalWrite(LED, HIGH);          // turn the LED off.
        delay(550);
      
      Serial.print("Smartconfig...");
      WiFi.beginSmartConfig();
      while (1) {
        delay(1000);
        if (WiFi.smartConfigDone()) {
          Serial.println("SmartConfig Success");
          break;
        }
      }
    }
  }

}
void setup() {
  pinMode(LED, OUTPUT);   // LED pin as output.    low to on led, high to off led
  Serial.begin(9600);
//      // connect to wifi.
//      WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
//      Serial.print("connecting");
//      while (WiFi.status() != WL_CONNECTED) {
//        digitalWrite(LED, LOW);          // turn the LED on.
//        Serial.print(".");
//        delay(250);
//        digitalWrite(LED, HIGH);          // turn the LED on.
//        delay(250);
//      }
  

  wifi_smartconfig();
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  timestamp[".sv"] = "timestamp";
  Serial.println();
  timestamp.prettyPrintTo(Serial);
  Serial.println();
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
void updateDistance()
{
  updateData["name"] = NODE_NAME;
  updateData["distance"] = distance;
  updateData["in_range"] = inRange;
  updateData["timestamp"] = timestamp;
  Firebase.set(NODE_KEY, updateData);
  Serial.println();
  Serial.println("Data update");
  updateData.prettyPrintTo(Serial);
  Serial.println();
  if (Firebase.failed()) {
    Serial.print("setting time failed:");
    Serial.println(Firebase.error());
    return;
  }
  digitalWrite(LED, LOW);          // turn the LED on.
  delay(200);
  digitalWrite(LED, HIGH);         // turn the LED off.
  delay(500);
}
//Function for sending the request to GCM
void sendToGCM() {
  Serial.print("connecting to ");
  Serial.println(host);
  if (!client.connect(host, 80)) {
    Serial.println("connection failed");
    return;
  }
  if (client.connected())
  {


    String body;
    body += F(NODE_NAME);
    if (inRange == true)body += F(" is safe");
    else body += F(" is out of range");
    long msg_size = strlen(GMC_START) + body.length() + strlen(GMC_END);

    Serial.println("sending request");
    client.print("POST /fcm/send HTTP/1.1\r\n"); // http POST request
    client.print("Host: fcm.googleapis.com\r\n");
    client.println("User-Agent: Arduino");
    client.print("Authorization:key=");//AIzaSyCvQvI2_NMNmkOEoyMnbaiuHWwsz6b-Hu0
    client.println(API_key);
    client.print("Content-Type: application/json\r\n");
    client.println("Connection: close");
    client.print("Content-length: "); // has to be exactly the number of characters (bytes) in the POST body
    //client.println(strlen(GMC_DATA)); // calculate content-length
    // client.println("");
    // client.print(GMC_DATA);

    client.println(msg_size); // calculate content-length
    client.println("");
    client.print(GMC_START);
    client.print(body);
    client.print(GMC_END);
    delay(10);
    Serial.println("request sent");
    while (client.connected()) {
      String line = client.readStringUntil('\n');
      if (line == "\r") {
        Serial.println("headers received");
        break;
      }
    }
    String line = client.readStringUntil('\n');
    Serial.println("reply was:");
    Serial.println("==========");
    Serial.println(line);
    Serial.println("==========");
  }
}
void loop()
{
  distance = ultrasonic.Ranging(CM);// CM or INC
  Serial.print("Distance:" );
  Serial.print(distance);
  Serial.println(" cm" );
  delay(100);
  if (distance < VEHICLE_RANG)
  {
    inRange = true;
    //recheck distance // anti noise
    for (int i = 0; i < 5; i++)
    {
      distance = ultrasonic.Ranging(CM);// CM or INC
      if (distance >= VEHICLE_RANG)
      {
        inRange = false;
        break;
      }
    }
  }
  if (inRange != lastState)
  {
    Serial.print("In Range:" );
    Serial.print(inRange);
    lastState = inRange;
    updateDistance();
    //Push notifycation
     sendToGCM();


  }
  else
  {
    long now = millis();
    if (now - lastMsg > 30000) {//every 30 seconds
      Serial.print("Update every:" );
      lastMsg = now;
      updateDistance();
    }
  }

}
