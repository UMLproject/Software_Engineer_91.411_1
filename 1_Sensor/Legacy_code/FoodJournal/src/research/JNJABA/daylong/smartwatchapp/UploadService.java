package research.JNJABA.daylong.smartwatchapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class UploadService extends Service implements SensorEventListener{
	//Constants for time
	public static final long SECOND = 1000;
	public static final long MINUTE = SECOND * 60;
	public static final long HOUR = MINUTE * 60;
	
	/*
	 * Classes used for starting app in foreground. This is needed so that older
	 * devices can have this functionality without error
	 */
	private static final Class<?>[] setForegroundSignature = new Class[] {boolean.class};
	private static final Class<?>[] startForegroundSignature = new Class[] {int.class, Notification.class};
	private static final Class<?>[] stopForegroundSignature = new Class[] {boolean.class};
	
	//Variables all used for starting in foreground
	private NotificationManager notiManager;
	private Method setForeground;
	private Method startForeground;
	private Method stopForeground;
	private Object[] setForegroundArgs = new Object[1];
	private Object[] startForegroundArgs = new Object[2];
	private Object[] stopForegroundArgs = new Object[1];
	
	private BluetoothAdapter bluetoothAdapter;
	
	//Bluetooth connection service
	private static BluetoothService btService;
	private BluetoothDevice mDevice = null;
	
	private String dataFile = null;
	private static String dataFileRef = "";
	//address used as a location to connect to for automatic connection
	private static String deviceAddress = "00:00:00:00:00:00";
	
	private static  Context mContext = null;
	
	//Place to stroe device address
	private SharedPreferences settings;
	private static SharedPreferences.Editor edit;
	
	//Flags used to determine the curent state of the app
	private boolean isBadWrite = false;
	private boolean isCreated = false;
	
	//Variables for making the sensorFile
	private File sensorFile = null;
	private FileOutputStream sensorFos = null;
	private String sensorData = "";
	
	private WakeLock wakeLock = null;
	
	//Timer used for automatic service end after an hour
	private Handler timer = new Handler();
	private Handler screen = new Handler();
	private long startTime = 0;
	
	//flag used to determine whether or not the next service can be started
	private static boolean uploading = false;
	
	/*
	 * Sensor variables, Gyroscope and Accelerometer
	 */
	
	private SensorManager sensorMgr;
	private Sensor sensorAccel;
	private Sensor sensorGyro;
	
	//Distance we want the user to move
	//private float distanceNeeded = 0;
	
	//Arrays that hold the data for acceleration and direction
	private float[] a = new float[3];
	private float[] tempAngles = new float[3];
	
	private final Runnable end = new Runnable() {
		@Override
		public void run() {
			if(btService.getState() != BluetoothService.STATE_CONNECTED) {
				connectDevice(deviceAddress, false);
			}
			
			if (!uploading) {
				uploadData();
			}
		}
	};
	
	//Handler used in conjunction with BluetoothService
	private static final Handler handler = new Handler() {		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch(msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					if(!btService.getDevice().getAddress().equals(deviceAddress)) {
						//When a device is found save its address
						deviceAddress = btService.getDevice().getAddress();
						edit.putString("device address", deviceAddress);
						edit.apply();
					}
					
					break;
				case BluetoothService.STATE_CONNECTING:
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					break;
				}
				break;
			//None other are needed as of now
			case BluetoothService.MESSAGE_WRITE:
				break;
			case BluetoothService.MESSAGE_READ:
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				break;
			case BluetoothService.MESSAGE_TOAST:
				break;
			}
		}
	};
	
	//Broadcast used to communicate different events of the app
	private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			//On first connect this could be used to find and connect
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDevice = device;
				
				if(!device.getAddress().equals(deviceAddress)) {
					deviceAddress = device.getAddress();
					edit.putString("device address", deviceAddress);
					edit.apply();
				}
				
				if (btService.getState() != BluetoothService.STATE_CONNECTED) {
					connectDevice(mDevice, true);
				}
			}
			if(action.equals("data")) {
				//sent when the user takes a video. Connects via bluetooth then uploads all data
				dataFile = intent.getStringExtra("datafile");
				
				if(!deviceAddress.equals("00:00:00:00:00:00") && btService.getState() != BluetoothService.STATE_CONNECTED) {
					connectDevice(deviceAddress, false);
				} else {
				}
				
			}
			if(action.equals("connected")) {
				if(!uploading) {
					uploadData();
				}
			}
			if(action.equals(Intent.ACTION_SCREEN_OFF)) {
				//Used to turn the sensors back on when the screen turns off (they turn off automatically)
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (sensorMgr != null) {
							unregisterListener();
							registerListener();
						}
					}
				}, 500);
			}
		}
	};
	
	public UploadService() {
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mContext = getApplicationContext();
		
		//setup the startForeground function
		notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		try {
			startForeground = getClass().getMethod("startForeground", startForegroundSignature);
			stopForeground = getClass().getMethod("stopForeground", stopForegroundSignature);
		} catch (NoSuchMethodException e) {
			startForeground = stopForeground = null;

			try {
				setForeground = getClass().getMethod("setForeground", setForegroundSignature);
			} catch (NoSuchMethodException ee) {
				throw new IllegalStateException("OS doesn't have setForeground");
			}
		}
		
		Intent i = new Intent(this, VideoCameraActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, i, 0);
		
		Notification noti = new Notification.Builder(mContext)
			.setContentTitle("Collecting Sensor Data")
			.setContentText("When finished eating, record your meal")
			.setSmallIcon(R.drawable.ic_stat_pizza_pic)
			.setContentIntent(pIntent)
			.build();
		
		//Start custom foreground
		startForegroundCompat(1, noti);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		LocalBroadcastManager.getInstance(mContext).registerReceiver(deviceReceiver, new IntentFilter("data"));
		LocalBroadcastManager.getInstance(mContext).registerReceiver(deviceReceiver, new IntentFilter("connected"));
		registerReceiver(deviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(deviceReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		settings = mContext.getSharedPreferences("settings", MODE_PRIVATE);
		edit = settings.edit();
		
		btService = new BluetoothService(mContext, handler);
		
		//Start service incase a device wants to connect to us via bluetooth
		if(btService != null) {
			if(btService.getState() == BluetoothService.STATE_NONE) {
				//Surround with if to start only when there is no device address set??
				//Will Stop an entire thread from running and continuously looping for a connection
				btService.start();
			}
		}
		
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Service");
		
		registerListener();
		wakeLock.acquire();
	}

	private void invokeMethod(Method method, Object[] args) {
		try {
			method.invoke(this, args);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	private void startForegroundCompat(int id, Notification noti) {
		if(startForeground != null) {
			startForegroundArgs[0] = Integer.valueOf(id);
			startForegroundArgs[1] = noti;
			invokeMethod(startForeground, startForegroundArgs);
			return;
		}
		
		setForegroundArgs[0] = Boolean.TRUE;
		invokeMethod(setForeground, setForegroundArgs);
		notiManager.notify(id, noti);
	}
	
	private void stopForegroundCompat(int id) {
		if(stopForeground != null) {
			stopForegroundArgs[0] = Boolean.TRUE;
			invokeMethod(stopForeground, stopForegroundArgs);
			return;
		}
		
		notiManager.cancel(id);
		setForegroundArgs[0] = Boolean.FALSE;
		invokeMethod(setForeground, setForegroundArgs);
	}
	
	public static boolean isUploading() {return uploading;}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Datafile where everything will be stored
		
		if (!isCreated) {
			dataFile = intent.getStringExtra("datafile");
			dataFileRef = dataFile;
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
			sensorFile = new File(dataFile + File.separator + "TXT_" + timeStamp + ".txt");
			try {
				sensorFos = new FileOutputStream(sensorFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		deviceAddress = settings.getString("device address", "00:00:00:00:00:00");
		
		//Connect on startup if need be
		if(!deviceAddress.equals("00:00:00:00:00:00") && btService.getState() != BluetoothService.STATE_CONNECTED) {
			connectDevice(deviceAddress, false);
		} else if (deviceAddress.equals("00:00:00:00:00:00")) {
			Toast.makeText(mContext, "Warning: No device set", Toast.LENGTH_SHORT).show();
		} else {
		}
		
		if (isCreated) {
			timer.removeCallbacks(end);
		}
		
		timer.postDelayed(end, MINUTE * 45);
		
		isCreated = true;
		
		return START_NOT_STICKY;
	}
	
	//Used to determine time in ms till end of day
	@SuppressWarnings("unused")
	private long determineTimeLeft(int hour, int minute) {
		long time = 0;
		
		int hours = 23 - hour;
		int minutes = 59 - minute;
		
		time += (hours * HOUR);
		time += (minutes * MINUTE);
		
		return time;
	}
	
	private void connectDevice (BluetoothDevice newDevice, boolean secure) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(newDevice.getAddress());
		btService.connect(device, secure);
	}
	
	private void connectDevice (String newAddress, boolean secure) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(newAddress);
		btService.connect(device, secure);
	}
	
	private void sendData(File data) throws IOException{
		if(btService.getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}
		btService.write(data);
	}
	
	// Make this function or something similar synchronized?
	private void uploadData() {
		// Turn off sensors if end of meal
		unregisterListener();
		try {
			sensorFos.flush();
			sensorFos.close();
			sensorFos = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Only upload if there is a connection, obviously
		if (btService.getState() == BluetoothService.STATE_CONNECTED && !uploading) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// open up file that has all the data stored in it
					String[] parent = dataFile.split("/DAT_");
					File directory = new File(parent[0]);
					File[] directories = directory.listFiles();

					// Go through every file
					if (directories == null) {
					} else if (directories.length == 0) {
					} else {
						for (File files : directories) {
							isBadWrite = false;
							File[] contents = files.listFiles();
							if (contents == null) {
							} else if (contents.length == 0) {
							} else {
								for (File newFiles : contents) {
									// Only upload at end of meal
									uploading = true;
									if (newFiles.length() > 0) {
										try {
											// Send data if everything is ok
											sendData(newFiles);
										} catch (IOException e) {
											// If Pipe breaks set flag
											isBadWrite = true;
											e.printStackTrace();
										}
									}
									// Only delete file if Pipe is good
									if (!isBadWrite) {
										newFiles.delete();
									}
								}
							}
							// Only delete file if Pipe is good
							if (!isBadWrite) {
								files.delete();
							}
						}
					}
					// Do nothing on first video
					// end service to allow for further use
					uploading = false;
					UploadService.this.stopSelf();
				}
			}).start();
		} else {
			// Not connected so don't try and send
			uploading = false;
			// Not connected at end of meal so store data and end service
			UploadService.this.stopSelf();
		}
	}
	
	public static String getDataPath() {
		return dataFileRef;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(btService != null)
			btService.stop();
		
		unregisterReceiver(deviceReceiver);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(deviceReceiver);
		
		if (sensorMgr != null) {
			unregisterListener();
		}
		wakeLock.release();
		
		screen.removeCallbacks(end);
		timer.removeCallbacks(end);
		
		stopForegroundCompat(1);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void registerListener() {
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		if(sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0)
			sensorAccel = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		if(sensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE).size() > 0)
			sensorGyro = sensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
		
		boolean accelSupported = sensorMgr.registerListener(this, sensorAccel,
				SensorManager.SENSOR_DELAY_NORMAL);
		boolean gyroSupported = sensorMgr.registerListener(this, sensorGyro,
				SensorManager.SENSOR_DELAY_NORMAL);

		if (!accelSupported) {
			Toast.makeText(this, "No accelerometer detected",Toast.LENGTH_SHORT).show();
		} if (!gyroSupported) {
			Toast.makeText(this, "No gyroscope detected", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void unregisterListener() {
		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this, sensorAccel);
			sensorMgr.unregisterListener(this, sensorGyro);
			sensorMgr = null;
		}
	}
	
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		float[] accelResult = new float[3];
		float[] acceleration;
		float[] gyroResult = new float[3];
		float[] angles;
		
		//Data being collected isn't raw but slightly preprocessed. If raw data is essential just write directly
		
		long curTime = System.currentTimeMillis();
		
		if ((curTime - startTime) >= 200) {
			startTime = curTime;
			
			if (sensorType == Sensor.TYPE_ACCELEROMETER) {
				acceleration = event.values;
				float kFilteringFactor = .88f;

				a[0] = acceleration[0] * kFilteringFactor + a[0] * (1.0f - kFilteringFactor);
				a[1] = acceleration[1] * kFilteringFactor + a[1] * (1.0f - kFilteringFactor);
				a[2] = acceleration[2] * kFilteringFactor + a[2] * (1.0f - kFilteringFactor);
				accelResult[0] = acceleration[0] - a[0];
				accelResult[1] = acceleration[1] - a[1];
				accelResult[2] = acceleration[2] - a[2];
				
				sensorData += "accl: time: " + System.currentTimeMillis()
						+ " x:" + accelResult[0] + " y:" + accelResult[1]
						+ " z:" + accelResult[2] + "\n";
			}
			if (sensorType == Sensor.TYPE_GYROSCOPE) {
				angles = event.values;
				float kFilteringFactor = .88f;

				tempAngles[0] = angles[0] * kFilteringFactor + tempAngles[0] * (1.0f - kFilteringFactor);
				tempAngles[1] = angles[1] * kFilteringFactor + tempAngles[1] * (1.0f - kFilteringFactor);
				tempAngles[2] = angles[2] * kFilteringFactor + tempAngles[2] * (1.0f - kFilteringFactor);
				gyroResult[0] = angles[0] - tempAngles[0];
				gyroResult[1] = angles[1] - tempAngles[1];
				gyroResult[2] = angles[2] - tempAngles[2];

				sensorData += "gyro: time: " + System.currentTimeMillis()
						+ " x:" + gyroResult[0] + " y:" + gyroResult[1]
						+ " z:" + gyroResult[2] + "\n";
			}
			if (sensorFos != null) {
				try {
					sensorFos.write(sensorData.getBytes());
				} catch (IOException e) {
				}
			}
			
			sensorData = "";
		}
	}
	
	//Not needed/necessary function
	public void onAccuracyChanged(Sensor sensor, int sensorAccuracy) {
	}
}