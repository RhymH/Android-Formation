package com.tp.myapp1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button m_startButton= null;
    private TextView m_counterView= null;
    private int m_counter;
    private SeekBar m_mySeekBar;
    private ProgressBar m_myProgressBar;
    private TextView m_myProgressTextView;
    private Thread m_counterThread;
    private Boolean m_counting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_counterView = (TextView)this.findViewById(R.id.counter);
        m_startButton = (Button)this.findViewById(R.id.start);
        m_startButton.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View view) {
        if( view == m_startButton) {
            IncrementAndDisplayCounter();
        }
    }

    private void IncrementAndDisplayCounter() {
        if( !m_counting) {
            m_counting = true;
            m_startButton.setText("Stop");
            m_counterThread = new IncrementAndDisplayCounterThread();
            m_counterThread.start();
        }
        else {
            m_counting = false;
            m_startButton.setText("Start");
        }
    }

    class IncrementAndDisplayCounterThread extends Thread
    {
        public void run()
        {
            while(m_counting)
            {
                // Step1 ; crash de l'appli
//                showCounter();
                // Step2 : refresh dans le MAIN Thread
                showCounterInMainThread();

                m_counter += 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void showCounterInMainThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showCounter();
            }
        });
    }

    private void showCounter() {
        m_myProgressTextView.setText("Progress: " + m_counter);
        m_counterView.setText(String.valueOf(m_counter));
        if(m_counter <= 100)
            m_myProgressBar.setProgress(m_counter);
    }
}
