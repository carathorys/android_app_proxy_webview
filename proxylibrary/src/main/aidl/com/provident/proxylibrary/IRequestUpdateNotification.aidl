// IRequestUpdateNotification.aidl
package com.provident.proxylibrary;

// Declare any non-default types here with import statements

interface IRequestUpdateNotification {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    oneway void requestNotificationUpdate(int requestCount, long inputSize, long outputSize);
}
