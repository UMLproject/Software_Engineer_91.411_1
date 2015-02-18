package falldetection.datamanagement.utilily;

import java.util.ArrayList;

import falldetection.communication.timeseries.DataFileUploader;
import falldetection.communication.timeseries.DataFileWriter;
import falldetection.datamanagement.userprofile.Profile;
import android.util.Log;

public class Global  {

	public static String  ADLClassChosen = "";
	
	public static boolean fallDetected = false;
	
	//Not the most efficient solution, but the fastest
	public static void manageDataWriterAndUploading( String filePath, ArrayList<AccelerometerValue> vectorList, String classifiedADLClass ){
		
		Profile currentProfile = new Profile( Profile.Gender.Male, 135, "57", 20, 22);
		currentProfile.setIdentifier( "EXPERIMENT-2.0" );
		
		DataFileWriter possibleFallDataFileWriter = new DataFileWriter( filePath, ADLClassChosen, currentProfile );

		ArrayList< AccelerometerValue > accelerometerValues = new ArrayList<AccelerometerValue>( );
		
		Log.i("size = ", String.valueOf( vectorList.size( ) ) );
		
		long lastTime = vectorList.get( (  vectorList.size( ) - 1 ) ).timeStamp;
		
		for (AccelerometerValue accelerometerValue : vectorList) {
			
			if( ( ( lastTime - accelerometerValue.timeStamp ) / 1000 ) > 3000000  ){
			
				accelerometerValues.add( new AccelerometerValue( accelerometerValue.getX( ), accelerometerValue.getY( ), accelerometerValue.getZ( ), accelerometerValue.timeStamp ) );
			
			}
		}
		
		possibleFallDataFileWriter.writeOutAllInformation( accelerometerValues, classifiedADLClass, "UI" );
		
		DataFileUploader possibleFallDataFileUploader = new DataFileUploader( possibleFallDataFileWriter );
		
		possibleFallDataFileUploader.uploadFile( );
	}
	
}
