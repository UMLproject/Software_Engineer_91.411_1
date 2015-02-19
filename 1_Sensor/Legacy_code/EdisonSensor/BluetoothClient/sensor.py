#!/bin/python
import adxl345
from time import sleep
from math import sqrt 
import bluetooth
#import Adafruit_BBIO.GPIO as GPIO


def sumSquares(x, y, z):
    return sqrt(x*x + y*y + z*z)

bd_addr = "F4:B7:E2:13:36:36"
port = 1
falls = 0

sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((bd_addr, port))
adxl345 = adxl345.ADXL345()
#GPIO.setup("P9_12", GPIO.IN)
#GPIO.add_event_detect("P9_12", GPIO.FALLING)
  
print"ADXL345 on address 0x%x:" % (adxl345.address)
try:
    while 1:
	if GPIO.event_detected("P9_12"):
  		print "event detected!"
        axes = adxl345.getAxes(True)
        x = axes['x']
        y = axes['y']
        z = axes['z']  
        sumsqr = sumSquares(x, y, z)
        if sumsqr > 1.35:
            falls = falls + 1
            print "VAL = %.3fG" % ( sumsqr )
            print "FALL DETECTED %d" % falls
            sock.send(str(x)+str(y)+str(z))
           
        sleep(.1) 
except KeyboardInterrupt:
    exit()
