/*  OpenNI grabber with filtering 
    by: Ryan Villarreal
	
	This application currently will get a depth frame from OpenNI
	and will convert to the PCL format.  It will then filter the
	cloud with passthrough filteration.

	Future work will include segmentation of the just the user data, 
	which will help with performance of the application.  Also, 
	rotation of the cloud is needed to compare with the pre-
	recorded data. 
	*/


// import neccessary header files
#include <pcl\point_cloud.h>
#include <pcl\point_types.h>
#include <pcl\io\openni_grabber.h>
#include <pcl\common\time.h>
#include <pcl\filters\passthrough.h>
#include <iostream>
#include <pcl\visualization\cloud_viewer.h>


class SimpleOpenNIProcessor
{
public:
	// create a Viewer for ouput
  SimpleOpenNIProcessor() : viewer("My viewer") {}

  // using the PointXYZRGB for both the depth data and the video stream
  void cloud_cb_ (const pcl::PointCloud<pcl::PointXYZ>::ConstPtr &cloud)
  {

    pcl::PointCloud<pcl::PointXYZ>::Ptr cloud_filtered (new pcl::PointCloud<pcl::PointXYZ>);
    static unsigned count = 0;
    static double last = pcl::getTime ();

	// every 30 frames get the average framerate and distance of the center pixel
    if (++count == 30)
    {
      double now = pcl::getTime ();
      std::cout << "distance of center pixel :" << cloud->points [(cloud->width >> 1) * (cloud->height + 1)].z << " mm. Average framerate: " << double(count)/double(now - last) << " Hz" <<  std::endl;
      count = 0;
      last = now;
    }
  
  // Create the filtering object here
  pcl::PassThrough<pcl::PointXYZ> pass;
  pass.setInputCloud (cloud);
  pass.setFilterFieldName ("z");
  pass.setFilterLimits (0.0, 1.0);
  pass.filter(*cloud_filtered);


  // Create roatation object here


  // Create PCL --> OpenNI object here


  if (!viewer.wasStopped())
         viewer.showCloud (cloud);
  }
  
  void run ()
  {
    // create a new grabber for OpenNI devices
    pcl::Grabber* interface = new pcl::OpenNIGrabber();

    // make callback function from member function
    boost::function<void (const pcl::PointCloud<pcl::PointXYZ>::ConstPtr&)> f =
      boost::bind (&SimpleOpenNIProcessor::cloud_cb_, this, _1);

    // connect callback function for desired signal. In this case its a point cloud with color values
    boost::signals2::connection c = interface->registerCallback (f);

    // start receiving point clouds
    interface->start ();

    // wait until user quits program with Ctrl-C
    while (true)
		std::cin.get();

    // stop the grabber
    interface->stop ();
  }

  pcl::visualization::CloudViewer viewer;

};

int main ()
{
SimpleOpenNIProcessor v;
v.run ();
return 0;
}