package com.example.contactvip.data.repository;

import android.app.Application;
import android.content.Context;

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
    private final Application application;
    private final AppDatabase db;
    private final ContactDao contactDao;
    private final ContactPhoneDao phoneDao;
    private final ContactGroupDao groupDao;
    private final LiveData<List<ContactDisplay>> allContacts;

    public ContactRepository(Application application) {
        this.application = application;
        this.db = AppDatabase.getDatabase(application);
        this.contactDao = db.contactDao();
        this.phoneDao = db.contactPhoneDao();
        this.groupDao = db.contactGroupDao();
        this.allContacts = contactDao.getAllContacts();

        // Tự động lắng nghe thay đổi từ Danh bạ Hệ thống (Google, SIM...)
        SystemContactsSyncManager.registerObserver(application, this::syncSystemContacts);
    }

    public void syncSystemContacts() {
        SystemContactsSyncManager.registerObserver(application, this::syncSystemContacts);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SystemContactsSyncManager.syncAllFromSystem(application, db);
        });
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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = contactDao.insert(contact);
            contact.id = id;
            List<ContactPhone> phones = phoneDao.getPhonesByContactId(id);
            SystemContactsSyncManager.saveContactToSystem(application, contact, phones);
            if (contact.systemContactId != null && contact.systemContactId > 0) {
                contactDao.update(contact);
            }
        });
    }

    public void update(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.update(contact);
            List<ContactPhone> phones = phoneDao.getPhonesByContactId(contact.id);
            SystemContactsSyncManager.saveContactToSystem(application, contact, phones);
        });
    }

    public void delete(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (contact.systemContactId != null && contact.systemContactId > 0) {
                SystemContactsSyncManager.deleteContactFromSystem(application, contact.systemContactId);
            }
            phoneDao.deletePhonesByContactId(contact.id);
            groupDao.deleteCrossRefsByContactId(contact.id);
            contactDao.delete(contact);
        });
    }

    public void setFavorite(Contact contact, boolean isFavorite) {
        contact.isFavorite = isFavorite;
        contact.updatedAt = System.currentTimeMillis();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.update(contact);
            if (contact.systemContactId != null && contact.systemContactId > 0) {
                SystemContactsSyncManager.setStarredInSystem(application, contact.systemContactId, isFavorite);
            }
        });
    }

    public void saveContactWithPhones(Contact contact, List<ContactPhone> phones, List<Long> groupIds, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long cid;
            if (contact.id == 0) {
                cid = contactDao.insert(contact);
                contact.id = cid;
            } else {
                cid = contact.id;
                contactDao.update(contact);
                phoneDao.deletePhonesByContactId(cid);
                groupDao.deleteCrossRefsByContactId(cid);
            }

            for (ContactPhone p : phones) {
                p.contactId = cid;
            }
            if (!phones.isEmpty()) {
                phoneDao.insertAll(phones);
            }

            if (groupIds != null) {
                for (Long gid : groupIds) {
                    groupDao.insertContactGroupCrossRef(new ContactGroupCrossRef(cid, gid));
                }
            }

            // Đồng bộ lên hệ thống
            SystemContactsSyncManager.saveContactToSystem(application, contact, phones);
            if (contact.systemContactId != null && contact.systemContactId > 0) {
                contactDao.update(contact);
            }

            if (onComplete != null) {
                onComplete.run();
            }
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
