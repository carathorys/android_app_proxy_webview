package com.provident.proxylibrary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
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

    public static void disableProxy(Context ctx) throws Exception {
        setSystemProperties(null, -1);
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
            ret = setProxyMM(ctx, host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * //    @SuppressLint("all")
     * //    @TargetApi(Build.VERSION_CODES.KITKAT)
     * //    private static boolean setKitKatProxy(Context context, String host, int port) {
     * //        Context appContext = context.getApplicationContext();
     * //        try {
     * //            Class applictionCls = appContext.getClass();
     * //            Field loadedApkField = applictionCls.getField("mLoadedApk");
     * //            loadedApkField.setAccessible(true);
     * //            Object loadedApk = loadedApkField.get(appContext);
     * //            Class loadedApkCls = Class.forName("android.app.LoadedApk");
     * //            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
     * //            receiversField.setAccessible(true);
     * //            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
     * //            for (Object receiverMap : receivers.values()) {
     * //                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
     * //                    Class clazz = rec.getClass();
     * //                    if (clazz.getName().contains("ProxyChangeListener")) {
     * //                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
     * //                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
     * //
     * //                        final String CLASS_NAME = "android.net.ProxyProperties";
     * //                        Class cls = Class.forName(CLASS_NAME);
     * //                        Constructor constructor = cls.getConstructor(String.class, Integer.TYPE, String.class);
     * //                        constructor.setAccessible(true);
     * //                        Object proxyProperties = constructor.newInstance(host, port, null);
     * //                        intent.putExtra("proxy", (Parcelable) proxyProperties);
     * //
     * //
     * //                        onReceiveMethod.invoke(rec, appContext, intent);
     * //                    }
     * //                }
     * //            }
     * //            return true;
     * //        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
     * //            StringWriter sw = new StringWriter();
     * //            e.printStackTrace(new PrintWriter(sw));
     * //            String exceptionAsString = sw.toString();
     * //            Log.v(TAG, e.getMessage());
     * //            Log.v(TAG, exceptionAsString);
     * //        }
     * //        return false;
     * //    }
     **/


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("all")
    private static boolean setProxyMM(Context ctx, String host, int port) {
        Log.w(TAG, "try to setProxyMM");
        Context appContext = ctx.getApplicationContext();

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
        System.setProperty("http.proxyPort", port > 0 ? port + "" : null);

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port > 0 ? port + "" : null);

    }
}
