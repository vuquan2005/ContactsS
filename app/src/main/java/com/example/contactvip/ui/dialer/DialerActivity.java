package com.example.contactvip.ui.dialer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.databinding.ActivityDialerBinding;
import com.example.contactvip.utils.CallUtils;
import com.example.contactvip.viewmodel.ContactViewModel;

public class DialerActivity extends AppCompatActivity {
    private ActivityDialerBinding binding;
    private final StringBuilder dialedNumber = new StringBuilder();
    private ContactViewModel contactViewModel;
    private Contact foundContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        setupDialPad();

        binding.btnDelete.setOnClickListener(v -> {
            if (dialedNumber.length() > 0) {
                dialedNumber.deleteCharAt(dialedNumber.length() - 1);
                updateDisplay();
            }
        });

        binding.btnDelete.setOnLongClickListener(v -> {
            dialedNumber.setLength(0);
            updateDisplay();
            return true;
        });

        binding.btnCall.setOnClickListener(v -> {
            if (dialedNumber.length() > 0) {
                String number = dialedNumber.toString();
                if (foundContact != null) {
                    CallUtils.makeCall(this, number, foundContact.getFullName(), foundContact.id, foundContact.avatarUri);
                } else {
                    CallUtils.makeCall(this, number);
                }
                finish();
            }
        });
    }

    private void setupDialPad() {
        for (int i = 0; i < binding.dialPad.getChildCount(); i++) {
            View child = binding.dialPad.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setOnClickListener(v -> {
                    dialedNumber.append(button.getText());
                    updateDisplay();
                });
            }
        }
    }

    private void updateDisplay() {
        String number = dialedNumber.toString();
        binding.tvDialedNumber.setText(number);
        
        if (number.isEmpty()) {
            binding.tvContactName.setText("");
            foundContact = null;
            return;
        }
        
        // Simple search logic
        contactViewModel.getAllContacts().observe(this, contacts -> {
            foundContact = null;
            for (ContactDisplay display : contacts) {
                if (display.primaryPhone != null && display.primaryPhone.contains(number)) {
                    foundContact = display.contact;
                    break;
                }
            }
            if (foundContact != null) {
                binding.tvContactName.setText(foundContact.getFullName());
            } else {
                binding.tvContactName.setText("Unknown");
            }
        });
    }
}
