# ADXL345 Python library for Raspberry Pi 
#
# author:  Jonathan Williamson
# license: BSD, see LICENSE.txt included in this package
# 
# This is a Raspberry Pi Python implementation to help you get started with
# the Adafruit Triple Axis ADXL345 breakout board:
# http://shop.pimoroni.com/products/adafruit-triple-axis-accelerometer
import Adafruit_BBIO.GPIO as GPIO
import smbus
from time import sleep

#interrupt pin
GPIO_INTERRUPT_PIN  = "P9_12"

# select the correct i2c bus for this revision of Raspberry Pi
#revision = ([l[12:-1] for l in open('/proc/cpuinfo','r').readlines() if l[:8]=="Revision"]+['0000'])[0]
bus =  smbus.SMBus(1)

# if int(revision, 16) >= 4 else 0)

# ADXL345 constants
EARTH_GRAVITY_MS2   = 9.80665
SCALE_MULTIPLIER    = 0.004

DATA_FORMAT         = 0x31
DUR		    = 0x21
WINDOW              = 0x23
TAP_WINDOW          = 0x05 
LATENT	            = 0x22
TAP_LATENCY         = 0x05
TAP_WINDOW   	    = 0x23 
BW_RATE             = 0x2C
POWER_CTL           = 0x2D
THRESH_ACT          = 0x05
TRESH_FF	    = 0x05 
TRESH_TAP	    = 0xCD
TIME_FF		    = 0x05
ACT_INACT_CTL       = 0x27
INT_MAP             = 0x2F
INT_ENABLE          = 0x2E
INT_SOURCE          = 0x30

BW_RATE_1600HZ      = 0x0F
BW_RATE_800HZ       = 0x0E
BW_RATE_400HZ       = 0x0D
BW_RATE_200HZ       = 0x0C
BW_RATE_100HZ       = 0x0B
BW_RATE_50HZ        = 0x0A
BW_RATE_25HZ        = 0x09

RANGE_2G            = 0x00
RANGE_4G            = 0x01
RANGE_8G            = 0x02
RANGE_16G           = 0x03

MEASURE             = 0x08
AXES_DATA           = 0x32
ACTIVITY_TRESH     = 0x1E
ACT_ENABLE          = 0x10
ACT_AXES            = 0xFF
FALL_TIME 	    = 0x05
FALL_TRESH	    = 0x05
TAP_TRESH	    = 0xF5
DUR_TRESH	    = 0xA5


class ADXL345:

    address = None

    def __init__(self, address=0x53, interrupt = False):        
        self.address = address
        self.setBandwidthRate(BW_RATE_100HZ)
        self.setRange(RANGE_2G)
        self.enableMeasurement()
	
	if interrupt:
	    self.enableInterrupt()


    def handleInterrupt(self, channel):
        #print "Detected activity"
	
        source = self.clearInterrupt() 
	if(source | (1 << 2)):
	    print "Free fall"

	elif (source | (1 << 4 )):
	    print "Activity"

    def enableInterrupt(self):
        #GPIO.setmode(GPIO.BOARD)
        GPIO.setup(GPIO_INTERRUPT_PIN, GPIO.IN)
        GPIO.add_event_detect(GPIO_INTERRUPT_PIN, GPIO.RISING, callback = self.handleInterrupt)
         
        bus.write_byte_data(self.address, THRESH_ACT, ACTIVITY_TRESH)
        bus.write_byte_data(self.address, ACT_INACT_CTL, ACT_AXES)
	bus.write_byte_data(self.address, TRESH_FF, FALL_TRESH)
	#bus.write_byte_data(self.address, TRESH_TAP, TAP_TRESH)
	#bus.write_byte_data(self.address, DUR, DUR_TRESH)
        #bus.write_byte_data(self.address, LATENT, TAP_LATENCY)
        #bus.write_byte_data(self.address, WINDOW, TAP_WINDOW)
        bus.write_byte_data(self.address, INT_MAP, 0x8B)
        bus.write_byte_data(self.address, INT_ENABLE, ACT_ENABLE)
               
	self.clearInterrupt()

    def clearInterrupt(self):
        return bus.read_byte_data(self.address, INT_SOURCE)

    def cleanup(self):
        GPIO.cleanup()

    def enableMeasurement(self):
        bus.write_byte_data(self.address, POWER_CTL, MEASURE)

    def setBandwidthRate(self, rate_flag):
        bus.write_byte_data(self.address, BW_RATE, rate_flag)

    # set the measurement range for 10-bit readings
    def setRange(self, range_flag):
        value = bus.read_byte_data(self.address, DATA_FORMAT)

        value &= ~0x0F;
        value |= range_flag;  
        value |= 0x08;

        bus.write_byte_data(self.address, DATA_FORMAT, value)
    
    # returns the current reading from the sensor for each axis
    #
    # parameter gforce:
    #    False (default): result is returned in m/s^2
    #    True           : result is returned in gs
    def getAxes(self, gforce = False):
        bytes = bus.read_i2c_block_data(self.address, AXES_DATA, 6)
        
        x = bytes[0] | (bytes[1] << 8)
        if(x & (1 << 16 - 1)):
            x = x - (1<<16)

        y = bytes[2] | (bytes[3] << 8)
        if(y & (1 << 16 - 1)):
            y = y - (1<<16)

        z = bytes[4] | (bytes[5] << 8)
        if(z & (1 << 16 - 1)):
            z = z - (1<<16)

        x = x * SCALE_MULTIPLIER 
        y = y * SCALE_MULTIPLIER
        z = z * SCALE_MULTIPLIER

        if gforce == False:
            x = x * EARTH_GRAVITY_MS2
            y = y * EARTH_GRAVITY_MS2
            z = z * EARTH_GRAVITY_MS2

        x = round(x, 4)
        y = round(y, 4)
        z = round(z, 4)

        return {"x": x, "y": y, "z": z}


if __name__ == "__main__":
    # if run directly we'll just create an instance of the class and output 
    # the current readings
    adxl345 = ADXL345()
    
    axes = adxl345.getAxes(True)
    print "ADXL345 on address 0x%x:" % (adxl345.address)
    print "   x = %.3fG" % ( axes['x'] )
    print "   y = %.3fG" % ( axes['y'] )
    print "   z = %.3fG" % ( axes['z'] )
