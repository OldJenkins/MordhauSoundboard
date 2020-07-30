package com.norman.mordhausoundboard;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// If you change Anything in the entry classes (ChildDataModel,ParentDataModel) you have to rise the Version number, else you will get an error
@androidx.room.Database(entities = {ChildDataModel.class,ParentDataModel.class}, version = 9)
public abstract class Database extends RoomDatabase {
    public abstract DaoChildData personDao();
    public abstract DaoParentData parentDao();
    private static Database INSTANCE;

    public static Database getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Database.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            Database.class, "database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}