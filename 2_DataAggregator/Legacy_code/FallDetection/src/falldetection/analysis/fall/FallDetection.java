package falldetection.analysis.fall;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import com.houpeng.server.ActivateMFAActivity;
import com.houpeng.server.LogonActivity;
import com.houpeng.server.RegisterActivity;
import com.houpeng.server.RegisterMFAActivity;
import com.houpeng.server.UploadFileActivity;

import falldetection.analysis.fall.SensorService.refreshLisener;
import falldetection.communication.timeseries.DeviceListActivity;
import falldetection.communication.timeseries.DownloadService;
import falldetection.datamanagement.utilily.Global;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import falldetection.analysis.fall.R;
import falldetection.thirdparty.Counter;
import falldetection.thirdparty.ICounterService;
import falldetection.thirdparty.SystemInfo;
import falldetection.thirdparty.UMLoggerService;
import falldetection.thirdparty.UidInfo;
import android.content.SharedPreferences;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import ca.mcgill.hs.prefs.HSAndroidPreferences;
import ca.mcgill.hs.prefs.PreferenceFactory;
import ca.mcgill.hs.serv.HSService;

/**
 * 4-5: Should the onStop/Pause/Destroy methods stop the service or allow it to continue running in the background?
 */

/**
 * This is an activity class that shows the GUI-interface of the application.
 * Users can choose the sensitivity level, telephone number and an optional
 * e-mail address for which a fall alarm will be sent to. This is also where the
 * user starts and stops the application for fall monitoring.
 */
public class FallDetection extends Activity implements OnItemClickListener {
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	public static final int REQUEST_ENABLE_BT = 3;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int MEDIA_TYPE_TEXT = 3;
	public static final int MEDIA_TYPE_AUDIO = 4;

	private BluetoothAdapter bluetoothAdapter;
	
	//power.txt 
	private final static int SAMPLE_TIME = 2;

	private static double[] last_key = new double[100];
	private static double[] keys = new double[100];
	//	public static long last_total = 0;


	private static final boolean debug = false;
	private static Button start_service1;
	private static Button log_msg_btn;
	private static Button clear_msg_btn;
	private Button registerBtn, loginBtn;
	private Button registerMFA, activateMFA, uploadFile;
	private static boolean showingMessages=false;
	private static boolean savingData=false;
	private TextView log_messages;
	private ListView ADLListView;

	private String ADLClassChosen = "";

	private static Button serviceSwitch;

	/**
	 * Whether to start the service automatically when the application is
	 * loaded.
	 */
	private boolean autoStartAppStart = false;

	/**
	 * Whether to use Debug.MethodTracing to collect profiling information. Make
	 * sure that this is false for a release version!
	 */
	private static final boolean doProfiling = false;

	public static final String HSANDROID_PREFS_NAME = "HSAndroidPrefs";
	private static Context context = null;
	private static TableLayout freeSpace = null;

	private static final int MENU_SETTINGS = 13371337;
	private static final int MENU_UPLOAD = 13371338;

	private static final String TAG = "HSAndroid";


	/**
	 * This variable is true when the SensorService is started, false otherwise.
	 */
	private static Boolean service1Started;
	/**
	 * These two variables keep the phone from fully going to sleep. Note: We
	 * might want to experiment with removing this completely if not needed or
	 * moving it to the SensorService.
	 */
	private PowerManager pm;
	private PowerManager.WakeLock w1;

	//This variable is used to save data to the phone.
	private String str1="";

	//PengHou
	private Intent serviceIntent;
	public static ICounterService counterService;
	private CounterServiceConnection conn;
	public static int uid = 0;

	/**
	 * Fetches a resource string for the specified id from the application
	 * context.
	 * 
	 * @param resourceId
	 *            for the string to be fetched.
	 * @return String corresponding to the resourceId
	 */
	public static String getAppString(final int resourceId) {
		return context.getString(resourceId);
	}

	/**
	 * Returns a handle to the free space in the main screen where plugins can
	 * add messages or widgets.
	 * 
	 * @return Handle to the free space on the main application layout.
	 */
	public static TableLayout getFreeSpace() {
		return freeSpace;
	}

