package falldetection.datamanagement.datacollector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import falldetection.analysis.fall.R;

public class ProfileSelector extends Activity implements OnItemClickListener {

	ListView loadProfilesListView;
	
	ArrayAdapter<Profile> loadProfilesArrayAdapter;
	
	//An array that holds each profile loaded from the phones memory
	ArrayList<Profile> loadedProfiles;
	
	Profile selectedProfile;
	
	boolean noProfilesExist = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView( R.layout.load_profile );
		
		Log.i("inside oncreate","inside profileselector");
		
		//Appears to be erroring
		loadProfilesListView = (ListView) findViewById( R.id.loadProfilesListView );
		loadProfilesListView.setOnItemClickListener( this );
		
		
		Log.i("after loadProfilesListView","after loadProfilesList");
		
		loadedProfiles = new ArrayList<Profile>(); //Holds the profiles read from memory
		
		//Synchronizes the list view and the loadedProfiles arraylist.
		
		Log.i("before loadProfilesArrayAdapter","after arrayList");
		
		loadProfilesArrayAdapter = new ArrayAdapter< Profile >( this, android.R.layout.simple_list_item_1, loadedProfiles );
		
		Log.i("after arrayAdapter","after arrayAdapter");
		
		loadProfilesListView.setAdapter( loadProfilesArrayAdapter );
		
		Log.i( "before profiles", "left" );
		
		loadProfiles( );
		
		super.onCreate(savedInstanceState);
	}

	//Loads the profiles stored in memory into the listView
	public void loadProfiles( ) {
		
		//Will hold our temporary profile information until it is inserted to the list
		Profile tmpProfile = new Profile( );
		
		File profileFile = new File( this.getFilesDir( ), Profile.PROFILE_FILE_NAME );
		
		//Detects that the file exists, also tests to make sure profiles have been created
		if( !profileFile.exists() ){
			
			Toast.makeText(this, "You do not currently have any profiles. Create one by pressing the menu button on the main screen",
			Toast.LENGTH_LONG).show( );
				
			noProfilesExist = true;
			
			return;
		}
		
		Scanner profileInputScanner = null;
		
		try {
			
			Log.i("inside try","before scanner");
			
			profileInputScanner = new Scanner( profileFile );
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if( profileInputScanner == null){
			
			Toast.makeText(this, "The input stream is null. This really shouldn't be happening.",
			Toast.LENGTH_LONG).show( );
		}
		
		while( profileInputScanner.hasNext() ){
			
			tmpProfile = new Profile( );
			
			Log.i("inside while","reading G");
			
			tmpProfile.weight = Integer.parseInt( profileInputScanner.next( ) );
			tmpProfile.height = profileInputScanner.next( );
			tmpProfile.age = Integer.parseInt( profileInputScanner.next( ) );
			tmpProfile.BMI = Integer.parseInt( profileInputScanner.next( ) );
			tmpProfile.identifier = profileInputScanner.next( ); 
			
			int tmp;
			
			tmp = Integer.parseInt( profileInputScanner.next( ) );
			
			if( tmp == 0){
				
				tmpProfile.gender = Profile.Gender.Male;
			} else {
				
				tmpProfile.gender = Profile.Gender.Female;
			}

			loadedProfiles.add( tmpProfile );
		
			Log.i("added profile","LIST");
		}
		
		Log.i("done reading","profiles");
		
		loadProfilesArrayAdapter.notifyDataSetChanged( );
		
		Log.i("done notifying","list view");
		
		profileInputScanner.close( );
		
		Log.i("ending method","profiles");
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		// TODO Auto-generated method stub
		
		try{
			
			Log.i("inside onItemClick","ITEM CLICK");
			
			selectedProfile = loadedProfiles.get( index );
			
			Intent intent = new Intent( );
			
			intent.putExtra( Profile.AGE_KEY, selectedProfile.getAge( ) );
			intent.putExtra( Profile.BMI_KEY, selectedProfile.getBMI( ) );
			intent.putExtra( Profile.WEIGHT_KEY, selectedProfile.getWeight( ) );
			intent.putExtra( Profile.HEIGHT_KEY, selectedProfile.getHeight( ) );
			intent.putExtra( Profile.IDENTIFIER_KEY, selectedProfile.getIdentifier( ) );
			//Returns 0 for male 1 for female
			//I used the ternary operator to save room if the profile is male return 0 else 1
			intent.putExtra( Profile.GENDER_KEY, ( selectedProfile.getGender() == Profile.Gender.Male ) ? 0 : 1 );
			
			setResult( 1, intent );
			
			finish();
		}
		catch ( Exception e ){
			
			Log.i("inside exception","ITEM CLICK");
		}
	}
	
	@Override
	public void onBackPressed() {
		
		//Attempt to keep the back button from crashing
		if( noProfilesExist ){
			
			Intent intent = new Intent( );
			
			setResult( 1, intent );

			super.onBackPressed();
			
		} else{
			
			try{
				Intent intent = new Intent( );
				
				intent.putExtra( Profile.AGE_KEY, selectedProfile.getAge( ) );
				intent.putExtra( Profile.BMI_KEY, selectedProfile.getBMI( ) );
				intent.putExtra( Profile.WEIGHT_KEY, selectedProfile.getWeight( ) );
				intent.putExtra( Profile.HEIGHT_KEY, selectedProfile.getHeight( ) );
				intent.putExtra( Profile.IDENTIFIER_KEY, selectedProfile.getIdentifier( ) );
				//Returns 0 for male 1 for female
				//I used the ternary operator to save room if the profile is male return 0 else 1
				intent.putExtra( Profile.GENDER_KEY, ( selectedProfile.getGender() == Profile.Gender.Male ) ? 0 : 1 );
				
				setResult( 1, intent );
				
			} catch( Exception e){
				
				
			}
			
			
			super.onBackPressed();
		}
	}

}
