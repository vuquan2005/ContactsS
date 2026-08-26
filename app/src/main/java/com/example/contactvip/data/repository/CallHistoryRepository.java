package com.example.contactvip.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.contactvip.data.dao.CallHistoryDao;
import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.CallHistory;

import java.util.List;

public class CallHistoryRepository {
    private final CallHistoryDao callHistoryDao;
    private final LiveData<List<CallHistory>> allCallHistory;

    public CallHistoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        callHistoryDao = db.callHistoryDao();
        allCallHistory = callHistoryDao.getAllCallHistory();
    }

    public LiveData<List<CallHistory>> getAllCallHistory() {
        return allCallHistory;
    }

    public void insert(CallHistory callHistory) {
        AppDatabase.databaseWriteExecutor.execute(() -> callHistoryDao.insert(callHistory));
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(callHistoryDao::deleteAll);
    }

    public void delete(CallHistory callHistory) {
        AppDatabase.databaseWriteExecutor.execute(() -> callHistoryDao.delete(callHistory));
    }
}
