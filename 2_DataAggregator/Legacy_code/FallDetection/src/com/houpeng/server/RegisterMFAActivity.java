package com.houpeng.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import falldetection.analysis.fall.R;

public class RegisterMFAActivity extends Activity {
	private Button registerMFA = null;
	private EditText phoneMAC, userEmail, passwd;
	private ImageView clearphoneMAC, clearUserEmail, clearPasswd;

	private boolean isNetError;
	private ProgressDialog proDialog;

	Handler registerMFAHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("registerMFAState")){
				Toast.makeText(RegisterMFAActivity.this, "RegisterMFA successfully!", Toast.LENGTH_LONG).show();
				finish();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(RegisterMFAActivity.this, "RegisterMFA failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(RegisterMFAActivity.this, "RegisterMFA failed. Please check your phoneMAC or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Handler uploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("uploadState")){
				finish();
				Toast.makeText(RegisterMFAActivity.this, "Upload file successfully!", Toast.LENGTH_LONG).show();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(RegisterMFAActivity.this, " Upload file failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(RegisterMFAActivity.this, "RegisterMFA failed. Please check your phoneMAC or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_mfa);

		registerMFA = (Button)findViewById(R.id.register_mfa);
		phoneMAC = (EditText)findViewById(R.id.register_mfa_mac);
		userEmail = (EditText)findViewById(R.id.register_mfa_email);
		passwd = (EditText)findViewById(R.id.register_mfa_passwd);

		clearphoneMAC = (ImageView)findViewById(R.id.register_mfa_mac_clear);
		clearUserEmail = (ImageView)findViewById(R.id.register_mfa_email_clear);
		clearPasswd = (ImageView)findViewById(R.id.register_mfa_passwd_clear);

		registerMFA.setEnabled(true);

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
		WifiInfo info = wifi.getConnectionInfo(); 
		phoneMAC.setText(info.getMacAddress());  

		registerMFA.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String user_name = phoneMAC.getText().toString().trim();
				String user_passwd = passwd.getText().toString().trim();

				if(user_name.equals("")){
					Toast.makeText(RegisterMFAActivity.this, "The user name cannot be empty.", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.equals("")){
					Toast.makeText(RegisterMFAActivity.this, "The password cannot be empty!", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.length() < 6){
					Toast.makeText(RegisterMFAActivity.this, "The length of the password should more than be six!", Toast.LENGTH_SHORT).show();
					passwd.setText("");
					return ;
				}

				proDialog = ProgressDialog.show(RegisterMFAActivity.this, "Connecting...",
						"Connecting..., wait for a second.", true, true);
				Thread RegisterMFAThread = new Thread(new RegisterMFAFailureHandler());
				RegisterMFAThread.start();


			}
		});

		clearPasswd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passwd.setText("");
			}
		});

		clearphoneMAC.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				phoneMAC.setText("");
			}
		});

		clearUserEmail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userEmail.setText("");
			}
		});
	}

	private boolean validateLocalRegisterMFA(String phoneMAC, String userEmail, String password) throws JSONException, ClientProtocolException, IOException {
		boolean registerMFAState = false;

		HttpClient httpClient = new DefaultHttpClient();  

		StringBuffer sb = new StringBuffer();

		sb.append("https://csr.cs.uml.edu/_api/?JSON=[{\"order\":1,"
				+ "\"call\":\"registerMFA\","
				+ "\"parameter\":[{"
				+ "\"USR_PHONE\":\""+phoneMAC+
				"\",\"USR_EMAIL\":\""+userEmail+
				"\",\"USR_PWD\":\""+password+
				"\"}]}]");

		Log.i("APP","sb: "+sb.toString());
		String urlparam = sb.toString().replace("\"", "%22")
				.replace("{", "%7b").replace("}", "%7d").replace("\r", "").replace("\n", "");	

		Log.i("APP","urlparam: "+urlparam);
		HttpGet getMethod = new HttpGet(urlparam);

		try {
			HttpResponse response = httpClient.execute(getMethod); 
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			Log.i("APP", "result = " + result);

			if(result.contains("200") && result.contains("OK")){
				registerMFAState = true;

				JSONArray array = new JSONArray(result);
				JSONObject jsonObject = (JSONObject) array.get(0);
				JSONObject tempJsonObject = jsonObject.getJSONObject("value");

				String pepper = tempJsonObject.getString("pepper");
				String salt = tempJsonObject.getString("salt");
				long date = tempJsonObject.optLong("time");

				Log.i("APP", "pepper: "+pepper+"  salt: "+salt+"  date:"+date);
				Editor edit = getSharedPreferences("configs", 0).edit();
				edit.putString("pepper", pepper);
				edit.putString("salt", salt);
				edit.putLong("date", date);
				edit.commit();


			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return registerMFAState;
	}

	class RegisterMFAFailureHandler implements Runnable {
		@Override
		public void run() {
			String phone_MAC = phoneMAC.getText().toString().trim();
			String user_email = userEmail.getText().toString().trim();
			String password = passwd.getText().toString().trim();

			boolean registerMFAState = false;
			try {
				registerMFAState = validateLocalRegisterMFA(phone_MAC, user_email, password);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(this.toString(), "validateRegisterMFA");

			if (registerMFAState) {
				proDialog.dismiss();
				Log.d(this.toString(), "RegisterMFA successfully!");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("registerMFAState", registerMFAState);
				message.setData(bundle);
				registerMFAHandler.sendMessage(message);
			} else {
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				bundle.putBoolean("registerMFAState", registerMFAState);
				message.setData(bundle);
				registerMFAHandler.sendMessage(message);
				Log.d(this.toString(), "RegisterMFA failed!");
			}
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

}
