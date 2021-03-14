package com.tp7webservice.rest;

import java.util.Map;

class RESTResult
{
    private static final String LOG_TAG = "RESTResult";

    private String m_response;
    private Map<String,String> m_headers;

    public RESTResult(String response)
    {
        m_response = response;
    }

    public RESTResult(String response, Map<String,String> headers)
    {
        m_response = response;
        m_headers = headers;
    }

    public Map<String,String> getHeaders()
    {
        return m_headers;
    }


    public String getResponse() {
        return m_response;
    }
}
