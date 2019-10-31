package insa.com.androidarduinobt;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

/**
 * Created by georges on 16/10/2018.
 */

public class BluetoothApplication extends Application {
    private static final String LOG_TAG = "RainbowApplication";
    private static final String APPLICATION_ID = "9bf4e8b000f011e886d9b5bbd3260792";

    private static BluetoothApplication m_instance = null;

    public static BluetoothApplication instance()
    {
        return m_instance;
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();

        Log.i(LOG_TAG, "onTerminate");
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate()
    {
        super.onCreate();

        m_instance = this;
    }

    public void initialize()
    {
        Log.i(LOG_TAG, "initialize");

        //RainbowContext.getInfrastructure().run(this);
        
        //Context context = RainbowSdk.instance().getContext();
        //context.bindService(new Intent(context, RainbowService.class), m_rainbowServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT)
    }

}
