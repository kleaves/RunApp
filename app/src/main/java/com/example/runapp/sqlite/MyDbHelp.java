package com.example.runapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDbHelp extends SQLiteOpenHelper {
    //    跑步信息数据库
    private String map_data = "create table if not exists map_data(" +
            "_id Integer primary key autoincrement," +
            "year text," +
            "month text," +
            "time text," +
            "distance text," +
            "speed text," +
            "kcal text," +
            "route text)";

    public MyDbHelp(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(map_data);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
