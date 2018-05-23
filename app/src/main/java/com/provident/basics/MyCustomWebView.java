package com.provident.basics;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.provident.basics.proxy.ProxyServiceRunningNotification;
import com.provident.basics.proxy.server.IProxyCallback;
import com.provident.basics.proxy.server.IProxyPortListener;
import com.provident.basics.proxy.server.IRequestUpdateNotification;
import com.provident.basics.proxy.server.ProxyService_;
import com.provident.basics.webview.MyWebViewClient;
import com.provident.basics.webview.ProxySettings;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyCustomWebView.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyCustomWebView#newInstance} factory method to
 * create an instance of this fragment.
 */
@EFragment
public class MyCustomWebView extends Fragment implements MyWebViewClient.OnUrlChangeListener {
    private static final String TAG = "MyCustomWebView";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @ViewById
    WebView webView;

    @ViewById
    FloatingActionButton floatingActionButton;

    @SystemService
    InputMethodManager mInputMethodManager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private boolean mBound;
    private ServiceConnection mProxyConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName component) {
            try {
                mBound = false;
                ProxySettings.resetProxy(getContext().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder) {
            Log.d(TAG, "Bind complete, proxy service acquired");
            IProxyCallback callbackService = IProxyCallback.Stub.asInterface(binder);
            if (callbackService != null) {
                try {
                    callbackService.getProxyPort(new IProxyPortListener.Stub() {
                        @Override
                        public void setProxyPort(final int port) throws RemoteException {
                            if (port != -1) {
                                Log.d(TAG, "Local proxy is bound on " + port);
                                ProxySettings.setProxy(getContext().getApplicationContext(), "localhost", port);
                            } else {
                                Log.e(TAG, "Received invalid port from Local Proxy,"
                                        + " PAC will not be operational");
                            }
                        }
                    });
                    callbackService.updateNotification(new IRequestUpdateNotification.Stub() {
                        @Override
                        public void requestNotificationUpdate(int requestCount, long in, long out) throws RemoteException {
                            ProxyServiceRunningNotification.notify(getContext(), requestCount, in, out);
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mBound = true;
        }
    };


    public MyCustomWebView() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyCustomWebView.
     */
    // TODO: Rename and change types and number of parameters
    public static MyCustomWebView newInstance(String param1, String param2) {
        MyCustomWebView fragment = new MyCustomWebView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @AfterViews
    @SuppressLint("SetJavaScriptEnabled")
    void initializeWebView() {
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheEnabled(false);

        loadUrl("https://carathorys.github.io/image.jpg");
    }

    /**
     * Triggers the webView to load the given URL
     *
     * @param url
     */
    public void loadUrl(String url) {
        webView.loadUrl(url);
        mInputMethodManager.hideSoftInputFromWindow(webView.getWindowToken(), 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Intent intent = new Intent(getContext(), ProxyService_.class);
        getContext().bindService(intent, mProxyConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBound) {
            getContext().unbindService(mProxyConnection);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_custom_web_view, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void UrlChanged(Uri url) {
        if (mListener != null) {
            mListener.onFragmentInteraction(url);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
