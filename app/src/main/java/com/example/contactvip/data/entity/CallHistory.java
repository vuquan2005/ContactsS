package com.example.contactvip.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "call_history")
public class CallHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long systemCallId; // ID from system CallLog.Calls._ID
    public long contactId; // -1 if not a contact
    public String phoneNumber;
    public String contactName;
    public String avatarUri;
    public String callType; // INCOMING, OUTGOING, MISSED, REJECTED
    public long timestamp;
    public long duration; // in seconds
}
