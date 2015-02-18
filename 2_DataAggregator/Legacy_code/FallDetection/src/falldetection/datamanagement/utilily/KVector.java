package falldetection.datamanagement.utilily;

import android.hardware.SensorManager;

public class KVector 
{
	public static int vectorCount = 0;
	public final float GRAVITY = SensorManager.GRAVITY_EARTH; 
	public final int X = 1;
	public final int Y = 2;
	public final int Z = 3;
	public final int RSS = 4;
	
	private float x;
	private float y;
	private float z;
	/**
	 * Root-of-Sum-Squares Value
	 * The total acceleration from the X,Y,and Z axis are combined in the 
	 * equation: sqrt(x^2 + y^2 + z^2)
	 */
	private double rss;
	private String name;
	//This is the time in nanoseconds that the vector was created.
	private long timestamp;
	//This is a shortened version of timestamp: (timestamp/1000)
	private long shortTimeStamp;
	
	
	
	public KVector ()
	{
		reset();		
		vectorCount++;
		this.name = "KVector" + vectorCount;
	}
	public KVector(KVector that)
	{
		this.set(that);
		vectorCount++;
		this.name = "KVector" + vectorCount;
	}
	public KVector (float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		calculateRSS();
		
		vectorCount++;
		this.name = "KVector" + vectorCount;
	}
	public KVector (float x, float y, float z, long timestamp)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.timestamp = timestamp;
		this.shortTimeStamp = (long)(this.timestamp / 1000);
		calculateRSS();
		
