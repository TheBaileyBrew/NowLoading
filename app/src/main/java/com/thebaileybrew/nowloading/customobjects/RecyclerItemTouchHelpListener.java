package com.thebaileybrew.nowloading.customobjects;

import android.graphics.Canvas;
import android.view.View;

import com.thebaileybrew.nowloading.database.InventoryCursorAdapter;
import com.thebaileybrew.nowloading.interfaces.RecyclerItemTouchListener;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil;

public class RecyclerItemTouchHelpListener extends ItemTouchHelper.SimpleCallback {
    private final RecyclerItemTouchListener itemTouchHelpListener;

    public RecyclerItemTouchHelpListener(int dragDirs, int swipeDirs, RecyclerItemTouchListener listener) {
        super(dragDirs, swipeDirs);
        this.itemTouchHelpListener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        itemTouchHelpListener.onSwiped(viewHolder, direction);

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((InventoryCursorAdapter.ViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Create the background view itself for displaying the DELETE view
        final View foregroundView = ((InventoryCursorAdapter.ViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDrawOver(c, recyclerView,foregroundView, dX, dY,
                actionState,isCurrentlyActive);
    }
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Create the background view itself for displaying the DELETE view
        final View foregroundView = ((InventoryCursorAdapter.ViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDraw(c, recyclerView,foregroundView, dX,dY,
                actionState,isCurrentlyActive);
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }
}
