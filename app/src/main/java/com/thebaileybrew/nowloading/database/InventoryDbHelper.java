package com.thebaileybrew.nowloading.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class InventoryDbHelper extends SQLiteOpenHelper {
    public static final String TAG = InventoryDbHelper.class.getSimpleName();

    //Name of the Database
    private static final String DATABASE_NAME = "nowloading.db";

    /*
    Version of current Database
       When Schema is changed - the version MUST be incremented
    */
    private static final int DATABASE_VERSION = 1;

    //Construct the new instance of the DB Helper
    public InventoryDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    //Initial DB creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        addInventoryTable(db);
        addGamesTable(db);
    }

    private void addInventoryTable(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.GAME_NAME + " TEXT NOT NULL, "
                + InventoryContract.InventoryEntry.GAME_SYSTEM + " INTEGER NOT NULL DEFAULT 5, "
                + InventoryContract.InventoryEntry.GAME_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.InventoryEntry.GAME_SALE_PRICE + " REAL, "
                + InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE + " REAL, "
                + InventoryContract.InventoryEntry.GAME_CONDITION + " TEXT, "
                + InventoryContract.InventoryEntry.GAME_UPC_CODE + " TEXT);";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }
    private void addGamesTable(SQLiteDatabase db) {
        String SQL_CREATE_GAME_TABLE = "CREATE TABLE " + InventoryContract.GameEntry.TABLE_NAME + " ("
                + InventoryContract.GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.GameEntry.GAME_NAME + " TEXT NOT NULL, "
                + InventoryContract.GameEntry.GAME_GENRE + " TEXT, "
                + InventoryContract.GameEntry.GAME_SYSTEM + " TEXT NOT NULL, "
                + InventoryContract.GameEntry.GAME_RELEASE_DATE + " TEXT NOT NULL, "
                + InventoryContract.GameEntry.GAME_DEVELOPER + " TEXT NOT NULL, "
                + InventoryContract.GameEntry.GAME_DEV_COUNTRY + " TEXT, "
                + InventoryContract.GameEntry.GAME_DEV_HQ + " TEXT, "
                + InventoryContract.GameEntry.GAME_DEV_ESTB + " TEXT);";
        db.execSQL(SQL_CREATE_GAME_TABLE);
    }

    //Checks for updates to the DB Schema
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            dropInventoryTable(db);
            dropGamesTable(db);
        }
        onCreate(db);
    }

    private void dropGamesTable(SQLiteDatabase db) {
        String sql = "DROP TABLE IF EXISTS " + InventoryContract.GameEntry.TABLE_NAME;
        db.execSQL(sql);
    }

    private void dropInventoryTable(SQLiteDatabase db) {
        String sql = "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;
        db.execSQL(sql);
    }
}
