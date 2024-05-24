package com.example.myapplication20;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class PaymentDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "payments.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y columnas
    public static final String TABLE_PAYMENTS = "payments";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DUE_DATE = "due_date";

    // Sentencia SQL para crear la tabla de pagos
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_DUE_DATE + " INTEGER NOT NULL)";

    public PaymentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de pagos
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Manejar la actualización de la base de datos si es necesario
        // Aquí puedes agregar migraciones de datos si la estructura de la base de datos cambia en futuras versiones
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PAYMENTS, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                long dueDate = cursor.getLong(cursor.getColumnIndex(COLUMN_DUE_DATE));
                Payment payment = new Payment(id, name, dueDate);
                payments.add(payment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return payments;
    }

    public boolean insertPayment(String name, long dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DUE_DATE, dueDate);
        long newRowId = db.insert(TABLE_PAYMENTS, null, values);
        return newRowId != -1;
    }
    public void deletePayment(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PAYMENTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }


}
