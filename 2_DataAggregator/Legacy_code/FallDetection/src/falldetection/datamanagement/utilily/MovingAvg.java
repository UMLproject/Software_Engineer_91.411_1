package falldetection.datamanagement.utilily;
import java.util.LinkedList;

//This class is for implementing a moving average.
//Here we take the last values (up to 5) and average them. 


public class MovingAvg {

	
	private LinkedList<Float> vals= new LinkedList<Float>();
	private float rtotal = 0;
	private int buffSize = 0;
	public MovingAvg(int x){
	
		buffSize=x;
		
	}
	
	
	
	public float add (float f){
		
		vals.add(f);
		rtotal += f;
		if (vals.size() >=buffSize){
			rtotal -= vals.peekFirst();
			vals.removeFirst();
			
			}

		//This algorithm will return an average even before the queue is full. This is so the sensors are never interrupted. The average just gets "better" when the queue is full
		return rtotal/vals.size();
	}
	
	
	//we don't want to use old data for a different fall event, so here we can reset the queue and running total
	public void avgReset(){
		rtotal=0;
		vals.clear();
	}
	
}
