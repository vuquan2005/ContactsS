package com.example.contactvip.ui.recents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactvip.R;
import com.example.contactvip.adapter.CallHistoryAdapter;
import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.databinding.FragmentRecentsBinding;
import com.example.contactvip.ui.contacts.ContactSwipeCallback;
import com.example.contactvip.utils.CallUtils;
import com.example.contactvip.viewmodel.CallHistoryViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RecentsFragment extends Fragment {
    private FragmentRecentsBinding binding;
    private CallHistoryViewModel viewModel;
    private CallHistoryAdapter adapter;
    private ContentObserver callLogObserver;

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

        // Register observer for real-time system call log updates
        callLogObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                super.onChange(selfChange, uri);
                if (viewModel != null) {
                    viewModel.syncSystemCallLogs();
                }
            }
        };

        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                requireContext().getContentResolver().registerContentObserver(
                        CallLog.Calls.CONTENT_URI,
                        true,
                        callLogObserver
                );
            }
        } catch (Exception ignored) {
        }

        viewModel.syncSystemCallLogs();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.syncSystemCallLogs();
        }
    }

    private void setupSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ContactSwipeCallback(getContext(), new ContactSwipeCallback.OnSwipeListener() {
            @Override
            public void onSwipeLeft(int position) {
                CallHistory history = adapter.getCurrentList().get(position);
                viewModel.delete(history);
                Snackbar.make(binding.getRoot(), R.string.call_history_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> viewModel.insert(history))
                        .show();
            }

            @Override
            public void onSwipeRight(int position) {
                CallHistory history = adapter.getCurrentList().get(position);
                CallUtils.makeCall(requireContext(), history.phoneNumber, history.contactName, history.contactId, history.avatarUri);
                adapter.notifyItemChanged(position); // Reset swipe state
            }
        }));
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewRecents);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (callLogObserver != null && getContext() != null) {
            try {
                requireContext().getContentResolver().unregisterContentObserver(callLogObserver);
            } catch (Exception ignored) {
            }
        }
        binding = null;
    }
}
