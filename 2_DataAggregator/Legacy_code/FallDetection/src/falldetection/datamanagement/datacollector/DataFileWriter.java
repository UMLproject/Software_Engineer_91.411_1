package falldetection.datamanagement.datacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;

import android.text.format.Time;
import android.util.Log;

public class DataFileWriter {

	String fileName;
	String filePath;
	String separatingCharacter = ",";
	
	Profile userProfile;

	File dataFileOutput;
	
	BufferedWriter dataFileWriter;
	
	public DataFileWriter( String filePath, String fileName, Profile userProfile ) {

		setFilePath( filePath );

		// I only assign the random in case an upload fails and I don't want a
		// file name error
		setFileName( fileName );

		dataFileOutput = new File( filePath, fileName );

		if ( !dataFileOutput.exists(  ) ) {

			try {

				Log.i( "inside writeOutProfile ", "creating file" );

				dataFileOutput.createNewFile(  );

			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace(  );
			}
		}
		
		dataFileWriter = null;

		try {

			Log.i( "trying", "buffered writer" );

			dataFileWriter = new BufferedWriter( new FileWriter( dataFileOutput  )  );

		} catch ( Exception e ) {

			Log.i( "catching", "buffered writer" );
		}
		
		Log.i( "fileName = ", "FILE = " + fileName );

		this.userProfile = userProfile;
	}

	//Speed selection is a string representing the accelerometer speed chosen
	public void writeOutAllInformation(  List<AccelerometerValue> accelerometerValues, String ADLClassClassified, String speedSelection  ){
		
		writeOutClassLabel(  accelerometerValues, ADLClassClassified, speedSelection  );
		writeOutProfileInformation(   );
		writeOutAccelerometerValues(  accelerometerValues  );
	}

	public void writeOutAllInformationAndUpload(  List<AccelerometerValue> accelerometerValues, String ADLClassClassified, String speedSelection  ){
		
		writeOutAllInformation(  accelerometerValues, ADLClassClassified, speedSelection  );
		
		uploadFile(   );
	}
	
	public void uploadFile(   ){
		
		DataFileUploader fileUploader = new DataFileUploader(  this  );
		
		fileUploader.uploadFile(   );
	}
	
	public void writeOutClassLabel(  List<AccelerometerValue> accelerometerValues,  String ADLClassClassified, String speedSelection  ){
		
			try{
				
				Log.i( "writing", "writing" );
				
				dataFileWriter.append(  returnClassLabel(   )  );
				dataFileWriter.append( separatingCharacter );
			
				dataFileWriter.append(  ADLClassClassified  );
				dataFileWriter.append( separatingCharacter );
				
				dataFileWriter
				.append( String.valueOf( accelerometerValues.get( 0 ).timeStamp ) );
				dataFileWriter.append( separatingCharacter );

				dataFileWriter.append( String.valueOf( accelerometerValues
				.get( accelerometerValues.size(  ) - 1 ).timeStamp ) );
				dataFileWriter.append( separatingCharacter );
				
				dataFileWriter.append(  speedSelection  );
	
				dataFileWriter.append( "\r\n" );
				
			} catch(  Exception e  ){
				
				
				
			}
	}
	
	public void writeOutProfileInformation(   ){
		
		// Writes out the profile information to the file
			try {

				dataFileWriter.append( String.valueOf( userProfile.age ) );
				dataFileWriter.append(separatingCharacter);

				dataFileWriter.append( String.valueOf( userProfile.BMI ) );
				dataFileWriter.append(separatingCharacter);

				dataFileWriter.append( String.valueOf( userProfile.weight ) );
				dataFileWriter.append(separatingCharacter);

				dataFileWriter
						.append( String
						.valueOf( ( userProfile.getGender(  ) == Profile.Gender.Male ) ? 0
										: 1 ) );
				dataFileWriter.append(separatingCharacter);

				dataFileWriter.append( userProfile.height );
				dataFileWriter.append(separatingCharacter);

				dataFileWriter.append( userProfile.identifier );

				dataFileWriter.append( "\r\n" );
			} catch ( Exception e ){
				
				
			}
	}
	
	public void writeOutAccelerometerValues( 
			List<AccelerometerValue> accelerometerValues ) {

		long lastTimeStamp = accelerometerValues.get(  accelerometerValues.size(   ) - 1  ).timeStamp;
		long firstTimeStamp = accelerometerValues.get(  0  ).timeStamp;
		
		for ( AccelerometerValue accelerometerValue : accelerometerValues ) {

			Log.i( "writing value", "values" );

			if(  (  (  lastTimeStamp - accelerometerValue.timeStamp  )  ) / 1000 > 3000000 && (  (  accelerometerValue.timeStamp - firstTimeStamp ) / 1000  ) > 1000000  )
				{
					try {
						
						dataFileWriter.append( String.valueOf(  System.currentTimeMillis(  )  ) );
						dataFileWriter.append(separatingCharacter);
		
						dataFileWriter.append( String.valueOf( accelerometerValue.x ) );
						dataFileWriter.append(separatingCharacter);
		
						dataFileWriter.append( String.valueOf( accelerometerValue.y ) );
						dataFileWriter.append(separatingCharacter);
		
						dataFileWriter.append( String.valueOf( accelerometerValue.z ) );
		
						dataFileWriter.append( "\r\n" );
		
					} catch ( IOException e ) {
						// TODO Auto-generated catch block
						Log.i( "catching error", "inside app" );
						e.printStackTrace(  );
		
					}
			}
			
			try {
			
				dataFileWriter.flush(  );
			
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace(  );
			}
		}

		try {
	
			dataFileWriter.close(  );
	
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace(  );
		}

	}

