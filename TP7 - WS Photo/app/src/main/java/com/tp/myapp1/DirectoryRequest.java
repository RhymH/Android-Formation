package com.tp.myapp1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
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
    private final Context m_context;
    private LinkedHashMap<String,Bitmap> m_photos;

    public DirectoryRequest(Context context,Handler handler)
    {
        m_gsonParser = new GsonBuilder().create();
        m_handler = handler;
        m_context = context;
        m_photos= new LinkedHashMap<>();
    }

    public List<String> getPhotosInfos(String baseUrl)
    {
        InputStream inputStream = null;
        List<String> result = new ArrayList<>();
        try
        {
            String uri = baseUrl + "/api/v1.0/photos";
            Log.i(LOG_TAG, "Connecting to URL; " + uri);

            // Create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(uri));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                Log.v(LOG_TAG, "inputStream OK");

                Message msgObj = m_handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("state", "Connected");
                msgObj.setData(b);
                m_handler.sendMessage(msgObj);

                PhotosInfos photosInfos = parseJsonPhotosInfos(inputStream);
                for(PhotosInfos.PhotoInfo photoinfo : photosInfos.photos)
                {
                    Log.v(LOG_TAG, "Photo name; " + photoinfo.name);
                    Bitmap photo = getPhoto(baseUrl, photoinfo.name);
                    m_photos.put(photoinfo.name, photo);
                }

                msgObj = m_handler.obtainMessage();
                b = new Bundle();
                b.putInt("refresh", 1);
                msgObj.setData(b);
                m_handler.sendMessage(msgObj);
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

    public Bitmap getPhoto(String baseUrl, String photoName)
    {
        String uri = baseUrl + "/api/v1.0/photo/" + photoName;
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

                result = BitmapFactory.decodeStream(inputStream);
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


    public void postPhotoCompressed(String baseUrl, String photoName, Bitmap photo)
    {
        String uri = baseUrl + "/api/v1.0/photo/" + photoName;
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

    public Bitmap postPhoto(String baseUrl, String photoName, File photoFile)
    {
        String uri = baseUrl + "/api/v1.0/photo/" + photoName;
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

        try {
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
