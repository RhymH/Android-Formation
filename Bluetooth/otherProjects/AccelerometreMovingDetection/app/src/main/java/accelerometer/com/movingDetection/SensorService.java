package accelerometer.com.movingDetection;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;


public class SensorService extends Service implements SensorEventListener {

    public static final String X_VALUE = "X_VALUE";
    public static final String Y_VALUE = "Y_VALUE";

    private SensorManager m_sensorManager;
    private Handler m_activityHandler = null;
    /**
     * The binder that glue to my service
     */
    private final Binder binder=new LocalBinder();

    public void setHandler(Handler handler) {
        m_activityHandler = handler;
    }

    /**
     * @author mSeguy
     * @goals This class aims to define the binder to use for my service
     */
    public class LocalBinder extends Binder {
        /**
         * @return the service you want to bind to : i.e. this
         */
        SensorService getService() {
            return (SensorService.this);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(SENSOR_SERVICE);

        Sensor accel = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];


        if( m_activityHandler != null ) {
            Bundle messageBundle = new Bundle();
            /**
             * Le message échangé entre la Thread et le Handler
             */
            Message myMessage = m_activityHandler.obtainMessage();
            messageBundle.putFloat(X_VALUE, x);
            messageBundle.putFloat(Y_VALUE, y);
            //Ajouter le Bundle au message
            myMessage.setData(messageBundle);
            //Envoyer le message
            m_activityHandler.sendMessage(myMessage);
        }
    }

    @Override
    public void onDestroy() {
        m_sensorManager.unregisterListener(this);
        m_activityHandler = null;
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}