		vectorCount++;
		this.name = "KVector" + vectorCount;
	}
	
	public float getX(){return this.x;}
	public float getY(){return this.y;}
	public float getZ(){return this.z;}
	/**
	 * This function returns the Root-of-Sum-Squares Value.
	 * The total acceleration from the X,Y,and Z axis are combined in the 
	 * equation: sqrt(x^2 + y^2 + z^2)
	 */
	public double getRSS(){return this.rss;}
	/**
	 * This function returns the dynamic total acceleration. 
	 * The total acceleration from the X,Y,and Z axis are combined in the 
	 * equation: sqrt(x^2 + y^2 + z^2) - GRAVITY
	 */
	public double getRSSd(){return this.rss - GRAVITY;}
	public String getName(){return this.name;}
	public long getTimeStamp(){return this.timestamp;}
	public long getShortTimeStamp(){return this.shortTimeStamp;}
	public void set(KVector v1){this.x=v1.x; this.y=v1.y; this.z=v1.z; this.timestamp = v1.getTimeStamp(); this.shortTimeStamp = v1.getShortTimeStamp(); calculateRSS(); }
	public void set(float x, float y, float z){this.x=x; this.y=y; this.z=z; calculateRSS(); }
	public void setX(float x){this.x = x; calculateRSS();}
	public void setY(float y){this.y = y; calculateRSS();}
	public void setZ(float z){this.z = z; calculateRSS();}
	public void setName(String n){this.name = n;}
	public void setTimeStamp(long l){this.timestamp = l; this.shortTimeStamp = (long)(this.timestamp / 1000);}
	
	public void reset()
	{
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		this.rss = 0.0d;
		this.timestamp = 0;
		this.shortTimeStamp = 0;
	}
	
	public KVector copy() { return new KVector(this.x, this.y, this.z, this.timestamp); }
	
	public void add(KVector that)
	{
		this.x += that.x;
		this.y += that.y;
		this.z += that.z;
	}
	public void add(float num)
	{
		this.x += num;
		this.y += num;
		this.z += num;
	}
	
	public void sub(KVector that)
	{
		this.x -= that.x;
		this.y -= that.y;
		this.z -= that.z;
	}
	public void sub(float num)
	{
		this.x -= num;
		this.y -= num;
		this.z -= num;
	}
	
	public void mul(KVector that)
	{
		this.x *= that.x;
		this.y *= that.y;
		this.z *= that.z;
	}
	public void mul(float num)
	{
		this.x *= num;
		this.y *= num;
		this.z *= num;
	}
	
	public void div(KVector that)
	{
		if(that.x == 0)
			this.x = 0;
		else
			this.x /= that.x;
		
		if(that.y == 0)
			this.y = 0;
		else
			this.y /= that.y;
		
		if(that.z == 0)
			this.z = 0;
		else
			this.z /= that.z;
	}
	public void div(float num)
	{
		if(num == 0)
		{
			reset();
			return;
		}
		
		this.x /= num;
		this.y /= num;
		this.z /= num;
	}
	
	public KVector square() { return new KVector(this.x*this.x, this.y*this.y, this.z*this.z); }
	
	/**
	 * This function returns a new vector that is a copy of this vector raised to power p.
	 * @param p
	 * @return KVector
	 */
	public KVector power(int p)
	{
		KVector tmp = new KVector(1,1,1);
		if(p >= 0)
			for(int i=0;i<p;i++)
			{
				KVector.mul(tmp, tmp);
			}
		else
			for(int i=0;i<Math.abs(p); i++)
			{
				KVector.div(tmp, tmp);
			}
		return tmp;
	}
	
	/**
	 * Returns the inner product of two vectors.
	 * 
	 * @param that
	 * @return float
	 */
	public float dot(KVector that) { return (float) (this.x*that.x + this.y*that.y + this.z*that.z); }
	
	/**
	* Calculates the scalar product of two vectors
	*/
	public float orientation(KVector that)
	{
		float ori = (float) Math.toDegrees(Math.acos((this.x * that.x + this.y * that.y + this.z * that.z) / (this.rss * that.rss)));
		
		if(ori >= 0.0)
			return ori;
		else 
			return 0.0f;
	}
	
	public String toString() { return "Name::" + this.name + "::x::" + this.x + "::y::" + this.y + "::z::" + this.z + "::RSS::" + this.rss; }
	
	private void calculateRSS() { rss = Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z); }
	
	/**
	 * This function calculates the Euclidean distance between two vectors.
	 *  
	 * @param that
	 * @return float
	 */
    public float distanceTo(KVector that) { return KVector.rss(KVector.sub(this, that)); }
	
    /**
     * This function returns the corresponding unit vector.
     * @return
     */
    public KVector direction() 
    {
    	if(this.rss == 0)
    		return new KVector();
    	
    	KVector copy = this.copy();
    	copy.mul((float)(1.0f/this.rss));
    	return copy;
    }
	
    
	
	/***************************************************
	 * 
	 * 
	 * STATIC FUNCTIONS
	 * 
	 * 
	 ***************************************************/
	
	
	
	/**
	 * This static function adds Vector1 to Vector2 and returns the new KVector result.
	 * @param v1
	 * @param v2
	 * @return KVector
	 */
	public static KVector add(KVector v1, KVector v2) { return new KVector(v1.x+v2.x, v1.y+v2.y, v1.z+v2.z); }
	public static KVector sub(KVector v1, KVector v2) { return new KVector(v1.x-v2.x, v1.y-v2.y, v1.z-v2.z); }
	public static KVector mul(KVector v1, KVector v2) { return new KVector(v1.x*v2.x, v1.y*v2.y, v1.z*v2.z); }
	public static KVector div(KVector v1, KVector v2) 
	{ 
		KVector tmp = new KVector();
		if(v2.x == 0)
			tmp.x = 0;
		else 
			tmp.x = v1.x / v2.x;
		
		if(v2.y == 0)
			tmp.y = 0;
		else 
			tmp.y = v1.y / v2.y;
		
		if(v2.z == 0)
			tmp.z = 0;
		else 
			tmp.z = v1.z / v2.z;
		
		return tmp; 
	}
	/**
	 * Returns the inner product of two vectors.
	 * 
	 * @param v1, v2
	 * @return float
	 */
	public static float dot(KVector v1, KVector v2) { return (float) (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z);	}
	public static float rss(KVector v1){ return (float) Math.sqrt(v1.dot(v1));}
	
	
	
	public static KVector add(KVector v1, float c) { return new KVector(v1.x+c, v1.y+c, v1.z+c); }
	
	
}















