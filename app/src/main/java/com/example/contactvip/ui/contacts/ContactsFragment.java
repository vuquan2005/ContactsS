package com.example.contactvip.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.adapter.ContactAdapter;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.data.entity.ContactGroup;
import com.example.contactvip.databinding.FragmentContactsBinding;
import com.example.contactvip.ui.call.CallActivity;
import com.example.contactvip.viewmodel.ContactViewModel;

public class ContactsFragment extends Fragment implements ContactAdapter.OnContactClickListener {
    private FragmentContactsBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;

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
        
        adapter = new ContactAdapter(this);
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewContacts.setAdapter(adapter);
        
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

        binding.btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditContactActivity.class);
            startActivity(intent);
        });

        binding.btnSort.setOnClickListener(this::showSortMenu);
        binding.btnFilter.setOnClickListener(this::showFilterMenu);

        binding.alphabetIndex.setOnIndexSelectedListener(letter -> {
            int position = adapter.getPositionForSection(letter);
            if (position != -1) {
                ((LinearLayoutManager) binding.recyclerViewContacts.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
        intent.putExtra("CONTACT_ID", contact.id);
        startActivity(intent);
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(0, 0, 0, "Name A-Z").setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.NAME_ASC);
        popup.getMenu().add(0, 1, 1, "Name Z-A").setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.NAME_DESC);
        popup.getMenu().add(0, 2, 2, "Recently Added").setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.RECENTLY_ADDED);
        popup.getMenu().add(0, 3, 3, "Oldest Added").setCheckable(true).setChecked(viewModel.getSortMode() == ContactViewModel.SortMode.OLDEST_ADDED);

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

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(0, -1, 0, "All Contacts").setCheckable(true).setChecked(viewModel.getFilterMode() == ContactViewModel.FilterMode.ALL);
        popup.getMenu().add(0, -2, 1, "Favorites").setCheckable(true).setChecked(viewModel.getFilterMode() == ContactViewModel.FilterMode.FAVORITES);

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
