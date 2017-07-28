/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.demo.garagecardetection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String NOTIFY_ORDER = "notify_order";

    private Builder notificationBuilder;
    private NotificationManager notificationManager;

    public int generateRandom() {
        Random random = new Random();
        return random.nextInt(9999 - 1000) + 1000;
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody) {
        final int idNotify = generateRandom();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        long notificationTime = System.currentTimeMillis();

        if (notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setTicker(title)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(getResources(),
                                    R.mipmap.ic_launcher))
                    .setPriority(Notification.PRIORITY_HIGH)
                    //.setContentInfo(info)
                    //.setOngoing(true)
                    //.setSound(defaultSoundUri)
                    .setWhen(notificationTime);

            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        } else {
            notificationBuilder.setContentTitle(title)
                    .setContentText(messageBody)
                    .setTicker(title)
                    .setWhen(notificationTime);
        }
        notificationManager.notify(idNotify /* ID of notification */, notificationBuilder.build());
    }


}
