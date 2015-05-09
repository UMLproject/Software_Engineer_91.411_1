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

    public UUID MY_UUID = UUID.randomUUID().toString();
    public float[] gravity;
    public float[] linear_acceleration;
    private TextView mTextView;

    private final BluetoothSocket mSocket;
    private SensorManager mSensorManager;//manages
    //private Sensor mSensor;//for accelerometer
    //private Sensor gSensor;//for gravity sensor
    private Sensor lSensor;//linear accel ( linear = accelerometer - gravity )
    private Sensor rSensor;//rotation
    private Sensor stepCounter;//for steps
    private TriggerEventListener mTriggerEventListener;//for sig motion
    private Sensor sigMotion;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private final OutputStream mOutStream;

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else { //does not support bluetooth
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK == requestCode) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
            // Loop through paired devices (should be only one)
                for (BluetoothDevice device : pairedDevices) {
                    // Set the device to be used
                    mDevice = device;
                }
            }
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
            }
        }else{
            //fail/refused
        }
    }

    public void onSensorChanged(SensorEvent event){
        // now subtract gSensor output from mSensor output
        final float alpha = 0.8;
          gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
          gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
          gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

          linear_acceleration[0] = event.values[0] - gravity[0];
          linear_acceleration[1] = event.values[1] - gravity[1];
          linear_acceleration[2] = event.values[2] - gravity[2];
          
          mOutStream = mSocket.getOutputStream();
          mOutStream.write(linear_acceleration[0]);
          mOutStream.write(linear_acceleration[1]);
          mOutStream.write(linear_acceleration[2]);
    }
}
