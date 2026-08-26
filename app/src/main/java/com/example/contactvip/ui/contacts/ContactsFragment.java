package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.R;
import com.example.contactvip.adapter.ContactAdapter;
import com.example.contactvip.adapter.FavoriteContactAdapter;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.databinding.FragmentContactsBinding;
import com.example.contactvip.viewmodel.ContactViewModel;

public class ContactsFragment extends Fragment implements ContactAdapter.OnContactClickListener, FavoriteContactAdapter.OnFavoriteClickListener {
    private FragmentContactsBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;
    private FavoriteContactAdapter favoriteAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        
        // Setup Main Contacts RecyclerView
        adapter = new ContactAdapter(this);
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewContacts.setAdapter(adapter);

        // Setup Favorites Section RecyclerView
        favoriteAdapter = new FavoriteContactAdapter(this);
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFavorites.setAdapter(favoriteAdapter);
        
        viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts == null || contacts.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewContacts.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewContacts.setVisibility(View.VISIBLE);
                adapter.submitList(contacts);
            }
        });

        // Observe Favorites to display at the top of Contacts
        viewModel.getFavoriteContacts().observe(getViewLifecycleOwner(), favorites -> {
            updateFavoritesSection(favorites);
        });

        binding.btnAddContact.setOnClickListener(v -> {
            binding.searchView.clearFocus();
            Intent intent = new Intent(getContext(), AddEditContactActivity.class);
            startActivity(intent);
        });

        binding.btnSort.setOnClickListener(v -> {
            binding.searchView.clearFocus();
            showSortMenu(v);
        });
        binding.btnFilter.setOnClickListener(v -> {
            binding.searchView.clearFocus();
            showFilterMenu(v);
        });

        binding.alphabetIndex.setOnIndexSelectedListener(letter -> {
            binding.searchView.clearFocus();
            int position = adapter.getPositionForSection(letter);
            if (position != -1) {
                ((LinearLayoutManager) binding.recyclerViewContacts.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                if (newText != null && !newText.trim().isEmpty()) {
                    binding.layoutFavoritesSection.setVisibility(View.GONE);
                } else {
                    updateFavoritesSection(viewModel.getFavoriteContacts().getValue());
                }
                return true;
            }
        });
    }

    @Override
    public void onContactClick(Contact contact) {
        if (binding != null) {
            binding.searchView.clearFocus();
        }
        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
        intent.putExtra("CONTACT_ID", contact.id);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Contact contact) {
        if (binding != null) {
            binding.searchView.clearFocus();
        }
        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
        intent.putExtra("CONTACT_ID", contact.id);
        startActivity(intent);
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(0, 0, 0, getString(R.string.sort_name_asc)).setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.NAME_ASC);
        popup.getMenu().add(0, 1, 1, getString(R.string.sort_name_desc)).setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.NAME_DESC);
        popup.getMenu().add(0, 2, 2, getString(R.string.sort_recently_added)).setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.RECENTLY_ADDED);
        popup.getMenu().add(0, 3, 3, getString(R.string.sort_oldest_added)).setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.OLDEST_ADDED);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0: viewModel.setSortMode(ContactViewModel.SortMode.NAME_ASC); break;
                case 1: viewModel.setSortMode(ContactViewModel.SortMode.NAME_DESC); break;
                case 2: viewModel.setSortMode(ContactViewModel.SortMode.RECENTLY_ADDED); break;
                case 3: viewModel.setSortMode(ContactViewModel.SortMode.OLDEST_ADDED); break;
            }
            return true;
        });
        popup.show();
    }

    private void updateFavoritesSection(List<ContactDisplay> favorites) {
        if (binding == null) return;
        if (favorites != null && !favorites.isEmpty() && viewModel.getFilterMode() == ContactViewModel.FilterMode.ALL) {
            binding.layoutFavoritesSection.setVisibility(View.VISIBLE);
            favoriteAdapter.submitList(favorites);
        } else {
            binding.layoutFavoritesSection.setVisibility(View.GONE);
        }
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(0, -1, 0, getString(R.string.filter_all)).setCheckable(true).setChecked(viewModel.getFilterMode() == ContactViewModel.FilterMode.ALL);
        popup.getMenu().add(0, -2, 1, getString(R.string.title_favorites)).setCheckable(true).setChecked(viewModel.getFilterMode() == ContactViewModel.FilterMode.FAVORITES);

        viewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                for (ContactGroup g : groups) {
                    popup.getMenu().add(1, (int) g.id, 2, g.name)
                            .setCheckable(true)
                            .setChecked(viewModel.getFilterMode() == ContactViewModel.FilterMode.GROUP && viewModel.getCurrentGroupId() == g.id);
                }
            }
        });

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == -1) viewModel.setFilterMode(ContactViewModel.FilterMode.ALL, -1);
            else if (item.getItemId() == -2) viewModel.setFilterMode(ContactViewModel.FilterMode.FAVORITES, -1);
            else viewModel.setFilterMode(ContactViewModel.FilterMode.GROUP, item.getItemId());
            updateFavoritesSection(viewModel.getFavoriteContacts().getValue());
            return true;
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
