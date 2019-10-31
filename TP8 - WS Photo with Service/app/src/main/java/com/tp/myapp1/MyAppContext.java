package com.tp.myapp1;

import android.util.Log;

/**
 * Created by georges on 31/12/15.
 */
public final class MyAppContext
{
    private static final String LOG_TAG = MyAppContext.class.getSimpleName();

    public enum ApplicationState
    {
        STOPPED, SERVICE_STARTED, SERVICE_UNBIND, SERVICE_BINDED
    }

    private static DirectoryRequest m_directoryRequest;

    private static ApplicationState m_applicationState = ApplicationState.STOPPED;

    public static ApplicationState getApplicationState()
    {
        return m_applicationState;
    }

    public static void setApplicationState(ApplicationState applicationState)
    {
        Log.i(LOG_TAG, applicationState.toString());
        m_applicationState = applicationState;
    }

    private MyAppContext()
    {
        // This is a static class
        throw new UnsupportedOperationException();
    }

    public static DirectoryRequest getDirectoryRequest()
    {
        return m_directoryRequest;
    }

    public static void setDirectoryRequest(DirectoryRequest directoryRequest)
    {
        Log.i(LOG_TAG, ">setDirectoryRequest");
        m_directoryRequest = directoryRequest;
    }
}
