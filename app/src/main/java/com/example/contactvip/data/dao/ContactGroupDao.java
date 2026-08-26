package com.example.contactvip.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactGroupCrossRef;

import java.util.List;

@Dao
public interface ContactGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroup(ContactGroup group);

    @Update
    void updateGroup(ContactGroup group);

    @Delete
    void deleteGroup(ContactGroup group);

    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    LiveData<List<ContactGroup>> getAllGroups();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContactGroupCrossRef(ContactGroupCrossRef crossRef);

    @Delete
    void deleteContactGroupCrossRef(ContactGroupCrossRef crossRef);

    @Query("DELETE FROM contact_group_cross_ref WHERE contactId = :contactId")
    void deleteCrossRefsByContactId(long contactId);

    @Query("SELECT * FROM contact_groups " +
           "INNER JOIN contact_group_cross_ref ON contact_groups.id = contact_group_cross_ref.groupId " +
           "WHERE contact_group_cross_ref.contactId = :contactId")
    LiveData<List<ContactGroup>> getGroupsForContact(long contactId);

    @Query("SELECT * FROM contact_groups " +
           "INNER JOIN contact_group_cross_ref ON contact_groups.id = contact_group_cross_ref.groupId " +
           "WHERE contact_group_cross_ref.contactId = :contactId")
    List<ContactGroup> getGroupsForContactSync(long contactId);
}
