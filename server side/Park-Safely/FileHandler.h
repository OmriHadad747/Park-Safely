#ifndef FILEHANDLER_h
#define FILEHANDLER_h

#include "Arduino.h"
#include <SD.h>
#include <ArduinoJson.h>

class FileHandler
{
  private:
    int SD_CS = D10;
    String APname, APpass;

    void readFromFile();
    void createFile();
    
  public:
    FileHandler();
    void setup();
    void writeToFile();    
    void setAPname(String);
    String getAPname(){return APname;}
    void setAPpass(String);
    String getAPpass(){return APpass;}
};
#endif
