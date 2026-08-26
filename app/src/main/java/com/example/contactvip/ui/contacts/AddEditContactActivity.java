package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.contactvip.R;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.data.entity.ContactPhone;
import com.example.contactvip.databinding.ActivityAddEditContactBinding;
import com.example.contactvip.databinding.ItemPhoneInputBinding;
import com.example.contactvip.utils.AvatarUtils;
import com.example.contactvip.viewmodel.ContactViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddEditContactActivity extends AppCompatActivity {
    private ActivityAddEditContactBinding binding;
    private ContactViewModel viewModel;
    private Contact existingContact;
    private String currentAvatarUri = null;
    private List<ItemPhoneInputBinding> phoneBindings = new ArrayList<>();
    private Set<Long> selectedGroupIds = new HashSet<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    currentAvatarUri = uri.toString();
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Preview ảnh mới ngay lập tức
                    AvatarUtils.loadAvatar(this, currentAvatarUri, binding.ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        long contactId = getIntent().getLongExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            binding.toolbar.setTitle(R.string.edit_contact);
            viewModel.getContactById(contactId).observe(this, contact -> {
                if (contact != null && existingContact == null) {
                    existingContact = contact;
                    currentAvatarUri = contact.avatarUri;
                    populateFields(contact);
                }
            });
            
            // Load Groups
            viewModel.getGroupsForContact(contactId).observe(this, groups -> {
                if (groups != null) {
                    for (ContactGroup g : groups) selectedGroupIds.add(g.id);
                    updateGroupChips(groups);
                }
            });
        } else {
            addPhoneField("", "Mobile", true);
        }

        viewModel.getAllGroups().observe(this, this::updateGroupChips);

        binding.btnChangePhoto.setOnClickListener(v -> 
            pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build())
        );
        binding.btnAddPhone.setOnClickListener(v -> addPhoneField("", "Mobile", false));
        binding.btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        binding.btnSave.setOnClickListener(v -> saveContact());
    }

    private void addPhoneField(String number, String label, boolean isPrimary) {
        ItemPhoneInputBinding phoneBinding = ItemPhoneInputBinding.inflate(LayoutInflater.from(this), binding.phonesContainer, false);
        phoneBinding.etPhone.setText(number);
        
        String defaultMobile = getString(R.string.label_mobile);
        phoneBinding.spinnerLabel.setText(label != null && !label.isEmpty() ? label : defaultMobile, false);
        
        String[] labels = {
                getString(R.string.label_mobile),
                getString(R.string.label_home),
                getString(R.string.label_work),
                getString(R.string.label_other)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels);
        phoneBinding.spinnerLabel.setAdapter(adapter);

        phoneBinding.btnRemove.setOnClickListener(v -> {
            if (phoneBindings.size() > 1) {
                binding.phonesContainer.removeView(phoneBinding.getRoot());
                phoneBindings.remove(phoneBinding);
            } else {
                Toast.makeText(this, R.string.at_least_one_phone, Toast.LENGTH_SHORT).show();
            }
        });

        binding.phonesContainer.addView(phoneBinding.getRoot());
        phoneBindings.add(phoneBinding);
    }

    private void updateGroupChips(List<ContactGroup> groups) {
        binding.groupChips.removeAllViews();
        if (groups != null) {
            for (ContactGroup group : groups) {
                Chip chip = new Chip(this);
                chip.setText(group.name);
                chip.setCheckable(true);
                chip.setChecked(selectedGroupIds.contains(group.id));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selectedGroupIds.add(group.id);
                    else selectedGroupIds.remove(group.id);
                });
                binding.groupChips.addView(chip);
            }
        }
    }

    private void showCreateGroupDialog() {
        TextInputEditText et = new TextInputEditText(this);
        et.setHint(R.string.group_name);
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_group)
                .setView(et)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) viewModel.insertGroup(new ContactGroup(name));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void populateFields(Contact contact) {
        binding.etName.setText(contact.name);
        binding.etEmail.setText(contact.email);
        binding.etCompany.setText(contact.company);
        binding.etJobTitle.setText(contact.jobTitle);
        binding.etAddress.setText(contact.address);
        binding.etNotes.setText(contact.notes);
        
        // Gán lại URI hiện tại để nếu không đổi ảnh thì vẫn giữ được ảnh cũ
        this.currentAvatarUri = contact.avatarUri;
        AvatarUtils.loadAvatar(this, contact.avatarUri, binding.ivAvatar);
        
        // Load Phones
        new Thread(() -> {
            List<ContactPhone> phones = viewModel.getPhonesForContactSync(contact.id);
            runOnUiThread(() -> {
                binding.phonesContainer.removeAllViews();
                phoneBindings.clear();
                if (phones.isEmpty()) {
                    addPhoneField("", "Mobile", true);
                } else {
                    for (ContactPhone p : phones) addPhoneField(p.phoneNumber, p.label, p.isPrimary);
                }
            });
        }).start();
    }

    private String getDisplayName() {
        return binding.etName.getText().toString().trim();
    }

    private void saveContact() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        boolean hasError = false;
        if (name.isEmpty()) {
            binding.tilName.setError("Name is required");
            hasError = true;
        } else {
            binding.tilName.setError(null);
        }

        List<ContactPhone> phonesToSave = new ArrayList<>();
        for (int i = 0; i < phoneBindings.size(); i++) {
            ItemPhoneInputBinding pb = phoneBindings.get(i);
            String num = pb.etPhone.getText().toString().trim();
            String lbl = pb.spinnerLabel.getText().toString();
            if (!num.isEmpty()) {
                phonesToSave.add(new ContactPhone(0, num, lbl, i == 0));
            }
        }

        if (phonesToSave.isEmpty()) {
            Toast.makeText(this, "At least one phone number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address");
            hasError = true;
        } else {
            binding.tilEmail.setError(null);
        }

        if (hasError) return;

        // Note: phone check skipped for brevity or handled by catching duplicate in list
        proceedToSave(name, email, phonesToSave);
    }

    private void proceedToSave(String name, String email, List<ContactPhone> phones) {
        // Tạo đối tượng sạch để tránh lỗi bộ nhớ đệm của Room/DiffUtil
        Contact contact = new Contact();
        if (existingContact != null) {
            contact.id = existingContact.id;
            contact.createdAt = existingContact.createdAt;
            contact.isFavorite = existingContact.isFavorite;
        } else {
            contact.createdAt = System.currentTimeMillis();
        }
        
        contact.name = name;
        contact.email = email;
        contact.company = binding.etCompany.getText().toString().trim();
        contact.jobTitle = binding.etJobTitle.getText().toString().trim();
        contact.address = binding.etAddress.getText().toString().trim();
        contact.notes = binding.etNotes.getText().toString().trim();
        contact.avatarUri = currentAvatarUri;
        contact.updatedAt = System.currentTimeMillis(); // Refresh UI trigger chính

        new Thread(() -> {
            long cid;
            if (existingContact == null) {
                cid = viewModel.getRepository().getContactDao().insert(contact);
            } else {
                cid = contact.id;
                viewModel.update(contact);
                viewModel.deletePhonesForContact(cid);
                viewModel.getRepository().getGroupDao().deleteCrossRefsByContactId(cid);
            }

            for (ContactPhone p : phones) {
                p.contactId = cid;
                viewModel.insertPhone(p);
            }

            for (Long gid : selectedGroupIds) {
                viewModel.addContactToGroup(cid, gid);
            }

            runOnUiThread(() -> {
                hideKeyboard();
                Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
