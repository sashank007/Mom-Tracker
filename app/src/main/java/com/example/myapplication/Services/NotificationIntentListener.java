package com.example.myapplication.Services;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationIntentListener extends NotificationListenerService {


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("NOTIFICATION_INTENT_LISTENER" , "binded notification listener");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        // Check the regex of the sbn
        Notification sbnNotification = sbn.getNotification();
        String text = null;
        String title = null;
        String packageName = null;
        String dollarAmount = null;
        String momKeepsTrackPackage = "com.example.myapplication";

        if(sbnNotification.extras.containsKey("android.text")&&sbnNotification.extras.containsKey("android.title")) {
            text = sbn.getNotification().extras.getString("android.text");
            title = sbn.getNotification().extras.getString("android.title");
            packageName = sbn.getPackageName();

            if(text!=null) {
                String[] words = text.split(" ");
                for (String word : words) {
                    if (word.startsWith("$")) {
                        word = word.replaceAll("[^\\d.]", "");
                        dollarAmount = word;
                        break;
                    }
                }
            }

            if(dollarAmount!=null&& !packageName.equals(momKeepsTrackPackage)){
                Intent intent = new  Intent(momKeepsTrackPackage);
                intent.putExtra("Title", title);
                intent.putExtra("Text", text);
                intent.putExtra("PackageName", packageName);
                intent.putExtra("DollarAmount" , dollarAmount);
                Log.d("NotificationIntentListener","GETTING NOTIFICATION " + dollarAmount + packageName);
                sendBroadcast(intent);
            }
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // Implement what you want here
    }

    private boolean matchedDollarNotification(StatusBarNotification sbn){
        String notification = sbn.getNotification().toString();
        String packageName = sbn.getPackageName();
        return false;
    }


}
