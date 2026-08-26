package com.example.contactvip.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactPhone;
import com.example.contactvip.data.repository.ContactRepository;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactViewModel extends AndroidViewModel {
    public enum SortMode { NAME_ASC, NAME_DESC, RECENTLY_ADDED, OLDEST_ADDED }
    public enum FilterMode { ALL, FAVORITES, GROUP }

    private final ContactRepository repository;
    private final MediatorLiveData<List<ContactDisplay>> displayedContacts = new MediatorLiveData<>();
    
    private SortMode currentSortMode = SortMode.NAME_ASC;
    private FilterMode currentFilterMode = FilterMode.ALL;
    private String currentSearchQuery = "";
    private long currentGroupId = -1;
    private LiveData<List<ContactDisplay>> currentSource;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository(application);
        updateDisplayedContacts();
    }

    public LiveData<List<ContactDisplay>> getAllContacts() {
        return displayedContacts;
    }

    private void updateDisplayedContacts() {
        if (currentSource != null) {
            displayedContacts.removeSource(currentSource);
        }

        if (currentFilterMode == FilterMode.FAVORITES) {
            currentSource = repository.getFavoriteContacts();
        } else if (currentFilterMode == FilterMode.GROUP && currentGroupId != -1) {
            currentSource = repository.getContactsByGroup(currentGroupId);
        } else if (!currentSearchQuery.isEmpty()) {
            currentSource = repository.searchContacts(currentSearchQuery);
        } else {
            currentSource = repository.getAllContacts();
        }

        displayedContacts.addSource(currentSource, contacts -> {
            if (contacts != null) {
                List<ContactDisplay> sortedList = new ArrayList<>(contacts);
                sortContacts(sortedList);
                displayedContacts.setValue(sortedList);
            }
        });
    }

    public void setSortMode(SortMode mode) {
        this.currentSortMode = mode;
        updateDisplayedContacts();
    }

    public void setFilterMode(FilterMode mode, long groupId) {
        this.currentFilterMode = mode;
        this.currentGroupId = groupId;
        updateDisplayedContacts();
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        updateDisplayedContacts();
    }

    public SortMode getSortMode() {
        return currentSortMode;
    }

    public FilterMode getFilterMode() {
        return currentFilterMode;
    }

    public long getCurrentGroupId() {
        return currentGroupId;
    }

    public LiveData<List<ContactGroup>> getAllGroups() {
        return repository.getAllGroups();
    }

    public LiveData<List<ContactDisplay>> getFavoriteContacts() {
        return repository.getFavoriteContacts();
    }

    public LiveData<Contact> getContactById(long id) {
        return repository.getContactById(id);
    }

    public void insert(Contact contact) {
        repository.insert(contact);
    }

    public void update(Contact contact) {
        repository.update(contact);
    }

    public void delete(Contact contact) {
        repository.delete(contact);
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        return repository.getContactByPhoneNumber(phoneNumber);
    }

    // Phone Numbers
    public List<ContactPhone> getPhonesForContactSync(long contactId) {
        return repository.getPhonesForContactSync(contactId);
    }

    public void insertPhone(ContactPhone phone) {
        repository.insertPhone(phone);
    }

    public void deletePhonesForContact(long contactId) {
        repository.deletePhonesForContact(contactId);
    }

    // Groups
    public void insertGroup(ContactGroup group) {
        repository.insertGroup(group);
    }

    public void updateGroup(ContactGroup group) {
        repository.updateGroup(group);
    }

    public void deleteGroup(ContactGroup group) {
        repository.deleteGroup(group);
    }

    public void addContactToGroup(long contactId, long groupId) {
        repository.addContactToGroup(contactId, groupId);
    }

    public void removeContactFromGroup(long contactId, long groupId) {
        repository.removeContactFromGroup(contactId, groupId);
    }

    public LiveData<List<ContactGroup>> getGroupsForContact(long contactId) {
        return repository.getGroupsForContact(contactId);
    }

    public ContactRepository getRepository() {
        return repository;
    }

    private void sortContacts(List<ContactDisplay> contacts) {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        contacts.sort((c1, c2) -> {
            switch (currentSortMode) {
                case NAME_DESC:
                    return -collator.compare(c1.contact.name != null ? c1.contact.name : "", c2.contact.name != null ? c2.contact.name : "");
                case RECENTLY_ADDED:
                    return Long.compare(c2.contact.createdAt, c1.contact.createdAt);
                case OLDEST_ADDED:
                    return Long.compare(c1.contact.createdAt, c2.contact.createdAt);
                case NAME_ASC:
                default:
                    return collator.compare(c1.contact.name != null ? c1.contact.name : "", c2.contact.name != null ? c2.contact.name : "");
            }
        });
    }
}
