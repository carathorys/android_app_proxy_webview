package com.provident.basics.proxy.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.EReceiver;

@EReceiver
public class MyReceiver extends BroadcastReceiver {

    static final String TAG = MyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: context: " + context);
        context.startService(ProxyService_.intent(context).get());
    }
}
