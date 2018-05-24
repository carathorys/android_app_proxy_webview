// IProxyCallback.aidl
package com.provident.proxylibrary;

// Declare any non-default types here with import statements

interface IProxyCallback {
   oneway void getProxyPort(IBinder callback);
//   oneway void updateNotification(IBinder callback);
}
