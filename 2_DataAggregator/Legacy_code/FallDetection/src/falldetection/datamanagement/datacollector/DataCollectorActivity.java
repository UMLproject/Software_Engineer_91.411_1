package falldetection.datamanagement.datacollector;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import falldetection.analysis.fall.R;

public class DataCollectorActivity extends Activity implements OnClickListener, SensorEventListener{

	private Button startButton;
	private Button stopButton;
	private Button saveUploadButton;
	
	private SensorManager sensorManager;
	
	private Sensor accelerometer;
	
	//Holds the accelerometer values generated during ADL performance
	private ArrayList< AccelerometerValue > accelerometerValues;
	
	private Profile currentUserProfile;
	
	//Represents the current ADLClass being performed
	private String ADLClass = "";
	
	//Keeps users from pressing start again while performing ADL's
	private boolean startIsPressed = false;
	
	//Represents the speed to run the accelerometer at
	private int speedConfigurations;
	private String speedSelection;
	//public TextView statusTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView( R.layout.data_collecting );
		
		//According to Ariel Keller DELAY_UI is the best option for
		//fall detection.
		speedConfigurations = SensorManager.SENSOR_DELAY_UI;
		speedSelection = "UI";
		
		Bundle dataBundle = this.getIntent( ).getBundleExtra( ADLSelector.ADLSELECTOR_BUNDLE );
		
		//statusTextView = ( TextView ) findViewById( R.id.statusTextView );
		
		//statusTextView.setText( "NOT COLLECTING" );
		
		if( dataBundle != null ){
			
			Log.i("INSIDE DATABUNDLE","DATABUNDLE");
			
			currentUserProfile = Profile.constructProfileFromBundle( dataBundle );
			
			ADLClass = (String) dataBundle.getCharSequence( ADLSelector.ADL_CLASS_KEY );
		}
		
		startButton  = ( Button ) findViewById( R.id.startCollectingButton );
		stopButton = ( Button ) findViewById( R.id.stopCollectingButton );
		saveUploadButton = ( Button ) findViewById( R.id.SaveUploadButton );
		
		startButton.setOnClickListener( this );
		stopButton.setOnClickListener( this );
		saveUploadButton.setOnClickListener( this );
		
		sensorManager = ( SensorManager ) getSystemService( SENSOR_SERVICE );
		accelerometer = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
		
		accelerometerValues = new ArrayList<AccelerometerValue>( );
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if( v.getId( ) == R.id.startCollectingButton ){
			
			Log.i("inside onClick","CLICKED");
			
			if( startIsPressed ){
				
				return;
			}
			
			setTitle( "COLLECTING ");
			
			startIsPressed = true;
			
			sensorManager.registerListener(this, accelerometer, speedConfigurations );
			
		} else if( v.getId( ) == R.id.stopCollectingButton ){
			
			sensorManager.unregisterListener( this );
		
			setTitle( "NOT COLLECTING" );
			
			startIsPressed = false;
			
		} else {
			
			startIsPressed = false;
			
			DataFileWriter fileWriter = new DataFileWriter( this.getFilesDir( ).toString(), ADLClass, currentUserProfile );
			
			fileWriter.writeOutAllInformation( accelerometerValues, "NA", speedSelection );

			DataFileUploader fileUploader = new DataFileUploader( fileWriter );
			fileUploader.uploadFile( );
			
			accelerometerValues.clear( );
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		float[] values = event.values;
	
        double x = values[0];
        
        double y = values[1];
        
        double z = values[2];
        
        accelerometerValues.add( new AccelerometerValue(x, y, z, event.timestamp ) );
        
        Log.i("X = ", String.valueOf( x ) );
        Log.i("Y = ", String.valueOf( y ) );
        Log.i("Z = ", String.valueOf( z ) );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
//		if( item.getItemId( ) == R.id.fastestSpeed ){
//			
//			speedConfigurations = SensorManager.SENSOR_DELAY_FASTEST;
//			speedSelection = "FASTEST";
//			
//		} else if( item.getItemId( ) == R.id.gameSpeed ){
//			
//			speedConfigurations = SensorManager.SENSOR_DELAY_GAME;
//			speedSelection = "GAME";
//			
//		} else if( item.getItemId( ) == R.id.UISpeed ){
//			
//			speedConfigurations = SensorManager.SENSOR_DELAY_UI;
//			speedSelection = "UI";
//			
//		} else if( item.getItemId( ) == R.id.NormalSpeed ){
//			
//			speedConfigurations = SensorManager.SENSOR_DELAY_NORMAL;
//			speedSelection = "NORMAL";
//		}
		 
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater(  ).inflate( R.menu.speed_selection_menu, menu );
		
		
		
		return true;
	}
}
