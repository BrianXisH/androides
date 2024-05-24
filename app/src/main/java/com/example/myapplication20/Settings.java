package com.example.myapplication20;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.appcompat.app.AppCompatDelegate;


public class Settings extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private Switch switchNotifications, switchModoOscuro, switchSonido, switchConfirmar;
    private Button buttonMoneda, buttonIdioma;
    private SeekBar seekBarSonido;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Inicializar los elementos de la UI
        switchNotifications = findViewById(R.id.switchNotifications);
        switchModoOscuro = findViewById(R.id.SwitchModo);
        switchSonido = findViewById(R.id.SwitchSonido);
        switchConfirmar = findViewById(R.id.SwitchConfirmar);
        buttonMoneda = findViewById(R.id.buttonMoneda);
        buttonIdioma = findViewById(R.id.buttonIdioma);
        seekBarSonido = findViewById(R.id.seekBarSonido);

        // Cargar configuraciones guardadas
        loadSettings();

        // Configurar listeners
        configureListeners();

    }

    private void configureListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notifications", isChecked);
            editor.apply();
            Toast.makeText(Settings.this, "Notificaciones: " + (isChecked ? "Activadas" : "Desactivadas"), Toast.LENGTH_SHORT).show();
            if (isChecked) {
                showNotification("Título de la Notificación", "Contenido de la Notificación");
            }
        });

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("modoOscuro", isChecked);
            editor.apply();
            Toast.makeText(Settings.this, "Modo Oscuro: " + (isChecked ? "Activado" : "Desactivado"), Toast.LENGTH_SHORT).show();

            // Aplicar el tema oscuro si está activado
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                // Aplicar el tema claro si está desactivado
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Recrea la actividad para aplicar los cambios de tema
            recreate();
        });

        switchSonido.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Si el switch de sonido está activado, obtener el volumen del SeekBar
                int progress = seekBarSonido.getProgress();
                float volumeLevel = (float) progress / seekBarSonido.getMax();
                // Reproducir sonido con el volumen del SeekBar
                playSound(volumeLevel);
            } else {
                // Si el switch de sonido está desactivado, detener la reproducción de sonido
                stopSound();
            }
        });

        switchConfirmar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("confirmarTransaccion", isChecked);
            editor.apply();
            Toast.makeText(Settings.this, "Confirmar Transacción: " + (isChecked ? "Activado" : "Desactivado"), Toast.LENGTH_SHORT).show();
        });

        buttonMoneda.setOnClickListener(v -> {
            // Mostrar un diálogo para seleccionar la moneda
            showCurrencyDialog();
        });

        buttonIdioma.setOnClickListener(v -> {
            // Mostrar un diálogo para seleccionar el idioma
            showLanguageDialog();
        });

        seekBarSonido.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Verificar si el switch de sonido está activado
                if (switchSonido.isChecked()) {
                    // Obtener el progreso actual del SeekBar
                    float volumeLevel = (float) progress / seekBar.getMax(); // Escalar el progreso al rango de 0.0 a 1.0
                    playSound(volumeLevel); // Llamar al método para reproducir el sonido con el volumen calculado
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No se necesita implementación aquí
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No se necesita implementación aquí
            }
        });
    }
    private void playSound(float volumeLevel) {
        // Reproducir el sonido con el volumen ajustado
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.campana);
        mediaPlayer.setVolume(volumeLevel, volumeLevel);
        mediaPlayer.start();
    }

    private void stopSound() {
        // Detener la reproducción de sonido si está en curso
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.campana);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void showCurrencyDialog() {
        final String[] currencies = {"$", "€", "£"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccione Moneda")
                .setItems(currencies, (dialog, which) -> {
                    String selectedCurrency = currencies[which];
                    buttonMoneda.setText(selectedCurrency);
                    editor.putString("moneda", selectedCurrency);
                    editor.apply();
                    Toast.makeText(Settings.this, "Moneda seleccionada: " + selectedCurrency, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLanguageDialog() {
        final String[] languages = {"Español", "Inglés"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccione Idioma")
                .setItems(languages, (dialog, which) -> {
                    String selectedLanguage = languages[which];
                    buttonIdioma.setText(selectedLanguage);
                    editor.putString("idioma", selectedLanguage);
                    editor.apply();
                    Toast.makeText(Settings.this, "Idioma seleccionado: " + selectedLanguage, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void loadSettings() {
        switchNotifications.setChecked(sharedPreferences.getBoolean("notifications", false));
        switchModoOscuro.setChecked(sharedPreferences.getBoolean("modoOscuro", false));
        switchSonido.setChecked(sharedPreferences.getBoolean("sonido", false));
        switchConfirmar.setChecked(sharedPreferences.getBoolean("confirmarTransaccion", false));
        buttonMoneda.setText(sharedPreferences.getString("moneda", "$"));
        buttonIdioma.setText(sharedPreferences.getString("idioma", "Español"));
        seekBarSonido.setProgress(sharedPreferences.getInt("volumenSonido", 50));
    }

    private void showNotification(String title, String message) {
        // Verificar si el switch de sonido está activado
        if (switchSonido.isChecked()) {
            // Obtener el volumen del SeekBar
            int progress = seekBarSonido.getProgress();
            float volumeLevel = (float) progress / seekBarSonido.getMax();
            // Reproducir sonido de notificación con el volumen del SeekBar
            playNotificationSound(volumeLevel);
        }
        // Verifica si se tiene permiso para mostrar notificaciones
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Se tiene permiso, construye y muestra la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        } else {
            // No se tiene permiso, solicita permiso al usuario
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    private void playNotificationSound(float volumeLevel) {
        // Reproducir el sonido de la notificación con el volumen ajustado
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.campana);
        mediaPlayer.setVolume(volumeLevel, volumeLevel);
        mediaPlayer.start();
    }
}
