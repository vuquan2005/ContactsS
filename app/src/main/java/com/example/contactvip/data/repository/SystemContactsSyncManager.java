package com.example.contactvip.data.repository;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactPhone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemContactsSyncManager {
    private static final String TAG = "ContactSyncManager";
    private static ContentObserver systemContactObserver;

    /**
     * Đồng bộ toàn diện danh bạ từ Hệ thống Android (Google Account, SIM 1, SIM 2, Local) vào Room Database.
     */
    public static synchronized void syncAllFromSystem(Context context, AppDatabase db) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            Uri dataUri = Data.CONTENT_URI;

            String[] projection = new String[]{
                    Data.CONTACT_ID,
                    Data.LOOKUP_KEY,
                    Data.DISPLAY_NAME_PRIMARY,
                    Data.PHOTO_URI,
                    Data.STARRED,
                    Data.MIMETYPE,
                    Data._ID,
                    Data.RAW_CONTACT_ID,
                    RawContacts.ACCOUNT_TYPE,
                    RawContacts.ACCOUNT_NAME,
                    RawContacts.VERSION,
                    CommonDataKinds.Phone.NUMBER,
                    CommonDataKinds.Phone.TYPE,
                    CommonDataKinds.Phone.LABEL,
                    CommonDataKinds.Phone.IS_PRIMARY,
                    CommonDataKinds.Email.ADDRESS,
                    CommonDataKinds.Organization.COMPANY,
                    CommonDataKinds.Organization.TITLE,
                    CommonDataKinds.Note.NOTE,
                    CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
            };

            Cursor cursor = resolver.query(
                    dataUri,
                    projection,
                    null,
                    null,
                    Data.CONTACT_ID + " ASC"
            );

            if (cursor == null) return;

            Map<Long, ContactBuilder> contactMap = new HashMap<>();
            Set<Long> systemContactIds = new HashSet<>();

            int colContactId = cursor.getColumnIndex(Data.CONTACT_ID);
            int colLookupKey = cursor.getColumnIndex(Data.LOOKUP_KEY);
            int colDisplayName = cursor.getColumnIndex(Data.DISPLAY_NAME_PRIMARY);
            int colPhotoUri = cursor.getColumnIndex(Data.PHOTO_URI);
            int colStarred = cursor.getColumnIndex(Data.STARRED);
            int colMimeType = cursor.getColumnIndex(Data.MIMETYPE);
            int colDataId = cursor.getColumnIndex(Data._ID);
            int colAccountType = cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE);
            int colAccountName = cursor.getColumnIndex(RawContacts.ACCOUNT_NAME);
            int colVersion = cursor.getColumnIndex(RawContacts.VERSION);

            int colPhoneNumber = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
            int colPhoneType = cursor.getColumnIndex(CommonDataKinds.Phone.TYPE);
            int colPhoneLabel = cursor.getColumnIndex(CommonDataKinds.Phone.LABEL);
            int colPhoneIsPrimary = cursor.getColumnIndex(CommonDataKinds.Phone.IS_PRIMARY);

            int colEmail = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
            int colCompany = cursor.getColumnIndex(CommonDataKinds.Organization.COMPANY);
            int colJobTitle = cursor.getColumnIndex(CommonDataKinds.Organization.TITLE);
            int colNote = cursor.getColumnIndex(CommonDataKinds.Note.NOTE);
            int colAddress = cursor.getColumnIndex(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);

            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(colContactId);
                if (contactId <= 0) continue;

                systemContactIds.add(contactId);
                ContactBuilder builder = contactMap.get(contactId);
                if (builder == null) {
                    builder = new ContactBuilder();
                    builder.systemContactId = contactId;
                    builder.lookupKey = colLookupKey >= 0 ? cursor.getString(colLookupKey) : null;
                    builder.name = colDisplayName >= 0 ? cursor.getString(colDisplayName) : "";
                    builder.avatarUri = colPhotoUri >= 0 ? cursor.getString(colPhotoUri) : null;
                    builder.isFavorite = colStarred >= 0 && cursor.getInt(colStarred) == 1;
                    builder.accountType = colAccountType >= 0 ? cursor.getString(colAccountType) : null;
                    builder.accountName = colAccountName >= 0 ? cursor.getString(colAccountName) : null;
                    builder.version = colVersion >= 0 ? cursor.getLong(colVersion) : 0;
                    contactMap.put(contactId, builder);
                }

                String mime = colMimeType >= 0 ? cursor.getString(colMimeType) : "";
                if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mime)) {
                    String number = colPhoneNumber >= 0 ? cursor.getString(colPhoneNumber) : "";
                    if (number != null && !number.trim().isEmpty()) {
                        long dataId = colDataId >= 0 ? cursor.getLong(colDataId) : 0;
                        int type = colPhoneType >= 0 ? cursor.getInt(colPhoneType) : CommonDataKinds.Phone.TYPE_MOBILE;
                        String customLabel = colPhoneLabel >= 0 ? cursor.getString(colPhoneLabel) : "";
                        boolean isPrimary = colPhoneIsPrimary >= 0 && cursor.getInt(colPhoneIsPrimary) == 1;

                        String label = "Mobile";
                        if (type == CommonDataKinds.Phone.TYPE_HOME) label = "Home";
                        else if (type == CommonDataKinds.Phone.TYPE_WORK) label = "Work";
                        else if (type == CommonDataKinds.Phone.TYPE_CUSTOM && customLabel != null && !customLabel.isEmpty()) label = customLabel;

                        ContactPhone phone = new ContactPhone();
                        phone.systemDataId = dataId;
                        phone.phoneNumber = number.trim();
                        phone.label = label;
                        phone.isPrimary = isPrimary || builder.phones.isEmpty();
                        builder.phones.add(phone);
                    }
                } else if (CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mime)) {
                    if (builder.email == null || builder.email.isEmpty()) {
                        builder.email = colEmail >= 0 ? cursor.getString(colEmail) : "";
                    }
                } else if (CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mime)) {
                    if (builder.company == null || builder.company.isEmpty()) {
                        builder.company = colCompany >= 0 ? cursor.getString(colCompany) : "";
                    }
                    if (builder.jobTitle == null || builder.jobTitle.isEmpty()) {
                        builder.jobTitle = colJobTitle >= 0 ? cursor.getString(colJobTitle) : "";
                    }
                } else if (CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mime)) {
                    if (builder.notes == null || builder.notes.isEmpty()) {
                        builder.notes = colNote >= 0 ? cursor.getString(colNote) : "";
                    }
                } else if (CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mime)) {
                    if (builder.address == null || builder.address.isEmpty()) {
                        builder.address = colAddress >= 0 ? cursor.getString(colAddress) : "";
                    }
                }
            }
            cursor.close();

            // Lưu và cập nhật vào Room Database theo Transaction
            db.runInTransaction(() -> {
                for (ContactBuilder cb : contactMap.values()) {
                    Contact existing = null;
                    if (cb.lookupKey != null && !cb.lookupKey.isEmpty()) {
                        existing = db.contactDao().getContactByLookupKey(cb.lookupKey);
                    }
                    if (existing == null) {
                        existing = db.contactDao().getContactBySystemId(cb.systemContactId);
                    }

                    long localContactId;
                    if (existing == null) {
                        // Thêm mới liên hệ từ hệ thống vào Room
                        Contact newContact = new Contact();
                        newContact.systemContactId = cb.systemContactId;
                        newContact.lookupKey = cb.lookupKey;
                        newContact.accountType = cb.accountType;
                        newContact.accountName = cb.accountName;
                        newContact.version = cb.version;
                        newContact.name = cb.name;
                        newContact.email = cb.email;
                        newContact.company = cb.company;
                        newContact.jobTitle = cb.jobTitle;
                        newContact.address = cb.address;
                        newContact.notes = cb.notes;
                        newContact.avatarUri = cb.avatarUri;
                        newContact.isFavorite = cb.isFavorite;
                        newContact.createdAt = System.currentTimeMillis();
                        newContact.updatedAt = System.currentTimeMillis();

                        localContactId = db.contactDao().insert(newContact);
                    } else {
                        // Cập nhật thông tin đồng bộ
                        localContactId = existing.id;
                        existing.systemContactId = cb.systemContactId;
                        existing.lookupKey = cb.lookupKey;
                        existing.accountType = cb.accountType;
                        existing.accountName = cb.accountName;
                        existing.version = cb.version;
                        existing.name = cb.name;
                        if (cb.email != null && !cb.email.isEmpty()) existing.email = cb.email;
                        if (cb.company != null && !cb.company.isEmpty()) existing.company = cb.company;
                        if (cb.jobTitle != null && !cb.jobTitle.isEmpty()) existing.jobTitle = cb.jobTitle;
                        if (cb.address != null && !cb.address.isEmpty()) existing.address = cb.address;
                        if (cb.notes != null && !cb.notes.isEmpty()) existing.notes = cb.notes;
                        if (cb.avatarUri != null && !cb.avatarUri.isEmpty()) existing.avatarUri = cb.avatarUri;
                        existing.isFavorite = cb.isFavorite;
                        existing.updatedAt = System.currentTimeMillis();

                        db.contactDao().update(existing);
                        db.contactPhoneDao().deletePhonesByContactId(localContactId);
                    }

                    // Lưu các số điện thoại
                    for (ContactPhone p : cb.phones) {
                        p.contactId = localContactId;
                    }
                    if (!cb.phones.isEmpty()) {
                        db.contactPhoneDao().insertAll(cb.phones);
                    }
                }

                // Dọn dẹp các liên hệ hệ thống đã bị xóa trên máy ngoài đời
                List<Long> localSystemIds = db.contactDao().getAllSystemContactIds();
                if (localSystemIds != null) {
                    for (Long sysId : localSystemIds) {
                        if (!systemContactIds.contains(sysId)) {
                            db.contactDao().deleteBySystemContactId(sysId);
                        }
                    }
                }
            });

            Log.d(TAG, "Đã đồng bộ thành công " + contactMap.size() + " liên hệ từ Hệ thống Android.");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi đồng bộ danh bạ từ hệ thống: " + e.getMessage(), e);
        }
    }

    /**
     * Ghi danh bạ được tạo hoặc chỉnh sửa từ ứng dụng lên Danh bạ Hệ thống (Google / SIM / Local).
     */
    public static void saveContactToSystem(Context context, Contact contact, List<ContactPhone> phones) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            if (contact.systemContactId == null || contact.systemContactId <= 0) {
                // Tạo mới RawContact
                int rawContactIndex = ops.size();
                ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                        .withValue(RawContacts.ACCOUNT_TYPE, contact.accountType)
                        .withValue(RawContacts.ACCOUNT_NAME, contact.accountName)
                        .build());

                // Họ tên
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                        .withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                        .build());

                // Các số điện thoại
                if (phones != null) {
                    for (ContactPhone phone : phones) {
                        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                                .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(CommonDataKinds.Phone.NUMBER, phone.phoneNumber)
                                .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
                                .build());
                    }
                }

                // Email
                if (contact.email != null && !contact.email.isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                            .withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.Email.DATA, contact.email)
                            .withValue(CommonDataKinds.Email.TYPE, CommonDataKinds.Email.TYPE_WORK)
                            .build());
                }

                // Công ty & Chức vụ
                if ((contact.company != null && !contact.company.isEmpty()) || (contact.jobTitle != null && !contact.jobTitle.isEmpty())) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                            .withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.Organization.COMPANY, contact.company != null ? contact.company : "")
                            .withValue(CommonDataKinds.Organization.TITLE, contact.jobTitle != null ? contact.jobTitle : "")
                            .build());
                }

                // Ghi chú
                if (contact.notes != null && !contact.notes.isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                            .withValue(Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.Note.NOTE, contact.notes)
                            .build());
                }

                // Địa chỉ
                if (contact.address != null && !contact.address.isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValueBackReference(Data.RAW_CONTACT_ID, rawContactIndex)
                            .withValue(Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, contact.address)
                            .build());
                }

                ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
                if (results != null && results.length > 0 && results[0].uri != null) {
                    long rawContactId = ContentUris.parseId(results[0].uri);
                    // Lấy contactId tương ứng
                    Cursor c = resolver.query(
                            RawContacts.CONTENT_URI,
                            new String[]{RawContacts.CONTACT_ID},
                            RawContacts._ID + " = ?",
                            new String[]{String.valueOf(rawContactId)},
                            null
                    );
                    if (c != null && c.moveToFirst()) {
                        int idx = c.getColumnIndex(RawContacts.CONTACT_ID);
                        if (idx >= 0) {
                            contact.systemContactId = c.getLong(idx);
                        }
                        c.close();
                    }
                }
            } else {
                // Cập nhật liên hệ hệ thống đã có
                long sysId = contact.systemContactId;

                // Xóa Data cũ của contact và thêm mới lại
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                        .build());
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                        .build());
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.Email.CONTENT_ITEM_TYPE})
                        .build());
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.Organization.CONTENT_ITEM_TYPE})
                        .build());
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.Note.CONTENT_ITEM_TYPE})
                        .build());
                ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
                        .withSelection(Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(sysId), CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE})
                        .build());

                // Tìm raw contact id
                long rawContactId = -1;
                Cursor c = resolver.query(
                        RawContacts.CONTENT_URI,
                        new String[]{RawContacts._ID},
                        RawContacts.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(sysId)},
                        null
                );
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(RawContacts._ID);
                    if (idx >= 0) rawContactId = c.getLong(idx);
                    c.close();
                }

                if (rawContactId > 0) {
                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValue(Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                            .build());

                    if (phones != null) {
                        for (ContactPhone phone : phones) {
                            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                    .withValue(Data.RAW_CONTACT_ID, rawContactId)
                                    .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                    .withValue(CommonDataKinds.Phone.NUMBER, phone.phoneNumber)
                                    .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
                                    .build());
                        }
                    }

                    if (contact.email != null && !contact.email.isEmpty()) {
                        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                .withValue(Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                .withValue(CommonDataKinds.Email.DATA, contact.email)
                                .build());
                    }

                    if ((contact.company != null && !contact.company.isEmpty()) || (contact.jobTitle != null && !contact.jobTitle.isEmpty())) {
                        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                .withValue(Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                                .withValue(CommonDataKinds.Organization.COMPANY, contact.company != null ? contact.company : "")
                                .withValue(CommonDataKinds.Organization.TITLE, contact.jobTitle != null ? contact.jobTitle : "")
                                .build());
                    }

                    if (contact.notes != null && !contact.notes.isEmpty()) {
                        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                .withValue(Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                                .withValue(CommonDataKinds.Note.NOTE, contact.notes)
                                .build());
                    }

                    if (contact.address != null && !contact.address.isEmpty()) {
                        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                .withValue(Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                                .withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, contact.address)
                                .build());
                    }

                    resolver.applyBatch(ContactsContract.AUTHORITY, ops);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu danh bạ lên hệ thống: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa liên hệ trên danh bạ hệ thống Android.
     */
    public static void deleteContactFromSystem(Context context, long systemContactId) {
        if (systemContactId <= 0 || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(systemContactId));
            resolver.delete(uri, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xóa danh bạ hệ thống: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật trạng thái Yêu thích (Starred) trên hệ thống.
     */
    public static void setStarredInSystem(Context context, long systemContactId, boolean isStarred) {
        if (systemContactId <= 0 || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(ContactsContract.Contacts.STARRED, isStarred ? 1 : 0);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(systemContactId));
            resolver.update(uri, values, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật Starred hệ thống: " + e.getMessage(), e);
        }
    }

    /**
     * Đăng ký ContentObserver để tự động đồng bộ khi danh bạ hệ thống thay đổi từ bên ngoài.
     */
    public static synchronized void registerObserver(Context context, Runnable onChangeCallback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (systemContactObserver != null) return;
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            systemContactObserver = new ContentObserver(handler) {
                private long lastTriggerTime = 0;

                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange, uri);
                    long now = System.currentTimeMillis();
                    // Debounce 1.5s để tránh sync quá nhiều lần khi hệ thống đang import
                    if (now - lastTriggerTime > 1500) {
                        lastTriggerTime = now;
                        if (onChangeCallback != null) {
                            onChangeCallback.run();
                        }
                    }
                }
            };
            context.getContentResolver().registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI,
                    true,
                    systemContactObserver
            );
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đăng ký ContentObserver: " + e.getMessage(), e);
        }
    }

    private static class ContactBuilder {
        long systemContactId;
        String lookupKey;
        String name;
        String email;
        String company;
        String jobTitle;
        String address;
        String notes;
        String avatarUri;
        boolean isFavorite;
        String accountType;
        String accountName;
        long version;
        List<ContactPhone> phones = new ArrayList<>();
    }
}
