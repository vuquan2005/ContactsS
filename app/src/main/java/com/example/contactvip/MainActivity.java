package com.example.contactvip;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.contactvip.databinding.ActivityMainBinding;
import com.example.contactvip.ui.contacts.AddEditContactActivity;
import com.example.contactvip.ui.contacts.ContactsFragment;
import com.example.contactvip.ui.dialer.DialerActivity;
import com.example.contactvip.ui.favorites.FavoritesFragment;
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

            if (itemId == R.id.navigation_favorites) {
                selectedFragment = new FavoritesFragment();
                title = getString(R.string.title_favorites);
            } else if (itemId == R.id.navigation_recents) {
                selectedFragment = new RecentsFragment();
                title = getString(R.string.title_recents);
            } else if (itemId == R.id.navigation_contacts) {
                selectedFragment = new ContactsFragment();
                title = getString(R.string.title_contacts);
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

        // Default FAB action
        binding.fabDialer.setOnClickListener(v -> {
            Intent intent = new Intent(this, DialerActivity.class);
            startActivity(intent);
        });

        // Set default fragment
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_contacts);
        }
    }

    private void updateFab(int itemId) {
        if (itemId == R.id.navigation_contacts) {
            binding.fabDialer.setImageResource(R.drawable.ic_add);
            binding.fabDialer.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditContactActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fabDialer.setImageResource(R.drawable.ic_dialer);
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
