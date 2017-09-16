package com.solutions.namnam;

import android.net.Uri;

/**
 * Created by max on 16.09.17.
 */

public class JsStrings {

    static String deviceToken(String deviceToken) {
        return String.format("javascript: document.addEventListener('deviceready', function() { setDeviceToken('%s'); });", deviceToken);
    }

    static String deepLinking(String action, Uri data) {

    }
}
