//#include <WiFiClient.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL345_U.h>
#include <math.h>

//variables
Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(12345);
double startX, startY, currX, currY;
boolean initMeasure = false;
boolean isParking = false;
boolean isClientConnected = false;
const char *accessPointName = "Park-Safely AP";
const char *accessPointPass= "01234567";
ESP8266WebServer accessPointServer(80);

//Set the range to whatever is appropriate for your project 
void chooseRange(int range)
{
  switch(range)
  {
    case 16:
    {
      accel.setRange(ADXL345_RANGE_16_G);
      Serial.println("chooseRange()succeeded");
      break;
    }
    case 8:
    {
      accel.setRange(ADXL345_RANGE_8_G);
      Serial.println("chooseRange()succeeded");
      break;
    }
    case 4:
    {
      accel.setRange(ADXL345_RANGE_4_G);
      Serial.println("chooseRange()succeeded");
      break;
    }
    case 2:
    {
      accel.setRange(ADXL345_RANGE_2_G);
      Serial.println("chooseRange()succeeded");
      break;
    } 
  }
}

void ConnectedOff()
{
  isClientConnected = false;
  Serial.println("client disconnected");
  accessPointServer.send(200, "text/html", "DONE");
}

void ConnectedOn()
{
   isClientConnected = true;
   Serial.println("client connected");
   accessPointServer.send(200, "text/html", "DONE");
}

void updateAccessPointDetails()
{
  if (!server.hasArg("plain"))
  { //Check if body received
    server.send(200, "text/plain", "Body not received");
    return;
  }
 
  String message = "Body received:\n";
  message += server.arg("plain");
  message += "\n";
 
  server.send(200, "text/plain", "DONE");
  Serial.println(message);
  //TODO
}

void startDetection()
{
  isParking = true;
  accessPointServer.send(200, "text/html", "DONE");
  Serial.println("Detection started");
}

void endDetection()
{
  isParking = false;
  accessPointServer.send(200, "text/html", "DONE");
  Serial.println("Detection ended");
}

void printAccelDate(sensors_event_t event)
{
  //Display the results (acceleration is measured in m/s^2)
  Serial.print("X: ");
  Serial.print(event.acceleration.x);
  Serial.print("  ");
  Serial.print("Y: "); 
  Serial.print(event.acceleration.y); 
  Serial.print("  ");
  Serial.print("Z: "); 
  Serial.print(event.acceleration.z); 
  Serial.print("  ");
  Serial.println("m/s^2 ");
}

void runDetection()
{
    //Get a new sensor event
    sensors_event_t event;
    accel.getEvent(&event);
    printAccelDate(event);
    if(!initMeasure)
    {
      startX = event.acceleration.x;
      startY = event.acceleration.y;
      initMeasure = true;
    }

    currX = event.acceleration.x;
    currY = event.acceleration.y;
    
    if(abs(startX-currX) >= 2.00)
    {
      Serial.println("X change!!");
    }
    if(abs(startY-currY) >= 2.00)
    {
      Serial.println("Y change!!");
    }
}

void setup(void) 
{
  Serial.begin(9600);

  //Initialise the access point
  WiFi.softAP(accessPointName, accessPointPass);
  accessPointServer.on("/start_detection", startDetection);
  accessPointServer.on("/end_detection", endDetection);
  accessPointServer.on("/update_access_point_details", updateAccessPointDetails);
  accessPointServer.on("/connected_on", ConnectedOn);
  accessPointServer.on("/connected_off", ConnectedOff);
  accessPointServer.begin();

  //Initialise the sensor
  if(!accel.begin())
  {
    Serial.println("Ooops, no ADXL345 detected ... Check your wiring!");
    while(1);
  }
  chooseRange(16);

  delay(3000);
  Serial.println("Setup is done");
  if(!isClientConnected)
  {
    Serial.println("no client connected");
  }
}

void loop(void) 
{  
  accessPointServer.handleClient();
  
  if(isClientConnected && isParking)
    runDetection();

  /*dont remove
  if(clientConnected)
  {
    
    while(Serial.available()>0)
    {
      char ch = Serial.read();
      str += ch;
      delay(5);
      Serial.write('x');
    }
  }
  yossi the gay*/
      
  delay(500);
}
