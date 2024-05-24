package com.example.myapplication20;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PaymentCheckWorker extends Worker {

    private static final String CHANNEL_ID = "payment_channel";
    private static final int NOTIFICATION_ID = 123;

    private PaymentDatabaseHelper databaseHelper;

    public PaymentCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseHelper = new PaymentDatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        checkForPaymentsDueToday();
        return Result.success();
    }

    private void checkForPaymentsDueToday() {
        List<Payment> payments = databaseHelper.getAllPayments();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String today = sdf.format(Calendar.getInstance().getTime());

        for (Payment payment : payments) {
            String paymentDueDate = sdf.format(payment.getDueDate());
            if (today.equals(paymentDueDate)) {
                sendReminder(getApplicationContext(), payment.getName());
            }
        }
    }

    private void sendReminder(Context context, String paymentName) {
        // Acceder a SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", false);
        boolean sonidoEnabled = sharedPreferences.getBoolean("sonido", false);
        int volumenSonido = sharedPreferences.getInt("volumenSonido", 50);
        float volumeLevel = (float) volumenSonido / 100; // Convertir a un rango de 0.0 a 1.0

        // Verificar si las notificaciones están habilitadas
        if (!notificationsEnabled) {
            return; // No enviar notificación si están deshabilitadas
        }

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Recordatorio de Pago")
                .setContentText("Debe pagar: " + paymentName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Reproducir sonido si está habilitado
        if (sonidoEnabled) {
            playNotificationSound(context, volumeLevel);
        }
    }

    private void playNotificationSound(Context context, float volumeLevel) {
        // Reproducir el sonido de la notificación con el volumen ajustado
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.campana);
        mediaPlayer.setVolume(volumeLevel, volumeLevel);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> mp.release()); // Liberar el recurso una vez que el sonido haya terminado de reproducirse
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Payment Channel";
            String description = "Channel for payment reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
