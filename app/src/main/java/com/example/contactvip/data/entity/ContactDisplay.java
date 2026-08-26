package com.example.contactvip.data.entity;

import androidx.room.Embedded;

public class ContactDisplay {
    @Embedded
    public Contact contact;
    public String primaryPhone;

    public String getFullName() {
        return (contact != null && contact.name != null) ? contact.name : "";
    }
}
