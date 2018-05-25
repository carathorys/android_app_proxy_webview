package com.provident.basics;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.provident.basics.proxy.server.MyAdminReceiver;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @ViewById
    TextView mDetailsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @UiThread
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SystemService
    DevicePolicyManager mDevPolMan;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ComponentName receiver = ComponentName.createRelative("com.provident.basics", MyAdminReceiver.class.getName());
            boolean isAllowed = mDevPolMan.isAdminActive(receiver);
            mDetailsText.setText(getString(R.string.detailsTxt, isAllowed ? getText(R.string.allowed) : getText(R.string.denied)));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
