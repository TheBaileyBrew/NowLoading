package com.thebaileybrew.nowloading.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.thebaileybrew.nowloading.R;
import com.thebaileybrew.nowloading.interfaces.onClickInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InventoryCursorAdapter extends RecyclerView.Adapter<InventoryCursorAdapter.ViewHolder> {

    private final CursorAdapter mCursor;
    private final Context context;
    private final onClickInterface clickInterface;
    private static String currentRowID;
    private static String currentQuantity;
    public final static int CURRENT_ROW_ID = R.id.current_id;
    public final static int CURRENT_QTY_ID = R.id.item_qty_view;

    public InventoryCursorAdapter(Context context, Cursor cursor, final onClickInterface clickInterface) {
        this.context = context;
        this.clickInterface = clickInterface;
        this.mCursor = new CursorAdapter(context, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.inventory_recycler_item, parent, false);
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void bindView(View view, final Context context, final Cursor cursor) {
                RelativeLayout itemRecyclerView = view.findViewById(R.id.view_object_contraint);
                final MaterialButton sellItemButton = view.findViewById(R.id.sell_this_button);
                TextView systemDetail = view.findViewById(R.id.system_detail);
                TextView itemDetail = view.findViewById(R.id.item_name_view);
                final TextView quantityDetail = view.findViewById(R.id.item_qty_view);
                TextView priceDetail = view.findViewById(R.id.item_price_view);
                final TextView currentRow = view.findViewById(R.id.current_id);
                int systemValue = cursor.getInt(cursor.getColumnIndexOrThrow("system"));
                switch (systemValue) {
                    case 0:
                        systemDetail.setText(R.string.sps3);
                        sellItemButton.setBackgroundResource(R.drawable.layer_list_ps3);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_ps3);
                        break;
                    case 1:
                        systemDetail.setText(R.string.sps4);
                        sellItemButton.setBackgroundResource(R.drawable.layer_list_ps4);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_ps4);
                        break;
                    case 2:
                        systemDetail.setText(R.string.mxbox);
                        sellItemButton.setBackgroundResource(R.drawable.layer_list_xbone);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_xbone);
                        break;
                    case 3:
                        systemDetail.setText(R.string.n3ds);
                        sellItemButton.setBackgroundResource(R.drawable.layer_list_3ds);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_3ds);
                        break;
                    case 4:
                        systemDetail.setText(R.string.nswitch);
                        sellItemButton.setBackgroundResource(R.drawable.layer_list_switch);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_switch);
                    default:
                        break;
                }
                String itemValue = cursor.getString((cursor.getColumnIndexOrThrow("game"))).toUpperCase();
                itemDetail.setText(itemValue);
                String quantityValue = cursor.getString((cursor.getColumnIndexOrThrow("quantity")));
                quantityDetail.setText(quantityValue);
                String priceValue = cursor.getString((cursor.getColumnIndexOrThrow("saleprice")));
                priceDetail.setText(priceValue);
                String currentRowValue = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                currentRow.setText(currentRowValue);
                currentRowID = currentRow.getText().toString();
                currentQuantity = quantityDetail.getText().toString();
                //Tag setting for row and quantity to be used in swipe left or right functionality
                view.setTag(CURRENT_ROW_ID, currentRowID);
                view.setTag(CURRENT_QTY_ID, currentQuantity);
                view.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        clickInterface.onLongClick(v, Integer.parseInt(currentRow.getText().toString()));
                        return true;
                    }
                });
                sellItemButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickInterface.onItemClick(v, Integer.parseInt(currentRow.getText().toString()),
                                Integer.parseInt(quantityDetail.getText().toString()));

                    }
                });


            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ConstraintLayout viewForeground;
        public final Button sellItemButton;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(final View itemView) {
            super(itemView);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            sellItemButton = itemView.findViewById(R.id.sell_this_button);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mCursor.newView(context, mCursor.getCursor(), parent);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.getCursor().moveToPosition(position);
        mCursor.bindView(holder.itemView, context, mCursor.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
