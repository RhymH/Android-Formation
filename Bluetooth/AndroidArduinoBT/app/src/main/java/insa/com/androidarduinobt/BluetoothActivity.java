package insa.com.androidarduinobt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;


public class BluetoothActivity extends Activity {

    // Tag for logging
    private static final String TAG = "BluetoothActivity";

    // MAC address of remote Bluetooth device
    // Replace this with the address of your own module
    private final String address = "98:D3:31:F5:94:A4";

    // The thread that does all the work
    BluetoothThread btt;

    // Handler for writing messages to the Bluetooth connection
    Handler writeHandler;

    /**
     * Launch the Bluetooth thread.
     */
    public void toggleConnectButton(View view) {
        Log.v(TAG, "Connect button pressed.");

        ToggleButton toggleButton = (ToggleButton) view;
        if ( toggleButton.isChecked() ) {
            connectButtonPressed();
        } else {
            disconnectButtonPressed();
        }
    }

    public void connectButtonPressed() {
        Log.v(TAG, "Connect button pressed.");

        // Only one thread at a time
        if (btt != null) {
            Log.w(TAG, "Already connected!");
            return;
        }

        // Initialize the Bluetooth thread, passing in a MAC address
        // and a Handler that will receive incoming messages
        btt = new BluetoothThread(address, new Handler() {

            @Override
            public void handleMessage(Message message) {

                String s = (String) message.obj;

                // Do something with the message
                if (s.equals("CONNECTED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    tv.setText("Connected.");
                    Button b = (Button) findViewById(R.id.writeButton);
                    b.setEnabled(true);
                } else if (s.equals("DISCONNECTED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    Button b = (Button) findViewById(R.id.writeButton);
                    b.setEnabled(false);
                    tv.setText("Disconnected.");
                } else if (s.equals("CONNECTION FAILED")) {
                    TextView tv = (TextView) findViewById(R.id.statusText);
                    tv.setText("Connection failed!");
                    btt = null;
                } else {
                    TextView tv = (TextView) findViewById(R.id.readField);
                    tv.setText(s);
                }
            }
        });

        // Get the handler that is used to send messages
        writeHandler = btt.getWriteHandler();

        // Run the thread
        btt.start();

        TextView tv = (TextView) findViewById(R.id.statusText);
        tv.setText("Connecting...");
    }

    /**
     * Kill the Bluetooth thread.
     */
    public void disconnectButtonPressed() {
        Log.v(TAG, "Disconnect button pressed.");

        if(btt != null) {
            btt.interrupt();
            btt = null;
        }
    }

    /**
     * Send a message using the Bluetooth thread's write handler.
     */
    public void writeButtonPressed(View v) {
        Log.v(TAG, "Write button pressed.");

        TextView tv = (TextView)findViewById(R.id.writeField);
        String data = tv.getText().toString();

        Message msg = Message.obtain();
        msg.obj = data;
        writeHandler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Button b = (Button) findViewById(R.id.writeButton);
        b.setEnabled(false);
    }

    /**
     * Kill the thread when we leave the activity.
     */
    protected void onPause() {
        super.onPause();

        if(btt != null) {
            btt.interrupt();
            btt = null;
        }
    }
}
