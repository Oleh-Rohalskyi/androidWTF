package com.solutions.namnam.helpers;

import android.net.Uri;
import android.os.Bundle;

import org.apache.cordova.CordovaWebView;

/**
 * Created by max on 16.09.17.
 */

public class JsStrings {

    private CordovaWebView webView;

    public JsStrings(CordovaWebView webView) {
        this.webView = webView;
    }

    private static final String TAG = "namnamtag";

    public String execJs(String js) {
        return String.format("javascript: %s", js);
    }

    public String onDeviceReady(String js) {
        return execJs(String.format("document.addEventListener('deviceready', function() { %s }, false);", js));
    }

    public void deviceToken(String deviceToken, Boolean useDeviceReady) {
        if (deviceToken != null) {
            String js = String.format(
                    "setDeviceToken('%s');",
//                    "alert('%s');",
                    deviceToken
            );
            webView.loadUrl(useDeviceReady ? onDeviceReady(js) : execJs(js));
        }
    }

    public void deepLinking(Uri uri, Boolean useDeviceReady) {
        if (uri != null) {
            String js = String.format(
                "localStorage.setItem('URL', '%s');",
//                "alert('%s');",
                uri.toString()
            );
            webView.loadUrl(useDeviceReady ? onDeviceReady(js) : execJs(js));
        }
    }

    public void notification(Bundle extras, Boolean useDeviceReady) {
        if (extras != null) {
            String data = extras.getString("data");
            if (data != null) {
                String js = String.format(
                    "setNotification(%s);",
//                    "alert('%s');",
                    data
                );
                webView.loadUrl(useDeviceReady ? onDeviceReady(js) : execJs(js));
            }
        }
    }
}
