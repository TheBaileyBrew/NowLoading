package com.thebaileybrew.nowloading;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.thebaileybrew.nowloading.customobjects.NumberTextWatcher;
import com.thebaileybrew.nowloading.database.InventoryContract;

import com.thebaileybrew.nowloading.queryelements.upcquery.upc;
import com.thebaileybrew.nowloading.queryelements.upcquery.upcLoader;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import static android.view.View.VISIBLE;

public class AddActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks {
    private static final String TAG = AddActivity.class.getSimpleName();
    private static final int SCAN_LOADER_ID = 1;
    private static final int UPDATE_LOADER_ID = 2;
    private final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    private final String requestUrl = "https://api.upcitemdb.com/prod/trial/lookup";

    private TextInputEditText mGameNameEditText, mGamePriceEditText, mQuantityEditText;
    private TextInputEditText mSupplierNameEditText, mSupplierPhoneEditText, mSupplierEmailEditText;
    private TextInputLayout mGameNameLayout, mGamePriceLayout, mQuantityLayout;
    private TextInputLayout mSupplierNameLayout, mSupplierPhoneLayout, mSupplierEmailLayout;
    private String mSuggestedPrice;

    private FloatingActionButton fabMenuClicker, fabCallClicker, fabEmailClicker;
    private LinearLayout fabCallLayout, fabEmailLayout;
    private TextView fabCallText, fabEmailText;

    private Spinner mSystemSpinner;
    private int mSystem = InventoryContract.InventoryEntry.SYSTEM_UNKNOWN;

    private TextView soldOutText;

    private Button mQuantityIncrease, mQuantityDecrease;

    private Boolean allValidData = false;
    private Boolean validGameName = false; private Boolean validGamePrice = false;
    private Boolean validGameQuantity = false; private Boolean validGameCondition = false;
    private Boolean validSupplier = false; private Boolean validSupplierPhoneEmail = false;
    private Boolean emailSupplied = false; private Boolean phoneSupplied = false;
    private Boolean barcodeSearched = false;
    private Boolean isFabOpen = false;

    private Animation animationFadeIn;

