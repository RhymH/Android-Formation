package com.tp.myapp1;

import android.app.Activity;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;


public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 456;

    private GridView m_gridPhotoView;
    private PhotoAdapter m_photoListAdapter;
    private DirectoryRequest m_directory;
    private ImageView m_myImage;
    private TextView m_myName;
    private Button m_cameraButton;
    private Button m_sendButton;

    private Messenger m_service;
    final Messenger m_messenger = new Messenger(new IncomingHandler());

    private ServiceConnection m_connection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            m_service = new Messenger(service);
            m_directory = MyAppContext.getDirectoryRequest();

            sendMsgToService(MyPhotoService.MSG_REGISTER_CLIENT);

            MyAppContext.setApplicationState(MyAppContext.ApplicationState.SERVICE_BINDED);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            m_service = null;
            MyAppContext.setApplicationState(MyAppContext.ApplicationState.SERVICE_UNBIND);
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

    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            Log.v(LOG_TAG, "Received ; "+msg.what);

            switch (msg.what)
            {
                case MyPhotoService.MSG_DIRECTORY_REFRESH:
                    Log.v(LOG_TAG, "Received MSG_DIRECTORY_REFRESH");
                    m_photoListAdapter.setListImages(m_directory.getCurrentPhotos());
                    m_photoListAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG_TAG, ">onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_myImage = (ImageView)this.findViewById(R.id.my_image);
        m_myName = (TextView)this.findViewById(R.id.my_name);

        m_gridPhotoView = (GridView) this.findViewById(R.id.images_gridView);
        m_photoListAdapter = new PhotoAdapter(this);
        m_gridPhotoView.setAdapter(m_photoListAdapter);
        m_gridPhotoView.setOnItemClickListener(m_photoClickListener);

        m_cameraButton = (Button)this.findViewById(R.id.my_camera);
        m_cameraButton.setOnClickListener(m_cameraListener);

        m_sendButton = (Button)this.findViewById(R.id.my_send);
        m_sendButton.setOnClickListener(m_sendListener);

        doBindService();

        m_directory = MyAppContext.getDirectoryRequest();
        if( m_directory != null &&
                m_directory.getCurrentPhotos() != null &&
                m_directory.getCurrentPhotos().size() > 0) {
            m_photoListAdapter.setListImages(m_directory.getCurrentPhotos());
            m_photoListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy(){
        Log.d(LOG_TAG, ">onDestroy");

        super.onDestroy();

        doUnbindService();
    }

    void doBindService()
    {
        Log.d(LOG_TAG, ">doBindService");

        if( MyAppContext.getApplicationState() == MyAppContext.ApplicationState.STOPPED) {
            startService(new Intent(this, MyPhotoService.class));
            MyAppContext.setApplicationState(MyAppContext.ApplicationState.SERVICE_STARTED);
        }

        bindService(new Intent(this, MyPhotoService.class), m_connection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService()
    {
        Log.d(LOG_TAG, ">doUnbindService");

        // Detach our existing connection.
        if( m_connection != null )
            unbindService(m_connection);

        MyAppContext.setApplicationState(MyAppContext.ApplicationState.SERVICE_UNBIND);
    }

    private AdapterView.OnItemClickListener m_photoClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Log.v(LOG_TAG, "onClick");
        m_myImage.setImageBitmap(m_photoListAdapter.getImage(position));
        m_myName.setText(m_photoListAdapter.getImageName(position));
        }
    };

    private View.OnClickListener m_cameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.v(LOG_TAG, "CameraButton; onClick");

            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    };

    private View.OnClickListener m_sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.v(LOG_TAG, "SendButton; onClick");
            String filePath = Environment.getExternalStorageDirectory()+File.separator + "image.jpg";
            final Bitmap picture = decodeFile(filePath);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String photoName = m_myName.getText().toString()+".jpg";
                    m_directory.postPhoto(photoName, picture);
                    //m_directory.postPhoto(url, "test.jpg", bitmap);
                }
            });
            thread.setName(LOG_TAG);
            thread.start();

            //m_sendButton.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            /*
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            storeImage(imageBitmap);
            */
            String filePath = Environment.getExternalStorageDirectory()+File.separator + "image.jpg";
            //final File pictureFile = new File(filePath);
            //Bitmap imageBitmap = decodeSampledBitmapFromFile(pictureFile.getAbsolutePath(), 1000, 700);
            Bitmap imageBitmap = decodeFile(filePath);
            
            m_myImage.setImageBitmap(imageBitmap);
            m_sendButton.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap decodeFile(String filePath)
    {
        try
        {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=300;
            Log.v(LOG_TAG, "Scale initial width="+o.outWidth);
            Log.v(LOG_TAG, "Scale initial height="+o.outHeight);

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE)
            {
                scale *= 2;
            }
            Log.v(LOG_TAG, "Scale="+scale);

            // Decode with inSampleSize
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            return BitmapFactory.decodeFile(filePath, options);
//            return BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Exception; "+e.getMessage());
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    private void storeImage(Bitmap image)
    {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null)
        {
            Log.d(LOG_TAG, "Error creating media file, check storage permissions");
            return;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile()
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "my_photo.jpg");
        return mediaFile;
    }


}
