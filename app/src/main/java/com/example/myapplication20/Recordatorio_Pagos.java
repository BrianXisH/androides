package com.example.myapplication20;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Recordatorio_Pagos extends AppCompatActivity {

    private EditText editTextPaymentName;
    private DatePicker datePickerDueDate;
    private Button buttonAddPayment;
    private TableLayout paymentsTable;

    private PaymentDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordatorio_pagos);
        PeriodicWorkRequest paymentCheckRequest = new PeriodicWorkRequest.Builder(PaymentCheckWorker.class, 1, TimeUnit.DAYS)
                .build();
        WorkManager.getInstance(this).enqueue(paymentCheckRequest);

        // Inicializar la base de datos SQLite
        databaseHelper = new PaymentDatabaseHelper(this);

        // Inicializar vistas
        editTextPaymentName = findViewById(R.id.editTextPaymentName);
        datePickerDueDate = findViewById(R.id.datePickerDueDate);
        buttonAddPayment = findViewById(R.id.buttonAddPayment);
        paymentsTable = findViewById(R.id.payments_table);

        // Configurar el listener del botón "Agregar Pago"
        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el nombre del pago y la fecha de vencimiento
                String paymentName = editTextPaymentName.getText().toString().trim();
                int day = datePickerDueDate.getDayOfMonth();
                int month = datePickerDueDate.getMonth();
                int year = datePickerDueDate.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                long dueDateMillis = calendar.getTimeInMillis();

                // Verificar si se ingresó un nombre de pago
                if (paymentName.isEmpty()) {
                    Toast.makeText(Recordatorio_Pagos.this, "Ingrese un nombre de pago", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Insertar el pago en la base de datos
                if (databaseHelper.insertPayment(paymentName, dueDateMillis)) {
                    // Mostrar un mensaje de éxito
                    Toast.makeText(Recordatorio_Pagos.this, "Pago agregado correctamente", Toast.LENGTH_SHORT).show();
                    // Limpiar los campos después de la inserción exitosa
                    editTextPaymentName.setText("");
                    // Reiniciar la fecha al día de hoy
                    datePickerDueDate.updateDate(Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    // Actualizar la tabla de pagos
                    updatePaymentsTable();
                } else {
                    // Mostrar un mensaje de error si la inserción falla
                    Toast.makeText(Recordatorio_Pagos.this, "Error al agregar el pago", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Cargar los pagos existentes al iniciar la actividad
        updatePaymentsTable();
    }

    private void updatePaymentsTable() {
        // Limpiar la tabla actual
        paymentsTable.removeAllViews();

        // Obtener todos los pagos de la base de datos
        List<Payment> payments = databaseHelper.getAllPayments();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Agregar filas para cada pago
        for (final Payment payment : payments) {
            TableRow row = new TableRow(this);
            TextView paymentName = new TextView(this);
            paymentName.setText(payment.getName());
            paymentName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            paymentName.setPadding(16, 16, 16, 16); // Añadir padding
            paymentName.setTextSize(16); // Tamaño de letra
            paymentName.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
            row.addView(paymentName);

            TextView paymentDate = new TextView(this);
            String formattedDate = sdf.format(payment.getDueDate());
            paymentDate.setText(formattedDate);
            paymentDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            paymentDate.setPadding(16, 16, 16, 16); // Añadir padding
            paymentDate.setTextSize(16); // Tamaño de letra
            paymentDate.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
            row.addView(paymentDate);

            // Botón de eliminar
            Button deleteButton = new Button(this);
            deleteButton.setText("Eliminar");
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Eliminar el pago de la base de datos
                    databaseHelper.deletePayment(payment.getId());
                    // Actualizar la tabla de pagos
                    updatePaymentsTable();
                    // Mostrar mensaje de éxito
                    Toast.makeText(Recordatorio_Pagos.this, "Pago eliminado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
            row.addView(deleteButton);

            // Agregar la fila a la tabla
            paymentsTable.addView(row);
        }
    }

    @Override
    protected void onDestroy() {
        // Cierra la base de datos cuando la actividad se destruye
        databaseHelper.close();
        super.onDestroy();
    }
}