    // Flag that signifies if game has been editted (true) or not (false)
    private Boolean mGameDataChanged = false;
    //Field for game details when updating
    private Uri mCurrentGameUri;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mGameDataChanged = true;
            Log.e(TAG, "onTextChanged: Something has changed...");
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private EditText mBarcodeEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Intent updateIntent = getIntent();
        mCurrentGameUri = updateIntent.getData();
        initBarcodeScanning();
        initViews();
        setupSystemSpinner();
        setOnTextChangeListeners();
        if(mCurrentGameUri == null) {
            setTitle(getString(R.string.add_a_game));
            invalidateOptionsMenu();
            hideFAB();
        } else {
            setTitle(getString(R.string.update_a_game));
            showFAB();
            barcodeSearched = true;
            //Grab the Loader
            getLoaderManager().initLoader(UPDATE_LOADER_ID, null, this);
        }

    }

    private void hideFAB() {
        fabMenuClicker.setVisibility(View.INVISIBLE);
    }
    private void showFAB() {
        fabMenuClicker.setAnimation(animationFadeIn);
        fabMenuClicker.setVisibility(VISIBLE);
    }

    private void setOnTextChangeListeners() {
        //Listen for Text Changes to Game Name
        mGameNameEditText.addTextChangedListener(mTextWatcher);
        //Listen for Text Changes to Game Price
        mGamePriceEditText.addTextChangedListener(mTextWatcher);
        //Listen for Text Changes to Game Quantity
        mQuantityEditText.addTextChangedListener(mTextWatcher);
        //Listen for Text Changes to Game Supplier
        mSupplierNameEditText.addTextChangedListener(mTextWatcher);
        //Listen for Text Changes to Game Supplier Phone
        mSupplierPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSupplierPhoneEditText.getText().toString().trim().isEmpty()) {
                    phoneSupplied = false;
                } else {
                    phoneSupplied = true;
                }
                mGameDataChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //Listen for Text Changes to Game Supplier Email
        mSupplierEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mSupplierEmailEditText.getText().toString().trim().isEmpty()) {
                    emailSupplied = false;
                } else {
                    emailSupplied = true;
                }
                mGameDataChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //Listen for Text Changes to Game Barcode
        mBarcodeEditText.addTextChangedListener(mTextWatcher);
    }

    /*
    Set up the barcode scanning buttons/views
       Initialize the onClick for Scanning
     */
    private void initBarcodeScanning() {
        ImageButton scanBarcodeButton = findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(this);
        mBarcodeEditText = findViewById(R.id.barcode_entry_text);
    }

    /*
    Gathers the data from barcode scanning
        Takes the contents of the scan result and sets the EditText to match
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.e(TAG, "onActivityResult: " + resultCode + " ::: request: " + requestCode);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            mBarcodeEditText.setText(scanContent);
        }
        checkForNetwork();
    }

    /*
    Checks for a valid network connection
     */
    private void checkForNetwork() {
        if (isNetworkAvailable()) {
            getDataRefresh();
        }
    }
    private boolean isNetworkAvailable(){
        ConnectivityManager connectionCheck = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionCheck.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void getDataRefresh() {
        Log.e(TAG, "getDataRefresh: ");
        if(!barcodeSearched) {
            getLoaderManager().destroyLoader(SCAN_LOADER_ID);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(SCAN_LOADER_ID, null, this);
        }
    }

    /*
    Set up the spinner for system selection
     */
    private void setupSystemSpinner() {
        ArrayAdapter systemAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_system_types, android.R.layout.simple_spinner_dropdown_item);
        systemAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSystemSpinner.setAdapter(systemAdapter);

        mSystemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if(!TextUtils.isEmpty(selectedItem)) {
                    if(selectedItem.equals(getString(R.string.sps3))) {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_PS3;
                    } else if (selectedItem.equals(getString(R.string.sps4))) {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_PS4;
                    } else if (selectedItem.equals(getString(R.string.mxbox))) {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_XBOXONE;
                    } else if (selectedItem.equals(getString(R.string.n3ds))) {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_N3DS;
                    } else if (selectedItem.equals(getString(R.string.nswitch))) {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_NSWITCH;
                    } else {
                        mSystem = InventoryContract.InventoryEntry.SYSTEM_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /*
    Initialize all other views
       Set up the TextChangeListener for GamePrice to format with $
       Set up the TextChangeListener for Barcode to validate if length is correct
     */
    private void initViews() {
        soldOutText = findViewById(R.id.sold_out);
        mGameNameEditText = findViewById(R.id.game_name_edit_text);
        mGamePriceEditText = findViewById(R.id.game_price_edit_text);
        mGamePriceEditText.addTextChangedListener(new NumberTextWatcher(mGamePriceEditText, "##.##"));
        mQuantityEditText = findViewById(R.id.game_quantity_edit_text);
        mQuantityIncrease = findViewById(R.id.inventory_qty_add);
        mQuantityIncrease.setOnClickListener(this);
        mQuantityDecrease = findViewById(R.id.inventory_qty_minus);
        mQuantityDecrease.setOnClickListener(this);
        mSystemSpinner = findViewById(R.id.inventory_system_spinner);
        mGameNameLayout = findViewById(R.id.game_name_layout);
        mGamePriceLayout = findViewById(R.id.game_price_layout);
        mQuantityLayout = findViewById(R.id.game_quantity_layout);
        mSupplierEmailEditText = findViewById(R.id.supplier_email_entry);
        mSupplierEmailLayout = findViewById(R.id.supplier_email_layout);
        mSupplierNameEditText = findViewById(R.id.supplier_name_entry);
        mSupplierNameLayout = findViewById(R.id.supplier_name_layout);
        mSupplierPhoneEditText = findViewById(R.id.supplier_phone_entry);
        mSupplierPhoneLayout = findViewById(R.id.supplier_phone_layout);
        fabMenuClicker = findViewById(R.id.fab_menu);
        fabCallClicker = findViewById(R.id.call_supplier_fab);
        fabEmailClicker = findViewById(R.id.email_supplier_fab);
        fabCallLayout = findViewById(R.id.call_supplier);
        fabEmailLayout = findViewById(R.id.email_supplier);
        fabCallText = findViewById(R.id.call_supplier_text);
        fabEmailText = findViewById(R.id.email_supplier_text);
        fabMenuClicker.setOnClickListener(this);
        fabEmailClicker.setOnClickListener(this);
        fabCallClicker.setOnClickListener(this);
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

    }

    /*
    * Insert items into inventory and validate all data before reaching ContentProvider
    */
    private void insertInventory() {
        //Read data from EditText Inputs and add to inventory table
        String gamePrice = mGamePriceEditText.getText().toString().trim();
        String gameQuantity = mQuantityEditText.getText().toString().trim();
        String currentGameName = mGameNameEditText.getText().toString().toUpperCase().trim();
        if(mGameNameEditText.getText().toString().trim().isEmpty()) {
            mGameNameLayout.setError("Must have a game name");
            validGameName = false;
        } else {
            validGameName = true;
        }
        //Validate pricing & format
        String[] currentSalePrice = mGamePriceEditText.getText().toString().split("$");
        String finalPrice = currentSalePrice[0].trim();
        String pricingPattern = "\\$\\d{1,6}\\.\\d{2}";
        if (mGamePriceEditText.getText().toString().trim().equalsIgnoreCase("") || mGamePriceEditText.getText().toString().isEmpty()) {
            mGamePriceLayout.setError("Price cannot be blank");
            validGamePrice = false;
        } else if (!String.valueOf(mGamePriceEditText.getText().toString().trim()).matches(pricingPattern)) {
            gamePrice = gamePrice + ".00";
            validGamePrice = true;
        } else if (String.valueOf(mGamePriceEditText.getText().toString().trim()).equals("$0.00")){
            mGamePriceLayout.setError("Price cannot be $0.00");
            validGamePrice = false;
        }else {
            validGamePrice = true;
        }
        //Checks for valid system selection
        if (mSystem == InventoryContract.InventoryEntry.SYSTEM_UNKNOWN) {
            Toast.makeText(this, R.string.valid_system, Toast.LENGTH_SHORT).show();
        }
        //Checks for valid quantity
        if (mQuantityEditText.getText().toString().trim().equals("") || mQuantityEditText.getText().toString().isEmpty()) {
            mQuantityLayout.setError(getString(R.string.cant_be_blank));
            validGameQuantity = false;
        } else {
            int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            if (quantity == 0){
                mQuantityLayout.setError(getString(R.string.now_sold_out));
                validGameQuantity = true;
            } else {
                validGameQuantity = true;
            }
        }
        if (mQuantityEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.must_have_qty, Toast.LENGTH_SHORT).show();
            validGameQuantity = false;
        }
        //Check for Valid Supplier
        if(mSupplierNameEditText.getText().toString().trim().isEmpty()) {
            mSupplierNameLayout.setError(getString(R.string.provide_a_supplier));
            validSupplier = false;
        } else {
            validSupplier = true;
        }

        //Check for valid Supplier Contact Details
        if(mSupplierPhoneEditText.getText().toString().trim().isEmpty() && mSupplierEmailEditText.getText().toString().trim().isEmpty()) {
            mSupplierPhoneLayout.setError(getString(R.string.must_have_phone_or_email));
            mSupplierEmailLayout.setError(getString(R.string.must_have_phone_or_email));
            validSupplierPhoneEmail = false;
        } else {
            if(mSupplierPhoneEditText.getText().toString().trim().isEmpty()) {
                emailSupplied = true;
            } else {
                phoneSupplied = true;
            }
            validSupplierPhoneEmail = true;
        }

        //Checks for all valid data
        if(validGameName && validGamePrice && validGameQuantity && validSupplier && validSupplierPhoneEmail) {
            mSuggestedPrice = getSuggestedPrice(mGamePriceEditText.getText().toString().trim());
            allValidData = true;
        } else {
            allValidData = false;
        }

        /*
        * Create ContentValues and add details from entry
        */
        if (allValidData) {
            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.GAME_NAME, currentGameName);
            values.put(InventoryContract.InventoryEntry.GAME_SALE_PRICE, gamePrice);
            values.put(InventoryContract.InventoryEntry.GAME_SYSTEM, mSystem);
            values.put(InventoryContract.InventoryEntry.GAME_QUANTITY, gameQuantity);
            values.put(InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE,mSuggestedPrice);
            values.put(InventoryContract.InventoryEntry.GAME_UPC_CODE, mBarcodeEditText.getText().toString().trim());
            values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER, mSupplierNameEditText.getText().toString().trim());
            values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER_CONTACT, mSupplierPhoneEditText.getText().toString().trim());
            values.put(InventoryContract.InventoryEntry.GAME_SUPPLIER_EMAIL, mSupplierEmailEditText.getText().toString().trim());

            //Insert item into database through ContentProvider
            if (mCurrentGameUri == null) {
                Uri newGameUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
                if (newGameUri == null) {
                    Toast.makeText(this, getString(R.string.editor_failed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_success), Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsUpdated = getContentResolver().update(mCurrentGameUri, values,null, null);
                //Show message depending on if rows were updated
                if (rowsUpdated == 0) {
                    Toast.makeText(this, R.string.to_err_is_human, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.success_is_not_guaranteed, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, R.string.yah_missed_sumfin, Toast.LENGTH_SHORT).show();
        }
    }

    //Calculate suggested price based on system selection
    private String getSuggestedPrice(String trimPrice) {
        String finalPrice =trimPrice.replace("$","");
        Log.e(TAG, "getSuggestedPrice: final price is " + finalPrice);
        switch(mSystem) {
            case InventoryContract.InventoryEntry.SYSTEM_PS3:
                return format.format(Double.parseDouble(finalPrice) * 1.245);
            case InventoryContract.InventoryEntry.SYSTEM_PS4:
                return format.format(Double.parseDouble(finalPrice) * 1.802);
            case InventoryContract.InventoryEntry.SYSTEM_XBOXONE:
                return format.format(Double.parseDouble(finalPrice) * 1.802);
            case InventoryContract.InventoryEntry.SYSTEM_N3DS:
                return format.format(Double.parseDouble(finalPrice) * 1.612);
            case InventoryContract.InventoryEntry.SYSTEM_NSWITCH:
                return format.format(Double.parseDouble(finalPrice) * 1.802);
            default:
                return null;
        }
    }

    public void showFABMenu() {
        isFabOpen = true;
        //On menu open - make layouts visible
        fabCallLayout.setVisibility(VISIBLE);
        fabEmailLayout.setVisibility(VISIBLE);
        fabMenuClicker.animate().rotationBy(315);
        fabCallLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_140));
        fabEmailLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_70))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if(!phoneSupplied) {
                            fabCallClicker.setBackgroundColor(getResources().getColor(R.color.colorAccentFadeOut));
                            fabCallClicker.setImageResource(R.drawable.ic_blocked_phone);
                            fabCallText.setText(R.string.supplier_phone_unav);
                            fabCallClicker.setClickable(false);
                        } else {
                            fabCallClicker.setImageResource(R.drawable.ic_phone);
                            fabCallText.setText(R.string.call_supplier);
                            fabCallClicker.setClickable(true);
                        }
                        if(!emailSupplied) {
                            fabEmailClicker.setBackgroundColor(getResources().getColor(R.color.colorAccentFadeOut));
                            fabEmailClicker.setImageResource(R.drawable.ic_blocked_email);
                            fabEmailText.setText(R.string.supplier_email_unav);
                            fabEmailClicker.setClickable(false);
                        } else {
                            fabEmailClicker.setImageResource(R.drawable.ic_email);
                            fabEmailText.setText(R.string.email_supplier);
                            fabEmailClicker.setClickable(true);
                        }
                        fabEmailText.setVisibility(View.INVISIBLE);
                        fabCallText.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fabCallText.setAnimation(animationFadeIn);
                        fabEmailText.setAnimation(animationFadeIn);
                        fabCallText.setVisibility(VISIBLE);
                        fabEmailText.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }
        );
    }
    public void hideFABMenu() {
        isFabOpen = false;
        //Animate FABS back to standard
        fabMenuClicker.animate().rotationBy(-315);
        fabCallLayout.animate().translationY(0);
        fabEmailLayout.animate().translationY(0).setListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        fabEmailText.setVisibility(View.INVISIBLE);
                        fabCallText.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!isFabOpen) {
                            fabCallLayout.setVisibility(View.INVISIBLE);
                            fabEmailLayout.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }
        );

    }

    //Create Menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the editor menu
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //If this is a NEW game, hide the DELETE and OTHER DETAILS menu items.
        if (mCurrentGameUri == null) {
            MenuItem menuItem;
            menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //Respond to the various menu options
            case R.id.action_save:
                insertInventory();
                if (allValidData) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                deleteRecord();
                return true;
            case R.id.home:
                if(!mGameDataChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                } else {
                    DialogInterface.OnClickListener discardChanges = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavUtils.navigateUpFromSameTask(AddActivity.this);
                        }
                    };
                    showDialogInterface(discardChanges);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mGameDataChanged) {
            super.onBackPressed();
            return;
        } else {
            DialogInterface.OnClickListener discardChanges = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };
            showDialogInterface(discardChanges);
        }
    }

    private void showDialogInterface(DialogInterface.OnClickListener discardChangesDialog) {
        // Create an AlertDialog.Builder to verify if user wants to keep editing or delete changes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_are_you_sure);
        builder.setPositiveButton(R.string.trash_them, discardChangesDialog);
        builder.setNegativeButton(R.string.keep_them, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.delete_it, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteSingleGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteSingleGame() {
        if (mCurrentGameUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentGameUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.error_delete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.success_delete, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /*
    * Increase quantity on hand and validate that quantity does not equal zero or above 1000
    */
    private String increaseQuantity() {
        int currentQuantity;
        String currentValue = mQuantityEditText.getText().toString();
        if (currentValue.equalsIgnoreCase("")) {
            currentQuantity = 0;
        } else {
            currentQuantity = Integer.parseInt(currentValue);
        }
        currentQuantity = currentQuantity + 1;
        if (currentQuantity >= 1000) {
            Toast.makeText(this, R.string.that_escalated_quickly, Toast.LENGTH_SHORT).show();
        }
        return String.valueOf(currentQuantity);
    }

    /*
    * Decrease quantity on hand and validate that quantity does not equal zero
    */
    private String decreaseQuantity() {
        int currentQuantity;
        String currentValue = mQuantityEditText.getText().toString();
        currentQuantity = Integer.parseInt(currentValue);
        if (currentQuantity == 1) {
            Toast.makeText(this, R.string.quantity_zero, Toast.LENGTH_SHORT).show();
            soldOutText.setVisibility(VISIBLE);
        } else {
            soldOutText.setVisibility(View.INVISIBLE);
        }
        currentQuantity = currentQuantity - 1;
        return String.valueOf(currentQuantity);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == SCAN_LOADER_ID) {
            String searchQuery = mBarcodeEditText.getText().toString();
            Uri baseUri = Uri.parse(requestUrl);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("upc", searchQuery);
            barcodeSearched = true;
            return new upcLoader(this, uriBuilder.toString());
        } else if (id == UPDATE_LOADER_ID){
            String[] projection = {
                    InventoryContract.InventoryEntry._ID,
                    InventoryContract.InventoryEntry.GAME_NAME,
                    InventoryContract.InventoryEntry.GAME_SYSTEM,
                    InventoryContract.InventoryEntry.GAME_QUANTITY,
                    InventoryContract.InventoryEntry.GAME_SALE_PRICE,
                    InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE,
                    InventoryContract.InventoryEntry.GAME_UPC_CODE,
                    InventoryContract.InventoryEntry.GAME_SUPPLIER,
                    InventoryContract.InventoryEntry.GAME_SUPPLIER_CONTACT,
                    InventoryContract.InventoryEntry.GAME_SUPPLIER_EMAIL};
            return new CursorLoader(this, mCurrentGameUri, projection, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int id = loader.getId();
        if (id == SCAN_LOADER_ID) {
            List<upc> newData;
            if (data instanceof List) {
               newData = (List<upc>) data;
            } else {
                newData = null;
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Cannot query network", Toast.LENGTH_SHORT).show();
            } else {
                if (newData != null) {
                    upc currentGame = newData.get(0);
                    String gameNameDetails = currentGame.getUpcGameName();
                    String gameUPCDetails = currentGame.getUpcCode();
                    String gameImageDetails = currentGame.getUpcGameImage();
                    String gamePriceDetails = currentGame.getUpcGameLowPrice();
                    mGameNameEditText.setText(gameNameDetails);
                    mBarcodeEditText.setText(gameUPCDetails);
                    mGamePriceEditText.setText(gamePriceDetails);
                    mQuantityEditText.setText(increaseQuantity());
                } else {
                    Toast.makeText(this, "Barcode returns zero results", Toast.LENGTH_SHORT).show();
                    mBarcodeEditText.setText("");
                }
            }
        } else if (id == UPDATE_LOADER_ID) {
            Cursor cursorData = (Cursor) data;
            if (cursorData == null || cursorData.getCount() < 1){
                return;
            }
            if (cursorData.moveToFirst()) {
                int gameNameColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_NAME);
                int gameSystemColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_SYSTEM);
                int gameQuantityColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_QUANTITY);
                int gamePriceColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_SALE_PRICE);
                int gameUPCColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_UPC_CODE);
                int gameSupplierIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_SUPPLIER);
                int gameSupplierPhoneIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_SUPPLIER_CONTACT);
                int gameSupplierEmailIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_SUPPLIER_EMAIL);

                String gameName = cursorData.getString(gameNameColumnIndex);
                mGameNameEditText.setText(gameName);
                int gameSystem = cursorData.getInt(gameSystemColumnIndex);
                switch (gameSystem) {
                    case InventoryContract.InventoryEntry.SYSTEM_PS3:
                        mSystemSpinner.setSelection(0);
                        break;
                    case InventoryContract.InventoryEntry.SYSTEM_PS4:
                        mSystemSpinner.setSelection(1);
                        break;
                    case InventoryContract.InventoryEntry.SYSTEM_XBOXONE:
                        mSystemSpinner.setSelection(2);
                        break;
                    case InventoryContract.InventoryEntry.SYSTEM_N3DS:
                        mSystemSpinner.setSelection(3);
                        break;
                    case InventoryContract.InventoryEntry.SYSTEM_NSWITCH:
                        mSystemSpinner.setSelection(4);
                        break;
                }
                String gameQuantity = cursorData.getString(gameQuantityColumnIndex);
                mQuantityEditText.setText(gameQuantity);
                String gamePrice = cursorData.getString(gamePriceColumnIndex);
                mGamePriceEditText.setText(gamePrice);
                String gameUPC = cursorData.getString(gameUPCColumnIndex);
                mBarcodeEditText.setText(gameUPC);
                String gameSupplier = cursorData.getString(gameSupplierIndex);
                mSupplierNameEditText.setText(gameSupplier);
                String gameSupplierPhone = cursorData.getString(gameSupplierPhoneIndex);
                if (gameSupplierPhone.equals("null")){
                    mSupplierPhoneEditText.setText("");
                } else {
                    mSupplierPhoneEditText.setText(gameSupplierPhone);
                }
                String gameSupplieEmail = cursorData.getString(gameSupplierEmailIndex);
                if (gameSupplieEmail.equals("null")) {
                    mSupplierEmailEditText.setText("");
                } else {
                    mSupplierEmailEditText.setText(gameSupplieEmail);
                }
            }
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.inventory_qty_add:
                mQuantityEditText.setText(increaseQuantity());
                soldOutText.setVisibility(View.INVISIBLE);
                break;
            case R.id.inventory_qty_minus:
                if(Integer.parseInt(mQuantityEditText.getText().toString()) == 0) {
                    Toast.makeText(this, "Cannot have a negative quantity on-hand", Toast.LENGTH_SHORT).show();
                    soldOutText.setVisibility(VISIBLE);
                } else {
                    mQuantityEditText.setText(decreaseQuantity());
                }
                break;
            case R.id.scan_barcode_button:
                if (!isNetworkAvailable()) {
                    Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
                } else if (!mBarcodeEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.already_have_barcode, Toast.LENGTH_SHORT).show();
                } else {
                    IntentIntegrator scanIntegration = new IntentIntegrator(this);
                    scanIntegration.initiateScan();
                }
                break;
            case R.id.fab_menu:
                if(!isFabOpen) {
                    showFABMenu();
                } else {
                    hideFABMenu();
                }
                break;
            case R.id.call_supplier_fab:
                if(isFabOpen) {
                    hideFABMenu();
                    if (phoneSupplied) {
                        Intent callSupplierForMore = new Intent(Intent.ACTION_DIAL);
                        String uri = "tel:" + mSupplierPhoneEditText.getText().toString().trim();
                        callSupplierForMore.setData(Uri.parse(uri));
                        startActivity(callSupplierForMore);
                    }
                }
                break;
            case R.id.email_supplier_fab:
                if(isFabOpen) {
                    hideFABMenu();
                    if(emailSupplied) {
                        Intent emailSupplierForMore = new Intent(Intent.ACTION_SEND);
                        emailSupplierForMore.setType("text/html");
                        emailSupplierForMore.putExtra(Intent.EXTRA_EMAIL, mSupplierEmailEditText.getText().toString());
                        emailSupplierForMore.putExtra(Intent.EXTRA_SUBJECT, "Order More: " + mGameNameEditText.getText().toString().trim());
                        startActivity(Intent.createChooser(emailSupplierForMore, "Send Email"));
                    }
                }
                break;
        }
    }

}
