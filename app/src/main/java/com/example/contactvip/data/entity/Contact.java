package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public long id;
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
        return (name != null) ? name : "";
    }
}
