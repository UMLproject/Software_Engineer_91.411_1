package com.houpeng.server;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import falldetection.analysis.fall.R;

public class ActivateMFAActivity extends Activity {
	private Button activateMFA = null;
	private EditText phoneMAC, userPIN;
	private ImageView clearphoneMAC, clearUserPIN;

	private boolean isNetError;
	private ProgressDialog proDialog;

	Handler activateMFAHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("activateMFAState")){
				Toast.makeText(ActivateMFAActivity.this, "ActivateMFA successfully!", Toast.LENGTH_LONG).show();
				finish();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(ActivateMFAActivity.this, "ActivateMFA failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(ActivateMFAActivity.this, "ActivateMFA failed. Please check your phoneMAC or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Handler uploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("uploadState")){
				finish();
				Toast.makeText(ActivateMFAActivity.this, "Upload file successfully!", Toast.LENGTH_LONG).show();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(ActivateMFAActivity.this, " Upload file failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(ActivateMFAActivity.this, "ActivateMFA failed. Please check your phoneMAC or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activate_mfa);

		activateMFA = (Button)findViewById(R.id.activate_mfa);
		phoneMAC = (EditText)findViewById(R.id.activate_mfa_mac);
		userPIN = (EditText)findViewById(R.id.activate_mfa_pin);

		clearphoneMAC = (ImageView)findViewById(R.id.activate_mfa_mac_clear);
		clearUserPIN = (ImageView)findViewById(R.id.activate_mfa_pin_clear);

		activateMFA.setEnabled(true);

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
		WifiInfo info = wifi.getConnectionInfo(); 
		phoneMAC.setText(info.getMacAddress());  

		activateMFA.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String user_name = phoneMAC.getText().toString().trim();
				String user_PIN = userPIN.getText().toString().trim();

				if(user_name.equals("")){
					Toast.makeText(ActivateMFAActivity.this, "The user name cannot be empty.", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_PIN.equals("")){
					Toast.makeText(ActivateMFAActivity.this, "The PIN cannot be empty!", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_PIN.length() != 6){
					Toast.makeText(ActivateMFAActivity.this, "The length of the PIN should be six!", Toast.LENGTH_SHORT).show();
					userPIN.setText("");
					
					return ;
				}

				proDialog = ProgressDialog.show(ActivateMFAActivity.this, "Connecting...",
						"Connecting..., wait for a second.", true, true);
				Thread ActivateMFAThread = new Thread(new ActivateMFAFailureHandler());
				ActivateMFAThread.start();


			}
		});

		clearUserPIN.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userPIN.setText("");
			}
		});

		clearphoneMAC.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				phoneMAC.setText("");
			}
		});
	}

	private boolean validateLocalActivateMFA(String phoneMAC, String PIN) throws JSONException, ClientProtocolException, IOException {
		boolean activateMFAState = false;

		HttpClient httpClient = new DefaultHttpClient();  

		StringBuffer sb = new StringBuffer();
		sb.append("https://csr.cs.uml.edu/_api/?JSON=[{\"order\":1,"
				+ "\"call\":\"activateMFA\","
				+ "\"parameter\":[{"
				+ "\"USR_PHONE\":\""+phoneMAC+
				"\",\"USR_PIN\":\""+PIN+
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

			if(result.contains("204")){
				activateMFAState = true;	
			
				Editor edit = getSharedPreferences("configs", 0).edit();
				edit.putInt("PIN", Integer.valueOf(PIN));
				edit.commit();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return activateMFAState;
	}

	class ActivateMFAFailureHandler implements Runnable {
		@Override
		public void run() {
			String phone_MAC = phoneMAC.getText().toString().trim();
			String PIN = userPIN.getText().toString().trim();

			boolean activateMFAState = false;
			try {
				activateMFAState = validateLocalActivateMFA(phone_MAC, PIN);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(this.toString(), "validateActivateMFA");

			if (activateMFAState) {
				proDialog.dismiss();
				Log.d(this.toString(), "ActivateMFA successfully!");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("activateMFAState", activateMFAState);
				message.setData(bundle);
				activateMFAHandler.sendMessage(message);
			} else {
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				bundle.putBoolean("activateMFAState", activateMFAState);
				message.setData(bundle);
				activateMFAHandler.sendMessage(message);
				Log.d(this.toString(), "ActivateMFA failed!");
			}
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

}
