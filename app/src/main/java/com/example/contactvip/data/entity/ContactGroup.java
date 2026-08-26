package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contact_groups")
public class ContactGroup {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public long createdAt;

    public ContactGroup() {}

    public ContactGroup(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }
}
