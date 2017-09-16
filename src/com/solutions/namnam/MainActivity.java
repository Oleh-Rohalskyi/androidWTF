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

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.pusher.android.PusherAndroid;
import com.pusher.android.notifications.ManifestValidator;
import com.pusher.android.notifications.PushNotificationRegistration;
import com.pusher.android.notifications.gcm.GCMPushNotificationReceivedListener;
import com.pusher.android.notifications.tokens.PushNotificationRegistrationListener;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import android.provider.Settings.Secure;

import org.apache.cordova.*;

public class MainActivity extends CordovaActivity implements PushNotificationRegistrationListener
{
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "yourtag";

    private static final String PROJECT_NUMBER = "420408569314";
    private static final String PUSHER_API_KEY = "b7fb078abcfaa701ca9c";

    private PushNotificationRegistration nativePusher;

    private String deviceToken;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        deviceToken = getDeviceToken();

//        listenToPusherChannel();
        listenToPusher();
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        String jsSetToken = String.format("javascript: document.addEventListener('deviceready', function() { setDeviceToken('%s'); });", deviceToken);

        this.appView.loadUrl(jsSetToken);
    }

    private void listenToPusher() {
        if (playServicesAvailable()) {
            PusherAndroid pusher = new PusherAndroid(PUSHER_API_KEY);
            nativePusher = pusher.nativePusher();
            try {
                nativePusher.registerGCM(this, PROJECT_NUMBER, this);
            } catch (ManifestValidator.InvalidManifestException e) {
                Log.e(TAG, "Error trying to register within GCM");
                Log.e(TAG, e.getStackTrace().toString());
            }
        } else {
            Log.e(TAG, "Error: Play Services are not available");
        }
    }

    private boolean playServicesAvailable()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onSuccessfulRegistration() {
        String token = getDeviceToken();
        Toast.makeText(this, token, Toast.LENGTH_LONG).show();
        nativePusher.subscribe(token);
        nativePusher.setGCMListener(new GCMPushNotificationReceivedListener() {
            @Override
            public void onMessageReceived(String from, Bundle data) {
                // do something magical 🔮
                String message = data.getBundle("notification").getString("body");
                String meta = data.getString("meta");
                Log.d(TAG, "Received push notification from: " + from);

                Log.d(TAG, "Message: " + message);
                Log.d(TAG, "Meta: " + meta);
            }
        });
    }

    @Override
    public void onFailedRegistration(int statusCode, String response) {
        System.out.println(
                "A real sad day. Registration failed with code " + statusCode +
                        " " + response
        );
    }

    private String getDeviceToken() {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            String token = instanceID.getToken(PROJECT_NUMBER,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM Registration Token: " + token);

            return token;
        }catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        return "11111";
    }
}
