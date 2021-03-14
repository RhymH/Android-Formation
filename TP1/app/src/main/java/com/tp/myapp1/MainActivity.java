package com.tp.myapp1;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private Button m_startButton= null;
    private TextView m_counterView= null;
    private int m_counter;
    private SeekBar m_mySeekBar;
    private ProgressBar m_myProgressBar;
    private TextView m_myProgressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_counterView = this.findViewById(R.id.counter);
        m_startButton = this.findViewById(R.id.start);


        m_myProgressTextView = this.findViewById(R.id.progressValueTextView);
        m_myProgressBar = this.findViewById(R.id.myProgressBar);
        m_mySeekBar = this.findViewById(R.id.mySeekBar);
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
    }

    public void onStartClick(View view) {
        if( view == m_startButton)
        {
            // Step 1 ;
            showToast();
            // Step 2 ;
//            showSnackBar();
            // Step 3 ;
//            incrementAndDisplayCounter();
            // Step 4 ;
//            loopIncrementAndDisplayCounter();
        }
    }

    private void showToast() {
        Toast.makeText(this,"Hello, my test is running well", Toast.LENGTH_LONG).show();
    }

    private void showSnackBar() {
        Snackbar snackbar = Snackbar
                .make(m_mySeekBar, "Hello, my test is running well", Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    private void incrementAndDisplayCounter() {
        m_counter += 1;
        m_counterView.setText(String.valueOf(m_counter));
    }

    private void loopIncrementAndDisplayCounter() {
        while(m_counter< 100) {
            m_counterView.setText(String.valueOf(m_counter));
            m_counter += 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
