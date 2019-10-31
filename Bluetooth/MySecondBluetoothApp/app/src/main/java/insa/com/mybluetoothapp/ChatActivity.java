package insa.com.mybluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ChatActivity";

    public static final String MODE = "mode";
    public static final String MODE_SERVER = "server";
    public static final String MODE_CLIENT = "client";
    public static final String SERVER_DEVICE = "serverId";


    private TextView m_modeTv;
    private TextView m_stateTv;
    private ListView m_chatLv;
    private ArrayAdapter<ChatMsg> m_chatArrayAdapter;

    private AbstractBtThread m_btThread;
    private Button m_btnSend;
    private EditText m_etSend;
    private BluetoothAdapter m_myBluetoothAdapter;
    private MyHandler m_handler;
    private ArrayList<ChatMsg> m_msgList;

    private BroadcastReceiver m_bluetoothTurnedOnOff = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                Log.d(TAG, "ChatActivity: BT Receiver received: STATE_OFF");


                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                ChatActivity.this.finish();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Bluetooth has been stopped. The activity will be closed.")
                .setPositiveButton("OK", dialogClickListener).show();
            }
        }
    };


    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if( ChatActivity.this.isDestroyed() ) {
                    return;
                }
            }

            String error = msg.getData().getString("error", null);
            if( error != null) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                ChatActivity.this.finish();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setMessage("Bluetooth has been stopped. The activity will be closed.")
                        .setPositiveButton("OK", dialogClickListener).show();
                return;
            }

            String from = msg.getData().getString("name", "");
            String message = msg.getData().getString("message", "");

            Log.v(TAG, "Message Received; " + message);
            ChatMsg newMsg = new ChatMsg(from, message);
            m_msgList.add(newMsg);
            m_chatArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(m_bluetoothTurnedOnOff, filter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        m_modeTv = (TextView) findViewById(R.id.mode);
        m_stateTv = (TextView)findViewById(R.id.state);

        m_btnSend = (Button) findViewById(R.id.btnSend);
        m_btnSend.setOnClickListener(this);
        m_etSend = (EditText) findViewById(R.id.editText);


        m_chatLv = (ListView)findViewById(R.id.chatListView);

        // create the arrayAdapter that contains the BTDevices, and set it to the ListView
        m_msgList = new ArrayList<>();
        m_chatArrayAdapter = new ChatListAdapter(this, R.layout.chat_adapter_view, m_msgList);
        m_chatLv.setAdapter(m_chatArrayAdapter);

        m_handler = new MyHandler();

        Intent intent = getIntent();
        String mode = intent.getStringExtra(ChatActivity.MODE);

        if ( mode.equals(MODE_SERVER) ) {
            m_modeTv.setText("Mode SERVER");
            m_stateTv.setText("Waiting for client Connection");

            m_btThread = new ServerThread(this, m_handler);
            m_btThread.start();
        } else {

            BluetoothDevice btDevice = intent.getParcelableExtra(ChatActivity.SERVER_DEVICE);

            m_modeTv.setText("Mode CLIENT");
            m_stateTv.setText("Server Name=" + btDevice.getName());

            m_btThread = new ClientThread(btDevice, m_handler);
            m_btThread.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_bluetoothTurnedOnOff);

        m_btThread.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        String messageString = m_etSend.getText().toString();
        byte[] bytes = messageString.getBytes(Charset.defaultCharset());
        m_btThread.write(bytes);
        m_etSend.setText("");


        ChatMsg newMsg = new ChatMsg("Me", messageString);
        m_msgList.add(newMsg);
        m_chatArrayAdapter.notifyDataSetChanged();
    }
}
