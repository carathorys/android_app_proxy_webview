package com.provident.basics.proxy.server;

interface IProxyPortListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    oneway void setProxyPort(int port);
}
