package com.tp.myapp1;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {

    private TextView m_serviceInfoView = null;
    private SeekBar m_mySeekBar;
    private ProgressBar m_myProgressBar;
    private TextView m_myProgressTextView;
    private Button m_startServiceButton= null;
    private Button m_bindButton;
    private Button m_startButton= null;
    private boolean m_isBound;
    private Messenger m_service;
    final Messenger m_messenger = new Messenger(new IncomingHandler());
    private Context m_context;
    private Handler m_myHandler = new Handler();

    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MyTestService.MSG_SET_INT_VALUE:
//                    m_serviceInfoView.setText("Int Message: " + msg.arg1);
                    m_myProgressBar.setProgress(msg.arg1);
                    break;
                case MyTestService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("str1");
                    m_myProgressTextView.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection m_connection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            m_service = new Messenger(service);
            m_serviceInfoView.setText("Connected.");

            sendMsgToService(MyTestService.MSG_REGISTER_CLIENT);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            m_service = null;
            m_serviceInfoView.setText("Disconnected.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_context = getApplicationContext();

        m_startServiceButton = (Button)this.findViewById(R.id.start_service);
        m_startServiceButton.setOnClickListener(m_startServiceListener);
        m_startButton = (Button)this.findViewById(R.id.start_button);
        m_startButton.setOnClickListener(m_startListener);
        m_bindButton = (Button)this.findViewById(R.id.bind);
        m_bindButton.setOnClickListener(m_bindListener);
//        m_bindButton.setEnabled(true);

        m_serviceInfoView = (TextView) this.findViewById(R.id.service_info);
        m_myProgressTextView = (TextView)this.findViewById(R.id.progressValueTextView);
        m_mySeekBar = (SeekBar)this.findViewById(R.id.mySeekBar);
        m_mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_myProgressBar.setProgress(i);
                m_myProgressTextView.setText(String.format("Progress ; %d", i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        m_myProgressBar = (ProgressBar) this.findViewById(R.id.myProgressBar);
    }

    /*@Override
    public void onDestroy()
    {
        super.onDestroy();
        doUnbindService();
        stopService(new Intent(MainActivity.this, MyTestService.class));
    }
    */

    private View.OnClickListener m_startServiceListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if( m_startServiceButton.getText().equals(m_context.getString(R.string.start_service)))
            {
                m_startServiceButton.setText(m_context.getString(R.string.stop_service));
                startService(new Intent(MainActivity.this, MyTestService.class));
                //m_bindButton.setEnabled(true);
            }
            else
            {
                m_startServiceButton.setText(m_context.getString(R.string.start_service));
                stopService(new Intent(MainActivity.this, MyTestService.class));
                m_bindButton.setText(m_context.getString(R.string.bind));
                //m_bindButton.setEnabled(false);
                m_startButton.setEnabled(false);
            }
        }
    };

    private View.OnClickListener m_bindListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if( m_bindButton.getText().equals(m_context.getString(R.string.bind)))
            {
                m_bindButton.setText(m_context.getString(R.string.unbind));
                doBindService();
                m_startButton.setEnabled(true);
            }
            else
            {
                m_bindButton.setText(m_context.getString(R.string.bind));
                doUnbindService();
                m_startButton.setEnabled(false);
            }
        }
    };

    private View.OnClickListener m_startListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if( m_startButton.getText().equals(m_context.getString(R.string.start))) {
                m_startButton.setText(m_context.getString(R.string.stop));
                sendMsgToService(MyTestService.MSG_START_COUNTER);
            }
            else {
                m_startButton.setText(m_context.getString(R.string.start));
                sendMsgToService(MyTestService.MSG_STOP_COUNTER);
            }
        }
    };

    private View.OnClickListener m_resetListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            sendMsgToService(MyTestService.MSG_RESET_COUNTER);
        }
    };

    private void sendMsgToService(int msgId) {
        try {
            Message msg = Message.obtain(null, msgId);
            msg.replyTo = m_messenger;
            m_service.send(msg);
        }
        catch (RemoteException e)
        {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    void doBindService()
    {
        bindService(new Intent(this, MyTestService.class), m_connection, Context.BIND_AUTO_CREATE);
        m_isBound = true;
        m_serviceInfoView.setText("Binding");
    }

    void doUnbindService()
    {
        if (m_isBound)
        {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (m_service != null)
            {
                try {
                    Message msg = Message.obtain(null, MyTestService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = m_messenger;
                    m_service.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(m_connection);
            m_isBound = false;
            m_serviceInfoView.setText("Disconnected.");
        }
    }
}