	public String returnClassLabel(   ){
		
		String classLabel = "";
		Log.i( "CLASS","LABEL = " + fileName  );
		//Fall ADL class labels
		
		if(  fileName.equals(  "Front-lying-FALL"  )  ){
			
			classLabel = "FLY";
			
		} else if(  fileName.equals(  "Front-protecting-lying-FALL"  )  ){
			
			classLabel = "FPLY";
			
		} else if(  fileName.equals(  "Front-knees-FALL"  )  ){
			
			classLabel = "FKN";
			
		} else if(  fileName.equals(  "Front-knees-lying-FALL"  )  ){
			
			classLabel = "FKLY";
			
		} else if(  fileName.equals(  "Front-right-FALL"  )  ){
		
			classLabel = "FR";
			
		} else if(  fileName.equals(  "Front-left-FALL"  )  ){
		
			classLabel = "FL";
			
		} else if(  fileName.equals(  "Front-quick-recovery-FALL"  )  ){
		
			classLabel = "FQR";
			
		} else if(  fileName.equals(  "Front-slow-recovery-FALL"  )  ){
		
			classLabel = "FSR";
			
		} else if(  fileName.equals(  "Back-sitting-FALL"  )  ){
		
			classLabel = "BS";
			
		} else if(  fileName.equals(  "Back-lying-FALL"  )  ){
		
			classLabel = "BLY";
			
		} else if(  fileName.equals(  "Back-right-FALL"  )  ){
		
			classLabel = "BR";
			
		} else if(  fileName.equals(  "Back-left-FALL"  )  ){
		
			classLabel = "BL";
			
		} else if(  fileName.equals(  "Right-sideway-FALL"  )  ){
		
			classLabel = "RS";
			
		} else if(  fileName.equals(  "Right-recovery-FALL"  )  ){
		
			classLabel = "RR";
			
		} else if(  fileName.equals(  "Left-sideway-FALL"  )  ){
		
			classLabel = "LS";
			
		} else if(  fileName.equals(  "Left-recovery-FALL"  )  ){
		
			classLabel = "LR";
			
		} else if(  fileName.equals(  "Syncope-FALL"  )  ){
		
			classLabel = "SYD";
			
		} else if(  fileName.equals(  "Syncope-wall-FALL"  )  ){
		
			classLabel = "SYW";
			
		} else if(  fileName.equals(  "Podium-FALL"  )  ){
		
			classLabel = "POD";
			
		} else if(  fileName.equals(  "Rolling-out-bed-FALL"  )  ){
		
			classLabel = "ROBE";
			
		} 
		
		//End of fall class labels
		//NON-fall ADL's
		else if(  fileName.equals(  "Lying-bed"  )  ){
			
			classLabel = "LYBE";
			
		} else if(  fileName.equals(  "Rising-bed"  )  ){
		
			classLabel = "RIBE";
			
		} else if(  fileName.equals(  "Sit-bed"  )  ){
		
			classLabel = "SIBE";
			
		} else if(  fileName.equals(  "Sit-chair"   )  ){
		
			classLabel = "SCH";
			
		} else if(  fileName.equals(  "Sit-sofa"  )  ){
		
			classLabel = "SSO";
			
		} else if(  fileName.equals(  "Sit-air"  )  ){
		
			classLabel = "SAI";
			
		} else if(  fileName.equals(  "Walking-forward"  )  ){
		
			classLabel = "WAF";
			
		} else if(  fileName.equals(  "Jogging"  )  ){
		
			classLabel = "JOF";
			
		} else if(  fileName.equals(  "Walking-backward"  )  ){
		
			classLabel = "WAB";
			
		} else if(  fileName.equals(  "Bending"  )  ){
		
			classLabel = "BEX";
			
		} else if(  fileName.equals(  "Bending-pick-up"  )  ){
		
			classLabel = "BEP";
			
		} else if(  fileName.equals(  "Stumble"  )  ){
		
			classLabel = "STU";
			
		} else if(  fileName.equals(  "Limp"  )  ){
		
			classLabel = "LIM";
			
		} else if(  fileName.equals(  "Squatting-down"  )  ){
		
			classLabel = "SQD";
			
		} else if(  fileName.equals(  "Trip-over"  )  ){
		
			classLabel = "TRO";
			
		} else if(  fileName.equals(  "Coughing-sneezing"  )  ){
		
			classLabel = "COSN";
			
		} 
		
		return classLabel;
	}
	
	public String getFileName(  ) {
		return fileName;
	}

	public void setFileName( String fileName ) {
		this.fileName = fileName;
	}

	public String getFilePath(  ) {
		return filePath;
	}

	public void setFilePath( String filePath ) {
		this.filePath = filePath;
	}

}
