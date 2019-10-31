package com.tp.myapp1;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by georges on 14/12/15.
 */
public class MyPhotoService extends Service
{
    private String LOG_TAG= MyPhotoService.class.getSimpleName();

    private Timer m_timer = null;

    ArrayList<Messenger> m_clients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_DIRECTORY_REFRESH = 3;
    IncomingHandler m_handler = new IncomingHandler();
    final Messenger m_messenger = new Messenger(m_handler);
    private DirectoryRequest m_directory;

    @Override
    public IBinder onBind(Intent intent) {
        return m_messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    m_clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    m_clients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }

            int refreshMsg = msg.getData().getInt("refresh", -1);
            if( refreshMsg != -1) {
                Log.v(LOG_TAG, "Received refresh msg");
                sendMessageToUI(MSG_DIRECTORY_REFRESH);
            }
        }
    }

    private void sendMessageToUI(int msgValue)
    {
        for (int i= m_clients.size()-1; i>=0; i--)
        {
            try
            {
                // Send data as an Integer
                m_clients.get(i).send(Message.obtain(null, msgValue, 0, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                m_clients.remove(i);
            }
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(LOG_TAG, "Service Started.");

        if( MyAppContext.getDirectoryRequest() == null ) {
            Log.v(LOG_TAG , "Setting NEW DirectoryRequest");

            if( m_directory == null) {
                m_directory = new DirectoryRequest(m_handler);
            }
            MyAppContext.setDirectoryRequest(m_directory);
        }
        else {
            Log.v(LOG_TAG, "DirectoryRequest is already Set");
        }

        m_timer = new Timer();
        m_timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                onTimerTick();
            }
        }, 0, 2000L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);

        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);

        Notification noti = new Notification.Builder(getApplicationContext())
                .setContentTitle("Geo")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.avatar6)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1234, noti);

        return Service.START_STICKY;
    }

    private void onTimerTick()
    {
        Log.i(LOG_TAG, "Timer doing work.");
        try
        {
            //if( MyAppContext.getApplicationState() == MyAppContext.ApplicationState.SERVICE_BINDED)
                m_directory.getPhotosInfos();
            //else
            //    Log.v(LOG_TAG, "Service Not Binded");
        }
        catch (Throwable t)
        {
            //you should always ultimately catch all exceptions in m_timer tasks.
            Log.e(LOG_TAG, "Timer Tick Failed.", t);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (m_timer != null)
        {
            m_timer.cancel();
        }
        Log.i(LOG_TAG, "Service Stopped.");
    }
}
