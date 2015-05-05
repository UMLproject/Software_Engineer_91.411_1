package edu.uml.cs411;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;


public class AddSensorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asensor);
        Button addDeviceButton = (Button) findViewById(R.id.button3);
        final EditText deviceIDEdit = (EditText) findViewById(R.id.editText2);
        final EditText deviceNameEdit = (EditText) findViewById(R.id.editText);
        final RadioGroup typeGroup = (RadioGroup) findViewById(R.id.radioGroup);

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioButton selected = (RadioButton) findViewById(typeGroup.getCheckedRadioButtonId());

                if ( (selected != null) &&  (!deviceIDEdit.getText().toString().equals("")) && (!deviceNameEdit.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), "Device added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No device to add!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_asensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
