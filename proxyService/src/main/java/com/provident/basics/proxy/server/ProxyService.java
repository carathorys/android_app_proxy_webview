/**
 * Copyright (c) 2013, The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.provident.basics.proxy.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.provident.basics.proxy.ProxyServiceRunningNotification;
import com.provident.proxylibrary.IProxyCallback;
import com.provident.proxylibrary.IProxyPortListener;
import com.provident.proxylibrary.IRequestUpdateNotification;

import org.androidannotations.annotations.EService;

import java.util.concurrent.TimeUnit;

import io.reactivex.subjects.ReplaySubject;

@EService
public class ProxyService extends Service {

    /** Keep these values up-to-date with PacManager.java */
    public static final String KEY_PROXY = "keyProxy";
    public static final String HOST = "localhost";
    // STOPSHIP This being a static port means it can be hijacked by other apps.
    public static final int PORT = 8182;
    public static final String EXCL_LIST = "";
    private static ProxyServer server = null;

    int mRequestCount = 0;
    long mInbound = 0, mOutbound = 0;
    private ReplaySubject<Long> Updater;

    @SuppressLint("CheckResult")
    public ProxyService() {
        Updater = ReplaySubject.create(1);
        Updater.throttleLatest(1, TimeUnit.SECONDS).subscribe(this::trigger);
    }

    public void trigger(Long l) {
        ProxyServiceRunningNotification.notify(this, mRequestCount, mInbound, mOutbound);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (server == null) {
            server = new ProxyServer();
            server.startServer();
            ProxyServiceRunningNotification.notify(this, 0, 0, 0);
        }
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stopServer();
            server = null;
            ProxyServiceRunningNotification.cancel(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IProxyCallback.Stub() {
            @Override
            public void getProxyPort(IBinder callback) throws RemoteException {
                if (server != null) {
                    IProxyPortListener portListener = IProxyPortListener.Stub.asInterface(callback);
                    if (portListener != null) {
                        server.setCallback(portListener);
                    }
                    server.setNotificationUpdated(new IRequestUpdateNotification.Stub() {
                        ///
                        @Override
                        public synchronized void requestNotificationUpdate(int requestCount, long inputSize, long outputSize) throws RemoteException {
                            mRequestCount = requestCount;
                            mInbound = inputSize;
                            mOutbound = outputSize;
                            Updater.onNext(0L);
                        }
                    });
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}