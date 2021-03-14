package cnam.com.chuck.rest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by georges on 28/12/15.
 */
public class ChuckRequest {
    private static final String LOG_TAG = ChuckRequest.class.getSimpleName();
    private static final int MY_SOCKET_TIMEOUT_MS = 5000;

    private final Gson m_gsonParser;
    private static Handler m_handler;
    private final Context m_context;
    private RequestQueue m_queue;

    private String m_baseServerUrl = "https://api.chucknorris.io/jokes";

    public ChuckRequest(Context context, Handler handler)
    {
        m_gsonParser = new GsonBuilder().create();
        m_handler = handler;
        m_context = context;
    }

    public void getCategory(String category, BasicListener listener) {
        String categoryUrl = "";
        if( category.equals("Random") ) {
            categoryUrl = m_baseServerUrl + "/random";
        } else {
            categoryUrl = m_baseServerUrl + "/random?category=" + category;
        }
        getRandomfromUrl(categoryUrl, listener);
    }

    public void getRandomfromUrl(String url, final BasicListener listener)
    {
        Log.i(LOG_TAG, "Connecting to URL; " + url);

        StringRequest jsonObjReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v(LOG_TAG, "onResponse OK");

                ChuckJoke joke = parseJsonJoke(new StringReader(response));

                if ( joke != null ) {
                    Message msgObj = m_handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("joke", joke.value);
                    msgObj.setData(b);
                    m_handler.sendMessage(msgObj);

                    if (joke.icon_url != null) {
                        getJokeIcon(joke, listener);
                    } else {
                        if (listener != null) listener.onSuccess();
                    }
                } else {
                    if (listener != null) listener.onSuccess();
                }
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

    public void getCategories(final BasicListener listener) {

        String completeUrl = m_baseServerUrl + "/categories";
        Log.i(LOG_TAG, "Connecting to URL; " + completeUrl);

        StringRequest jsonObjReq = new StringRequest(Request.Method.GET,
                completeUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v(LOG_TAG, "onResponse OK");

                ArrayList<String> categories = parseJsonArray(response);

                Message msgObj = m_handler.obtainMessage();
                Bundle b = new Bundle();
                b.putStringArrayList("categories", categories);
                msgObj.setData(b);
                m_handler.sendMessage(msgObj);
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

    private ArrayList<String> parseJsonArray(String response) {

        try {
            JSONArray categoriesJSON = new JSONArray(response);

            ArrayList<String> categoriesArray = new ArrayList<String>();
            if ( categoriesJSON != null) {
                for (int i=0;i<categoriesJSON.length();i++){
                    categoriesArray.add(categoriesJSON.getString(i));
                }
            }
            return categoriesArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getJokeIcon(ChuckJoke joke, final BasicListener listener) {

        ImageRequest imageRequest = new ImageRequest(joke.icon_url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                if( listener != null )
                    listener.onIconReceived(response);
            }
        }, 100, 100, null, null);

        imageRequest.setShouldCache(false);
        imageRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        this.getQueue().add(imageRequest);
    }

    public RequestQueue getQueue() {
        if ( m_queue == null) {
            m_queue = Volley.newRequestQueue(m_context);
        }
        return m_queue;
    }

    public ChuckJoke parseJsonJoke(StringReader reader)
    {
        if (reader == null)
            throw new IllegalArgumentException("InputStream can't be null");

        try {
//                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return m_gsonParser.fromJson(reader, ChuckJoke.class);
        }
        catch (final Exception e) {
            Log.e(LOG_TAG, "Error while parsing JSON Data");
        }

        return null;
    }

}
