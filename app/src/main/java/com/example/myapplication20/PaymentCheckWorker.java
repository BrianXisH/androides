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
        createNotificationChannel(context);

        // Reproducir el sonido de la notificación si está activado
        SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        boolean sonidoActivado = sharedPreferences.getBoolean("sonido", false);
        if (sonidoActivado) {
            playNotificationSound(context);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Recordatorio de Pago")
                .setContentText("Debe pagar: " + paymentName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void playNotificationSound(Context context) {
        // Reproducir el sonido de la notificación
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        int volumenGuardado = sharedPreferences.getInt("volumenSonido", 50);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int volumeLevel = (int) (maxVolume * (volumenGuardado / 100f)); // Escalar el valor del SeekBar al rango del volumen del sistema
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeLevel, 0);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.campana);
        mediaPlayer.start();
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
