package com.tp.myapp1;

import android.app.Activity;
import android.os.AsyncTask;
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
    private IncrementAndDisplayCounterAsyncTask m_counterAsyncTask;
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
        if( view == m_startButton)
        {
            IncrementAndDisplayCounterUsingAsyncTask();
        }
    }

    private void IncrementAndDisplayCounterUsingAsyncTask()
    {
        if( !m_counting) {
            m_counting = true;
            m_startButton.setText("Stop");
            m_counterAsyncTask = new IncrementAndDisplayCounterAsyncTask();
            m_counterAsyncTask.execute(m_counter);
        }
        else {
            m_counting = false;
            m_counterAsyncTask.stopCounting();
            m_counter = m_counterAsyncTask.getCountValue();
            m_startButton.setText("Start");
        }
    }

    class IncrementAndDisplayCounterAsyncTask extends AsyncTask<Integer, Integer, Void>
    {
        Boolean m_runningTask = true;
        int m_count = 0;

        public int getCountValue() {
            return m_count;
        }

        public void stopCounting() {
            m_runningTask = false;
        }

        @Override
        protected Void doInBackground(Integer... start) {
            m_count = start[0];
            while( m_runningTask && m_count < 100)
            {
                publishProgress(m_count);
                m_count += 1;

                // BE CAREFULL ; never do this in real application
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... counter)
        {
            int counterValue = counter[0];
            m_myProgressTextView.setText("Progress: " + counterValue);
            m_counterView.setText(String.valueOf(counterValue));
            if(counter[0] <= 100)
                m_myProgressBar.setProgress(counterValue);
        }

        @Override
        protected void onCancelled() {
            m_runningTask = false;
        }
    }

}
