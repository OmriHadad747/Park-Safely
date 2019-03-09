#include "Arduino.h"
#include "Camera.h"

int CAMERA_CS = D2;
ArduCAM myCam(OV2640, CAMERA_CS);

Camera::Camera(){}

void Camera::setup()
{ 
  Serial.println("camera setup start");
  
  Wire.begin();

  pinMode(CAMERA_CS, OUTPUT);
  digitalWrite(CAMERA_CS, HIGH);

  SPI.begin();

  //Reset the CPLD
  myCam.write_reg(0x07, 0x80);  
  delay(100);
  myCam.write_reg(0x07, 0x00);
  delay(100);

  while(1)  //Check if the ArduCAM SPI bus is OK
  {
    myCam.write_reg(ARDUCHIP_TEST1, 0x55);  
    uint8_t temp = myCam.read_reg(ARDUCHIP_TEST1);
    if (temp != 0x55)
    {
      Serial.println("SPI interface error");
      delay(1000);
      continue;
    }
    else
    {
      Serial.println("SPI interface OK");
      break;
    }
  }
  
  uint8_t vid, pid;
  while(1)  //Check if the c module type is OV2640
  {
    myCam.wrSensorReg8_8(0xff, 0x01);
    myCam.rdSensorReg8_8(OV2640_CHIPID_HIGH, &vid);
    myCam.rdSensorReg8_8(OV2640_CHIPID_LOW, &pid);
    if ((vid != 0x26 ) && (( pid != 0x41 ) || ( pid != 0x42 )))
    {
      Serial.println("can't find OV2640 module");
      delay(1000);
      continue;
    }
    else
    {
      Serial.println("OV2640 detected");
      break;
    }
  }
      
  myCam.set_format(JPEG);
  myCam.InitCAM();
  myCam.OV2640_set_JPEG_size(OV2640_320x240);
  myCam.OV2640_set_Special_effects(Normal);
  delay(1000);

  myCam.clear_fifo_flag();
  myCam.write_reg(ARDUCHIP_FRAMES,0x00);
  
  Serial.println("camera setup done");
}

bool Camera::capturePhoto()
{
  myCam.flush_fifo();
  myCam.clear_fifo_flag();
  myCam.start_capture();  //Start capture
  Serial.println("start Capture");
  while(!myCam.get_bit(ARDUCHIP_TRIG , CAP_DONE_MASK));
  Serial.println("capture done"); 

  return savePhoto();
}

bool Camera::savePhoto()
{
  char str[8];
  byte buf[256]; 
  static int i = 0, k = 0;
  uint8_t temp = 0,temp_last=0;
  uint32_t length = 0;
  bool is_header = false;
  File outFile;

  length = myCam.read_fifo_length();
  //Serial.print("The fifo length is: "); Serial.println(length, DEC);
  if (length >= MAX_FIFO_SIZE) //384K
  {
    Serial.println("over size");
    return false;
  }

  if (length == 0 ) //0 kb
  {
    Serial.println("size is 0");
    return false;
  }
  
  //Construct a file name
  k = k + 1;
  itoa(k, str, 10);
  strcat(str, ".jpg");
  //Serial.print("file name: ");Serial.println(str);

  outFile = SD.open(str, FILE_WRITE);  //Open the new file
  if(!outFile)
  {
    Serial.println("file open faild");
    return false;
  }
  
  myCam.CS_LOW();
  myCam.set_fifo_burst();

  while(length--)
  {
    temp_last = temp;
    temp =  SPI.transfer(0x00);
    //Read JPEG data from FIFO
    if((temp == 0xD9) && (temp_last == 0xFF)) //If find the end ,break while,
    {
      buf[i++] = temp;  //save the last  0XD9     
      //Write the remain bytes in the buffer
      myCam.CS_HIGH();
      outFile.write(buf, i);    
      outFile.close();
      Serial.println("image save OK");
      is_header = false;
      i = 0;
    }  
  
    if(is_header == true)
    { 
      if(i < 256)  //Write image data to buffer if not full
        buf[i++] = temp;
      else
      {
        //Write 256 bytes image data to file
        myCam.CS_HIGH();
        outFile.write(buf, 256);
        i = 0;
        buf[i++] = temp;
        myCam.CS_LOW();
        myCam.set_fifo_burst();
      }        
    }
    else if((temp == 0xD8) & (temp_last == 0xFF))
    {
      is_header = true;
      buf[i++] = temp_last;
      buf[i++] = temp;   
    } 
  } 
  Serial.println("gone send true");
  return true;
}
