package com.thebaileybrew.nowloading.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract() {}

    //Constant Providers
    public static final String CONTENT_AUTHORITY = "com.thebaileybrew.nowloading";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path Providers
    public static final String PATH_INVENTORY = "inventorypath";

    //InventoryEntry Builder
    public static final class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        public final static String TABLE_NAME = "disks";
        public final static String _ID = BaseColumns._ID;
        public final static String GAME_NAME = "game";
        public final static String GAME_SYSTEM = "system";
        public final static String GAME_QUANTITY = "quantity";
        public final static String GAME_SALE_PRICE = "saleprice";
        public final static String GAME_SUGGESTED_PRICE = "suggested";
        public final static String GAME_UPC_CODE = "barcode";
        public final static String GAME_SUPPLIER = "supplier";
        public final static String GAME_SUPPLIER_CONTACT = "contact";
        public final static String GAME_SUPPLIER_EMAIL = "email";

        //MIME Type
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Potential Category Fields
        public static final int SYSTEM_PS3 = 0;
        public static final int SYSTEM_PS4 = 1;
        public static final int SYSTEM_XBOXONE = 2;
        public static final int SYSTEM_N3DS = 3;
        public static final int SYSTEM_NSWITCH = 4;
        public static final int SYSTEM_UNKNOWN = 5;

        public static boolean isValidSystem(int system) {
            return system == SYSTEM_PS3
                    || system == SYSTEM_PS4
                    || system == SYSTEM_XBOXONE
                    || system == SYSTEM_N3DS
                    || system == SYSTEM_NSWITCH;
        }
    }
}
