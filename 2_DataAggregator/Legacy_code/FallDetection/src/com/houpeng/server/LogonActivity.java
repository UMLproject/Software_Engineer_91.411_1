package com.houpeng.server;

import java.io.IOException;

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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import falldetection.analysis.fall.R;

public class LogonActivity extends Activity {
	private Button logonButton = null;
	private EditText username, passwd;
	private ImageView clearUsername, clearPasswd;

	private boolean isNetError;
	private ProgressDialog proDialog;

	Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("loginState")){
				Toast.makeText(LogonActivity.this, "Login successfully!", Toast.LENGTH_LONG).show();
				finish();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(LogonActivity.this, "Login failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(LogonActivity.this, "Login failed. Please check your username or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	Handler uploadHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.getData().getBoolean("uploadState")){
				finish();
				Toast.makeText(LogonActivity.this, "Upload file successfully!", Toast.LENGTH_LONG).show();
			}else{
				isNetError = msg.getData().getBoolean("isNetError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (isNetError) {
					Toast.makeText(LogonActivity.this, " Upload file failed. Please your network!",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(LogonActivity.this, "Login failed. Please check your username or password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logon);

		logonButton = (Button)findViewById(R.id.logon_btn);
		clearUsername = (ImageView)findViewById(R.id.clear_username);
		clearPasswd = (ImageView)findViewById(R.id.clear_passwd);
		username = (EditText)findViewById(R.id.user_name);
		passwd = (EditText)findViewById(R.id.user_passwd);

		logonButton.setEnabled(true);
		logonButton.setBackgroundResource(R.drawable.logon_enable);
		init();


		logonButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String user_name = username.getText().toString();
				String user_passwd = passwd.getText().toString();

				if(user_name.equals("")){
					Toast.makeText(LogonActivity.this, "The user name cannot be empty.", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.equals("")){
					Toast.makeText(LogonActivity.this, "The password cannot be empty!", Toast.LENGTH_SHORT).show();
					return ;
				}

				if(user_passwd.length()<6){
					Toast.makeText(LogonActivity.this, "The length of the password should be more than six!", Toast.LENGTH_SHORT).show();
					passwd.setText("");
					return ;
				}

				Editor edit = getSharedPreferences("configs", 0).edit();
				edit.putString("user", user_name);
				edit.putString("passwd", user_passwd);
				edit.commit();

				proDialog = ProgressDialog.show(LogonActivity.this, "Connecting...",
						"Connecting..., wait for a second.", true, true);
				Thread loginThread = new Thread(new LoginFailureHandler());
				loginThread.start();
				
				
			}
		});

		clearPasswd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passwd.setText("");
			}
		});

		clearUsername.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				username.setText("");
			}
		});
	}

	private void init(){
		SharedPreferences preferences = getSharedPreferences("configs", 0);
		String user_name = preferences.getString("user", "");
		String user_passwd = preferences.getString("passwd", "");

		username.setText(user_name);
		passwd.setText(user_passwd);
	}

	private boolean validateLocalLogin(String userName, String password) throws JSONException, ClientProtocolException, IOException {
		boolean loginState = false;

		HttpClient httpClient = new DefaultHttpClient();  

		StringBuffer sb = new StringBuffer();
		sb.append("https://csr.cs.uml.edu/_api/?JSON=[{\"order\":1,\"call\":\"authenticateUser\",\"parameter\":[{\"usr_email\":\""+userName+"\",\"usr_pwd_1\":\""+password+"\"}]}]");
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
				loginState = true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return loginState;
	}

	class LoginFailureHandler implements Runnable {
		@Override
		public void run() {
			String userName = username.getText().toString();
			String password = passwd.getText().toString();

			boolean loginState = false;
			try {
				loginState = validateLocalLogin(userName, password);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(this.toString(), "validateLogin");

			if (loginState) {
				proDialog.dismiss();
				Log.d(this.toString(), "Login successfully!");

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("loginState", loginState);
				message.setData(bundle);
				loginHandler.sendMessage(message);
			} else {
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("isNetError", isNetError);
				bundle.putBoolean("loginState", loginState);
				message.setData(bundle);
				loginHandler.sendMessage(message);
				Log.d(this.toString(), "Login failed!");
			}
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();

		init();
	}

}
