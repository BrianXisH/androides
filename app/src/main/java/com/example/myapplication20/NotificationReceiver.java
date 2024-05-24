package com.example.myapplication20;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String paymentName = intent.getStringExtra("paymentName");

        if (paymentName != null) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NOTIFICATION_CHANNEL_ID")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Recordatorio de Pago")
                    .setContentText("Hoy es el día para pagar " + paymentName)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            if (notificationManager != null) {
                notificationManager.notify(1, builder.build());
            }
        }
    }
}
