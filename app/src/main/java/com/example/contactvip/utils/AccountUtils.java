package com.example.contactvip.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.RawContacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountUtils {

    public static class AccountItem {
        public final String displayName;
        public final String accountType;
        public final String accountName;
        public final int iconRes;

        public AccountItem(String displayName, String accountType, String accountName, int iconRes) {
            this.displayName = displayName;
            this.accountType = accountType;
            this.accountName = accountName;
            this.iconRes = iconRes;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Lấy danh sách tất cả các nơi lưu danh bạ khả dụng trên thiết bị
     */
    public static List<AccountItem> getAvailableAccounts(Context context) {
        List<AccountItem> list = new ArrayList<>();
        Set<String> addedKeys = new HashSet<>();

        // 1. Luôn có tùy chọn "Chỉ lưu trên thiết bị"
        list.add(new AccountItem("Thiết bị (Chỉ lưu trên máy)", null, null, com.example.contactvip.R.drawable.ic_person));
        addedKeys.add("null:null");

        // 2. Lấy tài khoản Google từ AccountManager nếu có
        try {
            AccountManager am = AccountManager.get(context);
            Account[] googleAccounts = am.getAccountsByType("com.google");
            if (googleAccounts != null) {
                for (Account acc : googleAccounts) {
                    String key = acc.type + ":" + acc.name;
                    if (!addedKeys.contains(key)) {
                        list.add(new AccountItem("Google (" + acc.name + ")", acc.type, acc.name, com.example.contactvip.R.drawable.ic_person));
                        addedKeys.add(key);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 3. Quét thêm từ RawContacts xem có tài khoản SIM / Vendor nào khác không
        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(
                    RawContacts.CONTENT_URI,
                    new String[]{RawContacts.ACCOUNT_TYPE, RawContacts.ACCOUNT_NAME},
                    RawContacts.ACCOUNT_TYPE + " IS NOT NULL",
                    null,
                    null
            );

            if (cursor != null) {
                int colType = cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE);
                int colName = cursor.getColumnIndex(RawContacts.ACCOUNT_NAME);
                while (cursor.moveToNext()) {
                    String type = colType >= 0 ? cursor.getString(colType) : null;
                    String name = colName >= 0 ? cursor.getString(colName) : null;
                    if (type != null && name != null) {
                        String key = type + ":" + name;
                        if (!addedKeys.contains(key)) {
                            String display = formatAccountDisplay(type, name);
                            list.add(new AccountItem(display, type, name, com.example.contactvip.R.drawable.ic_person));
                            addedKeys.add(key);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception ignored) {
        }

        return list;
    }

    public static String formatAccountDisplay(String accountType, String accountName) {
        if (accountType == null || accountType.isEmpty()) {
            return "Thiết bị (Chỉ lưu trên máy)";
        }
        if ("com.google".equalsIgnoreCase(accountType)) {
            return "Google (" + accountName + ")";
        }
        if (accountType.toLowerCase().contains("sim")) {
            return "Thẻ SIM (" + accountName + ")";
        }
        if (accountType.toLowerCase().contains("samsung")) {
            return "Samsung Account (" + accountName + ")";
        }
        if (accountType.toLowerCase().contains("xiaomi") || accountType.toLowerCase().contains("miui")) {
            return "Xiaomi Account (" + accountName + ")";
        }
        return accountName + " (" + accountType + ")";
    }

    public static String getShortAccountBadge(String accountType, String accountName) {
        if (accountType == null || accountType.isEmpty()) {
            return "Thiết bị";
        }
        if ("com.google".equalsIgnoreCase(accountType)) {
            return "Google: " + accountName;
        }
        if (accountType.toLowerCase().contains("sim")) {
            return "SIM: " + accountName;
        }
        return accountName;
    }
}
