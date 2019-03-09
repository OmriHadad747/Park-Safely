#ifndef ACCELEROMETER_h
#define ACCELEROMETER_h

#include "Arduino.h"
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL345_U.h>
#include "Camera.h"
#include <math.h>

class Accelerometer
{
  private:
    double startX, startY;
    bool initMeasure = false;
    bool isParking = false;
  
    void chooseRange(int range);
    void printAccelDate(sensors_event_t event);

  public:
    Accelerometer();
    void setup();
    bool runDetection(Camera);
    void setIsParking(bool);
    bool getIsParking(){return isParking;}
    void setInitMesure(bool);
};
#endif
