package me.anky.connectid.connections;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import me.anky.connectid.R;

abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final ColorDrawable background;
    private final Drawable deleteIcon;
    private final int iconMargin;

    SwipeToDeleteCallback(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        background = new ColorDrawable(ContextCompat.getColor(context, R.color.swipe_delete));
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white);
        iconMargin = Math.round(24 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        View item = viewHolder.itemView;
        if (dX > 0) {
            background.setBounds(item.getLeft(), item.getTop(),
                    item.getLeft() + Math.round(dX), item.getBottom());
        } else if (dX < 0) {
            background.setBounds(item.getRight() + Math.round(dX), item.getTop(),
                    item.getRight(), item.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(canvas);

        if (deleteIcon != null && dX != 0) {
            int iconWidth = deleteIcon.getIntrinsicWidth();
            int iconHeight = deleteIcon.getIntrinsicHeight();
            int iconTop = item.getTop() + (item.getHeight() - iconHeight) / 2;
            int iconLeft = dX > 0
                    ? item.getLeft() + iconMargin
                    : item.getRight() - iconMargin - iconWidth;
            deleteIcon.setBounds(iconLeft, iconTop,
                    iconLeft + iconWidth, iconTop + iconHeight);
            deleteIcon.draw(canvas);
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY,
                actionState, isCurrentlyActive);
    }
}
