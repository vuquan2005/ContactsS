package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "call_history")
public class CallHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long contactId; // -1 if not a contact
    public String phoneNumber;
    public String contactName;
    public String avatarUri;
    public String callType; // INCOMING, OUTGOING, MISSED
    public long timestamp;
    public long duration; // in seconds
}
