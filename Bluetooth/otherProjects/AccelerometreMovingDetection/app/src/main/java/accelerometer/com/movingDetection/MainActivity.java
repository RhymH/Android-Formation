package accelerometer.com.movingDetection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView m_XTextView;
    private TextView m_YTextView;
    private TextView m_ZTextView;
    private View m_canvas;
    private CanvasView m_customCanvas;

    private SensorService m_sensorService = null;


    private ServiceConnection onService = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            m_sensorService = ((SensorService.LocalBinder) rawBinder).getService();
            m_sensorService.setHandler(m_myHandler);
        }

        public void onServiceDisconnected(ComponentName className) {
            m_sensorService = null;
        }
    };

    private Handler m_myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            float x = msg.getData().getFloat(SensorService.X_VALUE);
            float y = msg.getData().getFloat(SensorService.Y_VALUE);

            m_XTextView.setText("X: " + String.valueOf(x));
            m_YTextView.setText("Y: " + String.valueOf(y));

            float xPercent = (x+10)/20;
            float yPercent = (y+10)/20;

            m_customCanvas.setCirclePosition(xPercent, yPercent);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_customCanvas = findViewById(R.id.signature_canvas);

        m_XTextView = findViewById(R.id.XTextView);
        m_YTextView = findViewById(R.id.YTextView);
        m_ZTextView = findViewById(R.id.ZTextView);



        bindService(new Intent(this, SensorService.class), onService, Context.BIND_AUTO_CREATE);
        startService(new Intent(this, SensorService.class));
    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            getAccelerometer(event);
//        }
//    }
//
//    private void getAccelerometer(SensorEvent event) {
//        float[] values = event.values;
//        // Movement
//        float x = values[0];
//        float y = values[1];
//        float z = values[2];
//
//        m_XTextView.setText("X: " + String.valueOf(x));
//        m_YTextView.setText("Y: " + String.valueOf(y));
//        m_ZTextView.setText("Z: " + String.valueOf(z));
//
//        float xPercent = (x+10)/20;
//        float yPercent = (y+10)/20;
//
//        m_customCanvas.setCirclePosition(xPercent, yPercent);
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // register this class as a listener for the orientation and
//        // accelerometer sensors
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_NORMAL);
//    }

//    @Override
//    protected void onPause() {
//        // unregister listener
//        super.onPause();
//        sensorManager.unregisterListener(this);
//    }
}