package insa.com.mybluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by georges on 04/10/2018.
 */

public class ServerThread extends AbstractBtThread {
    private static final String TAG = "ServerThread";

    public static final UUID SERVER_UUID = UUID.fromString("54bf25eb-485e-4961-88b0-35aecced6052");
    private static final String NAME = "SomeBody";

    BluetoothSocket mmSocket;

    InputStream mmInStream;
    OutputStream mmOutStream;
    ChatActivity.MyHandler m_activityHandler;

    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter m_myBluetoothAdapter;
    private final Context m_context;


    public ServerThread(Context context, ChatActivity.MyHandler handler) {
        m_context = context;
        m_activityHandler = handler;

        m_myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        makeDiscoverable();

        BluetoothServerSocket tmp = null;
        try {
            tmp = m_myBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, SERVER_UUID);
        } catch (IOException e) {
            Log.e(TAG, "write: Error on listenUsingRfComm. " + e.getMessage() );
            sendErrorMsg("listenUsingRfComm failed");
        }
        mmServerSocket = tmp;
    }

    public void makeDiscoverable() {
        Log.d(TAG, "makeDiscoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        m_context.startActivity(discoverableIntent);
    }

    public void run() {
        mmSocket = null;
        while (true) {
            try {
                mmSocket = mmServerSocket.accept();

                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();

                if (mmSocket != null) {
                    manageConnectedSocket();
                    cancel();
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "write: Error on accept. " + e.getMessage() );
                sendErrorMsg("accept failed");
                break;
            }
        }
    }

    private void manageConnectedSocket() {

        byte[] buffer = new byte[1024];  // buffer store for the stream

        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            // Read from the InputStream
            try {
                bytes = mmInStream.read(buffer);
                String incomingMessage = new String(buffer, 0, bytes);

                Log.d(TAG, "InputStream: " + incomingMessage);
                this.sendMsg("Client", incomingMessage);
            } catch (IOException e) {
                Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                sendErrorMsg("Read failed");
                break;
            }
        }
    }

    public void write(byte[] out) {
        Log.d(TAG, "write: Write Called.");

        try {
            if ( mmOutStream != null ) {
                mmOutStream.write(out);
            }
        } catch (IOException e) {
            Log.e(TAG, "write: Error on write. " + e.getMessage() );
            sendErrorMsg("Write failed");
        }
    }

    public void cancel() {
        try {
            if ( mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "write: Error on close. " + e.getMessage() );
            sendErrorMsg("Close failed");
        }
    }

    public void sendMsg(String from,String msg)
    {
        Message msgObj = m_activityHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("name", from);
        b.putString("message", msg);
        msgObj.setData(b);
        m_activityHandler.sendMessage(msgObj);
    }

    public void sendErrorMsg(String msg)
    {
        Message msgObj = m_activityHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("error", msg);
        msgObj.setData(b);
        m_activityHandler.sendMessage(msgObj);
    }

}