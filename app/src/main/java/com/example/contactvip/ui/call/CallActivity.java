package com.example.contactvip.ui.call;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.databinding.ActivityCallBinding;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.viewmodel.CallHistoryViewModel;

public class CallActivity extends AppCompatActivity {
    private ActivityCallBinding binding;
    private String phoneNumber;
    private String contactName;
    private long contactId;
    private static final int PERMISSION_REQUEST_CALL = 1;
    private CallHistoryViewModel callHistoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        callHistoryViewModel = new ViewModelProvider(this).get(CallHistoryViewModel.class);

        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        contactName = getIntent().getStringExtra("CONTACT_NAME");
        contactId = getIntent().getLongExtra("CONTACT_ID", -1);

        binding.tvCallerNumber.setText(phoneNumber);
        binding.tvCallerName.setText(contactName != null ? contactName : phoneNumber);

        binding.btnEndCall.setOnClickListener(v -> finish());

        if (contactId != -1) {
            new ViewModelProvider(this).get(com.example.contactvip.viewmodel.ContactViewModel.class)
                .getContactById(contactId).observe(this, contact -> {
                    if (contact != null) {
                        AvatarUtils.loadAvatar(this, contact.avatarUri, binding.ivAvatar);
                    }
                });
        }

        // Simulate connecting then start real call
        new Handler(Looper.getMainLooper()).postDelayed(this::startRealCall, 2000);
    }

    private void startRealCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL);
            return;
        }

        // Save to call history
        CallHistory history = new CallHistory();
        history.phoneNumber = phoneNumber;
        history.contactName = contactName;
        history.contactId = contactId;
        history.callType = "OUTGOING";
        history.timestamp = System.currentTimeMillis();
        callHistoryViewModel.insert(history);

        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not make call", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRealCall();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
