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

@Database(entities = {Contact.class, CallHistory.class, ContactPhone.class, ContactGroup.class, ContactGroupCrossRef.class}, version = 4, exportSchema = false)
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
                            .fallbackToDestructiveMigration() // Xóa sạch bảng cũ, tạo bảng mới theo schema gọn nhất
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                ContactDao dao = INSTANCE.contactDao();
                ContactPhoneDao pDao = INSTANCE.contactPhoneDao();
                
                Contact c1 = new Contact();
                c1.name = "Nguyễn Văn An";
                c1.isFavorite = true;
                c1.createdAt = System.currentTimeMillis();
                long id1 = dao.insert(c1);
                pDao.insert(new ContactPhone(id1, "0987654321", "Mobile", true));

                Contact c2 = new Contact();
                c2.name = "Trần Minh Đức";
                c2.isFavorite = true;
                c2.createdAt = System.currentTimeMillis();
                long id2 = dao.insert(c2);
                pDao.insert(new ContactPhone(id2, "0912345678", "Mobile", true));
            });
        }
    };
}
