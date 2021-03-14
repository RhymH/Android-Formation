package com.tp7webservice.rest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tp7webservice.models.PhotosInfos;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by georges on 28/12/15.
 */
public class DirectoryRequest {
    private static final String LOG_TAG = DirectoryRequest.class.getSimpleName();
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;

    private final Gson m_gsonParser;
    private static Handler m_handler;
    private final Context m_context;
    private LinkedHashMap<String,Bitmap> m_photos;
    private RequestQueue m_queue;

    private String m_baseServerUrl = "https://androidstudents-6f1d.restdb.io/media/";
    private String m_baseServerKey= "?key=7cd650c57e859ba05499fd7ee9a8f5b73c5ce";

    public DirectoryRequest(Context context,Handler handler)
    {
        m_gsonParser = new GsonBuilder().create();
        m_handler = handler;
        m_context = context;
        m_photos= new LinkedHashMap<>();
    }

    public void getPhotosInfos(final String baseUrl, final BasicListener listener)
    {
        String completeUrl = baseUrl + "/api/v1.0/photos";
        Log.i(LOG_TAG, "Connecting to URL; " + completeUrl);

        StringRequest jsonObjReq = new StringRequest(Request.Method.GET,
                completeUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v(LOG_TAG, "onResponse OK");

                InputStream inputStream = null;
                Message msgObj = m_handler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("state", "Connected");
                msgObj.setData(b);
                m_handler.sendMessage(msgObj);

                // ArrayList which contains all ours Movies
//                    Constant.listMovies = Arrays.asList(mGson.fromJson(response, Movie[].class));

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

                if( listener != null) listener.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "onResponse Error");
                // handle object
                if( listener != null) listener.onError(error.networkResponse.statusCode);
            }
        });

        jsonObjReq.setShouldCache(false);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        this.getQueue().add(jsonObjReq);
    }

    public RequestQueue getQueue() {
        if ( m_queue == null) {
            m_queue = Volley.newRequestQueue(m_context);
        }
        return m_queue;
    }

    public Bitmap getPhoto(String baseUrl, String photoName)
    {
        String uri = baseUrl + "/api/v1.0/photo/" + photoName;
        Log.i(LOG_TAG, "Getting photo from URL; " + uri);

        InputStream inputStream = null;
        Bitmap result = null;
        try
        {


            // receive response as inputStream
//            inputStream = httpResponse.getEntity().getContent();

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

//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost postRequest = new HttpPost(uri);
//        try
//        {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            photo.compress(Bitmap.CompressFormat.JPEG, 75, bos);
//            byte[] data = bos.toByteArray();
//            ByteArrayBody bab = new ByteArrayBody(data, "myphoto.jpg");
//
//            MultipartEntity reqEntity = new MultipartEntity(
//                    HttpMultipartMode.BROWSER_COMPATIBLE);
//            reqEntity.addPart("image", bab);
//            reqEntity.addPart("photoCaption", new StringBody("myphoto"));
//            postRequest.setEntity(reqEntity);
//            HttpResponse response = httpClient.execute(postRequest);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(
//                    response.getEntity().getContent(), "UTF-8"));
//
//            String sResponse;
//            StringBuilder s = new StringBuilder();
//            while ((sResponse = reader.readLine()) != null) {
//                s = s.append(sResponse);
//            }
//            System.out.println("Response: " + s);
//        }
//        catch (Exception e) {
//            // handle exception here
//            Log.e(e.getClass().getName(), e.getMessage());
//        }
    }

    public Bitmap postPhoto(final String contactName, final String photoPath)
    {
        //        final String url= "https://androidstudents-6f1d.restdb.io/media/"+m_myName.getText().toString()+"?key=22631469345172666884";
        String completeUrl = m_baseServerUrl + contactName + m_baseServerKey;
        Log.i(LOG_TAG, "Uploading photo at URL; " + completeUrl);

        InputStream inputStream = null;
        Bitmap result = null;

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, completeUrl,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        if ( response.statusCode == 200 ) {
                            Toast.makeText( m_context, "Post Photo made successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText( m_context, error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

                params.put("image", new DataPart("photo", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        // Adding request to request queue
        this.getQueue().add(volleyMultipartRequest);

        return result;
    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }



    public PhotosInfos parseJsonPhotosInfos(InputStream inputStream)
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
