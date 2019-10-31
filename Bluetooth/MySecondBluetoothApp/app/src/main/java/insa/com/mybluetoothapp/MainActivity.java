package insa.com.mybluetoothapp;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class MainActivity extends AppCompatActivity implements OnClickListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 100;

    private Button m_onBtn;
    private Button m_offBtn;
    private Button m_listPairedDevicesBtn;
    private Button m_discoverDevicesBtn;
    private Button m_startServerBtn;
    private TextView m_statusTv;
    private BluetoothAdapter m_myBluetoothAdapter;
    private Set<BluetoothDevice> m_pairedDevices;
    private ListView m_deviceLv;
//    private ArrayAdapter<String> m_BTArrayAdapter;

    public DeviceListAdapter m_deviceListAdapter;

    public ArrayList<BluetoothDevice> m_BTDevices = new ArrayList<>();

    private final BroadcastReceiver m_bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("[MainActivity]","BluetoothReceiver : " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                m_BTDevices.add(device);
                Log.d("[MainActivity]","device found : " + device.getName());

                m_deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                refreshStatusDisplayed();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        m_statusTv = (TextView) findViewById(R.id.text);
        m_onBtn = (Button)findViewById(R.id.turnOn);
        m_onBtn.setOnClickListener(this);

        m_offBtn = (Button)findViewById(R.id.turnOff);
        m_offBtn.setOnClickListener(this);

        m_listPairedDevicesBtn = (Button)findViewById(R.id.paired);
        m_listPairedDevicesBtn.setOnClickListener(this);

        m_discoverDevicesBtn = (Button)findViewById(R.id.discover);
        m_discoverDevicesBtn.setOnClickListener(this);

        m_startServerBtn = (Button)findViewById(R.id.startServer);
        m_startServerBtn.setOnClickListener(this);

        m_deviceLv = (ListView)findViewById(R.id.listView1);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
//        m_BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//        m_deviceLv.setAdapter(m_BTArrayAdapter);

        m_deviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, m_BTDevices);
        m_deviceLv.setAdapter(m_deviceListAdapter);

        m_deviceLv.setOnItemClickListener(MainActivity.this);

        // take an instance of BluetoothAdapter - Bluetooth radio
        m_myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_myBluetoothAdapter == null) {
            m_onBtn.setEnabled(false);
            m_offBtn.setEnabled(false);
            m_listPairedDevicesBtn.setEnabled(false);
            m_startServerBtn.setEnabled(false);
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
        } else if ( v == m_startServerBtn ) {
            startChatActivity(ChatActivity.MODE_SERVER, null);
        } else if ( v == m_discoverDevicesBtn ) {
            discoverClicked(v);
        }
    }

    private void discoverClicked(View v) {
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
            m_discoverDevicesBtn.setText(this.getText(R.string.Find));
            // the button is pressed when it discovers, so cancel the discovery
            m_myBluetoothAdapter.cancelDiscovery();
        }
        else {
            m_discoverDevicesBtn.setText(this.getText(R.string.cancelSearch));

            m_BTDevices.clear();
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

        m_BTDevices.clear();

        // put it inside Data adapter
        for(BluetoothDevice device : m_pairedDevices) {
            m_BTDevices.add(device);
        }
        m_deviceListAdapter.notifyDataSetChanged();

        Snackbar.make(m_listPairedDevicesBtn, "Show Paired Devices", Snackbar.LENGTH_LONG).show();
    }

    public void startChatActivity(String mode,BluetoothDevice device) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.MODE, mode);
        if ( device != null ) {
            intent.putExtra(ChatActivity.SERVER_DEVICE, device);
        }
        startActivity(intent);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //first cancel discovery because its very memory intensive.
        m_myBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = m_BTDevices.get(position).getName();
        String deviceAddress = m_BTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            m_BTDevices.get(position).createBond();

            startChatActivity(ChatActivity.MODE_CLIENT, m_BTDevices.get(position));

//            m_BTDevice = m_BTDevices.get(position);
//            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }

    }
}