package com.tp.myapp1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by georges on 28/12/15.
 */
public class DirectoryRequest {
    private static final String LOG_TAG = DirectoryRequest.class.getSimpleName();

    private final Gson m_gsonParser;
    private static Handler m_handler;
    private LinkedHashMap<String,Bitmap> m_photos;
    private PhotoPoolMgr m_photoPool = null;
    private static String m_baseUrl = "http://10.28.202.28:5000";
    //private static String m_baseUrl = "http://192.168.0.25:5000";

    public DirectoryRequest(Handler handler)
    {
        Log.d(LOG_TAG, "Creating DirectoryRequest");
        m_gsonParser = new GsonBuilder().create();
        m_handler = handler;
        m_photos= new LinkedHashMap<>();
        m_photoPool = new PhotoPoolMgr();
    }

    public List<String> getPhotosInfos()
    {
        InputStream inputStream = null;
        List<String> result = new ArrayList<>();
        try
        {
            String uri = m_baseUrl + "/api/v1.0/photos";
            //Log.i(LOG_TAG, "Connecting to URL; " + uri);

            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(uri));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
            {
                Log.v(LOG_TAG, "inputStream OK");

                Message msgObj = m_handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("state", "Connected");
                msgObj.setData(b);
                m_handler.sendMessage(msgObj);

                boolean isRefreshNeeded = false;
                PhotosInfos newPhotosInfos = parseJsonPhotosInfos(inputStream);
                for(PhotosInfos.PhotoInfo photoinfo : newPhotosInfos.photos)
                {
                    //Log.v(LOG_TAG, "Photo name; " + photoinfo.name);
                    //Log.v(LOG_TAG, "Photo hash; " + photoinfo.hash);
                    if( m_photoPool.hasPhotoChanged(photoinfo))
                    {
                        Log.v(LOG_TAG, "Photo "+photoinfo.name+" has changed => getting Photo" );
                        Bitmap photo = getPhoto(photoinfo.name);
                        m_photos.put(photoinfo.name, photo);
                        isRefreshNeeded = true;

                        sendRefreshNeededMsg();
                    }
                }
                if( m_photoPool.isPhotoMissing(newPhotosInfos))
                {
                    Log.v(LOG_TAG, "A photo has been suppressed from the Server" );
                    // Need to remove photo as well
                    List<String> photoMissingNameList = m_photoPool.getPhotoMissingNameList(newPhotosInfos);
                    for (String curPhotoName : photoMissingNameList) {
                        Log.v(LOG_TAG, "Removing photo ; "+curPhotoName );
                        m_photos.remove(curPhotoName);
                    }
                    isRefreshNeeded = true;
                }
                Log.v(LOG_TAG, "Photos download finished");

                m_photoPool.setPhotosInfos(newPhotosInfos);

                if(isRefreshNeeded == true) {
                    Log.v(LOG_TAG, "Photo changed => RefreshNeeded" );
                    sendRefreshNeededMsg();
                }
            }
            else {
                Log.v(LOG_TAG, "inputStream KO");
            }
        }
        catch (Exception e) {
            Log.d(LOG_TAG, "Exception; " + e.getMessage());
        }

