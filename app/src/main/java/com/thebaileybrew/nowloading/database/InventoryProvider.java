package com.thebaileybrew.nowloading.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InventoryProvider extends ContentProvider {
    public static final String TAG = InventoryProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private InventoryDbHelper mDbHelper;
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    private static UriMatcher buildUriMatcher() {
        String content = InventoryContract.CONTENT_AUTHORITY;
        UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(content, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(content, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
        return sUriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match) {
            case INVENTORY:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs,null,null,sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String [] {String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        /*
        * Set notifications on the Cursor to notifiy if Uri has changed
        * If the data at the called ContentUri changes, then the cursor needs to be updated
        */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        Uri returnUri;
        switch (match) {
            case INVENTORY:
                _id = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
                if (_id >= 0) {
                    returnUri = ContentUris.withAppendedId(uri,_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert row into: " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=? " ;
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                //For the INVENTORY_ID code, extract the ID from the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not support for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //TODO: Update the selection
        if (values.containsKey(InventoryContract.InventoryEntry.GAME_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.GAME_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a Description");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.GAME_SYSTEM)) {
            int category = values.getAsInteger(InventoryContract.InventoryEntry.GAME_SYSTEM);
            if (!InventoryContract.InventoryEntry.isValidSystem(category)) {
                throw new IllegalArgumentException("Item requires a valid System");
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
