package com.thebaileybrew.nowloading;

import android.animation.Animator;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thebaileybrew.nowloading.database.InventoryContract;
import com.thebaileybrew.nowloading.database.InventoryCursorAdapter;
import com.thebaileybrew.nowloading.interfaces.onClickInterface;


import java.util.Random;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.VISIBLE;

public class CatalogActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CatalogActivity.class.getSimpleName();

    RelativeLayout noDataLayout;
    private Boolean isFabMenuOpen = false;
    private FloatingActionButton fabMenu;
    private FloatingActionButton fabAddInventory; private LinearLayout fabAddLayout; private TextView fabAddText;
    private FloatingActionButton fabDeleteInventory; private LinearLayout fabDeleteLayout; private TextView fabDeleteText;
    private FloatingActionButton fabViewStats; private LinearLayout fabViewLayout; private TextView fabViewText;
    private boolean isFabViewed = false;
    private Animation animationFadeOut, animationFadeIn, animationFadeOutQuick, animationFadeInQuick;

    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout fadingLayoutForFabs;
    private RecyclerView recyclerView;
    private InventoryCursorAdapter inventoryCursorAdapter;
    private ItemTouchHelper.SimpleCallback itemTouchCallBack;
    private SharedPreferences hasData;
    private SharedPreferences.Editor hasDataEditor;
    private Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        noDataLayout = findViewById(R.id.no_database_data);
        hasData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initFabs();
        if(!hasData.contains("data")) {
            noDataLayout.setVisibility(View.INVISIBLE);
        }
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeOutQuick = AnimationUtils.loadAnimation(this, R.anim.fade_out_quick);
        animationFadeInQuick = AnimationUtils.loadAnimation(this, R.anim.fade_in_quick);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseDetails();
    }

    private void displayDatabaseDetails() {
        String[] projection = {
            InventoryContract.InventoryEntry._ID,
            InventoryContract.InventoryEntry.GAME_NAME,
            InventoryContract.InventoryEntry.GAME_SYSTEM,
            InventoryContract.InventoryEntry.GAME_QUANTITY,
            InventoryContract.InventoryEntry.GAME_SALE_PRICE,
            InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE,
            InventoryContract.InventoryEntry.GAME_SUPPLIER,
            InventoryContract.InventoryEntry.GAME_SUPPLIER_CONTACT,
            InventoryContract.InventoryEntry.GAME_SUPPLIER_EMAIL};
        cursor = getContentResolver().query(
                InventoryContract.InventoryEntry.CONTENT_URI, projection,
                null, null, null, null);
        updateSharedPrefs(cursor);

        /* Checks SharedPreferences to determine if data available
         * This was a fix for returning to Catalog from DetailsDisplay & EditorActivity
         * If SharedPref does contain "data" then animation will stay invisible
         * If SharedPref does NOT contain "data" then animation will fade in and be visible until "data" exists
         */
        if (!hasData.contains("data")) {
            final ImageView bg_image = findViewById(R.id.background_image_empty_layout);
            final Animation bg_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_and_fade);

            bg_image.startAnimation(bg_animation);
            bg_animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    noDataLayout.startAnimation(animationFadeOutQuick);
                    noDataLayout.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            noDataLayout.setVisibility(VISIBLE);
            noDataLayout.startAnimation(animationFadeIn);
            final ImageView bg_image2 = findViewById(R.id.background_image_empty_layout);
            final Animation bg_animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_and_rotate);
            bg_image2.startAnimation(bg_animation2);
        }
        recyclerView = findViewById(R.id.recyclerView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, cursor, new onClickInterface() {
            //Defines what happens on item click
            @Override
            public void onItemClick(View v, int position, int quantity) {
                ContentValues values = new ContentValues();
                final Uri currentGame = ContentUris.withAppendedId(
                        InventoryContract.InventoryEntry.CONTENT_URI, position);
                if (quantity == 0) {
                    Toast.makeText(CatalogActivity.this,
                            "You can't sell what you don't have...", Toast.LENGTH_SHORT).show();
                } else if (quantity == 1) {
                    int newQuantity = quantity - 1;
                    values.put(InventoryContract.InventoryEntry.GAME_QUANTITY, newQuantity);
                    int rowsUpdated = getContentResolver().update(
                            currentGame, values, null, null);
                    if (rowsUpdated != 0) {
                        Toast.makeText(CatalogActivity.this,
                                "Qty updated", Toast.LENGTH_SHORT).show();
                    }
                    inventoryCursorAdapter.notifyDataSetChanged();
                    Snackbar buyMoreSnackBar = Snackbar.make(coordinatorLayout,
                            "Selling Last - Order More?", Snackbar.LENGTH_LONG)
                            .setAction("YES", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent updateIntent = new Intent(CatalogActivity.this, AddActivity.class);
                                    updateIntent.setData(currentGame);
                                    startActivity(updateIntent);
                                }
                            });
                    buyMoreSnackBar.show();
                    displayDatabaseDetails();
                } else {
                    int newQuantity = quantity - 1;
                    values.put(InventoryContract.InventoryEntry.GAME_QUANTITY, newQuantity);
                    int rowsUpdated = getContentResolver().update(
                            currentGame, values,null,null);
                    if (rowsUpdated != 0) {
                        Toast.makeText(CatalogActivity.this,
                                "Qty updated", Toast.LENGTH_SHORT).show();
                        Toast.makeText(CatalogActivity.this,
                                "Button belongs to: " + position, Toast.LENGTH_SHORT).show();
                    }
                    inventoryCursorAdapter.notifyDataSetChanged();
                    displayDatabaseDetails();
                }

            }
            //Defines what happens on long click
            @Override
            public void onLongClick(View v, int position) {
                showOnLongClickDialog(position);
            }

        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(inventoryCursorAdapter);
        //Run the animation loader
        runLayoutAnimation(recyclerView);
    }

    //Declares how to handle the opening animation based on the cursor returned on query
    private void updateSharedPrefs(Cursor cursor) {
        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            hasDataEditor = hasData.edit();
            hasDataEditor.remove("data");
            hasDataEditor.apply();
        } else {
            hasDataEditor = hasData.edit();
            hasDataEditor.putString("data", "data");
            hasDataEditor.apply();
        }
    }

    private void runLayoutAnimation(RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_from_bottom);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    /*
    * Initialize all Floating Action Buttons & Menu Layouts
    * Set onClickListeners to all FloatingAction Buttons
    */
    private void initFabs() {
        //Intitialize add FABS
        fabAddInventory = findViewById(R.id.inventory_add);
        fabAddLayout = findViewById(R.id.add_layout);
        fabAddText = findViewById(R.id.inventory_add_text);
        //Initialize delete FABS
        fabDeleteInventory = findViewById(R.id.delete_all);
        fabDeleteLayout = findViewById(R.id.delete_all_layout);
        fabDeleteText = findViewById(R.id.delete_all_text);
        //Initialize stats FABS
        fabViewStats = findViewById(R.id.stats_fab);
        fabViewLayout = findViewById(R.id.stats_layout);
        fabViewText = findViewById(R.id.inventory_stats_text);
        //Initialize  menu FAB
        fabMenu = findViewById(R.id.fab_menu);
        //Initialize the fade view
        fadingLayoutForFabs = findViewById(R.id.fade_layout);

        //Declare onClicks
        fabAddInventory.setOnClickListener(this);
        fabDeleteInventory.setOnClickListener(this);
        fabViewStats.setOnClickListener(this);
        fabMenu.setOnClickListener(this);
    }

    /*
    * Show and Hide the Secret FAB Menu
    */
    private void showFABMenu() {
        isFabMenuOpen = true;
        //On Menu open - make layouts visible
        fabAddLayout.setVisibility(VISIBLE);
        fabViewLayout.setVisibility(VISIBLE);
        fabDeleteLayout.setVisibility(VISIBLE);
        //Animate FABS - spin main, and translate x/y for other layouts
        fabMenu.animate().rotationBy(270);
        fabAddLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_140));
        fabDeleteLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_70))
                .translationX(-getResources().getDimension(R.dimen.standard_45));
        fabViewLayout.animate().translationX(-getResources().getDimension(R.dimen.standard_90)).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabAddText.setVisibility(View.INVISIBLE);
                fabDeleteText.setVisibility(View.INVISIBLE);
                fabViewText.setVisibility(View.INVISIBLE);
            }
            //Set the END animation for FAB layouts
            @Override
            public void onAnimationEnd(Animator animation) {
                fadingLayoutForFabs.setVisibility(VISIBLE);
                if (!isFabViewed) {
                    //Fade text in (anim) on first run
                    fabAddText.setAnimation(animationFadeIn);
                    fabViewText.setAnimation(animationFadeIn);
                    fabDeleteText.setAnimation(animationFadeIn);
                    fabAddText.setVisibility(VISIBLE);
                    fabDeleteText.setVisibility(VISIBLE);
                    fabViewText.setVisibility(VISIBLE);
                    //Fade text out (anim) on first run
                    fabAddText.setAnimation(animationFadeOut);
                    fabViewText.setAnimation(animationFadeOut);
                    fabDeleteText.setAnimation(animationFadeOut);
                    fabAddText.setVisibility(View.INVISIBLE);
                    fabDeleteText.setVisibility(View.INVISIBLE);
                    fabViewText.setVisibility(View.INVISIBLE);
                } else {
                    isFabViewed = true;
                    fabAddText.setVisibility(View.INVISIBLE);
                    fabDeleteText.setVisibility(View.INVISIBLE);
                    fabViewText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

    }
    private void closeFABMenu() {
        isFabMenuOpen = false;
        //Animate FABS - spin all, and translate x/y for other layouts
        fabMenu.animate().rotationBy(-270);
        fabAddInventory.animate().rotationBy(-360);
        fabDeleteInventory.animate().rotationBy(-360);
        fabViewStats.animate().rotationBy(-360);
        fabAddLayout.animate().translationY(0);
        fabDeleteLayout.animate().translationY(0).translationX(0);
        fabViewLayout.animate().translationX(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabAddText.setVisibility(View.INVISIBLE);
                fabDeleteText.setVisibility(View.INVISIBLE);
                fabViewText.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isFabMenuOpen) {
                    fadingLayoutForFabs.setVisibility(View.GONE);
                    fabAddLayout.setVisibility(View.GONE);
                    fabDeleteLayout.setVisibility(View.GONE);
                    fabViewLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    //Menu creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    //Menu option selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy:
                insertDummyGame();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Delete All Inventory method call
    */
    private void deleteAllInventory() {
        getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI,null,null);
        recyclerView.getAdapter().notifyDataSetChanged();
        displayDatabaseDetails();
    }

    /*
    * Insert dummy data based on random number generation, and String[] collection
    */
    private void insertDummyGame() {
        String[] gameNames = {"Call of Duty 4: Modern Warfare",
                "Uncharted 3: Drake's Deception",
                "Meikyuu Touro Legasista",
                "Tom Clancy's Ghost Recon Wildlands",
                "Final Fantasy XIV",
                "Unreal Tournament 3",
                "Pokken Tournament DX",
                "Skate 2",
                "Draw a Stickman: Epic",
                "Super Smash Bros. Ultimate",
                "Donkey Kong Country: Tropical Freeze",
                "Teenage Mutant Ninja Turtles"};
        String[] gamePrices = {"$14.44", "$31.16", "$191.23", "$20.14", "$48.32", "$10.09",
                "$79.01", "$321.19", "$1.14", "$3.99", "$42.55", "$500.00"};
        int[] gameSystems = {InventoryContract.InventoryEntry.SYSTEM_N3DS, InventoryContract.InventoryEntry.SYSTEM_NSWITCH,
                InventoryContract.InventoryEntry.SYSTEM_PS4, InventoryContract.InventoryEntry.SYSTEM_XBOXONE,
                InventoryContract.InventoryEntry.SYSTEM_PS4, InventoryContract.InventoryEntry.SYSTEM_PS3,
                InventoryContract.InventoryEntry.SYSTEM_N3DS, InventoryContract.InventoryEntry.SYSTEM_NSWITCH,
                InventoryContract.InventoryEntry.SYSTEM_PS4, InventoryContract.InventoryEntry.SYSTEM_XBOXONE,
                InventoryContract.InventoryEntry.SYSTEM_PS4, InventoryContract.InventoryEntry.SYSTEM_PS3};
        Random r2 = new Random(); Random r3 = new Random(); Random r4 = new Random();
        Random r5 = new Random(); Random r6 = new Random();
        int selection2 = r2.nextInt(12 - 1) + 1;
        int selection3 = r3.nextInt(12 - 1) + 1;
        int selection4 = r4.nextInt(12 - 1) + 1;
        int selection5 = r5.nextInt(12 - 1) + 1;
        int selection6 = r6.nextInt(12 - 1) + 1;
        String gameSupplierName = "Amazon.com";
        String gameSupplierPhone = "1-888-280-4331";
        String gameSupplierEmail = "amazon_help@amazon.com";
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.GAME_NAME, gameNames[selection2].toUpperCase());
        values.put(InventoryContract.InventoryEntry.GAME_SALE_PRICE, gamePrices[selection3]);
        values.put(InventoryContract.InventoryEntry.GAME_SYSTEM, gameSystems[selection5]);
        values.put(InventoryContract.InventoryEntry.GAME_QUANTITY, selection4);
        values.put(InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE,gamePrices[selection6]);
        values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER, gameSupplierName);
        values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER_EMAIL, gameSupplierEmail);
        values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER_CONTACT, gameSupplierPhone);
        //Insert data and update recycler
        getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
        recyclerView.getAdapter().notifyDataSetChanged();
        displayDatabaseDetails();
    }

    /*
    Alert Dialog Setup
       For deleting ALL items via FAB or Menu selection
     */
    private void showDeleteAllConfirmationDialog() {
        //Show an AlertDialog.Builder to set message and clickListeners
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Are you sure you want to delete all game inventory?");
        dialogBuilder.setNegativeButton(R.string.delete_them, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllInventory();
            }
        });
        dialogBuilder.setPositiveButton(R.string.save_them, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /*
    Alert Dialog Setup - OnLongClick
       For prompt to decide what to do with a specific list item selection
     */
    private void showOnLongClickDialog(int value) {
        final Uri currentGame = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, value);

        final AlertDialog.Builder longClickBuilder = new AlertDialog.Builder(this);
        longClickBuilder.setMessage("What would you like to do with this item?");
        longClickBuilder.setPositiveButton("Update It", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fabMenu.setAnimation(animationFadeOutQuick);
                Intent updateIntent = new Intent(CatalogActivity.this, AddActivity.class);
                updateIntent.setData(currentGame);
                startActivity(updateIntent);
            }
        });
        longClickBuilder.setNeutralButton("Delete this item", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(currentGame,null,null);
                displayDatabaseDetails();
            }
        });
        AlertDialog longClick = longClickBuilder.create();
        longClick.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_menu:
                if(!isFabMenuOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
                break;
            case R.id.inventory_add:
                Intent addDevice = new Intent(CatalogActivity.this, AddActivity.class);
                if(isFabMenuOpen) { closeFABMenu(); }
                startActivity(addDevice);
                break;
            case R.id.delete_all:
                if(isFabMenuOpen) { closeFABMenu(); }
                showDeleteAllConfirmationDialog();
                break;
            case R.id.stats_fab:
                if(isFabMenuOpen) { closeFABMenu(); }
                break;
        }
    }
}
