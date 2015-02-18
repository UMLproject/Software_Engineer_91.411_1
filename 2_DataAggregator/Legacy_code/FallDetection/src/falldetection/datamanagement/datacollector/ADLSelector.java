package falldetection.datamanagement.datacollector;

import falldetection.analysis.fall.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/* 
 * This application was designed to allow researchers to easily gather training and testing data
 * of multiple types of ADL's and upload the results to a local server. Simply select an ADL
 * after you have created / selected a profile. From there you can select start collecting
 * which will the begin collecting the data, minus 2 seconds before starting and stopping to
 * compensate for collection error with the tester getting into position, placing the phone down, etc.
 * Once you select stop the data will be uploaded to the remote server you have defined. You may also
 * pause the data collection at any time. In addition to just the accelerometer data, the profile and time
 * stamp data will also be recorded.
 * 
 * Last updated: 7 / 20 / 2013
 * Author: Donald Buhl-Brown
 */

public class ADLSelector extends Activity implements OnItemClickListener{

	static final String ADLSELECTOR_BUNDLE = "ADLSELECTOR_BUNDLE";
	static final String ADL_CLASS_KEY = "ADL_CLASS_KEY";
	
	boolean profileLoaded = false;
	
	Profile currentUserProfile = null;
	
	ListView ADLSelectionListView;
	
	ArrayAdapter< String > ADLSelectionArrayAdapter;
	
	String ADLClassToBeCollected = "";
	
	Bundle dataCollectorBundle;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		
		/* test case of my file uploading code
		DataFileWriter fileWriter = new DataFileWriter( this.getFilesDir( ).toString( ) );
		
		fileWriter.setFileName(Profile.PROFILE_FILE_NAME );
		
		DataFileUploader fileUploader = new DataFileUploader( fileWriter );
		
		fileUploader.uploadFile(  );
		
		*/
		
		//The values for this list view are stored in strings.xml
		ADLSelectionListView = ( ListView ) findViewById( R.id.ADLSelectionListView );
		
		ADLSelectionListView.setOnItemClickListener( this );
		
		}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater(  ).inflate( R.menu.activity_main, menu );
		return true;
	}

	//The index represents the class of the ADL currently being collected
	@Override
	public void onItemClick( AdapterView<?> arg0, View view, int index, long arg3 ) {
		// TODO Auto-generated method stub
		
		//The user cannot select an ADL until they have successfully loaded a profile
		if( !profileLoaded ){
			                 
			Toast.makeText(this, "You do not currently have a profile loaded, press the menu button", 
			Toast.LENGTH_LONG).show( );
			
			return;
		}

		ADLClassToBeCollected = (String) ((TextView) view).getText();
		
		initializeDataCollectorBundle( );
		
		Intent intent = new Intent( this, DataCollectorActivity.class );
		intent.putExtra( ADLSELECTOR_BUNDLE, dataCollectorBundle );
		
		startActivity( intent );
	}

	public void initializeDataCollectorBundle( ){
		
		dataCollectorBundle = Profile.profileToBundle( currentUserProfile );
		
		dataCollectorBundle.putCharSequence(ADLSelector.ADL_CLASS_KEY, ADLClassToBeCollected );
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		Log.i("INSIDE ONMENU ITEM",(String)item.getTitle() );
		
		//Loads the profileSelector class and allows the user to select a profile and returns the results
//		if(  item.getItemId(   ) == R.id.selectProfile  ){
//			
//			Intent intent = new Intent(  this, ProfileSelector.class  );
//			 
//			startActivityForResult(  intent, 1  );
//			
//		} else if( item.getItemId( ) == R.id.createProfile ){
//			
//			Intent intent = new Intent( this, ProfileCreator.class );
//			
//			startActivity( intent );
//		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		//The intent returns the profile class as independent variables
		//eventually I should use serialization but it's a small class so
		//it seemed to cumbersome for now.
		
		Bundle profileBundle = null;
		
		if( data != null ){
			
			Log.i("inside data!=null","DATA");
			
			profileBundle = new Bundle( );
			
			profileBundle = data.getExtras( );
		}
		
		if( profileBundle != null ){
			
			currentUserProfile = Profile.constructProfileFromBundle( profileBundle );
			
			Toast.makeText( this, "Profile Loaded!", Toast.LENGTH_LONG).show( );
			
			profileLoaded = true;
			
			super.onActivityResult( requestCode, resultCode, data );
		}
	}
}
