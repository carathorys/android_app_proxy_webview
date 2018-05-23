package com.provident.basics.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import org.apache.http.HttpHost;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for setting WebKit proxy used by Android WebView
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class ProxySettings {

    static final String TAG = "ProxySettings";

    static final int PROXY_CHANGED = 193;

    private static Object getDeclaredField(Object obj, String name) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);

        return f.get(obj);
    }

    public static Object getRequestQueue(Context ctx) throws Exception {
        Object ret = null;
        Class networkClass = Class.forName("android.webkit.Network");
        if (networkClass != null) {
            Object networkObj = invokeMethod(networkClass, "getInstance", new Object[]{ctx},
                    Context.class);
            if (networkObj != null) {
                ret = getDeclaredField(networkObj, "mRequestQueue");
            }
        }
        return ret;
    }

    private static Object invokeMethod(Object object, String methodName, Object[] params,
                                       Class... types) throws Exception {
        Object out = null;
        Class c = object instanceof Class ? (Class) object : object.getClass();
        if (types != null) {
            Method method = c.getMethod(methodName, types);
            out = method.invoke(object, params);
        } else {
            Method method = c.getMethod(methodName);
            out = method.invoke(object);
        }

        return out;
    }

    public static void resetProxy(Context ctx) throws Exception {
        Object requestQueueObject = getRequestQueue(ctx);
        if (requestQueueObject != null) {
            setDeclaredField(requestQueueObject, "mProxyHost", null);
        }
    }

    private static void setDeclaredField(Object obj, String name, Object value)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    /**
     * Override WebKit Proxy settings
     *
     * @param ctx  Android ApplicationContext
     * @param host
     * @param port
     * @return true if Proxy was successfully set
     */
    public static boolean setProxy(Context ctx, String host, int port) {
        Log.d(TAG, "Set proxy");
        boolean ret = false;
        setSystemProperties(host, port);

        try {
            if (Build.VERSION.SDK_INT > 20) {
                ret = setProxyMM(ctx, host, port);
            } else if (Build.VERSION.SDK_INT > 18) {
                ret = setKitKatProxy(ctx, host, port);
            } else if (Build.VERSION.SDK_INT > 13) {
                ret = setICSProxy(host, port);
            } else if (Build.VERSION.SDK_INT > 19) {
                Object requestQueueObject = getRequestQueue(ctx);
                if (requestQueueObject != null) {
                    // Create Proxy config object and set it into request Q
                    HttpHost httpHost = new HttpHost(host, port, "http");

                    setDeclaredField(requestQueueObject, "mProxyHost", httpHost);
                    ret = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @SuppressLint("PrivateApi")
    private static boolean setICSProxy(String host, int port) throws ClassNotFoundException,
            NoSuchMethodException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        Class webViewCoreClass = Class.forName("android.webkit.WebViewCore");
        Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
        if (webViewCoreClass != null && proxyPropertiesClass != null) {
            Method m = webViewCoreClass.getDeclaredMethod("sendStaticMessage", Integer.TYPE,
                    Object.class);
            Constructor c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE,
                    String.class);
            m.setAccessible(true);
            c.setAccessible(true);
            Object properties = c.newInstance(host, port, null);
            m.invoke(null, PROXY_CHANGED, properties);
            return true;
        }
        return false;

    }

    @SuppressLint("PrivateApi")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean setKitKatProxy(Context context, String host, int port) {
        Context appContext = context.getApplicationContext();
        try {
            Class applictionCls = appContext.getClass();
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        /*********** optional, may be need in future *************/
                        final String CLASS_NAME = "android.net.ProxyProperties";
                        Class cls = Class.forName(CLASS_NAME);
                        Constructor constructor = cls.getConstructor(String.class, Integer.TYPE, String.class);
                        constructor.setAccessible(true);
                        Object proxyProperties = constructor.newInstance(host, port, null);
                        intent.putExtra("proxy", (Parcelable) proxyProperties);
                        /*********** optional, may be need in future *************/


                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (NoSuchFieldException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (IllegalArgumentException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (NoSuchMethodException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (InvocationTargetException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (InstantiationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        }
        return false;
    }


    private static boolean setProxyMM(Context ctx, String host, int port) {
        Log.w(TAG, "try to setProxyKKPlus");
        Context appContext = ctx.getApplicationContext();
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");

        try {
            Class applictionCls = appContext.getClass();
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
             Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        Bundle extras = new Bundle();
                        List<String> exclusionsList = new ArrayList<>(1);
                        ProxyInfo proxyInfo = ProxyInfo.buildDirectProxy(host, port, exclusionsList);
                        extras.putParcelable("android.intent.extra.PROXY_INFO", proxyInfo);
                        intent.putExtras(extras);

                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "setProxyKKPlus - exception : {}", e);
            return false;
        }
        return true;
    }


    private static void setSystemProperties(String host, int port) {

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");

    }
}
