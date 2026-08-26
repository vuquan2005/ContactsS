package com.example.contactvip.ui.recents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.adapter.CallHistoryAdapter;
import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.databinding.FragmentRecentsBinding;
import com.example.contactvip.ui.call.CallActivity;
import com.example.contactvip.ui.contacts.ContactSwipeCallback;
import com.example.contactvip.viewmodel.CallHistoryViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RecentsFragment extends Fragment {
    private FragmentRecentsBinding binding;
    private CallHistoryViewModel viewModel;
    private CallHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CallHistoryViewModel.class);
        
        adapter = new CallHistoryAdapter();
        binding.recyclerViewRecents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewRecents.setAdapter(adapter);
        
        setupSwipe();
        
        viewModel.getAllCallHistory().observe(getViewLifecycleOwner(), calls -> {
            if (calls == null || calls.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewRecents.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewRecents.setVisibility(View.VISIBLE);
                adapter.submitList(calls);
            }
        });
    }

    private void setupSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ContactSwipeCallback(getContext(), new ContactSwipeCallback.OnSwipeListener() {
            @Override
            public void onSwipeLeft(int position) {
                CallHistory history = adapter.getCurrentList().get(position);
                viewModel.delete(history);
                Snackbar.make(binding.getRoot(), "Call history deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> viewModel.insert(history))
                        .show();
            }

            @Override
            public void onSwipeRight(int position) {
                CallHistory history = adapter.getCurrentList().get(position);
                Intent intent = new Intent(getContext(), CallActivity.class);
                intent.putExtra("PHONE_NUMBER", history.phoneNumber);
                intent.putExtra("CONTACT_NAME", history.contactName);
                intent.putExtra("CONTACT_ID", history.contactId);
                startActivity(intent);
                adapter.notifyItemChanged(position); // Reset swipe state
            }
        }));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewRecents);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
