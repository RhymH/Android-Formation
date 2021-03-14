package cnam.com.mytestchuck.rest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.StringReader;

public class ChuckRequest {

    private static final int MY_SOCKET_TIMEOUT_MS = 5000;
    private final Handler m_handler;
    private final Context m_context;
    private final Gson m_gsonParser;

    private String m_baseServerUrl = "https://api.chucknorris.io/jokes";
    private RequestQueue m_queue;

    public ChuckRequest(Context context, Handler handler) {
        m_gsonParser = new GsonBuilder().create();
        m_handler = handler;
        m_context = context;
    }


    public void getRandom() {

        String url = m_baseServerUrl + "/random";
        StringRequest jsonObjReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("ChuckRequest", "onResponse OK");

                ChuckJoke joke = parseJsonJoke(new StringReader(response));
                if( joke != null ) {
                    Message msgObj = m_handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("joke", joke.value);
                    msgObj.setData(b);
                    m_handler.sendMessage(msgObj);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ChuckRequest", "onResponse ERROR");

            }
        });

        jsonObjReq.setShouldCache(false);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        this.getQueue().add(jsonObjReq);
    }

    private ChuckJoke parseJsonJoke(StringReader reader) {
        if (reader == null )
            return null;

        return m_gsonParser.fromJson(reader, ChuckJoke.class);
    }

    public RequestQueue getQueue() {
        if ( m_queue == null ) {
            m_queue = Volley.newRequestQueue(m_context);
        }
        return m_queue;
    }
}
