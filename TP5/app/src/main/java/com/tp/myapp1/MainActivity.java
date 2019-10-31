package com.tp.myapp1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private Button m_startButton= null;
    private TextView m_counterView= null;
    private int m_counter;
    private SeekBar m_mySeekBar;
    private ProgressBar m_myProgressBar;
    private TextView m_myProgressTextView;
    private Boolean m_counting = false;
    private Timer m_timer;
    private MyHandler m_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_counterView = (TextView)this.findViewById(R.id.counter);
        m_startButton = (Button)this.findViewById(R.id.start);
        m_startButton.setOnClickListener(m_startListener);

        m_myProgressTextView = (TextView)this.findViewById(R.id.progressValueTextView);
        m_mySeekBar = (SeekBar)this.findViewById(R.id.mySeekBar);
        m_mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                m_counter = i;
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

        m_handler = new MyHandler();
    }

    private View.OnClickListener m_startListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IncrementAndDisplayCounter();
        }
    };

    private void IncrementAndDisplayCounter()
    {
        if( !m_counting)
        {
            m_counting = true;
            m_startButton.setText("Stop");
            m_timer = new Timer();
            m_timer.scheduleAtFixedRate(new MyTimerTask(), new Date(), 1000);
        }
        else
        {
            m_counting = false;
            m_startButton.setText("Start");
            m_timer.cancel();
        }
    }

    //tells handler to send a message
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            m_counter += 1;
            // Step1 ; using EmptyMessage
//            m_handler.sendEmptyMessage(0);
            // Step2 ; using Message with parameters
            sendMsg("in progress...");
        }
    };

    public void sendMsg(String msg)
    {
        Message msgObj = m_handler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("message", "in progress...");
        b.putInt("counter", m_counter);
        msgObj.setData(b);
        m_handler.sendMessage(msgObj);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String aResponse = msg.getData().getString("message", "Empty");
            int counterToDisplay = msg.getData().getInt("counter", m_counter);

            Log.v("TP", "Message Received; " + aResponse);
            m_counterView.setText(String.valueOf(counterToDisplay));
            m_myProgressTextView.setText("Progress; "+String.valueOf(counterToDisplay));
            if (counterToDisplay <= 100) {
                m_myProgressBar.setProgress(counterToDisplay);
            }
        }
    }


}
