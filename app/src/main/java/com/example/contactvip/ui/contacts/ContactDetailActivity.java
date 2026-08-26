package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.R;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactPhone;
import com.example.contactvip.databinding.ActivityContactDetailBinding;
import com.example.contactvip.databinding.ItemPhoneDetailBinding;
import com.example.contactvip.utils.AccountUtils;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.utils.CallUtils;
import com.example.contactvip.viewmodel.ContactViewModel;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ContactDetailActivity extends AppCompatActivity {
    private ActivityContactDetailBinding binding;
    private ContactViewModel viewModel;
    private Contact currentContact;
    private List<ContactPhone> currentPhones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact != null) {
                    currentContact = contact;
                    displayContact(contact);
                }
            });

            // Observe groups
            viewModel.getGroupsForContact(contactId).observe(this, groups -> {
                updateGroupChips(groups);
            });

            // Fetch phones
            new Thread(() -> {
                currentPhones = viewModel.getPhonesForContactSync(contactId);
                runOnUiThread(this::displayPhones);
            }).start();

        } else {
            finish();
        }

        binding.btnCall.setOnClickListener(v -> startCallDefault());
        binding.btnMessage.setOnClickListener(v -> startSms());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnShare.setOnClickListener(v -> shareContact());
        binding.btnEdit.setOnClickListener(v -> editContact());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());

        binding.btnAddPhoneEmpty.setOnClickListener(v -> editContact());
    }

    @Override
    protected void onResume() {
        super.onResume();
        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            new Thread(() -> {
                currentPhones = viewModel.getPhonesForContactSync(contactId);
                runOnUiThread(this::displayPhones);
            }).start();
        }
    }

    private void displayContact(Contact contact) {
        String displayName = contact.getFullName();
        if (displayName == null || displayName.trim().isEmpty()) {
            binding.tvName.setText("Unnamed Contact");
        } else {
            binding.tvName.setText(displayName);
        }

        AvatarUtils.loadAvatar(this, contact.avatarUri, binding.ivAvatar);

        // Storage Account Badge & Info Row
        String storageDisplay = AccountUtils.formatAccountDisplay(contact.accountType, contact.accountName);
        String shortBadge = AccountUtils.getShortAccountBadge(contact.accountType, contact.accountName);
        binding.chipStorageAccount.setText(shortBadge);
        binding.tvStorageAccount.setText(storageDisplay);

        // Subtitle / Headline (Job Title & Company)
        String company = contact.company != null ? contact.company.trim() : "";
        String jobTitle = contact.jobTitle != null ? contact.jobTitle.trim() : "";
        if (!jobTitle.isEmpty() && !company.isEmpty()) {
            binding.tvHeadline.setText(jobTitle + " • " + company);
            binding.tvHeadline.setVisibility(View.VISIBLE);
        } else if (!jobTitle.isEmpty()) {
            binding.tvHeadline.setText(jobTitle);
            binding.tvHeadline.setVisibility(View.VISIBLE);
        } else if (!company.isEmpty()) {
            binding.tvHeadline.setText(company);
            binding.tvHeadline.setVisibility(View.VISIBLE);
        } else {
            binding.tvHeadline.setVisibility(View.GONE);
        }

        // Email Section
        String email = contact.email != null ? contact.email.trim() : "";
        if (!email.isEmpty()) {
            binding.layoutEmail.setVisibility(View.VISIBLE);
            binding.tvEmail.setText(email);
            binding.layoutEmail.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                    startActivity(intent);
                } catch (Exception ignored) {}
            });
        } else {
            binding.layoutEmail.setVisibility(View.GONE);
        }

        // Work Section
        if (!jobTitle.isEmpty() || !company.isEmpty()) {
            binding.layoutWork.setVisibility(View.VISIBLE);
            if (!jobTitle.isEmpty() && !company.isEmpty()) {
                binding.tvWork.setText(jobTitle + " at " + company);
            } else if (!jobTitle.isEmpty()) {
                binding.tvWork.setText(jobTitle);
            } else {
                binding.tvWork.setText(company);
            }
        } else {
            binding.layoutWork.setVisibility(View.GONE);
        }

        // Address Section
        String address = contact.address != null ? contact.address.trim() : "";
        if (!address.isEmpty()) {
            binding.layoutAddress.setVisibility(View.VISIBLE);
            binding.tvAddress.setText(address);
        } else {
            binding.layoutAddress.setVisibility(View.GONE);
        }

        // Notes Section
        String notes = contact.notes != null ? contact.notes.trim() : "";
        if (!notes.isEmpty()) {
            binding.layoutNotes.setVisibility(View.VISIBLE);
            binding.tvNotes.setText(notes);
        } else {
            binding.layoutNotes.setVisibility(View.GONE);
        }

        // Check if all information fields are empty
        boolean hasAnyInfo = !email.isEmpty() || !company.isEmpty() || !jobTitle.isEmpty() || !address.isEmpty() || !notes.isEmpty();
        if (hasAnyInfo) {
            binding.emptyInfoView.setVisibility(View.GONE);
        } else {
            binding.emptyInfoView.setVisibility(View.VISIBLE);
        }

        // Favorite Button UI
        if (contact.isFavorite) {
            binding.btnFavorite.setIconResource(R.drawable.ic_star);
            binding.btnFavorite.setIconTintResource(R.color.favorite_star);
        } else {
            binding.btnFavorite.setIconResource(R.drawable.ic_star_border);
            if (binding.btnMessage.getIconTint() != null) {
                binding.btnFavorite.setIconTint(binding.btnMessage.getIconTint());
            } else {
                binding.btnFavorite.setIconTintResource(R.color.primary_light);
            }
        }
    }

    private void displayPhones() {
        binding.phonesContainer.removeAllViews();
        if (currentPhones != null && !currentPhones.isEmpty()) {
            binding.emptyPhonesView.setVisibility(View.GONE);
            binding.btnCall.setAlpha(1.0f);
            binding.btnMessage.setAlpha(1.0f);

            for (ContactPhone phone : currentPhones) {
                ItemPhoneDetailBinding pb = ItemPhoneDetailBinding.inflate(LayoutInflater.from(this), binding.phonesContainer, false);
                if (phone.label != null && !phone.label.isEmpty() && !phone.label.equalsIgnoreCase("Mobile") && !phone.label.equalsIgnoreCase("Phone")) {
                    pb.tvLabel.setText(phone.label);
                    pb.tvLabel.setVisibility(View.VISIBLE);
                } else {
                    pb.tvLabel.setVisibility(View.GONE);
                }
                pb.tvPhone.setText(phone.phoneNumber);
                pb.btnCall.setOnClickListener(v -> startCall(phone.phoneNumber));
                pb.getRoot().setOnClickListener(v -> startCall(phone.phoneNumber));
                binding.phonesContainer.addView(pb.getRoot());
            }
        } else {
            binding.emptyPhonesView.setVisibility(View.VISIBLE);
            binding.btnCall.setAlpha(0.5f);
            binding.btnMessage.setAlpha(0.5f);
        }
    }

    private void updateGroupChips(List<ContactGroup> groups) {
        binding.groupChips.removeAllViews();
        if (groups != null && !groups.isEmpty()) {
            binding.groupsContainer.setVisibility(View.VISIBLE);
            for (ContactGroup group : groups) {
                Chip chip = new Chip(this);
                chip.setText(group.name);
                chip.setClickable(false);
                binding.groupChips.addView(chip);
            }
        } else {
            binding.groupsContainer.setVisibility(View.GONE);
        }
    }

    private void startCallDefault() {
        if (currentPhones != null && !currentPhones.isEmpty()) {
            ContactPhone primary = currentPhones.get(0);
            for (ContactPhone p : currentPhones) {
                if (p.isPrimary) {
                    primary = p;
                    break;
                }
            }
            startCall(primary.phoneNumber);
        } else {
            Toast.makeText(this, R.string.no_phone_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void startCall(String number) {
        if (currentContact == null) {
            CallUtils.makeCall(this, number);
        } else {
            CallUtils.makeCall(this, number, currentContact.getFullName(), currentContact.id, currentContact.avatarUri);
        }
    }

    private void shareContact() {
        if (currentContact == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(currentContact.getFullName()).append("\n");
        if (currentPhones != null) {
            for (ContactPhone p : currentPhones) {
                sb.append(p.label).append(": ").append(p.phoneNumber).append("\n");
            }
        }
        if (currentContact.email != null && !currentContact.email.trim().isEmpty()) {
            sb.append(getString(R.string.email)).append(": ").append(currentContact.email).append("\n");
        }
        if (currentContact.company != null && !currentContact.company.trim().isEmpty()) {
            sb.append(getString(R.string.company)).append(": ").append(currentContact.company).append("\n");
        }
        if (currentContact.jobTitle != null && !currentContact.jobTitle.trim().isEmpty()) {
            sb.append(getString(R.string.job_title)).append(": ").append(currentContact.jobTitle).append("\n");
        }
        if (currentContact.address != null && !currentContact.address.trim().isEmpty()) {
            sb.append(getString(R.string.address)).append(": ").append(currentContact.address).append("\n");
        }
        if (currentContact.notes != null && !currentContact.notes.trim().isEmpty()) {
            sb.append(getString(R.string.notes)).append(": ").append(currentContact.notes).append("\n");
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
    }

    private void startSms() {
        if (currentContact == null || currentPhones == null || currentPhones.isEmpty()) {
            Toast.makeText(this, R.string.no_phone_messaging, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ContactPhone primary = currentPhones.get(0);
            for (ContactPhone p : currentPhones) {
                if (p.isPrimary) {
                    primary = p;
                    break;
                }
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + primary.phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.no_sms_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        if (currentContact == null) return;
        boolean newFav = !currentContact.isFavorite;
        currentContact.isFavorite = newFav;
        currentContact.updatedAt = System.currentTimeMillis();
        viewModel.setFavorite(currentContact, newFav);
        displayContact(currentContact);
    }

    private void editContact() {
        if (currentContact == null) return;
        Intent intent = new Intent(this, AddEditContactActivity.class);
        intent.putExtra("CONTACT_ID", currentContact.id);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (currentContact == null) return;
        String name = currentContact.getFullName();
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_contact_title)
                .setMessage(getString(R.string.delete_contact_message, name))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.delete(currentContact);
                    Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
