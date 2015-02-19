package research.JNJABA.daylong.smartwatchapp;

import java.io.File;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VideoCameraActivity extends CameraSurfaceActivity{
	private MediaRecorder recorder;
	
	//Views placed on top of camera preview
	private ProgressBar timeBar;
	private TextView videoTimer;
	private ImageView dot;
	
	private long time = 0;
	private int progressStatus = 0;
	private int timer = 0;
	private boolean running = false;
	
	//File where the video is being stored
	private File video = null;
	
	//Timer for blinking dot while recording
	private Handler dotTimer = null;
	
	
	//When SurfaceView is created start video and make screen clickable, helps prevent crashes
	private BroadcastReceiver surfaceCreated = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("surface found")) {
				surfaceView.setClickable(true);
				surfaceView.performClick();
				surfaceView.setClickable(false);
			}
		}
	};
	
	private final Runnable blink = new Runnable() {
		@Override
		public void run() {
			VideoCameraActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dot.isShown()) {
						dot.setVisibility(View.INVISIBLE);
					} else {
						dot.setVisibility(View.VISIBLE);
					}
				}
			});
			
			dotTimer.postDelayed(this, UploadService.SECOND / 2);
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		
		recorder = new MediaRecorder();
		dotTimer = new Handler();
		
		timeBar = (ProgressBar) findViewById(R.id.pbTimer);
		videoTimer = (TextView) findViewById(R.id.tvVideoTime);
		dot = (ImageView) findViewById(R.id.ivDot);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(surfaceCreated, new IntentFilter("surface found"));
	}
	
	@Override
	protected void onPause() {
		if(running) {
			//If app closes while video is running, end it
			surfaceView.setClickable(true);
			surfaceView.performClick();
		}
		
		if (recorder != null) {
			releaseMediaRecorder();
		}
		
		super.onPause();
		
		LocalBroadcastManager.getInstance(this).unregisterReceiver(surfaceCreated);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void releaseMediaRecorder() {
		if(recorder != null) {
			if(running) {
				recorder.stop();
			}
			recorder.reset();
			recorder.release();
			recorder = null;
		}
	}
	
	private boolean initCamera() {
		//Think bug here when retaking video
		recorder.release();
		
		//super.openCamera();
		recorder = new MediaRecorder();
		
		//Is this needed?
		camera.unlock();
		
		recorder.setCamera(camera);
		recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
		
		video = getOutputMediaFile(CameraSurfaceActivity.MEDIA_TYPE_VIDEO);
		recorder.setOutputFile(video.getPath());
		
		recorder.setPreviewDisplay(surfaceHolder.getSurface());
		
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	//Start recording in a separate background thread to allow UI thread to update with animations
	private class progressTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				recorder.start();
				running = true;
				time = System.currentTimeMillis(); 
				while (progressStatus < 100) {
					if(timer == 3) {
						surfaceView.setClickable(true);
					}
					
					progressStatus = startProgressBar();
					timeBar.setProgress(progressStatus);
					//Updating ProgressBar/Timer
					if ((timeBar.getProgress() / 6.7) > timer) {
						timer++;
						VideoCameraActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								videoTimer.setText(timer + "/15");
							}
						});
					}
					
					if (!running) {
						break;
					}
					
					//Create Runnables only when absolutley needed. This one stops recording after 15 secs
					if (progressStatus >= 100) {
						VideoCameraActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								surfaceView.performClick();
							}
						});
						break;
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				isFirstClick = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public void onClick(View v) {
		super.onClick(v);
		
		//Used for starting camera, not currently used for this app
		if(surfaceView.isClickable()){
			if (isFirstClick) {
				//Setup camera
				if (initCamera()) {
					running = true;
					isFirstClick = false;
					
					//Could eliminate this if I make the count thread smoother
					//Could also put the count in this thread to make it smoother
					dotTimer.postDelayed(blink, UploadService.SECOND / 2);
					
					//Start the recording
					new progressTask().execute();
				} else {
					finish();
				}
			} else {
				dotTimer.removeCallbacks(blink);
				try {
					recorder.stop();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (RuntimeException ee) {
					ee.printStackTrace();
				}
				isFirstClick = true;
				running = false;
			}
		}
	}
	
	//Sets up how much of progress bar to fill
	private int startProgressBar() {
		long curTime = System.currentTimeMillis();
		return (int) ((curTime - time) / 150);
	}
	
	@Override
	public void onBackPressed() {
		//Make back press do nothing special??
		if (!isFirstClick) {
			super.onBackPressed();
		}
	}
}
