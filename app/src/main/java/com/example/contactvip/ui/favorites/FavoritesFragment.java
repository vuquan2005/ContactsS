package com.example.contactvip.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.adapter.ContactAdapter;
import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.databinding.FragmentFavoritesBinding;
import com.example.contactvip.ui.contacts.ContactDetailActivity;
import com.example.contactvip.viewmodel.ContactViewModel;

public class FavoritesFragment extends Fragment implements ContactAdapter.OnContactClickListener {
    private FragmentFavoritesBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        
        adapter = new ContactAdapter(this);
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFavorites.setAdapter(adapter);
        
        viewModel.getFavoriteContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts == null || contacts.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewFavorites.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewFavorites.setVisibility(View.VISIBLE);
                adapter.submitList(contacts);
            }
        });
    }

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
        intent.putExtra("CONTACT_ID", contact.id);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
