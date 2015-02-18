package falldetection.datamanagement.datacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;
import falldetection.analysis.fall.R;

/*A class that allows the user to create their new profile, this
 * class is called from load profile where it can be selected using the menu key
 * 
 *Last Updated: 7 / 20 / 2013
 */

public class ProfileCreator extends Activity {

	EditText ageEditText;
	EditText BMIEditText;
	EditText weightEditText;
	EditText heightEditText;
	EditText userIdentifierEditText;
	
	//Represents whether the user is male or female
	RadioButton maleRadioButton;
	RadioButton femaleRadioButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		setContentView( R.layout.create_profile );
		
		ageEditText = ( EditText ) findViewById( R.id.ageEditText );
		BMIEditText = ( EditText ) findViewById( R.id.BMIEditText );
		weightEditText = ( EditText ) findViewById( R.id.weightEditText );
		heightEditText = ( EditText ) findViewById( R.id.heightEditText );
		userIdentifierEditText = ( EditText ) findViewById( R.id.userIdentifierEditText );
		
		
		maleRadioButton = ( RadioButton ) findViewById( R.id.maleRadioButton );
		femaleRadioButton = ( RadioButton ) findViewById( R.id.femaleRadioButton );
		 
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "inide back pressed", Toast.LENGTH_LONG).show( );

		writeOutProfile( );
		
		
		super.onBackPressed();
	}
	
	public void writeOutProfile( ){
		
		Log.i("inside","WRITE OUT");
		
		if( weightEditText.getText( ).toString( ).matches( "" ) || ageEditText.getText( ).toString( ).matches( "" ) || 
		    BMIEditText.getText( ).toString( ).matches( "" ) || heightEditText.getText( ).toString( ).matches( "" )){
			
			return;
		}
		
		File profileInputFile = new File( this.getFilesDir( ), Profile.PROFILE_FILE_NAME );
		
		if( !profileInputFile.exists() ){
			
			try {
			
				Log.i("inside writeOutProfile ", "creating file");
				
				profileInputFile.createNewFile( );
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedWriter profileWriter = null;
		
		try {
			
			Log.i("trying","buffered writer");
			
			profileWriter = new BufferedWriter(new FileWriter( profileInputFile,true ) );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
				
				Log.i("writing","FILEOUT");
				
				//The Integer.parse may not be necessary but files can be annoying.
				profileWriter.append( weightEditText.getText( ).toString( ) );
				profileWriter.append( "\r\n" );
				
				Log.i("writing", weightEditText.getText( ).toString( ) );
				
				profileWriter.append( heightEditText.getText( ).toString( ) );
				profileWriter.append( "\r\n" );
				
				Log.i("writing", heightEditText.getText( ).toString( ) );
				
				profileWriter.append( ageEditText.getText( ).toString( ) );
				profileWriter.append( "\r\n" );
				
				Log.i("writing", ageEditText.getText( ).toString( ) );
				
				profileWriter.append( BMIEditText.getText( ).toString( )  );
				profileWriter.append( "\r\n" );
				
				Log.i("writing", BMIEditText.getText( ).toString( ) );
				
				profileWriter.append( userIdentifierEditText.getText( ).toString( )  );
				profileWriter.append( "\r\n" );
				
				Log.i("writing", userIdentifierEditText.getText( ).toString( ) );
				
				if( maleRadioButton.isChecked() ){
					
					profileWriter.append( "0" );
					
				} else {
					
					profileWriter.append( "1" );
					 
				}
				
				profileWriter.append( "\r\n" );
				
				profileWriter.flush( );
				
				profileWriter.close();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
