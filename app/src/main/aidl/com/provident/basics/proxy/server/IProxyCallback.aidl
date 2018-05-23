// IProxyCallback.aidl
package com.provident.basics.proxy.server;

// Declare any non-default types here with import statements

interface IProxyCallback {
   oneway void getProxyPort(IBinder callback);
   oneway void updateNotification(IBinder callback);
}
