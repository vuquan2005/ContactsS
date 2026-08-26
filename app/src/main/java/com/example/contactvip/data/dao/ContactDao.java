package com.example.contactvip.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert
    long insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT contacts.*, (SELECT phoneNumber FROM contact_phones WHERE contactId = contacts.id ORDER BY isPrimary DESC, id ASC LIMIT 1) as primaryPhone FROM contacts ORDER BY name ASC")
    LiveData<List<ContactDisplay>> getAllContacts();

    @Query("SELECT contacts.*, (SELECT phoneNumber FROM contact_phones WHERE contactId = contacts.id ORDER BY isPrimary DESC, id ASC LIMIT 1) as primaryPhone FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    LiveData<List<ContactDisplay>> getFavoriteContacts();

    @Query("SELECT DISTINCT contacts.*, (SELECT phoneNumber FROM contact_phones WHERE contactId = contacts.id ORDER BY isPrimary DESC, id ASC LIMIT 1) as primaryPhone FROM contacts " +
           "LEFT JOIN contact_phones ON contacts.id = contact_phones.contactId " +
           "WHERE name LIKE :query OR contact_phones.phoneNumber LIKE :query")
    LiveData<List<ContactDisplay>> searchContacts(String query);

    @Query("SELECT DISTINCT contacts.*, (SELECT phoneNumber FROM contact_phones WHERE contactId = contacts.id ORDER BY isPrimary DESC, id ASC LIMIT 1) as primaryPhone FROM contacts " +
           "INNER JOIN contact_group_cross_ref ON contacts.id = contact_group_cross_ref.contactId " +
           "WHERE contact_group_cross_ref.groupId = :groupId")
    LiveData<List<ContactDisplay>> getContactsByGroup(long groupId);

    @Query("SELECT * FROM contacts WHERE id = :id")
    LiveData<Contact> getContactById(long id);

    @Query("SELECT contacts.* FROM contacts " +
           "INNER JOIN contact_phones ON contacts.id = contact_phones.contactId " +
           "WHERE contact_phones.phoneNumber = :phoneNumber LIMIT 1")
    Contact getContactByPhoneNumber(String phoneNumber);
}
