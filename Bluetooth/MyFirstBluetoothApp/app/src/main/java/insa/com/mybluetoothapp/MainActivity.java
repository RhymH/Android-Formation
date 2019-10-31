package insa.com.mybluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class MainActivity extends Activity implements OnClickListener {

    private static final int REQUEST_ENABLE_BT = 123;

    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 100;

    private Button m_onBtn;
    private Button m_offBtn;
    private Button m_listPairedDevicesBtn;
    private Button m_findBtn;
    private TextView m_statusTv;
    private BluetoothAdapter m_myBluetoothAdapter;
    private Set<BluetoothDevice> m_pairedDevices;
    private ListView m_deviceLv;
    private ArrayAdapter<String> m_BTArrayAdapter;

    ///////////////////////////////////////////////////////
    /// This broadcast receiver handles apaired devices ///
    ///  and status changes                             ///
    ///////////////////////////////////////////////////////
    private final BroadcastReceiver m_bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("[MainActivity]","BluetoothReceiver : " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                m_BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                m_BTArrayAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                refreshStatusDisplayed();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_statusTv = (TextView) findViewById(R.id.text);
        m_onBtn = (Button)findViewById(R.id.turnOn);
        m_onBtn.setOnClickListener(this);

        m_offBtn = (Button)findViewById(R.id.turnOff);
        m_offBtn.setOnClickListener(this);

        m_listPairedDevicesBtn = (Button)findViewById(R.id.paired);
        m_listPairedDevicesBtn.setOnClickListener(this);

        m_findBtn = (Button)findViewById(R.id.search);
        m_findBtn.setOnClickListener(this);

        m_deviceLv = (ListView)findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        m_BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        m_deviceLv.setAdapter(m_BTArrayAdapter);

        // take an instance of BluetoothAdapter - Bluetooth radio
        m_myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_myBluetoothAdapter == null) {
            m_onBtn.setEnabled(false);
            m_offBtn.setEnabled(false);
            m_listPairedDevicesBtn.setEnabled(false);
            m_findBtn.setEnabled(false);
            m_statusTv.setText("Status: not supported");

            Snackbar.make(m_listPairedDevicesBtn, "Your device does not support Bluetooth", Snackbar.LENGTH_LONG).show();
            return;
        }

        registerReceiver(m_bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        registerReceiver(m_bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        refreshStatusDisplayed();
    }

    private void refreshStatusDisplayed() {
        if ( m_myBluetoothAdapter.isEnabled() ) {
            m_statusTv.setText("Status: Enabled");
        } else  {
            m_statusTv.setText("Status: Disabled");
        }
    }

    @Override
    public void onClick(View v) {
        if ( v == m_onBtn ) {
            onBtnClicked(v);
        } else if ( v == m_offBtn ) {
            offBtnClicked(v);
        } else if ( v == m_listPairedDevicesBtn ) {
            listPairedBtnClicked(v);
        } else if ( v == m_findBtn ) {
            findBtnClicked(v);
        }
    }

    public void onBtnClicked(View view){
        Log.d("[MainActivity]","ON Buttton Clicked");
        if (!m_myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Snackbar.make(m_listPairedDevicesBtn, "Bluetooth turned on", Snackbar.LENGTH_LONG).show();
        }
        else{
            Snackbar.make(m_listPairedDevicesBtn, "Bluetooth is already on", Snackbar.LENGTH_LONG).show();
        }
    }

    public void offBtnClicked(View view){
        Log.d("[MainActivity]","OFF Buttton Clicked");
        m_myBluetoothAdapter.disable();
        m_statusTv.setText("Status: Disabled");

        Snackbar.make(m_listPairedDevicesBtn, "Bluetooth turned off", Snackbar.LENGTH_LONG).show();
    }

    public void listPairedBtnClicked(View view){
        Log.d("[MainActivity]","List Paired Buttton Clicked");
        // get paired devices
        m_pairedDevices = m_myBluetoothAdapter.getBondedDevices();

        // put it inside Data adapter
        for(BluetoothDevice device : m_pairedDevices)
            m_BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        Snackbar.make(m_listPairedDevicesBtn, "Show Paired Devices", Snackbar.LENGTH_LONG).show();
    }

    public void findBtnClicked(View view) {
        Log.d("[MainActivity]","Discovery Buttton Clicked");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = this.checkSelfPermission(ACCESS_COARSE_LOCATION);
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                doDiscovery();
                return;
            }

            this.requestPermissions( new String[]{ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSIONS);
        } else {
            doDiscovery();
        }
    }

    public void doDiscovery() {

        if (m_myBluetoothAdapter.isDiscovering()) {
            m_findBtn.setText(this.getText(R.string.Find));
            // the button is pressed when it discovers, so cancel the discovery
            m_myBluetoothAdapter.cancelDiscovery();
        }
        else {
            m_findBtn.setText(this.getText(R.string.cancelSearch));
            m_BTArrayAdapter.clear();
            m_myBluetoothAdapter.startDiscovery();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doDiscovery();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.permission_failure),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_ENABLE_BT){
            if(m_myBluetoothAdapter.isEnabled()) {
                m_statusTv.setText("Status: Enabled");
            } else {
                m_statusTv.setText("Status: Disabled");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_bluetoothReceiver);
    }

}