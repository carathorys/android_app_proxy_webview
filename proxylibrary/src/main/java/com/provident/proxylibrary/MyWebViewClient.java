package com.provident.proxylibrary;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    OnUrlChangeListener mListener;

    public MyWebViewClient(OnUrlChangeListener ctx) {
        if(ctx instanceof OnUrlChangeListener) {
            this.mListener = (OnUrlChangeListener)ctx;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if(mListener != null) {
            mListener.UrlChanged(request.getUrl());
        }
        return super.shouldOverrideUrlLoading(view, request);
    }



    public interface OnUrlChangeListener {
        void UrlChanged(Uri url);
    }
}

