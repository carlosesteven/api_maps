package com.example.melo;

import android.util.Log;

public class Util {

    public static void ConsolaDebug(String msg)
    {

        if ( msg != null && !msg.isEmpty() )
        {
            Log.d("csc_debug", msg);
        }

    }

    public static void ConsolaDebug(String TAG, String msg)
    {

        if ( msg != null && !msg.isEmpty() && TAG != null && !TAG.isEmpty() )
        {
            Log.d("csc_debug", "[ " + TAG.toUpperCase() + " ] -> " + msg);
        }

    }

    public static void ConsolaDebugError(String TAG, String msg)
    {

        if ( msg != null && !msg.isEmpty() && TAG != null && !TAG.isEmpty()  )
        {
            Log.e("csc_debug", "[ " + TAG.toUpperCase() + " ] -> " + msg);
        }

    }

}
