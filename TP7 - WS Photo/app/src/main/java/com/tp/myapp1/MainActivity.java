package com.tp.myapp1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;


public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 456;

    private Button m_connectButton= null;
    private static MyHandler m_handler;
    private TextView m_stateTextView;
    private EditText m_ipAddressET;
    private GridView m_gridPhotoView;
    private PhotoAdapter m_photoListAdapter;
    private DirectoryRequest m_directory;
    private ImageView m_myImage;
    private TextView m_myName;
    private Button m_cameraButton;
    private Button m_sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_handler = new MyHandler();

        m_myImage = (ImageView)this.findViewById(R.id.my_image);
        m_myName = (TextView)this.findViewById(R.id.my_name);

        m_ipAddressET = (EditText)this.findViewById(R.id.ip_address);
        m_stateTextView = (TextView)this.findViewById(R.id.statusTextView);
        m_connectButton = (Button) this.findViewById(R.id.connect);
        m_connectButton.setOnClickListener(m_connectListener);

        m_gridPhotoView = (GridView) this.findViewById(R.id.images_gridView);
        m_photoListAdapter = new PhotoAdapter(this);
        m_gridPhotoView.setAdapter(m_photoListAdapter);
        m_gridPhotoView.setOnItemClickListener(m_photoClickListener);

        m_cameraButton = (Button)this.findViewById(R.id.my_camera);
        m_cameraButton.setOnClickListener(m_cameraListener);

        m_sendButton = (Button)this.findViewById(R.id.my_send);
        m_sendButton.setOnClickListener(m_sendListener);

        m_directory = new DirectoryRequest(this, m_handler);
    }

    private AdapterView.OnItemClickListener m_photoClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Log.v(LOG_TAG, "onClick");
            m_myImage.setImageBitmap(m_photoListAdapter.getImage(position));
            m_myName.setText(m_photoListAdapter.getImageName(position));
        }
    };

    private View.OnClickListener m_connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String url= "http://"+m_ipAddressET.getText()+":5000";

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    m_directory.getPhotosInfos(url);
                }
            });
            thread.setName(LOG_TAG);
            thread.start();
        }
    };

    private View.OnClickListener m_cameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.v(LOG_TAG, "CameraButton; onClick");

            //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            //{
            //    //takePictureIntent.putExtra("crop", "true");
            //    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            //}
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
            final String url= "http://"+m_ipAddressET.getText()+":5000";
            final Bitmap bitmap = ((BitmapDrawable)m_myImage.getDrawable()).getBitmap();
            //final File pictureFile = getOutputMediaFile();
            final File pictureFile = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String photoName = m_myName.getText().toString()+".jpg";
                    m_directory.postPhoto(url, photoName, pictureFile);
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
            final File pictureFile = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
            Bitmap imageBitmap = decodeSampledBitmapFromFile(pictureFile.getAbsolutePath(), 1000, 700);
            
            m_myImage.setImageBitmap(imageBitmap);
            m_sendButton.setVisibility(View.VISIBLE);
        }
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

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String stateMsg = msg.getData().getString("state");
            if( stateMsg != null)
                m_stateTextView.setText(stateMsg);
            int refreshMsg = msg.getData().getInt("refresh", -1);
            if( refreshMsg != -1) {
                m_photoListAdapter.setListImages(m_directory.getCurrentPhotos());

                m_photoListAdapter.notifyDataSetChanged();
            }
        }
    }


}
