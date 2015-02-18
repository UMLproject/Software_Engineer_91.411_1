package falldetection.datamanagement.utilily;

import java.util.List;

public class AccelerometerValue {

	public double x;
	public double y;
	public double z;
	
	public long timeStamp;
	
	public AccelerometerValue( double x, double y, double z, long timeStamp ){
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.timeStamp = timeStamp;
	}
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void setZ(double z) {
		this.z = z;
	}
}
