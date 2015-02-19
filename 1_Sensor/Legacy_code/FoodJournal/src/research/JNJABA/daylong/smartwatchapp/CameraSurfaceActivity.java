package research.JNJABA.daylong.smartwatchapp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.Toast;

public abstract class CameraSurfaceActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {
	//Types of files that can be sent
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int MEDIA_TYPE_TEXT = 3;
	public static final int MEDIA_TYPE_AUDIO = 4;
	
	//Types of messages to write to the user as a toast
	public static final int MESSAGE_STATE_CHANGED = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MEASSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICENAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	//State of the bluetooth connection
	public static final int STATE_NONE = 1;
	public static final int STATE_LISTEN = 2;
	public static final int STATE_CONNECTING = 3;
	public static final int STATE_CONNECTED = 4;
	
	//Requests for bluetooth
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	
	private BluetoothAdapter bluetoothAdapter = null;

	//Values used to display camera
	protected SurfaceHolder surfaceHolder;
	protected static Camera camera = null;
	protected SurfaceView surfaceView;
	
	//Flags all used to determine certain steps of the activity
	protected boolean isVideo = true;
	private boolean previewRunning = false;
	protected boolean isFirstClick = true;
	protected boolean isSecondClick = false;
	
	//Locations of all the data being stored
	private String dataFile = null;
	private String dataPath = "temp_store3";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Stops the user from running again if a previous isntance is still uploading
		if(isServiceRunning(UploadService.class)) {
			if (UploadService.isUploading()) {
				Toast.makeText(this, "Application still uploading", Toast.LENGTH_LONG).show();
				onDestroy();
				return;
			}
		}
		
		setContentView(R.layout.fragment_camera_surface);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		
		//Used to organize the files into folders
		if(!isServiceRunning(UploadService.class)) {
			dataPath += "/DAT_" + timeStamp;
		} else {
			String[] loc = UploadService.getDataPath().split("/DAT_");
			dataPath += "/DAT_" + loc[1];
		}
		
		makeDirectory();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (camera != null) {
			camera.stopPreview();
			previewRunning = false;
			releaseCamera();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		//Creates the camera preview view
		surfaceView = (SurfaceView) findViewById(R.id.svVideoView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback((Callback) this);
						
		//Not clickable yet to allow for the camera preview to be set up
		surfaceView.setOnClickListener((OnClickListener) this);
		surfaceView.setClickable(false);
		
		if(!bluetoothAdapter.isEnabled())
        	startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		else {
			//if(!isServiceRunning(UploadService.class)) {
				//If bluetooth is on then allow app to run as intended
				Intent service = new Intent(this, UploadService.class);
				service.putExtra("datafile", dataFile);
				
				startService(service);
			//}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	//Gets the name for each file needed
	protected File getOutputMediaFile(int type) {
		makeDirectory();
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		
		if (type == MEDIA_TYPE_IMAGE) {
			return new File(dataFile + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			return new File(dataFile + File.separator + "VID_" + timeStamp + ".mp4");
		} else if(type == MEDIA_TYPE_TEXT) {
			return new File(dataFile + File.separator + "TXT_" + timeStamp + ".txt");
		}
		
		return null;
	}
	
	private void makeDirectory() {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), dataPath);
		if(!mediaStorageDir.exists()) {
			if(mediaStorageDir.mkdirs()) {
				dataFile = mediaStorageDir.getPath();
			}
		} else {
			dataFile = mediaStorageDir.getPath();
		}
	}
	
	/*
	 * Threaded system for starting the camera. Makes things faster
	 * and easier on the main threadn (May not actually help since threads
	 * run on main thread by default, Just kidding it's in a handler thread)
	 */
	
	public static void getCameraInstance() {
		try {
			camera = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void openCamera() {
		if(cameraThread == null) {
			cameraThread = new CameraHandlerThread();
		}
		
		synchronized (cameraThread) {
			cameraThread.openCamera();
		}
	}
	
	private CameraHandlerThread cameraThread = null;
	private static class CameraHandlerThread extends HandlerThread {
		Handler mHandler = null;
		
		CameraHandlerThread() {
			super("CameraHandlerThread");
			start();
			mHandler = new Handler(getLooper());
		}
		
		synchronized void notifyCameraOpened() {
			notify();
		}
		
		void openCamera() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					getCameraInstance();
					notifyCameraOpened();
				}
			});
			
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Camera.Size getBestPreviewSize(Camera.Parameters parameters, int w, int h) {
		Camera.Size result = null;
		
		for(Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= w && size.height <= h) {
				if (result == null) {
					result = size;
				}
				else {
					int resultDelta = w - result.width + h - result.height;
					int newDelta = w - size.width + h - size.height;
					
					if(newDelta < resultDelta)
						result = size;
				}
			}
		}
		
		return result;
	}
	
	private void releaseCamera() {
		if(camera != null) {
			camera.release();
			camera = null;
		}
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		openCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		//Can I delete this try? May be useless
		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(surfaceHolder.getSurface() == null) {
			return;
		}
		
		Camera.Parameters params = null;
		Camera.Size size = null;
		if (camera != null) {
			params = camera.getParameters();
			size = getBestPreviewSize(params, width, height);
		}
		
		if(size != null) {
			params.setPreviewSize(size.width, size.height);
			camera.setParameters(params);
		}
		
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			camera.stopPreview();
			previewRunning = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		camera.startPreview();
		previewRunning = true;
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("surface found"));
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(previewRunning) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}
		
		previewRunning = false;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.svVideoView) {
			if (isFirstClick) {
				//First click just starts camera in child classes
			} else {
				//Second click sends data to be stored on the device and possibly uploaded
				isSecondClick = true;
				
				Intent data = new Intent("data");

				data.putExtra("datafile", dataFile);
				data.putExtra("file", getOutputMediaFile(MEDIA_TYPE_TEXT).getPath());

				LocalBroadcastManager.getInstance(this).sendBroadcast(data);
				
				finish();
			}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_OK) {
				//if(!isServiceRunning(UploadService.class)) {
					Intent service = new Intent(this, UploadService.class);
					service.putExtra("datafile", dataFile);
					
					startService(service);
					
					//View isn't clickable until camera is ready
					surfaceView.setClickable(true);
				//}
			} else {
				//If user does not permit bluetooth then the app closes, can be turned off and still work if need be
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
	}
	
}
