package com.example.contactvip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.contactvip.databinding.ActivityMainBinding;
import com.example.contactvip.ui.contacts.AddEditContactActivity;
import com.example.contactvip.ui.contacts.ContactsFragment;
import com.example.contactvip.ui.dialer.DialerActivity;
import com.example.contactvip.ui.recents.RecentsFragment;
import com.example.contactvip.ui.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_contacts) {
                selectedFragment = new ContactsFragment();
                title = getString(R.string.title_contacts);
            } else if (itemId == R.id.navigation_recents) {
                selectedFragment = new RecentsFragment();
                title = getString(R.string.title_recents);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                binding.toolbar.setTitle(title);
                updateFab(itemId);
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_contacts);
        } else {
            updateFab(binding.bottomNavigation.getSelectedItemId());
        }

        // Request Phone Call, Call Log and Contacts permissions
        String[] requiredPermissions = new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
        };

        boolean needPermission = false;
        for (String perm : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needPermission = true;
                break;
            }
        }

        if (needPermission) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            new com.example.contactvip.data.repository.CallHistoryRepository(getApplication()).syncSystemCallLogs();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            new com.example.contactvip.data.repository.ContactRepository(getApplication()).syncSystemContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                new com.example.contactvip.data.repository.CallHistoryRepository(getApplication()).syncSystemCallLogs();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                new com.example.contactvip.data.repository.ContactRepository(getApplication()).syncSystemContacts();
            }
        }
    }

    private void updateFab(int itemId) {
        if (itemId == R.id.navigation_contacts) {
            binding.fabDialer.setImageResource(R.drawable.ic_add);
            binding.fabDialer.setContentDescription(getString(R.string.btn_add_contact));
            binding.fabDialer.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditContactActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fabDialer.setImageResource(R.drawable.ic_dialer);
            binding.fabDialer.setContentDescription(getString(R.string.dialer));
            binding.fabDialer.setOnClickListener(v -> {
                Intent intent = new Intent(this, DialerActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
