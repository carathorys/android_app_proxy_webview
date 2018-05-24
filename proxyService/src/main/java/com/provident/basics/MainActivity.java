package com.provident.basics;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@EActivity(R.layout.activity_main)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class MainActivity extends AppCompatActivity implements MyCustomWebView.OnFragmentInteractionListener {

    public static final String TAG = "MainActivity";

    @ViewById
    TextInputLayout textInputLayout;

    @FragmentById(R.id.fragmentX)
    MyCustomWebView fragment;

    @ViewById
    TextInputEditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @UiThread
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @AfterViews
    void initialize() {
        textInputEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    go(textInputEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Click
    void goButton() {
        go(textInputEditText.getText().toString());
    }

    void go(String url) {
        fragment.loadUrl(url);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "Fragment interaction happened: ");
        Log.d(TAG, uri.toString());
        if (textInputEditText != null) {
            textInputEditText.setText(uri.toString());
        }
    }
}
