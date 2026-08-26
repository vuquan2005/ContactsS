package com.example.contactvip.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.contactvip.data.dao.ContactDao;
import com.example.contactvip.data.dao.ContactGroupDao;
import com.example.contactvip.data.dao.ContactPhoneDao;
import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactGroupCrossRef;
import com.example.contactvip.data.entity.ContactPhone;

import java.util.List;

public class ContactRepository {
    private final ContactDao contactDao;
    private final ContactPhoneDao phoneDao;
    private final ContactGroupDao groupDao;
    private final LiveData<List<ContactDisplay>> allContacts;

    public ContactRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        contactDao = db.contactDao();
        phoneDao = db.contactPhoneDao();
        groupDao = db.contactGroupDao();
        allContacts = contactDao.getAllContacts();
    }

    public LiveData<List<ContactDisplay>> getAllContacts() {
        return allContacts;
    }

    public LiveData<List<ContactDisplay>> getFavoriteContacts() {
        return contactDao.getFavoriteContacts();
    }

    public LiveData<Contact> getContactById(long id) {
        return contactDao.getContactById(id);
    }

    public void insert(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> contactDao.insert(contact));
    }

    public void update(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> contactDao.update(contact));
    }

    public void delete(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            phoneDao.deletePhonesByContactId(contact.id);
            groupDao.deleteCrossRefsByContactId(contact.id);
            contactDao.delete(contact);
        });
    }

    public LiveData<List<ContactDisplay>> searchContacts(String query) {
        return contactDao.searchContacts("%" + query + "%");
    }

    public LiveData<List<ContactDisplay>> getContactsByGroup(long groupId) {
        return contactDao.getContactsByGroup(groupId);
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        return contactDao.getContactByPhoneNumber(phoneNumber);
    }

    // Phone Numbers
    public List<ContactPhone> getPhonesForContactSync(long contactId) {
        return phoneDao.getPhonesByContactId(contactId);
    }

    public void insertPhone(ContactPhone phone) {
        AppDatabase.databaseWriteExecutor.execute(() -> phoneDao.insert(phone));
    }

    public void deletePhonesForContact(long contactId) {
        AppDatabase.databaseWriteExecutor.execute(() -> phoneDao.deletePhonesByContactId(contactId));
    }

    // Groups
    public LiveData<List<ContactGroup>> getAllGroups() {
        return groupDao.getAllGroups();
    }

    public void insertGroup(ContactGroup group) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.insertGroup(group));
    }

    public void updateGroup(ContactGroup group) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.updateGroup(group));
    }

    public void deleteGroup(ContactGroup group) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.deleteGroup(group));
    }

    public void addContactToGroup(long contactId, long groupId) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.insertContactGroupCrossRef(new ContactGroupCrossRef(contactId, groupId)));
    }

    public void removeContactFromGroup(long contactId, long groupId) {
        AppDatabase.databaseWriteExecutor.execute(() -> groupDao.deleteContactGroupCrossRef(new ContactGroupCrossRef(contactId, groupId)));
    }

    public LiveData<List<ContactGroup>> getGroupsForContact(long contactId) {
        return groupDao.getGroupsForContact(contactId);
    }

    public List<ContactGroup> getGroupsForContactSync(long contactId) {
        return groupDao.getGroupsForContactSync(contactId);
    }

    public ContactDao getContactDao() {
        return contactDao;
    }

    public ContactGroupDao getGroupDao() {
        return groupDao;
    }
}
