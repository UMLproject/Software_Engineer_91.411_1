package softeng.first;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class MainActivity extends Activity {

    public float[] gravity;
    public float[] linear_acceleration;
    private TextView mTextView;

    private SensorManager mSensorManager;//manages
    //private Sensor mSensor;//for accelerometer
    //private Sensor gSensor;//for gravity sensor
    private Sensor lSensor;//linear accel ( linear = accelerometer - gravity )
    private Sensor rSensor;//rotation
    private Sensor stepCounter;//for steps
    private TriggerEventListener mTriggerEventListener;//for sig motion
    private Sensor sigMotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        lSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                // Significant motion stuff happense here
            }
        };

        mSensorManager.requestTriggerSensor(mTriggerEventListener, sigMotion);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 0;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else { //does not support bluetooth
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK == requestCode) {
            //Yay
        }else{
            //fail/refused
        }
    }

    public void onSensorChanged(SensorEvent event){
        // now subtract gSensor output from mSensor output
        final float alpha = 0.8f;
          gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
          gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
          gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

          linear_acceleration[0] = event.values[0] - gravity[0];
          linear_acceleration[1] = event.values[1] - gravity[1];
          linear_acceleration[2] = event.values[2] - gravity[2];
    }
}
