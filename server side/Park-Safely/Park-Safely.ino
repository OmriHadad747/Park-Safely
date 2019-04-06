#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include "Accelerometer.h"
#include "FileHandler.h"
#include "Camera.h"

Accelerometer accelerometer;
FileHandler fileHandler;
Camera camera;
ESP8266WebServer APserver(80);
boolean hasNewPhoto = true;

void hasNewPhotos()  //check if there are new photos
{
  if(hasNewPhoto)
    APserver.send(200, "text/html", "YES");
  else
    APserver.send(200, "text/html", "NO");   
}

void clonePhotos()
{
  File file = SD.open("1.jpg");
  if(!file)
    Serial.println("file dont exist");
  int fileSize = file.size();
  Serial.println("file size is: " + String(fileSize));
 
  byte buff[3];
  for(int i=0; i<3; i++)
  {
    if(file.available())
      buff[i] = file.read();

    Serial.println("buff [" + String(i) + "]: " + String(buff[i]));
  }

  WiFiClient client1 = APserver.client();
  if(!client1)
    Serial.println("no client");  
  else
  {
    Serial.println("AP port: " + String(client1.localPort()));
    IPAddress myIP = client1.localIP();
    Serial.println("AP ip: " + myIP.toString());
    
    Serial.println("client port: " + String(client1.remotePort()));
    IPAddress himIP = client1.remoteIP();
    Serial.println("client ip: " + himIP.toString());
  }
  APserver.send(200, "text/html", "OK");
  Serial.println("sending...");

  String str = "";
  for(int i=0; i<3; i++)
  {
    str += String(buff[i]);
  }
//  client1.print(str);
//  client1.write(buff, 3);
}

void startEndDetection()  //this function set isParking on/off
{
    StaticJsonBuffer<200> jsonBuffer;
    String jsonStr = "";
    
    jsonStr += APserver.arg("plain");
    
    JsonObject& json = jsonBuffer.parseObject(jsonStr);
    if(!json.success())
      Serial.println("Json Parse Is Failed");
    else
    {
      String state = json["state"];
      if(state == "true")
      {
        accelerometer.setIsParking(true);
        Serial.println("Detection started");
      }
      else if(state == "false")
      {
        accelerometer.setIsParking(false);
        Serial.println("Detection ended");
      }
      APserver.send(200, "text/html", "DONE");
    }
}

void updateAccessPointDetails()  //this function update the sysfile.txt after change
{
  StaticJsonBuffer<200> jsonBuffer;
  String jsonStr = "";
  jsonStr += APserver.arg("plain");
  JsonObject& json = jsonBuffer.parseObject(jsonStr);
  if(!json.success())
    Serial.println("Json Parse Is Failed");
  else
  {
    fileHandler.setAPname(json["accessPointName"]);
    fileHandler.setAPpass(json["accessPointPass"]);
    fileHandler.writeToFile();
    WiFi.softAPdisconnect(true);
    WiFi.softAP(fileHandler.getAPname(), fileHandler.getAPpass());  //Initialise the access point with new details
    APserver.send(200, "text/html", "DONE");
  }
}

void setup(void) 
{
    delay(2000);
    
    Serial.begin(9600);
    Serial.println("main setup start");

    fileHandler.setup();
    camera.setup();
    accelerometer.setup();

    String APname = fileHandler.getAPname();
    String APpass = fileHandler.getAPpass();
    WiFi.softAP(APname, APpass);  //Initialise the access point
    APserver.on("/start_end_detection", startEndDetection);
    APserver.on("/update_access_point_details", updateAccessPointDetails);
    APserver.on("/has_new_photos", hasNewPhotos);
    APserver.on("/clone_photos", clonePhotos);
    APserver.begin(); 
    Serial.println("main setup done");
}

void loop(void) 
{  
    APserver.handleClient();
  
    if(accelerometer.getIsParking())
    {
      if(accelerometer.runDetection(camera))
        hasNewPhoto = true;
    }
    else
      accelerometer.setInitMesure(false);
      
  delay(500);
}
