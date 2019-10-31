package insa.com.mybluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by georges on 04/10/2018.
 */

public class ClientThread extends AbstractBtThread {
    private static final String TAG = "ClientThread";

    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter m_myBluetoothAdapter;

    BluetoothSocket mmSocket;

    InputStream mmInStream;
    OutputStream mmOutStream;
    ChatActivity.MyHandler m_activityHandler;


    public ClientThread(BluetoothDevice device, ChatActivity.MyHandler handler) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        m_activityHandler = handler;
        m_myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            tmp = device.createRfcommSocketToServiceRecord(ServerThread.SERVER_UUID);
        } catch (IOException e) {
            Log.e(TAG, "write: Error on createRfCommSocket. " + e.getMessage() );
            sendErrorMsg("createRfCommSocket failed");
        }
        mmSocket = tmp;
    }

    public void run() {
        m_myBluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();

            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();

            manageConnectedSocket();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "write: Error on close. " + e.getMessage() );
                sendErrorMsg("Close failed");
            }
            return;
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
                this.sendMsg("Server", incomingMessage);
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
            e.printStackTrace();
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