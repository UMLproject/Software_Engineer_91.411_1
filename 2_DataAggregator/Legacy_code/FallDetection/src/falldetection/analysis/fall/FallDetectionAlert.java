package falldetection.analysis.fall;

import falldetection.analysis.fall.R;
import falldetection.analysis.fall.R.id;
import falldetection.analysis.fall.R.layout;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
//import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
//import android.widget.Button;
import android.widget.TextView;



/**
 * This is another activity class which shows the user a notification if a fall is
 * detected. In here, an alert message can be sent out either via SMS, e-mail, phone call or
 * any combination of them. In the current prototype, only outgoing SMS and phone calls
 * were implemented.
 *
 */

public class FallDetectionAlert extends Activity
{
	private static final String tag = "Alert";
	private static final boolean debug = true;
	private final int COUNT_DOWN_SECONDS = 20000;
	private final int COUNT_DOWN_TICKS = 1000;
	
	private boolean cancelled;
	private CountDownTimer ct;
	private TextView alert_seconds_tf;
	private TextView alert_msg_tf;
	//private TextView log_messages;
	private NotificationManager nm;
	
		
	public void onCreate (Bundle savedInstanceState) 
	{
		if (debug) 
			Log.i(tag, "Alert Activity Created");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert);
		
		
		this.runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				cancelled =false;
				
				//setContentView(R.layout.alert);
				alert_seconds_tf = (TextView) findViewById(R.id.alert_seconds_tf);
				alert_msg_tf = (TextView) findViewById(R.id.alert_msg_tf);
							
				//start a timer that will call for help at the end of the count down
				ct = new CountDownTimer(COUNT_DOWN_SECONDS, COUNT_DOWN_TICKS)
				{
					@Override
					public void onFinish() 
					{
						if (cancelled) 
							finish();
						else 
							callHelp();
					}
					
					@Override
					public void onTick(long millisUntilFinished) 
					{
						alert_seconds_tf.setText("Seconds Remaining: "+ millisUntilFinished/1000);
					}
					
				}.start();
				
				
				Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
				Notification notification = new Notification();
				notification.sound = ringURI;
				
				long[] vibrate = new long[] {0, 1000, 0, 1000, 0, 1000};
				notification.vibrate= (vibrate);
				
				nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				nm.notify(1, notification);
				
			}
		});
		
		if (debug) 
			Log.i(tag, "Alert Activity Finished");
	}
	
	@Override
	public void onDestroy ()
	{
		nm.cancel(1);
		super.onDestroy();
	}
	
	@Override
	public void onResume ()
	{
		if (debug) 
			Log.i(tag, "onResume()");
		
		super.onResume();
	}
	
	public void onButtonYesClick(View v)
	{
		if (debug) 
			Log.i(tag, "onButtonYesClick()");
		
		cancelled = false;
		ct.cancel();
		ct=null;
		
		callHelp();
	}
	
	public void onButtonNoClick(View v)
	{
		
		if (debug) 
			Log.i(tag, "onButtonNoClick()");
		
		cancelled = true;
		ct.cancel();
		ct = null;
				
		finish();
	}
	
	public void onButtonExitClick(View v)
	{
		finish();
	}
	
	public void callHelp()
	{
		if (debug) 
			Log.i(tag, "callHelp()");
		
		cancelled = true;
		alert_msg_tf.setText("Calling for assistance");
		
		try 
		{
			Log.i(tag, "Sending SMS Message!");
			finish();
		} 
		catch (ActivityNotFoundException e) 
		{
			Log.e("helloandroid dialing example", "Call failed", e);
			finish();
		}
	}
	
	
}















