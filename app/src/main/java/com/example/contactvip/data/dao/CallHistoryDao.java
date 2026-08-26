package com.example.contactvip.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.contactvip.data.entity.CallHistory;

import java.util.List;

@Dao
public interface CallHistoryDao {
    @Insert
    void insert(CallHistory callHistory);

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    LiveData<List<CallHistory>> getAllCallHistory();

    @Query("DELETE FROM call_history")
    void deleteAll();

    @Delete
    void delete(CallHistory callHistory);
}
