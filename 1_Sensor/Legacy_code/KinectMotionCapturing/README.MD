						Kinect Computational Sensing Research
							Ryan Villarreal
							  Alpha 1.0
							  July, 2013


What is it? 
------------

By using the Microsoft Xbox Kinect hardware it is possible to provide an inexpensive viable solution for 
in-home care for rehabilitation patients.  The OpenNI package allows for the capture of depth data on 
multiple platforms.  The NiTE middleware library will allow for subtraction of the background, leaving 
only the depth data for the subject.  The subject’s depth data can then be translated into format for the
Point Cloud Data type so that rotation and filtering can be performed on the depth data.  The reasoning 
for rotating the depth data is to compare to pre recorded data that is facing the opposite direction of 
the Kinect depth data.  Once the progress of the patient is compared and recorded the depth data must be 
returned to its original state to prepare for the next frame. 



The Latest Version 
-------------------

Current version is Alpha 1.0  More information can be found at https://sites.google.com/site/utcbmi/?pli=1



Documentation
---------------

The documentation available as of the date of this release is included in HTML format in the docs/manual/ 
directory.  The most up-to-date documentation can be found at https://sites.google.com/site/utcbmi/?pli=1


Requirements
-------------

Microsoft Windows 7 - 32/64
Microsoft Visual Studio 2010 32/64
OpenNI SDK v1.5.4.0
OpenNI-Compliant Sensor Driver v5.1.2.1
NiTE v1.5.2.21
PCL 1.6.0
Cmake 2.8



Installation 
-----------------

	Windows
		1. Choose the appropriate platform you would like to use - 32 or 64
		2. Run in order
			a. PCL All-In-One Installer
			b. OpenNI installer
			c. NiTE installer
		3. Navigate to the directory of the project code
		4. Open the solution file for Visual Studios that was included with the package
		5. Check to make sure the solution has correctly added all of the libs and dependencies
		6. Compile and Run - See usuage section for how to interact with the applicationi
		 
	OSx
		Platform not currently supported. 
	Linux
		Platform not currently supported. 


Usuage 
--------

As of the current version in order for the program to track the user you must be standing more than one foot
away from the Kinect hardware and no further than six feet away.  To calibrate the camera the subject must
extend arms to the side for the tracking algorithm to detect movement.  The camera system will capture the depth
data from OpenNi and convert it to PCL where it will then be filtered using the passthrough method.  After the 
data is passed through it will be outputed to the screen using the simple viewer method.  Also the average frame-
rate and distance from the camera will be recorded in the console.  


Future Plans
------------

Need to upgrade to OpenNI 2.2 and NiTE 2.0 when the PCL 1.7 is released.  As of right now, OpenNI 2.2 and NiTE 2.0 
are not compatible with PCL 1.6.  Need to implement a better background removal algorithm in order to be more adept
to detecting users when lying in a bed.  Multiple platform research needs to be started. 


Contacts
--------------

If you want to be informed about new code releases, bug fixes, security fixes, general news, and information 
about the OpenNI project please view the site https://sites.google.com/site/utcbmi/?pli=1
Project Leader: Dr. Yu Cao 
Research Assitant: Xiuwei Yi
Research Assitant: Ryan Villarreal



