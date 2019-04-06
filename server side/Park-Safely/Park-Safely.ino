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
static unsigned long offset = 0;

void hasNewPhotos()  //check if there are new photos
{
  if(hasNewPhoto)
    APserver.send(200, "text/html", "YES");
  else
    APserver.send(200, "text/html", "NO");   
}

void clonePhotos()
{
  //static int k = 1;
  bool eof = false;
  File file = SD.open("1.jpg");
  if(!file)
    Serial.println("file dont exist");

  if(!file.seek(offset))
    Serial.println("seek was failed");
 
  String chunk = "";
  String tmp;
  for(int i=1; i<=180; i++)
  {
    if(file.available())
    {
      tmp = String(file.read());
      Serial.println(String(i) + ") " + tmp);
      chunk += tmp;
    }
//    else
//    {
//      APserver.send(200, "text/html", "DONE");
//      eof = true;
//      Serial.println("sending chunk" + String(k));
//      offset = 0;
//      break;
//    }
  }
//  file.close();
//  if(!eof)
//  {
    APserver.send(200, "text/html", chunk);
    //Serial.println("sending chunk: " + String(k++));
    //offset += 180;
  //}
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
