package com.example.contactvip.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.contactvip.data.entity.ContactPhone;

import java.util.List;

@Dao
public interface ContactPhoneDao {
    @Insert
    void insert(ContactPhone contactPhone);

    @Update
    void update(ContactPhone contactPhone);

    @Delete
    void delete(ContactPhone contactPhone);

    @Query("SELECT * FROM contact_phones WHERE contactId = :contactId")
    List<ContactPhone> getPhonesByContactId(long contactId);

    @Query("DELETE FROM contact_phones WHERE contactId = :contactId")
    void deletePhonesByContactId(long contactId);
}
