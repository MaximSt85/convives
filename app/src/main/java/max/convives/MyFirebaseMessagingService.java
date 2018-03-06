package max.convives;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.max.chat_firebase.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Max on 14.12.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "mDebugger";
    DatabaseHelperAll mDatabaseHelperAll;

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final String messageBody = remoteMessage.getNotification().getBody();
        final String userId1 = remoteMessage.getData().get("USER_ID1");
        final String userId2 = remoteMessage.getData().get("USER_ID2");
        final String username = remoteMessage.getData().get("USER_NAME2");
        final int userage = Integer.valueOf(remoteMessage.getData().get("USER_AGE2"));
        final boolean usersex = Boolean.valueOf(remoteMessage.getData().get("USER_SEX2"));

        int isMute;
        mDatabaseHelperAll = new DatabaseHelperAll(this);
        String [] args = {userId1};
        Cursor cursor = mDatabaseHelperAll.queryMute(DatabaseHelperAll.KEY_USER_ID + "=?", args);
        if (cursor.moveToFirst()) {
            isMute = cursor.getInt(cursor.getColumnIndex(DatabaseHelperAll.KEY_IS_MUTED));
            cursor.close();
        }
        else {
            isMute = 0;
        }
        boolean preferenceMute;
        if (isMute == 1) {
            preferenceMute = true;
        }
        else {preferenceMute = false;}

        if (ChatActivity.isChatActivityActive) {
            if (!ChatActivity.userId2.equals(userId2)) {
                sendNotification(userId1, userId2, username, userage, usersex, messageBody, preferenceMute);
            }
        }
        else {sendNotification(userId1, userId2, username, userage, usersex, messageBody, preferenceMute);}
    }

    private void sendNotification(String userId1, String userId2, String username, int  userage, boolean usersex, String messageBody, boolean isMute) {

        Intent intent = new Intent(this, ChatActivity.class);
        Bundle extras = new Bundle();
        extras.putString("USER_ID1",userId1);
        extras.putString("USER_ID2",userId2);
        extras.putString("USER_NAME2",username);
        extras.putInt("USER_AGE2", userage);
        extras.putBoolean("USER_SEX2", usersex);
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri;
        long [] vibration;
        if (!isMute) {
            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //defaultSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            vibration = new long[] { 300, 300, 300, 300, 300 };
        }
        else {
            defaultSoundUri = null;
            vibration = new long[] { 0 };
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_convives)
                .setContentTitle("New message from " + username)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(vibration)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        /*else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_convives)
                .setContentTitle("New message from " + username)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(vibration)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX);

            notificationManager.notify(0, notificationBuilder.build());
        }*/
    }
}





//final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        /*mDatabase.child("mute").child(userId1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Boolean muteAll = Boolean.valueOf(String.valueOf(dataSnapshot.getValue()));
                mDatabase.child("muteNotifications").child(userId1).child(userId2).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean muteUser = Boolean.valueOf(String.valueOf(dataSnapshot.getValue()));
                        boolean isMute = false;
                        if (muteAll) {isMute = true;}
                        else {
                            if (muteUser) {isMute = true;}
                        }
                        if (ChatActivity.isChatActivityActive) {
                            if (!ChatActivity.userId2.equals(userId2)) {
                                sendNotification(userId1, userId2, username, userage, usersex, messageBody, isMute);
                            }
                        }
                        else {sendNotification(userId1, userId2, username, userage, usersex, messageBody, isMute);}
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
