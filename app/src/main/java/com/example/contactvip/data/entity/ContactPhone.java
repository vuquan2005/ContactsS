package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "contact_phones",
    foreignKeys = @ForeignKey(
        entity = Contact.class,
        parentColumns = "id",
        childColumns = "contactId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("contactId")}
)
public class ContactPhone {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long contactId;
    public String phoneNumber;
    public String label; // Mobile, Home, Work, Other
    public boolean isPrimary;

    public ContactPhone() {}

    public ContactPhone(long contactId, String phoneNumber, String label, boolean isPrimary) {
        this.contactId = contactId;
        this.phoneNumber = phoneNumber;
        this.label = label;
        this.isPrimary = isPrimary;
    }
}
