package com.hstudy.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String title=intent.getStringExtra("title"); if(title==null) title="Study time!";
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) manager.createNotificationChannel(new NotificationChannel("hstudy_reminders","Study reminders",NotificationManager.IMPORTANCE_DEFAULT));
        android.app.Notification.Builder builder=Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(context,"hstudy_reminders"):new android.app.Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("H STUDY").setContentText(title).setAutoCancel(true);
        manager.notify((int)System.currentTimeMillis(),builder.build());
    }
}