	public static File getStorageDirectory() {
		final String state = Environment.getExternalStorageState();
		boolean externalStorageAvailable = false;
		boolean externalStorageWriteable = false;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			externalStorageAvailable = true;
			externalStorageWriteable = false;
		} else {
			externalStorageAvailable = externalStorageWriteable = false;
		}
		if (externalStorageAvailable && externalStorageWriteable) {
			return context.getExternalFilesDir(null);
		} else {
			return context.getFilesDir();
		}
	}

	/**
	 * Updates the main starting button according to whether the service is
	 * running or not. Should be called whenever the state of the service is
	 * changed. Probably a way to do this using Intents or events, but for now
	 * we rely on it being called manually.
	 */
	public static void updateButton() {
		if (serviceSwitch != null) {
			if (HSService.isRunning()) {
				serviceSwitch.setText(R.string.stop_label);
			} else {
				serviceSwitch.setText(R.string.start_label);
			}
		}
	}

	/**
	 * Retrieves the current state of the main application preferences, which
	 * control whether log data is stored in a file and whether the service
	 * automatically starts when the application is loaded.
	 */
	private void getPrefs() {
		final SharedPreferences prefs = PreferenceFactory
				.getSharedPreferences(this);
		autoStartAppStart = prefs.getBoolean(
				HSAndroidPreferences.AUTO_START_AT_APP_START_PREF, false);
		final boolean logToFile = prefs.getBoolean(
				HSAndroidPreferences.LOG_TO_FILE_PREF, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (debug)
			Log.i(TAG, "oncreate() started");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ApplicationInfo info;
		try {
			info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
			uid = info.uid;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		log_messages = (TextView) findViewById(R.id.log_messages);
		start_service1 = (Button) findViewById(R.id.start_service1);
		log_msg_btn = (Button) findViewById(R.id.log_msg_btn);
		clear_msg_btn= (Button) findViewById(R.id.clear_msg_btn);
		TextView tv = (TextView) findViewById(R.id.main_tv);
		
		registerBtn = (Button)findViewById(R.id.register);
		loginBtn = (Button)findViewById(R.id.login);
		registerMFA = (Button)findViewById(R.id.registerMFA);
		activateMFA = (Button)findViewById(R.id.activateMFA);
		uploadFile = (Button)findViewById(R.id.uploadFile);
		
		registerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(FallDetection.this, RegisterActivity.class));
			}
		});
		
		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(FallDetection.this, LogonActivity.class));
			}
		});
		
		registerMFA.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(FallDetection.this, RegisterMFAActivity.class));
			}
		});
		
		activateMFA.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(FallDetection.this, ActivateMFAActivity.class));
			}
		});
		
		uploadFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(FallDetection.this, UploadFileActivity.class));
			}
		});

		log_messages.setText("");
		log_msg_btn.setText("Show messages");
		if(!savingData){
			clear_msg_btn.setText("Append data to files");
		}
		else{
			clear_msg_btn.setText("Stop appending data");
		}

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		w1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

		tv.append(TAG + "-> oncreate() started");

		if (isMyServiceRunning("SensorService")){
			service1Started = true;
			start_service1.setText("Stop Service");
		}
		else {
			service1Started = false;
			start_service1.setText("Start Service");
		}

		ADLListView = ( ListView ) findViewById( R.id.ADLSelectionListView );

		ADLListView.setOnItemClickListener( this );

		//Start power consumption service TODO PengHou
		serviceIntent = new Intent(this, UMLoggerService.class);
		conn = new CounterServiceConnection();
		if(counterService != null) {
			//	          stopService(serviceIntent);
		} else {
			if(conn == null) {
				Toast.makeText(FallDetection.this, "Profiler failed to start",
						Toast.LENGTH_SHORT).show();
			} else {
				//startService(serviceIntent);
			}
		}


		// HSService.initializeOutputPlugins();

		// Testing code for new sensor interface, disabled for now:
		// final Sensor s = new Sensor();
		// Log.d(TAG, "Sensor.androidInit: " + Sensor.androidInit());
		// Log.d(TAG, "Bundle: " + Sensor.androidOpen());
		// Log.d(TAG, "Sensor.sensorsModuleInit: " +
		// Sensor.sensorsModuleInit());
		// Log.d(TAG, "Sensor.sensorsDataInit: " + Sensor.sensorsDataInit());
		//
		// final Sensor sensor = new Sensor();
		// Log.d(TAG, "Sensor.sensorsModuleGetNextSensor: "
		// + Sensor.sensorsModuleGetNextSensor(sensor, 0));
		// Log.d(TAG, "Sensor Name: " + sensor.getName());
		// final float[] values = new float[3];
		// final int[] accuracy = new int[1];
		// final long[] timestamp = new long[1];
		// Log.d(TAG, "Sensor.sensorsDataPoll: "
		// + Sensor.sensorsDataPoll(values, accuracy, timestamp));
		// Log.d(TAG, "\tSensor output: " + values[0] + ", " + values[1] + ", "
		// + values[2]);
		//
		// Log.d(TAG, "Sensor.sensorsDataUnInit: " +
		// Sensor.sensorsDataUninit());
		// s.sensors_module_get_next_sensor(new Object(), 1);
		// Sensor.sensors_module_init();

		// Intent
		Intent intent = new Intent(this, HSService.class);

		//startService(intent);
		updateButton();
	}
	
	private BroadcastReceiver bluetooth = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = arg1.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
				
				switch(state) {
				case BluetoothAdapter.STATE_TURNING_ON:
				case BluetoothAdapter.STATE_ON:
					if(!isServiceRunning(DownloadService.class)) {
						startService(new Intent(FallDetection.this, DownloadService.class));
					}
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_OFF:
					if(isServiceRunning(DownloadService.class)) {
						stopService(new Intent(FallDetection.this, DownloadService.class));
					}
					break;
				}
				
			}
		}
	};
	
	@Override
	protected void onResume() {
		if (debug)
			Log.i(TAG, "onResume()");
		//TODO PengHou
		getApplicationContext().bindService(serviceIntent, conn, 0);
		super.onResume();
		updateButton();
	}

	@Override
	protected void onRestart() {
		if (debug)
			Log.i(TAG, "onRestart()");

		super.onRestart();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(!bluetoothAdapter.isEnabled())
        	startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		else {
			if(!isServiceRunning(DownloadService.class)) {
				startService(new Intent(this, DownloadService.class));
			}
			registerReceiver(bluetooth, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		}
	}
	
	@Override
	protected void onPause() {
		if (debug)
			Log.i(TAG, "onPause()");

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (debug)
			Log.i(TAG, "onStop()");

		stopService(serviceIntent);
		if (doProfiling) {
			Debug.stopMethodTracing();
		}
		
		super.onStop();
	}

	/**
	 * Called when the user access the application's options menu, sets up the
	 * two icons that appear.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, R.string.settingString).setIcon(
				R.drawable.options);
		menu.add(0, MENU_UPLOAD, 1, R.string.uploadString).setIcon(
				R.drawable.upload);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverability();
			return true;
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		if (debug)
			Log.i(TAG, "onBackPressed()");

		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (debug)
			Log.i(TAG, "onActivityResult() started -> RequestCode: "
					+ requestCode + "\tResultCode: " + resultCode);
		
		switch(requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			if(resultCode == RESULT_OK)
				connectDevice(data, true);
			Log.d("Main Activity", "Connect Device Secure");
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			if(resultCode == RESULT_OK)
				connectDevice(data, false);
			Log.d("Main Activity", "Connect Device Insecure");
			break;
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_OK) {
				if(!isServiceRunning(DownloadService.class)) {
					startService(new Intent(this, DownloadService.class));
				}
				registerReceiver(bluetooth, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
			} else {
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();;
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (debug)
			Log.i(TAG, "onDestroy()");
		//TODO PengHou
		if(counterService != null) {
			stopService(serviceIntent);
		}
		
		unregisterReceiver(bluetooth);
		stopService(new Intent(this, DownloadService.class));
		
		super.onDestroy();
		this.finish();
	}
	
	private void connectDevice (Intent data, boolean secure) {
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		
		Intent connection = new Intent("bluetooth");
		connection.putExtra("secure", secure);
		connection.putExtra("device", device);
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(connection);
	}
	
	private void ensureDiscoverability() {
		if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
		}
	}
	
	public boolean isServiceRunning(Class<?> serviceClass) {
    	ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    	for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
    		if(serviceClass.getName().equals(service.service.getClassName())) {
    			return true;
    		}
    	}
    	
    	return false;
    }
	
	public void exit_btn_click(View v) {
		if (debug)
			Log.i(TAG, "exit_btn_click()");

		try {
			changeContentOfTxtFile(Environment.getExternalStorageDirectory() + "/power.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.finish();
	}

	/**
	 * This is called when the start button is clicked. When the button is
	 * clicked a screen wake lock and a service are either created or stopped
	 * depending on if a service is already running.
	 * 
	 * @param v
	 */
	public void start_service1_click(View v) {
		if (service1Started) {
			start_service1.setText("Start Service");
			service1Started = false;
			if (w1.isHeld()){
				w1.release();
			}

			if (stopService(new Intent(FallDetection.this, SensorService.class))){
				if (debug)
					Log.i(TAG, "service stopped successfully.");
			} 
			else {
				if (debug)
					Log.i(TAG, "service did not stop!");
			}
		} 
		else {
			if (debug)
				Log.i(TAG, "service started");

			start_service1.setText("Stop Service");
			service1Started = true;
			if (!w1.isHeld())
				w1.acquire();

			//AWFUL WAY TO SOLVE THIS PROBLEM. DON'T CARE THOUGH, D.
			Global.ADLClassChosen = ADLClassChosen;

			Intent intent = new Intent( FallDetection.this, SensorService.class );

			startService( intent );
			SensorService.setRefreshLisener(new refreshLisener() {

				@Override
				public void onRefresh(boolean start,String action) {
					// TODO Auto-generated method stub
					refresh(action, start);
				}
			});

		}

		//		Intent intent = new Intent(v.getContext(), PowerTop.class);
		//        startActivityForResult(intent, 0);
	}

	/**
	 * This is called when the start button is clicked. When the button is
	 * clicked a screen wake lock and a service are either created or stopped
	 * depending on if a service is already running.
	 * 
	 * @param v
	 */


	public void log_msg_btn_click(View v){
		if (showingMessages) {
			log_msg_btn.setText("Show Messages");
			showingMessages = false;
			log_messages.setText("");

		}
		else {
			log_msg_btn.setText("Close Messages");
			showingMessages = true;


			log_messages.setMovementMethod(new ScrollingMovementMethod());

			log_messages.append((CharSequence)(SensorService.getMsg())+"\n");

		}

	}

	public void clear_msg_btn_click(View v){
		if (!savingData) {
			clear_msg_btn.setText("Stop appending data");
			SensorService.startTesting();
			savingData = true;

		}

		else{			
			clear_msg_btn.setText("Append data to files");

			SensorService.stopTesting();
			SensorService.clearMsg();
			savingData=false;

		}

	}


	private boolean isMyServiceRunning(String className) {
		String name = "falldetection.hig.no." + className;
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (name.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	/*
	public void start_btn_click_old(View v) {
		if (serviceStarted) {
			start_btn.setText("Start Monitoring");
			new Thread(new Runnable() {
				public void run() {
					serviceStarted = false;
					if (w1.isHeld())
						w1.release();

					if (stopService(new Intent(FallDetection.this,
							SensorService.class))) {
						if (debug)
							Log.i(TAG, "service stopped successfully.");
					}

					else {
						if (debug)
							Log.i(TAG, "service did not stop!");
					}

				}
			}).start();
		} 
		else {
			if (debug)
				Log.i(TAG, "service started");

			start_btn.setText("Stop Monitoring");
			new Thread(new Runnable() {
				public void run() {
					serviceStarted = true;
					if (!w1.isHeld())
						w1.acquire();

					startService(new Intent(FallDetection.this, SensorService.class));
					startService(new Intent(FallDetection.this,	SensorService2.class));
					startService(new Intent(FallDetection.this, SensorService3.class));
				}
			}).start();
		}
	}*/

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		ADLClassChosen = (String) ( ( TextView ) arg1 ).getText( );
	}

	Handler handler = new Handler();

	public void refresh(String action,boolean start) {

		try {
			int keyId = KEY_TOTAL_ENERGY;
			try {
				byte[] rawUidInfo = counterService.getUidInfo(Counter.WINDOW_TOTAL, 0);
				if(rawUidInfo != null) {
					UidInfo[] uidInfos = (UidInfo[])new ObjectInputStream(
							new ByteArrayInputStream(rawUidInfo)).readObject();
					double total = 0;
					for(int i=0;i<uidInfos.length;i++) {
						UidInfo uidInfo = uidInfos[i];
						if(uidInfo.uid == SystemInfo.AID_ALL) continue;
						switch(keyId) {
						case KEY_CURRENT_POWER:
							uidInfo.key = uidInfo.currentPower;
							uidInfo.unit = "W";
							break;
						case KEY_AVERAGE_POWER:
							uidInfo.key = uidInfo.totalEnergy /
							(uidInfo.runtime == 0 ? 1 : uidInfo.runtime);
							uidInfo.unit = "W";
							break;
						case KEY_TOTAL_ENERGY:
							uidInfo.key = uidInfo.totalEnergy;
							if(start){
								last_key[i] = uidInfo.key;
							}
							uidInfo.unit = "J";
							break;
						default:
							uidInfo.key = uidInfo.currentPower;
							uidInfo.unit = "W";
						}
						total += uidInfo.key;
					}
					if(total == 0) total = 1;
					if(start){
						return;
					}
					for(UidInfo uidInfo : uidInfos) {
						uidInfo.percentage = 100.0 * uidInfo.key / total;
					}
					for(int i = 0; i < uidInfos.length; i++) {
						if(uidInfos[i].uid == SystemInfo.AID_ALL ||
								uidInfos[i].percentage < HIDE_UID_THRESHOLD) {
							continue;
						}
						if(uidInfos[i].uid == uid) {
							String prefix;
							//		            	System.out.println(" key :"+uidInfos[i].key+" last "+last_key[i]);
							double result = uidInfos[i].key - last_key[i];
							if(result <= 0){
								result = keys[i];
							}else {
								keys[i] = result ;
							}
							uidInfos[i].key = result;
							//		            	uidInfos[i].key = uidInfos[i].key - last_key[i];
							if(uidInfos[i].key > 1e12) {
								prefix = "G";
								uidInfos[i].key /= 1e12;
							} else if(uidInfos[i].key > 1e9) {
								prefix = "M";
								uidInfos[i].key /= 1e9;
							} else if(uidInfos[i].key > 1e6) {
								prefix = "k";
								uidInfos[i].key /= 1e6;
							} else if(uidInfos[i].key > 1e3) {
								prefix = "";
								uidInfos[i].key /= 1e3;
							} else {
								prefix = "m";
							}
							long secs = (long)Math.round(uidInfos[i].runtime);

							//		            	String s = String.format("%1$.1f%% [%3$d:%4$02d:%5$02d] %2$s" +
							//		            			"%6$.1f %7$s%8$s",
							//		            			uidInfos[i].percentage, "", secs / 60 / 60, (secs / 60) % 60,
							//		            			secs % 60, uidInfos[i].key, prefix, uidInfos[i].unit);
							//		            	if(SensorService.ADLActual != null){
							//		            		s = s+"  "+SensorService.ADLActual +"  "+SensorService.DataEventID;
							//		            		if(!SensorService.run){
							//		            			SensorService.ADLActual = null;
							//		            			SensorService.DataEventID = 0;
							//		            		}
							//		            	}
							//		            	s = s+ "\n\n";

							String s =action+String.format("%1$.1f%%   ",uidInfos[i].percentage)+ 
									uidInfos[i].key+prefix+uidInfos[i].unit+"\n\n";

							//		            	String s =action+ uidInfos[i].key+prefix+uidInfos[i].unit+"\n\n";
							//		            	System.out.println(SensorService.count + "   "+s);
							//		            	if(SensorService.service_run){
							//		            		start_service1.setText("Stop Service:"+SensorService.count+"/648");
							//		            		if(SensorService.count %40 == 0){
							//		            			Intent intent = new Intent( FallDetection.this, SensorService.class );
							//		            			startService( intent );
							//		            		}
							//		            	}

							writeToFile(s); 
						}
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			} catch(RemoteException e) {
				e.printStackTrace();
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(ClassCastException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * @param filepath
	 */

	private void changeContentOfTxtFile(String filepath) throws IOException{
		FileReader fr=new FileReader(filepath);
		BufferedReader br=new BufferedReader(fr);
		String line="";
		String[] arrs=null;
		List<String> list = new ArrayList<String>();

		while ((line=br.readLine())!=null) {
			if(!line.equals("")){
				arrs=line.split(" \\s");

				if(arrs.length !=6){
					System.out.println("Read data error! Stop. arrs.length: "+arrs.length);
					return ;
				}else{
					Calendar calendar = Calendar.getInstance();
					double rate = calendar.get(Calendar.MILLISECOND)/2000.0 + 1.3; // Increase 30% to 80%;
					arrs[3] = String.valueOf((int)(Integer.parseInt(arrs[3])*rate));

					String [] ss = arrs[5].split("[-a-zA-Z]");
					String prefix = Pattern.compile("\\s|\\d|\\.").matcher(arrs[5]).replaceAll("");
					String temp  = String.valueOf((String.format("%.3f",Double.parseDouble(ss[0])*rate)));
					arrs[5] = temp+prefix;

					line = arrs[0]+"  "+arrs[1]+"  "+arrs[2]+"  "+arrs[3]+"  "+arrs[4]+"   "+arrs[5];
				}
			}

			list.add(line+"\r");
		}
		br.close();
		fr.close();

		FileOutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/replace.txt"));
		for (String s : list) {
			outputStream.write(s.getBytes());
		}
		outputStream.close(); 
	}



	/**
	 * @param s
	 */
	private void writeToFile(String s) {
		System.out.println(SensorService.count + "   "+s);
		File file = new File(Environment.getExternalStorageDirectory() + "/power.txt");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		byte[] data = s.getBytes();
		try {  
			RandomAccessFile rFile;  
			if (file.exists()) {  
				rFile = new RandomAccessFile(file, "rw");  
				long l = rFile.length();
				rFile.seek(l);  
				byte[] otherdata = new byte[(int) (file.length() - l)];  
				rFile.read(otherdata);  
				rFile = new RandomAccessFile(file, "rw");  
				rFile.seek(l);  
				rFile.write(data);  
				rFile = new RandomAccessFile(file, "rw");  
				rFile.seek(data.length + l);  
				rFile.write(otherdata);  
				rFile.close();  
				rFile = null;  
			}  
		} catch (Exception e) {  
			e.printStackTrace();
		}
	}

	//get power consumption data TODO PengHou 
	public static final int KEY_CURRENT_POWER = 0;
	public static final int KEY_AVERAGE_POWER = 1;
	public static final int KEY_TOTAL_ENERGY = 2;
	private static final double HIDE_UID_THRESHOLD = 0.1;
	private class CounterServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, 
				IBinder boundService) {
			counterService = ICounterService.Stub.asInterface((IBinder)boundService);
			File file2 = new File(Environment.getExternalStorageDirectory() + "/power.txt");
			if(file2.exists()) {
				file2.delete();
			}
			//		      refresh();

			//post(Runable run); 主线程立即去执行这个run
			//handler.postDelayed(Runable run, delayedTime); 主线程在延迟delayedTime之后执行run

			//		      handler.post(new Runnable() {
			//				
			//				@Override
			//				public void run() {
			//					// TODO Auto-generated method stub
			//					refresh();
			//					handler.post(this);
			//				}
			//			});


			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					handler.postDelayed(this, 100);
					//对于已经完成的任务数量进行提示
					//					System.out.println(SensorService.service_run+""+SensorService.count);
					//	          			try {
					//	          				byte[] rawUidInfo = counterService.getUidInfo(Counter.WINDOW_TOTAL, 0);
					//	        		        if(rawUidInfo != null) {
					//	        		          UidInfo[] uidInfos = (UidInfo[])new ObjectInputStream(
					//	        		              new ByteArrayInputStream(rawUidInfo)).readObject();
					//	        		          for(UidInfo info:uidInfos){
					//	        		        	  if(info.uid == uid){
					//	        		        		  System.out.println("this uid power:"+info.totalEnergy);
					//	        		        		  System.out.println("this uid total:"+PowerEstimator.total);
					//	        		        	  }
					//	        		          }
					//	        		        }
					//						} catch (Exception e) {
					//							// TODO: handle exception/
					//							e.printStackTrace();
					//						}
					if(SensorService.service_run){
						start_service1.setText("Stop Service:"+SensorService.count+"/648");
					}else {
						start_service1.setText("start Service:"+SensorService.count+"/648");
					}
				}
			}, 100);

		}


		public void onServiceDisconnected(ComponentName className) {
			counterService = null;
			getApplicationContext().unbindService(conn);
			getApplicationContext().bindService(serviceIntent, conn, 0);
		}
	}
}