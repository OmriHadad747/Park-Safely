#include "Arduino.h"
#include "Accelerometer.h"

Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(12345);

Accelerometer::Accelerometer(){}

void Accelerometer::setup()
{
  Serial.println("accelerometer setup start");
  
  if(!accel.begin())  //Initialise the ADXL345
  {
    Serial.println("ADXL345 initialization failed!");
    while(1);
  }
  chooseRange(2); 

  Serial.println("accelerometer setup done");
}

void Accelerometer::chooseRange(int range)  /*Set the range to whatever is appropriate for your project*/
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

void Accelerometer::printAccelDate(sensors_event_t event)   /*Display the results (acceleration is measured in m/s^2)*/
{
    Serial.print("X: ");
    Serial.print(event.acceleration.x);
    Serial.print("  ");
    Serial.print("Y: "); 
    Serial.print(event.acceleration.y); 
    Serial.print("  ");
    Serial.println("m/s^2 ");
}

bool Accelerometer::runDetection(Camera camera)
{
  sensors_event_t event;
    
  accel.getEvent(&event); /*Get a new sensor event*/
  printAccelDate(event);
  if(!initMeasure)
  {
    startX = event.acceleration.x;
    startY = event.acceleration.y;
    initMeasure = true;
  }

  double currX = event.acceleration.x;
  double currY = event.acceleration.y;
    
  if(abs(startX-currX) >= 2.00)
  {
    Serial.println("X change!!");
    startX = event.acceleration.x;
    return camera.capturePhoto();
  }
  else if(abs(startY-currY) >= 2.00)
  {
    Serial.println("Y change!!");
    startY = event.acceleration.y;
    return camera.capturePhoto();
  } 

  return false;
}

void Accelerometer::setIsParking(bool state)
{
  isParking = state;
}

void Accelerometer::setInitMesure(bool state)
{
  initMeasure = state;
}
