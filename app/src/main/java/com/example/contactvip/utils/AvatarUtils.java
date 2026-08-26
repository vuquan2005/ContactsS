package com.example.contactvip.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.contactvip.R;

public class AvatarUtils {
    /**
     * Load avatar từ URI trực tiếp. Không sử dụng logic thời gian hay cache phức tạp.
     */
    public static void loadAvatar(Context context, String uri, ImageView imageView) {
        if (uri != null && !uri.isEmpty()) {
            Glide.with(context)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }
}