        return result;
    }

    private void sendRefreshNeededMsg()
    {
        Message msgObj = m_handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("refresh", 1);
        msgObj.setData(b);
        m_handler.sendMessage(msgObj);
    }

    public Bitmap getPhoto(String photoName)
    {
        String uri = m_baseUrl + "/api/v1.0/photo/" + photoName;
        Log.i(LOG_TAG, "Getting photo from URL; " + uri);

        InputStream inputStream = null;
        Bitmap result = null;
        try
        {
            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(uri));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                Log.v(LOG_TAG, "inputStream OK");

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                result = BitmapFactory.decodeStream(inputStream, null, options);
            }
            else {
                Log.v(LOG_TAG, "inputStream KO");
            }
        }
        catch (Exception e) {
            Log.d(LOG_TAG, "Exception; " + e.getMessage());
        }

        return result;
    }


    public void postPhotoCompressed(String photoName, Bitmap photo)
    {
        String uri = m_baseUrl + "/api/v1.0/photo/" + photoName;
        Log.i(LOG_TAG, "Uploading photo at URL; " + uri);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(uri);
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 75, bos);
            byte[] data = bos.toByteArray();
            ByteArrayBody bab = new ByteArrayBody(data, "myphoto.jpg");

            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("image", bab);
            reqEntity.addPart("photoCaption", new StringBody("myphoto"));
            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));

            String sResponse;
            StringBuilder s = new StringBuilder();
            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            System.out.println("Response: " + s);
        }
        catch (Exception e) {
            // handle exception here
            Log.e(e.getClass().getName(), e.getMessage());
        }
    }

    public Bitmap postPhoto(String photoName, Bitmap photoBitmap)
    {
        String uri = m_baseUrl + "/api/v1.0/photo/" + photoName;
        Log.i(LOG_TAG, "Uploading photo at URL; " + uri);

        InputStream inputStream = null;
        Bitmap result = null;
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(uri);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            ByteArrayBody bab = new ByteArrayBody(byteArray, "myphoto.jpg");

            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("image", bab);
            reqEntity.addPart("photoCaption", new StringBody("myphoto"));
            post.setEntity(reqEntity);


            HttpResponse httpResponse = client.execute(post);
            HttpEntity r_entity = httpResponse.getEntity();

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                // Server response
                String responseString = EntityUtils.toString(r_entity);
                Log.d("Log", responseString);
            }
            else
            {
                String responseString = "Error occurred! Http Status Code: "
                        + statusCode + " -> " + httpResponse.getStatusLine().getReasonPhrase();
                Log.d("Log", responseString);
            }
        }
        catch (Exception e) {
            Log.d(LOG_TAG, "Exception; " + e.getMessage());
        }

        return result;
    }

    protected int byteSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        }
        else {
            return data.getAllocationByteCount();
        }
    }

    public Bitmap postPhoto(String photoName, File photoFile)
    {
        String uri = m_baseUrl + "/api/v1.0/photo/" + photoName;
        Log.i(LOG_TAG, "Uploading photo at URL; " + uri);

        InputStream inputStream = null;
        Bitmap result = null;
        try
        {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(uri);

            int photoSize = (int)photoFile.length();
            byte[] bFile = new byte[(int) photoFile.length()];
            FileInputStream fileInputStream = new FileInputStream(photoFile);
            fileInputStream.read(bFile);
            fileInputStream.close();

            ByteArrayBody bab = new ByteArrayBody(bFile, "myphoto.jpg");

            MultipartEntity reqEntity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("image", bab);
            reqEntity.addPart("photoCaption", new StringBody("myphoto"));
            post.setEntity(reqEntity);


            HttpResponse httpResponse = client.execute(post);
            HttpEntity r_entity = httpResponse.getEntity();

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                // Server response
                String responseString = EntityUtils.toString(r_entity);
                Log.d("Log", responseString);
            }
            else
            {
                String responseString = "Error occurred! Http Status Code: "
                        + statusCode + " -> " + httpResponse.getStatusLine().getReasonPhrase();
                Log.d("Log", responseString);
            }
        }
        catch (Exception e) {
            Log.d(LOG_TAG, "Exception; "+e.getMessage());
        }

        return result;
    }

    public PhotosInfos parseJsonPhotosInfos(InputStream inputStream) throws XmlPullParserException, IOException, IllegalArgumentException
    {
        if (inputStream == null)
            throw new IllegalArgumentException("InputStream can't be null");

        try
        {
            if (inputStream != null)
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return m_gsonParser.fromJson(reader, PhotosInfos.class);
            }
        }
        catch (final Exception e) {
            Log.e(LOG_TAG, "Error while parsing JSON Data");
        }

        return null;
    }

    public LinkedHashMap<String,Bitmap> getCurrentPhotos() {
        return m_photos;
    }
}
