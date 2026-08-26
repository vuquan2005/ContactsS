package com.example.contactvip.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactvip.data.entity.Contact;
import com.example.contactvip.data.entity.ContactDisplay;
import com.example.contactvip.databinding.ItemFavoriteContactBinding;
import com.example.contactvip.utils.AvatarUtils;

public class FavoriteContactAdapter extends ListAdapter<ContactDisplay, FavoriteContactAdapter.FavoriteViewHolder> {
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Contact contact);
    }

    public FavoriteContactAdapter(OnFavoriteClickListener listener) {
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
            String oldUri = oldItem.contact.avatarUri;
            String newUri = newItem.contact.avatarUri;
            boolean avatarSame = (oldUri == null && newUri == null) || (oldUri != null && oldUri.equals(newUri));
            return avatarSame && oldItem.getFullName().equals(newItem.getFullName());
        }
    };

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteContactBinding binding = ItemFavoriteContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoriteContactBinding binding;

        public FavoriteViewHolder(ItemFavoriteContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContactDisplay item, OnFavoriteClickListener listener) {
            binding.tvFavName.setText(item.contact.name != null && !item.contact.name.isEmpty() ? item.contact.name : item.getFullName());
            AvatarUtils.loadAvatar(itemView.getContext(), item.contact.avatarUri, binding.ivFavAvatar);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(item.contact);
                }
            });
        }
    }
}
