package com.thebaileybrew.nowloading.interfaces;

import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerItemTouchListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);
}
