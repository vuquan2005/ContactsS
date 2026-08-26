package com.example.contactvip.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.contactvip.data.dao.CallHistoryDao;
import com.example.contactvip.data.dao.ContactDao;
import com.example.contactvip.data.dao.ContactGroupDao;
import com.example.contactvip.data.dao.ContactPhoneDao;
import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactGroupCrossRef;
import com.example.contactvip.data.entity.ContactPhone;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Contact.class, CallHistory.class, ContactPhone.class, ContactGroup.class, ContactGroupCrossRef.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
    public abstract CallHistoryDao callHistoryDao();
    public abstract ContactPhoneDao contactPhoneDao();
    public abstract ContactGroupDao contactGroupDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "phone_contacts_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
