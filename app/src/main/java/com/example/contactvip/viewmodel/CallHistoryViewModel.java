package com.example.contactvip.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.data.repository.CallHistoryRepository;

import java.util.List;

public class CallHistoryViewModel extends AndroidViewModel {
    private final CallHistoryRepository repository;
    private final LiveData<List<CallHistory>> allCallHistory;

    public CallHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CallHistoryRepository(application);
        allCallHistory = repository.getAllCallHistory();
    }

    public LiveData<List<CallHistory>> getAllCallHistory() {
        return allCallHistory;
    }

    public void insert(CallHistory callHistory) {
        repository.insert(callHistory);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void delete(CallHistory callHistory) {
        repository.delete(callHistory);
    }
}
