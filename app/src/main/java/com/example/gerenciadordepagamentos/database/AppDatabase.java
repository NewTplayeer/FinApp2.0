package com.example.gerenciadordepagamentos.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Conta.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instancia;

    public abstract ContaDAO contaDAO();

    public static synchronized AppDatabase getInstancia(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "finday_database"
                    ).allowMainThreadQueries() // Permite rodar na thread principal para facilitar o início
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instancia;
    }
}