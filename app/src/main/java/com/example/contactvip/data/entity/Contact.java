package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // System Synchronization Identifiers
    public Long systemContactId;
    public String lookupKey;
    public String accountType;
    public String accountName;
    public long version;

    public String name;
    public String email;
    public String company;
    public String jobTitle;
    public String address;
    public String notes;
    public String avatarUri;
    public boolean isFavorite;
    public long createdAt;
    public long updatedAt;

    public String getFullName() {
        return (name != null && !name.trim().isEmpty()) ? name : "No Name";
    }
}
