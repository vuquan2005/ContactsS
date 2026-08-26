package com.example.contactvip.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.contactvip.R;
import com.example.contactvip.data.database.AppDatabase;
import com.example.contactvip.data.entity.CallHistory;

public class CallUtils {
    public static final int REQUEST_CODE_CALL_PHONE = 101;

    public static void makeCall(Context context, String phoneNumber, String contactName, Long contactId, String avatarUri) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(context, R.string.no_phone_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanNumber = phoneNumber.trim();

        // Direct call using ACTION_CALL if permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode(cleanNumber)));
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
                return;
            } catch (SecurityException ignored) {
            }
        }

        // If permission is not granted, request permission from user
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL_PHONE);
        }

        // Fallback to ACTION_DIAL if CALL_PHONE permission is not yet granted
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + Uri.encode(cleanNumber)));
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, R.string.no_phone_available, Toast.LENGTH_SHORT).show();
        }
    }

    public static void makeCall(Context context, String phoneNumber) {
        makeCall(context, phoneNumber, null, null, null);
    }
}

