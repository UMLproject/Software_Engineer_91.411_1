package com.houpeng.server;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import falldetection.analysis.fall.R;

public class RegisterActivity extends Activity {
	private Button saveButton;
	private EditText userName, userNameFirst, userNameMiddle, userNameLast, userPhoneArea;
	private EditText userPhoneCountry, userPhoneNumber, userPhoneExt, passwd, passwdAgain;
	private ImageView clearUserName, clearUserNameFirst, clearUserNameMiddle, clearUserNameLast, clearUserPhoneExt;
	private ImageView clearUserPhoneArea, clearPhoneNumber, clearUserPhoneCountry, clearPasswd, clearPasswdAgain;
	
	private boolean isNetError;
	private ProgressDialog proDialog;
	
	Handler registerHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("RegisterState")){
				Toast.makeText(RegisterActivity.this, "Register successfully!", Toast.LENGTH_LONG).show();
				finish();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(RegisterActivity.this, "Register failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(RegisterActivity.this, "Register failed. Please check your username or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);

		userName = (EditText)findViewById(R.id.register_user_name);
		userNameFirst = (EditText)findViewById(R.id.register_usr_name_first);
		userNameMiddle = (EditText)findViewById(R.id.register_usr_name_middle);
		userNameLast = (EditText)findViewById(R.id.register_usr_name_last);
		userPhoneArea = (EditText)findViewById(R.id.register_usr_phone_area);
		userPhoneCountry = (EditText)findViewById(R.id.register_usr_phone_country);
		userPhoneExt = (EditText)findViewById(R.id.register_usr_phone_ext);
		userPhoneNumber = (EditText)findViewById(R.id.register_usr_phone_number);
		passwd = (EditText)findViewById(R.id.register_usr_pwd_1);
		passwdAgain = (EditText)findViewById(R.id.register_usr_pwd_2);
		
		clearUserName = (ImageView)findViewById(R.id.clear_register_name);
		clearUserNameFirst = (ImageView)findViewById(R.id.clear_usr_name_first);
		clearUserNameMiddle = (ImageView)findViewById(R.id.clear_usr_name_middle);
		clearUserNameLast = (ImageView)findViewById(R.id.clear_usr_name_last);
		clearPhoneNumber = (ImageView)findViewById(R.id.clear_usr_phone_number);
		clearUserPhoneArea = (ImageView)findViewById(R.id.clear_usr_phone_area);
		clearUserPhoneCountry = (ImageView)findViewById(R.id.clear_usr_phone_country);
		clearUserPhoneExt = (ImageView)findViewById(R.id.clear_usr_phone_ext);
		clearPasswd = (ImageView)findViewById(R.id.clear_usr_pwd_1);
		clearPasswdAgain = (ImageView)findViewById(R.id.clear_usr_pwd_2);
		
		saveButton = (Button)findViewById(R.id.save_btn);

		saveButton.setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View arg0) {

				String user_name = userName.getText().toString().trim();
				String user_passwd = passwd.getText().toString().trim();
				String user_passwd_again = passwdAgain.getText().toString().trim();

				if(user_name.equals("")){
					Toast.makeText(RegisterActivity.this, "The user name cannot be empty, please try again!", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.equals("") || user_passwd_again.equals("")){
					Toast.makeText(RegisterActivity.this, "The password cannot be empty, please try again!¡", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(!user_passwd.equals(user_passwd_again)){
					Toast.makeText(RegisterActivity.this, "Two passwords do not match!¡", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.length()<6){
					Toast.makeText(RegisterActivity.this, "The length of the password must be more than six!", Toast.LENGTH_SHORT).show();
					return ;
				}

				Editor edit = getSharedPreferences("configs", 0).edit();
				edit.putString("user", user_name);
				edit.putString("passwd", user_passwd);
				edit.commit();

				proDialog = ProgressDialog.show(RegisterActivity.this, "Connecting...",
						"Connecting..., wait for a second.", true, true);
				Thread RegisterThread = new Thread(new RegisterFailureHandler());
				RegisterThread.start();
			}
		});


		clearUserName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userName.setText("");
			}
		});

		clearUserNameFirst.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userNameFirst.setText("");
			}
		});
		
		clearUserNameMiddle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userNameMiddle.setText("");
			}
		});

		clearUserNameLast.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userNameLast.setText("");
			}
		});

		clearUserPhoneArea.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userPhoneArea.setText("");
			}
		});

		clearUserPhoneCountry.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userPhoneCountry.setText("");
			}
		});

		clearUserPhoneExt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userPhoneExt.setText("");
			}
		});

		clearPhoneNumber.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userPhoneNumber.setText("");
			}
		});
	
		clearPasswd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passwd.setText("");
			}
		});

		clearPasswdAgain.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passwdAgain.setText("");
			}
		});
	}
	
	
	private boolean validateLocalRegister(String user_name, String user_name_first, String user_name_middle, String user_name_last, String user_phone_area,String user_phone_country
			,String user_phone_ext, String user_phone_number, String password, String password_again, String user_dob) throws JSONException, ClientProtocolException, IOException {
		boolean RegisterState = false;

		HttpClient httpClient = new DefaultHttpClient();  

		StringBuffer sb = new StringBuffer();
		sb.append("https://csr.cs.uml.edu/_api/?JSON=[{\"order\":1,\"call\":\"registerUser\",\"parameter\":[{"
				+ "\"usr_email\":\""+user_name+"\","
				+ "\"usr_name_first\":\""+user_name_first+"\","
				+ "\"usr_name_middle\":\""+user_name_middle+"\","
				+ "\"usr_name_last\":\""+user_name_last+"\","
				+ "\"usr_phone_country\":\""+user_phone_country+"\","
				+ "\"usr_phone_area\":\""+user_phone_area+"\","
				+ "\"usr_phone_number\":\""+user_phone_number+"\","
				+ "\"usr_phone_ext\":\""+user_phone_ext+"\","
				+ "\"usr_pwd_1\":\""+password+"\","
				+ "\"usr_pwd_2\":\""+password_again+"\","
				+ "\"usr_dob\":\""+user_dob+"\""
				+ "}]}]");
		Log.i("APP","sb: "+sb.toString());
		String urlparam = sb.toString().replace("\"", "%22")
				.replace("{", "%7b").replace("}", "%7d").replace("\r", "").replace("\n", "");	

		Log.i("APP","urlparam: "+urlparam);
		HttpGet getMethod = new HttpGet(urlparam);

		try {
			HttpResponse response = httpClient.execute(getMethod); 
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			Log.i("APP", "result = " + result);

			if(result.contains("204")){
				RegisterState = true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return RegisterState;
	}
	
	
	class RegisterFailureHandler implements Runnable {
		@Override
		public void run() {
           String user_name = userName.getText().toString().trim();
           String user_name_first = userNameFirst.getText().toString().trim();
           String user_name_middle = userNameMiddle.getText().toString().trim();
           String user_name_last = userNameLast.getText().toString().trim();
           String user_phone_area = userPhoneArea.getText().toString().trim();
           String user_phone_country = userPhoneCountry.getText().toString().trim();
           String user_phone_ext = userPhoneExt.getText().toString().trim();
           String user_phone_number = userPhoneNumber.getText().toString().trim();
           String password = passwd.getText().toString().trim();
           String password_again = passwdAgain.getText().toString().trim();
           
           SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");     
           String user_dob = sDateFormat.format(new java.util.Date());
           Log.i("APP", "userDob "+user_dob);

			boolean RegisterState = false;
			try {
				RegisterState = validateLocalRegister(user_name, user_name_first, user_name_middle, user_name_last, user_phone_area,user_phone_country
						,user_phone_ext, user_phone_number, password, password_again, user_dob);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(this.toString(), "validateRegister");

			if (RegisterState) {
				proDialog.dismiss();
				Log.d(this.toString(), "Register successfully!");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("RegisterState", RegisterState);
				message.setData(bundle);
				registerHandler.sendMessage(message);
			} else {
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				bundle.putBoolean("RegisterState", RegisterState);
				message.setData(bundle);
				registerHandler.sendMessage(message);
				Log.d(this.toString(), "Register failed!");
			}
		}

	}
	
	
	
	
}
