package com.houpeng.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import falldetection.analysis.fall.R;

public class UploadFileActivity extends Activity {
	private Button uploadFileBtn = null;
	
	private String mPepper, mSalt;
	private long mDate;
    private int mPIN;
    private String mToken;

	private boolean isNetError;
	private ProgressDialog proDialog;

	Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("uploadFileState")){
				Toast.makeText(UploadFileActivity.this, "UploadFile successfully!", Toast.LENGTH_LONG).show();
				
				Thread uploadThread = new Thread(new UploadFailureHandler());
				uploadThread.start();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(UploadFileActivity.this, "UploadFile failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(UploadFileActivity.this, "UploadFile failed. Please check your username or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Handler uploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("uploadState")){
				finish();
				Toast.makeText(UploadFileActivity.this, "Upload file successfully!", Toast.LENGTH_LONG).show();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(UploadFileActivity.this, " Upload file failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(UploadFileActivity.this, "UploadFile failed. Please check your username or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upload_file_mfa);


		uploadFileBtn = (Button)findViewById(R.id.activate_mfa);
		uploadFileBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				SharedPreferences preferences = getSharedPreferences("configs", 0);
			    mPepper = preferences.getString("pepper", "");
			    mSalt = preferences.getString("salt", "");
			    mDate = preferences.getLong("date", 0);
			    mPIN = preferences.getInt("PIN", 0);
			    
			    if(mPepper.equals("")){
			    	Toast.makeText(UploadFileActivity.this, "the pepper is empty! Please register MFA first.", Toast.LENGTH_SHORT).show();
			    	finish();
			    	return ;
			    }
			    
			    if(mSalt.equals("")){
			    	Toast.makeText(UploadFileActivity.this, "the salt is empty! Please register MFA first.", Toast.LENGTH_SHORT).show();
			    	finish();
			    	return ;
			    }
			    
			    if(mPIN == 0){
			    	Toast.makeText(UploadFileActivity.this, "the PIN is empty! Please register MFA first.", Toast.LENGTH_SHORT).show();
			    	finish();
			    	return ;
			    }
			    
			    try {
					mToken = calculateToken(mPepper, mSalt, mDate, mPIN);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// return token.genToken() ;
				Log.i("APP", "Token: "+mToken);

				proDialog = ProgressDialog.show(UploadFileActivity.this, "Connecting...",
						"Connecting..., wait for a second.", true, true);
				Thread loginThread = new Thread(new UploadFailureHandler());
				loginThread.start();
			}
		});
	}

	private String  calculateToken(String pepper, String salt, long date, int PIN) throws Exception{
		CSRToken token = new CSRToken() ;
		// Set all the necessary fields 
		token.setMfaDeviceSalt(salt);
		token.setMfaDevicePepper(pepper);
		token.setMfaDeviceDate(date);
		token.setMfaDevicePin(PIN);
		
		return token.genToken();
	}

	private boolean uploadLocalFile(String filepath) throws JSONException, ClientProtocolException, IOException {
		boolean uploadState = false;

		String phoneMAC = "";
		String MIME = "text/plain";
		File file = new File(filepath);
		if(!file.exists()){
			Log.i("APP", "File is not exsit.");
			return false;
		}else{
			Log.i("APP", "File"+filepath+" is  exsit.");
		}

		FileInputStream fis = new FileInputStream(new File(filepath));
		StringBuffer fileContent = new StringBuffer("");

		byte[] buffer = new byte[1024];
		int num = 0;

		while ((num = fis.read(buffer)) != -1) { 
			fileContent.append(new String(buffer, 0, num)); 
		}
		String data = Base64.encodeToString(fileContent.toString().getBytes(), Base64.DEFAULT);
		
		HttpClient httpClient = new DefaultHttpClient();  

		StringBuffer sb = new StringBuffer();

		sb.append("https://csr.cs.uml.edu/_api/?JSON="
				+ "[{\"order\":1,"
				+ "\"call\":\"authenticateMFA\","
				+ "\"parameter\":"
				+ "[{\"USR_PHONE\":\""+phoneMAC+"\","
				+"\"USR_TOKEN\":\""+mToken+"\","
				+ "\"ENCODING\":\"base64\"}]}"
								+ ",[{\"order\":2,"
				+ "\"call\":\"storeFile\","
				+ "\"parameter\":"
				+ "[{\"MIME\":\""+MIME+"\","
				+"\"USR_TOKEN\":\""+mToken+"\","
				+"\"DATA\":\""+data+"\","
				+ "\"ENCODING\":\"base64\"}]}]");;
				
		Log.i("APP","sb: "+sb.toString());
		String urlparam = sb.toString().replace("\"", "%22")
				.replace("{", "%7b").replace("}", "%7d").replace("\r", "").replace("\n", "");	

		Log.i("APP","urlparam: "+urlparam);
		HttpGet getMethod = new HttpGet(urlparam);

		try {
			HttpResponse response = httpClient.execute(getMethod); 
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			Log.i("APP", "result = " + result);

			if(result.contains("201")){
				uploadState = true;
				Log.i("APP","201: File created.");
			}else if(result.contains("400")){
				Log.i("APP","400: Bad request.");
			}else if(result.contains("401")){
				Log.i("APP","401: Unauthorized.");
			}else if(result.contains("409")){
				Log.i("APP","409: Conflict.");
			}else if(result.contains("415")){
				Log.i("APP","415: Invalid File Type.");
			}else if(result.contains("500")){
				Log.i("APP","500: Server error!");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return uploadState;
	}

	class UploadFailureHandler implements Runnable {
		@Override
		public void run() {
			String filepath = Environment.getExternalStorageDirectory()+"/test/"+"test.txt";

			boolean uploadState = false;
			try {
				uploadState = uploadLocalFile(filepath);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (uploadState) {
				proDialog.dismiss();

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("uploadState", uploadState);
				message.setData(bundle);
				uploadHandler.sendMessage(message);
			} else {
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				bundle.putBoolean("uploadState", uploadState);
				message.setData(bundle);
				uploadHandler.sendMessage(message);
				Log.d(this.toString(), "Upload file failed!");
			}
		}

	}

	public class CSRToken {
		 
		////
		// Variables

		// Encoding Variables
		private String 	encryption ;// The encryption algorithm to use
		private String 	encoding ; 	// The encoding of the characters

		// Window validity length
		private int 	iteration ; // 1 is the current token, each increase goes back one precision
		private int 	precision ; // Gives a (2x) second maximum window with a life span of the value
		
		//	Device Values
		private String 	mfa_device_salt ;
		private String 	mfa_device_pepper ;
		private int 	mfa_device_pin ;
		private long 	mfa_device_date ;

		// Constructor 
		
		/**
		 * CSRToken
		 * 
		 * Default constructor
		 */
		public CSRToken( ) {
	    	setEncryption( "SHA-256" ) ;
	    	setEncoding( "UTF-8" ) ;  
	    	setIteration( 1 ) ;
	    	setPrecision( 15 ) ;  
	    }
		
		/**
		 * CSRToken
		 * 
		 * Default constructor
		 * 
		 * @param 	encryption		The encryption algoritm
		 * @param 	encoding		The character encoding
		 * @param 	iteration		The number of cycles to check startting at 0
		 * @param 	precision		The time length of a cycle in seconds
		 */
	    public CSRToken( String encryption , 	
	    				 String encoding  , 
	    				 int iteration  ,
	    				 int precision  ) {
	    	// Store variables 
	    	setEncryption( encryption ) ;
	    	setEncoding( encoding ) ;  
	    	setIteration( iteration ) ;
	    	setPrecision( precision ) ;  
	    }   
	    
	    // Setters
	    
	    public void		setIteration( int iteration ){
	    	this.iteration = iteration ;
	    }
	    public void		setPrecision( int precision ){
	    	this.precision = precision ;
	    }
	    public void 	setMfaDevicePin( int mfa_device_pin ){
	    	this.mfa_device_pin = mfa_device_pin ;
	    }
	    
	    public void 	setEncryption( String encryption ) {
	    	this.encryption = encryption ;
	    }
	    public void 	setEncoding( String encoding ) {
	    	this.encoding = encoding ;
	    }
	    public void 	setMfaDeviceSalt( String mfa_device_salt ){
	    	this.mfa_device_salt = mfa_device_salt ;
	    }
	    public void 	setMfaDevicePepper( String mfa_device_pepper ){
	    	this.mfa_device_pepper = mfa_device_pepper ;
	    }
	    public void 	setMfaDeviceDate( long mfa_device_date ){
	    	this.mfa_device_date = mfa_device_date ;
	    }
	    
	    // Getters
	    
	    public int		getIteration( ){
	    	return this.iteration ;
	    }
	    public int		getPrecision( ){
	    	return this.precision ;
	    }
	    public int 		getMfaDevicePin( ){
	    	return this.mfa_device_pin ;
	    }
	    
	    public String 	getEncryption( ) {
	    	return this.encryption ;
	    }
	    public String 	getEncoding( ) {
	    	return this.encoding ;
	    }
	    public String 	getMfaDeviceSalt( ){
	    	return this.mfa_device_salt ;
	    }
	    public String 	getMfaDevicePepper( ){
	    	return this.mfa_device_pepper ;
	    }
	    public long 	getMfaDeviceDate( ){
	    	return this.mfa_device_date ;
	    }
	   
	    
	    public long    	genTime( ) {
	    	return System.currentTimeMillis() / 1000 ;
	    }
	    
	    public String 	genToken( ) throws Exception  {
	    	
	    	long epoch = genTime() ;
	    	
	    	try {
	    		MessageDigest md = MessageDigest.getInstance( this.encryption ) ;
	   	    	
		    	String text = this.mfa_device_salt + 
					 this.mfa_device_date +
					 (int) ( ( epoch + ( this.iteration * this.precision ) ) / this.precision ) +
					 this.mfa_device_pin + 
					 this.mfa_device_pepper ;
		    	
		    	System.out.printf( "%s" , text + "\n" ) ;
		    			
		    	md.update( text.getBytes( this.encoding ) ) ; 
	    	
		    	return getHexString(md.digest()) ;

		    	
	    	} catch ( NoSuchAlgorithmException e ) {
	    		System.err.println( "NoSuchAlgorithmException" ) ;
	    	} catch ( UnsupportedEncodingException e) {
	    		System.err.println( "UnsupportedEncodingException" ) ;
			}
	    	return null ;
	    }
	    
		public String getHexString(byte[] bytes) throws Exception {
			String hexStr = "";

			for(int i = 0; i < bytes.length; ++i) {
				hexStr = hexStr + Integer.toString(256 + (255 & bytes[i]), 16).substring(1);
			}

			return hexStr;
		}
	}
}

