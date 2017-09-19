/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.solutions.namnam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pusher.android.PusherAndroid;
import com.pusher.android.notifications.ManifestValidator;
import com.pusher.android.notifications.PushNotificationRegistration;
import com.pusher.android.notifications.gcm.GCMPushNotificationReceivedListener;
import com.pusher.android.notifications.tokens.PushNotificationRegistrationListener;
import com.solutions.namnam.helpers.JsStrings;

import org.apache.cordova.CordovaActivity;

public class MainActivity extends CordovaActivity implements PushNotificationRegistrationListener, GCMPushNotificationReceivedListener, Runnable {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // debug tag
    private static final String TAG = "namnamtag";

    // within this time one can exit by clicking back button
    private static final int DOUBLE_BACK_PRESS_TIME = 1500;

    private boolean doubleBackToExitPressedOnce = false;
    private PushNotificationRegistration nativePusher;
    private JsStrings jsStrings;
    private String deviceToken;

    private Intent nextIntent;

    private Handler h = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background

        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        nextIntent = intent;
        deviceToken = getDeviceToken();

        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        // connect and subscribe to Pusher interest
        listenToPusher();

        // setup JsString to interact with the app
        jsStrings = new JsStrings(appView);

        // start listening for the app url to be loaded
        h.postDelayed(this, 500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        nextIntent = intent;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show();

        h.postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, DOUBLE_BACK_PRESS_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        injectDataToDocument();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove intent so that it won't be handled twice
        nextIntent = null;
    }

    private void injectDataToDocument() {
        if (nextIntent == null) return;

        Bundle extras = nextIntent.getExtras();
        Uri uri = nextIntent.getData();

        // pass device token via deviceready event
        jsStrings.deviceToken(deviceToken, true);

        // check and send deep linking data
        jsStrings.deepLinking(uri, true);

        // check and send notification data
        jsStrings.notification(extras, true);
    }

    private void listenToPusher() {
        PusherAndroid pusher = new PusherAndroid(getString(R.string.pusher_api_key));
        nativePusher = pusher.nativePusher();
        try {
            nativePusher.registerFCM(this);
        } catch (ManifestValidator.InvalidManifestException e) {
            Log.e(TAG, "Error trying to register within GCM");
            Log.e(TAG, e.getStackTrace().toString());
        }
    }

//    private boolean playServicesAvailable()
//    {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
//                        .show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }

    @Override
    public void onSuccessfulRegistration() {

        // show device id if needed
//        Toast.makeText(this, deviceToken, Toast.LENGTH_LONG).show();
        Log.i(TAG, "device token: " + deviceToken);

        nativePusher.subscribe(deviceToken);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        // do something magical 🔮
        String message = data.getBundle("notification").getString("body");
        String extraData = data.getString("data");
        Log.d(TAG, "Received push notification from: " + from);

        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Data: " + extraData);
    }

    @Override
    public void onFailedRegistration(int statusCode, String response) {
        Log.e(TAG, "A real sad day. Registration failed with code " + statusCode +
                        " " + response);
    }

    private String getDeviceToken() {
        return Settings.Secure.getString(
                getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    @Override
    public void run() {
        injectDataToDocument();
    }
}
