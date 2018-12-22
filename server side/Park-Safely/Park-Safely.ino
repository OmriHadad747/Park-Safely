#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL345_U.h>
#include <SPI.h>
#include <SD.h>
#include <math.h>
#include <ArduinoJson.h>

int csPin = D10;
Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(12345);
boolean initMeasure = false;
boolean isParking = false;
boolean hasNewPhotos_ = true;
const char *accessPointName, *accessPointPass;
ESP8266WebServer accessPointServer(80);

void chooseRange(int range)  /*Set the range to whatever is appropriate for your project*/
{
    switch(range)
    {
        case 16:
        {
          accel.setRange(ADXL345_RANGE_16_G);
          break;
        }
        case 8:
        {
          accel.setRange(ADXL345_RANGE_8_G);
          break;
        }
        case 4:
        {
          accel.setRange(ADXL345_RANGE_4_G);
          break;
        }
        case 2:
        {
          accel.setRange(ADXL345_RANGE_2_G);
          break;
        } 
    }
}

void hasNewPhotos() /*check if there are new photos*/
{
    if(hasNewPhotos_)
        accessPointServer.send(200, "text/html", "YES");
    else
        accessPointServer.send(200, "text/html", "NO");
}

void updateAccessPointDetails() /*this function update the sysfile.txt after change*/
{
    StaticJsonBuffer<200> jsonBuffer;
    String jsonStr = "";
    jsonStr += accessPointServer.arg("plain");
    /*Serial.println(jsonStr);*/
    JsonObject& json = jsonBuffer.parseObject(jsonStr);
    if(!json.success())
        Serial.println("Json Parse Is Failed");
    else
    {
        accessPointName = json["accessPointName"];
        /*Serial.println("***********");
        Serial.println(accessPointName);*/
        accessPointPass = json["accessPointPass"];
        /*Serial.println("***********");
        Serial.println(accessPointPass);*/
        writeToFile();
        loadNewDetails();
        accessPointServer.send(200, "text/html", "DONE");
    }
}

void startEndDetection() /*this function set isParking on&off*/
{
    StaticJsonBuffer<200> jsonBuffer;
    String jsonStr = "";
    jsonStr += accessPointServer.arg("plain");
    /*Serial.println(jsonStr);*/
    JsonObject& json = jsonBuffer.parseObject(jsonStr);
    if(!json.success())
        Serial.println("Json Parse Is Failed");
    else
    {
        String state = json["state"];
        if(state == "true")
        {
            isParking = true;
            Serial.println("Detection started");
        }
        else if(state == "false")
        {
            isParking = false;
            Serial.println("Detection ended");
        }
        accessPointServer.send(200, "text/html", "DONE");
    }
}

void printAccelDate(sensors_event_t event)   /*Display the results (acceleration is measured in m/s^2)*/
{
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
    sensors_event_t event;
    double startX, startY, currX, currY;
    
    accel.getEvent(&event); /*Get a new sensor event*/
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
        Serial.println("X change!!");

    if(abs(startY-currY) >= 2.00)
        Serial.println("Y change!!");
}

void loadNewDetails()
{
    WiFi.softAPdisconnect(true);
    WiFi.softAP(accessPointName, accessPointPass);  /*Initialise the access point with new details*/
}

void writeToFile()
{
    SD.remove("sysfile.txt");
    File jsonFile = SD.open("sysfile.txt", FILE_WRITE);
    StaticJsonBuffer<200> jsonBuffer;
    JsonObject& json = jsonBuffer.createObject();
    json["accessPointName"] = accessPointName;
    json["accessPointPass"] = accessPointPass;
    String jsonStr = "";
    json.printTo(jsonStr);
    if(jsonFile)
        jsonFile.println(jsonStr);
    jsonFile.close();
}

void readFromFile() /*read data from file and set AP name&password*/
{
    File jsonFile = SD.open("sysfile.txt");
    if(jsonFile)
    {
        StaticJsonBuffer<200> jsonBuffer;
        String jsonStr = "";
        while (jsonFile.available())
            jsonStr = jsonFile.readStringUntil('\n');
        /*Serial.println(jsonStr);*/
        jsonFile.close();
        JsonObject& json = jsonBuffer.parseObject(jsonStr);
        accessPointName = json["accessPointName"];
        accessPointPass = json["accessPointPass"];
    }
}

void createFile() /*create the file for the first time with default values*/
{
    /*Serial.println("creating sysfile.txt...");*/
    File jsonFile = SD.open("sysfile.txt", FILE_WRITE);
    if(jsonFile)
    {
        StaticJsonBuffer<200> jsonBuffer;
        JsonObject& json = jsonBuffer.createObject();
        accessPointName = "Park-Safely AP";
        accessPointPass = "01234567";
        json["accessPointName"] = accessPointName;
        json["accessPointPass"] = accessPointPass;
        String jsonStr = "";
        json.printTo(jsonStr);
        jsonFile.print(jsonStr);
        jsonFile.close();
    }
}

void setup(void) 
{
    delay(3000);
    Serial.begin(9600);
    Serial.println("setup is start");

    if(!SD.begin(csPin)) /*Initialise the SD card*/
    {
        Serial.println("SD card initialization failed!");
        while(1);
    }

    if(SD.exists("sysfile.txt"))
        readFromFile();
    else
        createFile();

    WiFi.softAP(accessPointName, accessPointPass);  /*Initialise the access point*/
    accessPointServer.on("/start_end_detection", startEndDetection);
    accessPointServer.on("/update_access_point_details", updateAccessPointDetails);
    accessPointServer.on("/has_new_photos", hasNewPhotos);
    accessPointServer.begin();
    
    if(!accel.begin()) /*Initialise the ADXL345*/
    {
        Serial.println("ADXL345 initialization failed!");
        while(1);
    }
    chooseRange(16);  
    Serial.println("setup is done");
}

void loop(void) 
{  
    accessPointServer.handleClient();
  
    if(isParking)
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
  }*/
      
  delay(500);
}
