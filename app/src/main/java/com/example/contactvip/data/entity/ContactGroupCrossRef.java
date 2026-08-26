package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "contact_group_cross_ref",
    primaryKeys = {"contactId", "groupId"},
    foreignKeys = {
        @ForeignKey(entity = Contact.class, parentColumns = "id", childColumns = "contactId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = ContactGroup.class, parentColumns = "id", childColumns = "groupId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("groupId")}
)
public class ContactGroupCrossRef {
    public long contactId;
    public long groupId;

    public ContactGroupCrossRef() {}

    public ContactGroupCrossRef(long contactId, long groupId) {
        this.contactId = contactId;
        this.groupId = groupId;
    }
}
