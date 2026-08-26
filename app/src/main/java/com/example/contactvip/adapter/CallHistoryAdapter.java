package com.example.contactvip.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.databinding.ItemCallHistoryBinding;
import com.example.contactvip.utils.AvatarUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallHistoryAdapter extends ListAdapter<CallHistory, CallHistoryAdapter.CallHistoryViewHolder> {
    
    public CallHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CallHistory> DIFF_CALLBACK = new DiffUtil.ItemCallback<CallHistory>() {
        @Override
        public boolean areItemsTheSame(@NonNull CallHistory oldItem, @NonNull CallHistory newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CallHistory oldItem, @NonNull CallHistory newItem) {
            return oldItem.timestamp == newItem.timestamp &&
                    oldItem.phoneNumber.equals(newItem.phoneNumber);
        }
    };

    @NonNull
    @Override
    public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCallHistoryBinding binding = ItemCallHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CallHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CallHistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class CallHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCallHistoryBinding binding;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        public CallHistoryViewHolder(ItemCallHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CallHistory callHistory) {
            binding.tvCallerName.setText(callHistory.contactName != null ? callHistory.contactName : callHistory.phoneNumber);
            binding.tvCallDetails.setText(callHistory.callType + " • " + callHistory.phoneNumber);
            binding.tvTimestamp.setText(sdf.format(new Date(callHistory.timestamp)));
            AvatarUtils.loadAvatar(itemView.getContext(), callHistory.avatarUri, binding.ivAvatar);

            if ("INCOMING".equalsIgnoreCase(callHistory.callType)) {
                binding.ivCallType.setImageResource(com.example.contactvip.R.drawable.ic_call_incoming);
            } else if ("MISSED".equalsIgnoreCase(callHistory.callType)) {
                binding.ivCallType.setImageResource(com.example.contactvip.R.drawable.ic_call_missed);
            } else {
                binding.ivCallType.setImageResource(com.example.contactvip.R.drawable.ic_call_outgoing);
            }

            binding.btnCall.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(itemView.getContext(), com.example.contactvip.ui.call.CallActivity.class);
                intent.putExtra("PHONE_NUMBER", callHistory.phoneNumber);
                intent.putExtra("CONTACT_NAME", callHistory.contactName);
                intent.putExtra("CONTACT_ID", callHistory.contactId);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
