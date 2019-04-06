#include "Arduino.h"
#include "FileHandler.h"

FileHandler::FileHandler()
{
  APname = "Park-Safely AP"    ;
  APpass = "01234567";
}

void FileHandler::setup()
{
  Serial.println("file handler setup start");
  
  if(!SD.begin(SD_CS))  //Initialise the SD card 
  {
        Serial.println("SD card initialization failed!");
        while(1);
  }

  if(SD.exists("sysfile.txt"))
    readFromFile();
  else
    createFile();
  
  Serial.println("file handler setup done");
}

void FileHandler::createFile()  //create the file for the first time with default values
{
    File jsonFile = SD.open("sysfile.txt", FILE_WRITE);
    if(jsonFile)
    {
        StaticJsonBuffer<200> jsonBuffer;
        JsonObject& json = jsonBuffer.createObject();
        json["accessPointName"] = APname;
        json["accessPointPass"] = APpass;
        String jsonStr = "";
        json.printTo(jsonStr);
        jsonFile.print(jsonStr);
        jsonFile.close();
    }
}

void FileHandler::readFromFile()  //read data from file and set AP name&password
{
    File jsonFile = SD.open("sysfile.txt");
    if(jsonFile)
    {
        StaticJsonBuffer<200> jsonBuffer;
        String jsonStr = "";
        while(jsonFile.available())
            jsonStr = jsonFile.readStringUntil('\n');
        jsonFile.close();
        JsonObject& json = jsonBuffer.parseObject(jsonStr);
        APname = json["accessPointName"].as<String>();
        APpass = json["accessPointPass"].as<String>();
        delay(20);
    }
}

void FileHandler::writeToFile()
{
    SD.remove("sysfile.txt");
    File jsonFile = SD.open("sysfile.txt", FILE_WRITE);
    StaticJsonBuffer<200> jsonBuffer;
    JsonObject& json = jsonBuffer.createObject();
    json["accessPointName"] = APname;
    json["accessPointPass"] = APpass;
    String jsonStr = "";
    json.printTo(jsonStr);
    if(jsonFile)
        jsonFile.println(jsonStr);
    jsonFile.close();
}

void FileHandler::setAPname(String APname_)
{
  APname = APname_;
}

void FileHandler::setAPpass(String APpass_)
{
  APpass = APpass_;
}
