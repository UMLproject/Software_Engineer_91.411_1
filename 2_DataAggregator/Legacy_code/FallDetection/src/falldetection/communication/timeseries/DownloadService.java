package falldetection.communication.timeseries;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends Service {
	private BluetoothAdapter bluetoothAdapter;
	private static BluetoothService btService;
	
	private BluetoothDevice mDevice = null;
	
	private static String deviceAddress = "00:00:00:00:00:00";
	
	private static  Context mContext = null;
	
	private SharedPreferences settings;
	private static SharedPreferences.Editor edit;
	
	private static final Handler handler = new Handler() {		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch(msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					Log.d("Service", "Connected");
					if(!btService.getDevice().getAddress().equals(deviceAddress)) {
						Log.d("Service", "Settingn up new address");
						deviceAddress = btService.getDevice().getAddress();
						edit.putString("device address", deviceAddress);
						edit.apply();
					}
					break;
				case BluetoothService.STATE_CONNECTING:
					Log.d("Service", "Connecting");
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					Log.d("Service", "Not Connected");
					break;
				}
				break;
			case BluetoothService.MESSAGE_WRITE:
				//byte[] writeBuf = (byte[]) msg.obj;
				break;
			case BluetoothService.MESSAGE_READ:
				//byte[] readBuf = (byte[]) msg.obj;
				//String readMessage = new String(readBuf, 0, msg.arg1);
				//Do something with read message
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				//deviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(mContext, msg.getData().getString(BluetoothService.TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			Log.d("Service", "Broadcast " + action + " recieved");
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.d("Service", "new device found");
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDevice = device;
				
				if(!device.getAddress().equals(deviceAddress)) {
					deviceAddress = device.getAddress();
					edit.putString("device address", deviceAddress);
					edit.apply();
				}
				
				connectDevice(mDevice, true);
				
				Log.d("Service", "Found device");
			} else if(action.equals("bluetooth")) {
				Log.d("Service", "Manually connecting");
				mDevice = intent.getParcelableExtra("device");
				connectDevice(mDevice, intent.getBooleanExtra("secure", false));
			}
		}
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Service", "Service started");
		mContext = getApplicationContext();
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		registerReceiver(deviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		LocalBroadcastManager.getInstance(mContext).registerReceiver(deviceReceiver,
				new IntentFilter("bluetooth"));
		
		settings = mContext.getSharedPreferences("settings", MODE_PRIVATE);
		edit = settings.edit();
		
		startBluetooth();
		
		deviceAddress = settings.getString("device address", "00:00:00:00:00:00");
		
		if(!deviceAddress.equals("00:00:00:00:00:00") && btService.getState() != BluetoothService.STATE_CONNECTED) {
			Log.d("Service", "preConnected device found: " + deviceAddress);
		} else {
			Log.d("Service", deviceAddress);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	public void startBluetooth() {
		btService = new BluetoothService(handler);
		Log.d("Service", "Starting Bluetooth");
		
		if(btService != null) {
			if(btService.getState() == BluetoothService.STATE_NONE) {
				btService.start();
				Log.d("Service", "Bluetoothservice started");
			}
		}
	}
	
	private void connectDevice (BluetoothDevice newDevice, boolean secure) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(newDevice.getAddress());
		btService.connect(device, secure);
	}
	
	public static File getOutputMediaFile(String fileName) {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "SmartWatchApp");
		if(!mediaStorageDir.exists()) {
			if(!mediaStorageDir.mkdirs()) {
				Log.d("MainActivity", "Failed to create directory");
			}
		}
		
		File newFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
		if(!newFile.exists()) {
			try {
				newFile.createNewFile();
				Log.d("MainActiviy", "Created new file");
			} catch (IOException e) {
				Log.d("MainActivity", "Failed creating new file");
				e.printStackTrace();
			}
		}
		
		return newFile;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d("Service", "Ending Service");
		
		if(btService != null) {
			btService.stop();
		}
		
		unregisterReceiver(deviceReceiver);
		
		Log.d("Service", "Service ended");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}