package com.example.contactvip.ui.contacts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactvip.R;

public class ContactSwipeCallback extends ItemTouchHelper.SimpleCallback {
    private final OnSwipeListener listener;
    private final Drawable callIcon;
    private final Drawable deleteIcon;
    private final ColorDrawable callBackground;
    private final ColorDrawable deleteBackground;

    public interface OnSwipeListener {
        void onSwipeLeft(int position);
        void onSwipeRight(int position);
    }

    public ContactSwipeCallback(Context context, OnSwipeListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
        this.callIcon = ContextCompat.getDrawable(context, R.drawable.ic_call);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_backspace);
        this.callBackground = new ColorDrawable(Color.parseColor("#4CAF50")); // Green
        this.deleteBackground = new ColorDrawable(Color.parseColor("#F44336")); // Red
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT) {
            listener.onSwipeLeft(viewHolder.getAdapterPosition());
        } else {
            listener.onSwipeRight(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        if (dX > 0) { // Swipe Right (Call)
            callBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            callBackground.draw(c);
            int iconMargin = (itemView.getHeight() - callIcon.getIntrinsicHeight()) / 2;
            callIcon.setBounds(itemView.getLeft() + iconMargin, itemView.getTop() + iconMargin, itemView.getLeft() + iconMargin + callIcon.getIntrinsicWidth(), itemView.getBottom() - iconMargin);
            callIcon.draw(c);
        } else if (dX < 0) { // Swipe Left (Delete)
            deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            deleteBackground.draw(c);
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop() + iconMargin, itemView.getRight() - iconMargin, itemView.getBottom() - iconMargin);
            deleteIcon.draw(c);
        }
    }
}
