package com.provident.basics.proxy.server;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;

public class MyAdminReceiver extends DeviceAdminReceiver {

    static final String TAG = MyAdminReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: context: " + context);
        context.startService(ProxyService_.intent(context).get());
    }

    @Override
    public DevicePolicyManager getManager(Context context) {
        return super.getManager(context);
    }

    @Override
    public ComponentName getWho(Context context) {
        return super.getWho(context);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent, UserHandle user) {
        super.onPasswordChanged(context, intent, user);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle user) {
        super.onPasswordFailed(context, intent, user);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent, UserHandle user) {
        super.onPasswordSucceeded(context, intent, user);
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent, UserHandle user) {
        super.onPasswordExpiring(context, intent, user);
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
        super.onLockTaskModeEntering(context, intent, pkg);
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
        super.onLockTaskModeExiting(context, intent);
    }

    @Override
    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri, String alias) {
        return super.onChoosePrivateKeyAlias(context, intent, uid, uri, alias);
    }

    @Override
    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
        super.onSystemUpdatePending(context, intent, receivedTime);
    }

    @Override
    public void onBugreportSharingDeclined(Context context, Intent intent) {
        super.onBugreportSharingDeclined(context, intent);
    }

    @Override
    public void onBugreportShared(Context context, Intent intent, String bugreportHash) {
        super.onBugreportShared(context, intent, bugreportHash);
    }

    @Override
    public void onBugreportFailed(Context context, Intent intent, int failureCode) {
        super.onBugreportFailed(context, intent, failureCode);
    }

    @Override
    public void onSecurityLogsAvailable(Context context, Intent intent) {
        super.onSecurityLogsAvailable(context, intent);
    }

    @Override
    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken, int networkLogsCount) {
        super.onNetworkLogsAvailable(context, intent, batchToken, networkLogsCount);
    }

    @Override
    public void onUserAdded(Context context, Intent intent, UserHandle newUser) {
        super.onUserAdded(context, intent, newUser);
    }

    @Override
    public void onUserRemoved(Context context, Intent intent, UserHandle removedUser) {
        super.onUserRemoved(context, intent, removedUser);
    }
}
