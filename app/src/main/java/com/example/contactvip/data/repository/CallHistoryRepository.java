package com.example.contactvip.data.repository;

import android.Manifest;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.example.contactvip.data.dao.CallHistoryDao;
import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.data.entity.Contact;

import java.util.ArrayList;
import java.util.List;

public class CallHistoryRepository {
    private final Application application;
    private final AppDatabase db;
    private final CallHistoryDao callHistoryDao;
    private final LiveData<List<CallHistory>> allCallHistory;

    public CallHistoryRepository(Application application) {
        this.application = application;
        this.db = AppDatabase.getDatabase(application);
        this.callHistoryDao = db.callHistoryDao();
        this.allCallHistory = callHistoryDao.getAllCallHistory();
    }

    public LiveData<List<CallHistory>> getAllCallHistory() {
        return allCallHistory;
    }

    public void syncSystemCallLogs() {
        if (ContextCompat.checkSelfPermission(application, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                ContentResolver resolver = application.getContentResolver();
                Uri uri = CallLog.Calls.CONTENT_URI;
                String[] projection = new String[]{
                        CallLog.Calls._ID,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION
                };

                // Use DATE + " DESC" without LIMIT in sortOrder to prevent SQLite/ContentResolver parser crashes on Android 10+
                Cursor cursor = resolver.query(uri, projection, null, null, CallLog.Calls.DATE + " DESC");
                if (cursor != null) {
                    List<CallHistory> list = new ArrayList<>();
                    int idCol = cursor.getColumnIndex(CallLog.Calls._ID);
                    int numCol = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int nameCol = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int typeCol = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateCol = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durCol = cursor.getColumnIndex(CallLog.Calls.DURATION);

                    int count = 0;
                    while (cursor.moveToNext() && count < 200) {
                        count++;
                        long sysId = idCol >= 0 ? cursor.getLong(idCol) : 0;
                        String number = numCol >= 0 ? cursor.getString(numCol) : "";
                        String cachedName = nameCol >= 0 ? cursor.getString(nameCol) : "";
                        int type = typeCol >= 0 ? cursor.getInt(typeCol) : CallLog.Calls.INCOMING_TYPE;
                        long date = dateCol >= 0 ? cursor.getLong(dateCol) : System.currentTimeMillis();
                        long duration = durCol >= 0 ? cursor.getLong(durCol) : 0;

                        String callTypeStr;
                        switch (type) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callTypeStr = "INCOMING";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callTypeStr = "OUTGOING";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callTypeStr = "MISSED";
                                break;
                            case CallLog.Calls.REJECTED_TYPE:
                                callTypeStr = "REJECTED";
                                break;
                            default:
                                callTypeStr = "INCOMING";
                                break;
                        }

                        CallHistory history = new CallHistory();
                        history.systemCallId = sysId;
                        history.phoneNumber = (number != null) ? number.trim() : "";
                        history.timestamp = date;
                        history.duration = duration;
                        history.callType = callTypeStr;

                        // Match with local contacts by exact or normalized phone number
                        if (history.phoneNumber != null && !history.phoneNumber.isEmpty()) {
                            Contact contact = null;
                            try {
                                contact = db.contactDao().getContactByPhoneNumber(history.phoneNumber);
                                if (contact == null && history.phoneNumber.length() > 6) {
                                    String normalized = history.phoneNumber.replaceAll("[^0-9]", "");
                                    if (normalized.length() > 6) {
                                        String endDigits = "%" + normalized.substring(normalized.length() - 7);
                                        List<com.example.contactvip.data.entity.ContactDisplay> matched = db.contactDao().searchContactsSync(endDigits);
                                        if (matched != null && !matched.isEmpty()) {
                                            contact = matched.get(0).contact;
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            if (contact != null) {
                                history.contactId = contact.id;
                                history.contactName = contact.getFullName();
                                history.avatarUri = contact.avatarUri;
                            } else {
                                history.contactId = -1;
                                history.contactName = (cachedName != null && !cachedName.trim().isEmpty()) ? cachedName.trim() : history.phoneNumber;
                            }
                        } else {
                            history.contactId = -1;
                            history.contactName = (cachedName != null && !cachedName.trim().isEmpty()) ? cachedName.trim() : "Unknown";
                        }

                        list.add(history);
                    }
                    cursor.close();

                    // Atomically replace local database records with system call logs
                    callHistoryDao.replaceAll(list);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void insert(CallHistory callHistory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (ContextCompat.checkSelfPermission(application, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                try {
                    ContentResolver resolver = application.getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CallLog.Calls.NUMBER, callHistory.phoneNumber != null ? callHistory.phoneNumber : "");
                    values.put(CallLog.Calls.DATE, callHistory.timestamp > 0 ? callHistory.timestamp : System.currentTimeMillis());
                    values.put(CallLog.Calls.DURATION, callHistory.duration);
                    
                    int typeInt = CallLog.Calls.INCOMING_TYPE;
                    if ("OUTGOING".equalsIgnoreCase(callHistory.callType)) {
                        typeInt = CallLog.Calls.OUTGOING_TYPE;
                    } else if ("MISSED".equalsIgnoreCase(callHistory.callType)) {
                        typeInt = CallLog.Calls.MISSED_TYPE;
                    } else if ("REJECTED".equalsIgnoreCase(callHistory.callType)) {
                        typeInt = CallLog.Calls.REJECTED_TYPE;
                    }
                    values.put(CallLog.Calls.TYPE, typeInt);
                    values.put(CallLog.Calls.NEW, 1);
                    if (callHistory.contactName != null && !callHistory.contactName.isEmpty()) {
                        values.put(CallLog.Calls.CACHED_NAME, callHistory.contactName);
                    }
                    Uri newUri = resolver.insert(CallLog.Calls.CONTENT_URI, values);
                    if (newUri != null) {
                        try {
                            callHistory.systemCallId = ContentUris.parseId(newUri);
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            callHistoryDao.insert(callHistory);
        });
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            callHistoryDao.deleteAll();
            if (ContextCompat.checkSelfPermission(application, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                try {
                    application.getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void delete(CallHistory callHistory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            callHistoryDao.delete(callHistory);
            if (callHistory.systemCallId > 0 && ContextCompat.checkSelfPermission(application, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                try {
                    application.getContentResolver().delete(
                            CallLog.Calls.CONTENT_URI,
                            CallLog.Calls._ID + "=?",
                            new String[]{String.valueOf(callHistory.systemCallId)}
                    );
                } catch (Exception ignored) {
                }
            }
        });
    }
}
