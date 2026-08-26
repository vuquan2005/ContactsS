package com.example.contactvip.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.databinding.ItemContactBinding;
import com.example.contactvip.utils.AvatarUtils;

public class ContactAdapter extends ListAdapter<ContactDisplay, ContactAdapter.ContactViewHolder> {
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactAdapter(OnContactClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ContactDisplay> DIFF_CALLBACK = new DiffUtil.ItemCallback<ContactDisplay>() {
        @Override
        public boolean areItemsTheSame(@NonNull ContactDisplay oldItem, @NonNull ContactDisplay newItem) {
            return oldItem.contact.id == newItem.contact.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ContactDisplay oldItem, @NonNull ContactDisplay newItem) {
            // Chỉ so sánh các giá trị hiển thị thực tế
            String oldUri = oldItem.contact.avatarUri;
            String newUri = newItem.contact.avatarUri;
            boolean avatarSame = (oldUri == null && newUri == null) || (oldUri != null && oldUri.equals(newUri));
            
            return avatarSame &&
                    oldItem.getFullName().equals(newItem.getFullName()) &&
                    oldItem.contact.isFavorite == newItem.contact.isFavorite &&
                    (oldItem.primaryPhone == null ? newItem.primaryPhone == null : oldItem.primaryPhone.equals(newItem.primaryPhone));
        }
    };

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            ContactDisplay item = getItem(i);
            String nameForIndexing = (item.contact.name != null) ? item.contact.name.toUpperCase() : "";
            if (!nameForIndexing.isEmpty() && nameForIndexing.charAt(0) == section) return i;
        }
        return -1;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;

        public ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContactDisplay item, OnContactClickListener listener) {
            binding.tvContactName.setText(item.getFullName());
            binding.tvPhoneNumber.setText(item.primaryPhone != null ? item.primaryPhone : "");
            binding.ivFavorite.setVisibility(item.contact.isFavorite ? android.view.View.VISIBLE : android.view.View.GONE);
            AvatarUtils.loadAvatar(itemView.getContext(), item.contact.avatarUri, binding.ivAvatar);
            
            itemView.setOnClickListener(v -> listener.onContactClick(item.contact));

            if (item.primaryPhone != null && !item.primaryPhone.isEmpty()) {
                binding.btnCall.setVisibility(android.view.View.VISIBLE);
                binding.btnCall.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(itemView.getContext(), com.example.contactvip.ui.call.CallActivity.class);
                    intent.putExtra("PHONE_NUMBER", item.primaryPhone);
                    intent.putExtra("CONTACT_NAME", item.getFullName());
                    intent.putExtra("CONTACT_ID", item.contact.id);
                    itemView.getContext().startActivity(intent);
                });
            } else {
                binding.btnCall.setVisibility(android.view.View.GONE);
            }
        }
    }
}
