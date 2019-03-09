#ifndef CAMERA_h
#define CAMERA_h

#include "Arduino.h"
#include <Wire.h>
#include <ArduCAM.h>
#include <SPI.h>
#include <SD.h>
#include "memorysaver.h"

#define MAX_FIFO_SIZE 384000
#define ARDUCHIP_FRAMES 0x01  

class Camera
{
  private:    
    bool savePhoto();
    
  public:
    Camera();
    void setup();
    bool capturePhoto();  
};
#endif
