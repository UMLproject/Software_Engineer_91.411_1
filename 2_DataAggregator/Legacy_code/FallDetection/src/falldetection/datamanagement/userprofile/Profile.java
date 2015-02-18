package falldetection.datamanagement.userprofile;

import android.os.Bundle;
import android.widget.Toast;

/* 
 * Currently this class just represents extremely basic characteristics of a data collector as
 * defined in "Monitor of human movements for fall detection and activities
 * recognition in elderly care using wireless sensor networks: a survey", Abbate et al.
 * 
 * Future work: Make this not a text file since it does extremely limit what I can do with it.
 * Use a database. 
 * Future work: Create a static method that takes a profile object and returns a bundle containing the data
 * 
 * NOTE: the format for the file is below, keep in mind that gender is written as 0 or 1 
 * 
 * weight
 * height
 * age
 * BMI
 * gender
 * 
 * Last updated: 7 / 21 / 2013
 * Author: Donald Buhl-Brown
 */

public class Profile {
	
	//Simple enumerated type to represent the gender
	public enum Gender {
		
		Male,
		Female
	}
	
	//Users weight
	public int weight;
	
	//Users height
	public String height;
	
	//Used to identify the user using the application
	public String identifier;

	//User age
	public int age;
	
	//Users Body Mass Index ( BMI )
	public int BMI;
	
	Gender gender;

	static final String AGE_KEY = "AGE";
	static final String BMI_KEY = "BMI";
	static final String WEIGHT_KEY = "WEIGHT";
	static final String HEIGHT_KEY = "HEIGHT";
	static final String GENDER_KEY = "GENDER";
	static final String IDENTIFIER_KEY = "IDENTIFIER";
	static final String PROFILE_FILE_NAME = "profiles.txt";
	
	public Profile( Gender gender, int weight, String height, int age, int BMI)
	{
		setGender( gender );
		setWeight( weight );
		setHeight( height );
		setAge( age );
		setBMI( BMI );
	}
	
	Profile( ){
		
		
	}
	
	//A utility method to help with reconstructing profiles because I don't have
	//the time to deal with serialization.
	static public Profile constructProfileFromBundle( Bundle bundle )
	{
		Profile profile = new Profile( );
		
		profile.setAge( bundle.getInt( Profile.AGE_KEY ) );
		profile.setBMI( bundle.getInt( Profile.BMI_KEY ) );
		profile.setHeight( bundle.getString( Profile.HEIGHT_KEY ) );
		profile.setWeight( bundle.getInt( Profile.WEIGHT_KEY ) );
		profile.setIdentifier( bundle.getString( Profile.IDENTIFIER_KEY ) );
		
		int tmp = bundle.getInt( Profile.GENDER_KEY );
		
		if( tmp == 0 ){
		
			profile.setGender( Profile.Gender.Male );
			
		} else {
			
			profile.setGender( Profile.Gender.Male );
		}
		
		return profile;
	}
	
	static public Bundle profileToBundle( Profile profile )
	{
		Bundle profileBundle = new Bundle( );

		profileBundle.putInt( Profile.AGE_KEY, profile.getAge( ) );
		profileBundle.putInt( Profile.BMI_KEY, profile.getBMI( ) );
		profileBundle.putInt( Profile.WEIGHT_KEY, profile.getWeight( ) );
		profileBundle.putCharSequence( Profile.HEIGHT_KEY, profile.getHeight( ) );
		profileBundle.putCharSequence( Profile.IDENTIFIER_KEY, profile.getIdentifier( ) );
		//Returns 0 for male 1 for female
		//I used the ternary operator to save room if the profile is male return 0 else 1
		profileBundle.putInt( Profile.GENDER_KEY, ( profile.getGender() == Profile.Gender.Male ) ? 0 : 1 );
		
		
		return profileBundle;
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getBMI() {
		return BMI;
	}

	public void setBMI(int bMI) {
		BMI = bMI;
	}
	
	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
	
		return getIdentifier();

	}
	
}
