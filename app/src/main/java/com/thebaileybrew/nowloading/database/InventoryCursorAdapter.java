package com.thebaileybrew.nowloading.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thebaileybrew.nowloading.R;
import com.thebaileybrew.nowloading.interfaces.onClickInterface;

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

            @Override
            public void bindView(View view, Context context, final Cursor cursor) {
                RelativeLayout itemRecyclerView = view.findViewById(R.id.view_object_contraint);
                TextView systemDetail = view.findViewById(R.id.system_detail);
                TextView itemDetail = view.findViewById(R.id.item_name_view);
                TextView quantityDetail = view.findViewById(R.id.item_qty_view);
                TextView priceDetail = view.findViewById(R.id.item_price_view);
                final TextView currentRow = view.findViewById(R.id.current_id);
                int systemValue = cursor.getInt(cursor.getColumnIndexOrThrow("system"));
                switch (systemValue) {
                    case 0:
                        systemDetail.setText(R.string.sps3);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_ps3);
                        break;
                    case 1:
                        systemDetail.setText(R.string.sps4);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_ps4);
                        break;
                    case 2:
                        systemDetail.setText(R.string.mxbox);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_xbone);
                        break;
                    case 3:
                        systemDetail.setText(R.string.n3ds);
                        itemRecyclerView.setBackgroundResource(R.drawable.layer_list_3ds);
                        break;
                    case 4:
                        systemDetail.setText(R.string.nswitch);
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
                view.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        clickInterface.onLongClick(v, Integer.parseInt(currentRow.getText().toString()));
                        return true;
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickInterface.onItemClick(v, Integer.parseInt(currentRow.getText().toString()));
                    }
                });
                currentRowID = currentRow.getText().toString();
                currentQuantity = quantityDetail.getText().toString();
                //Tag setting for row and quantity to be used in swipe left or right functionality
                view.setTag(CURRENT_ROW_ID, currentRowID);
                view.setTag(CURRENT_QTY_ID, currentQuantity);
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ConstraintLayout viewForeground;

        ViewHolder(View itemView) {
            super(itemView);
            viewForeground = itemView.findViewById(R.id.view_foreground);
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
