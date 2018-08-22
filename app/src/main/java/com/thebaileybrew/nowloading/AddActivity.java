package com.thebaileybrew.nowloading;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
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

public class AddActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks {
    private static final String TAG = AddActivity.class.getSimpleName();
    private static final int SCAN_LOADER_ID = 1;
    private static final int UPDATE_LOADER_ID = 2;
    private final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    private final String requestUrl = "https://api.upcitemdb.com/prod/trial/lookup";

    private TextInputEditText mGameNameEditText;
    private TextInputEditText mGamePriceEditText;
    private String mSuggestedPrice;

    private Spinner mSystemSpinner;
    private int mSystem = InventoryContract.InventoryEntry.SYSTEM_UNKNOWN;

    private EditText mQuantityEditText;
    private Button mQuantityIncrease;
    private Button mQuantityDecrease;

    private RadioGroup mConditionRadioGroup;
    private RadioButton mConditionPoor;
    private RadioButton mConditionGood;
    private RadioButton mConditionGreat;
    private String mConditionSelected;

    private Boolean allValidData = false;
    private Boolean validGameName = false; private Boolean validGamePrice = false;
    private Boolean validGameQuantity = false; private Boolean validGameCondition = false;
    private Boolean barcodeSearched = false;

    // Flag that signifies if game has been editted (true) or not (false)
    private Boolean mGameDataChanged = false;
    //Field for game details when updating
    private Uri mCurrentGameUri;

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGameDataChanged = true;
            return false;
        }
    };

    private EditText mBarcodeEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Intent updateIntent = getIntent();
        mCurrentGameUri = updateIntent.getData();
        if(mCurrentGameUri == null) {
            setTitle("Add A Game");
            invalidateOptionsMenu();
        } else {
            setTitle("Update This Game");
            barcodeSearched = true;
            //Grab the Loader
            getLoaderManager().initLoader(UPDATE_LOADER_ID, null, this);
        }
        initBarcodeScanning();
        initViews();
        setupSystemSpinner();
        setOnTouchListeners();
    }

    private void setOnTouchListeners() {
        mGameNameEditText.setOnTouchListener(mTouchListener);
        mGamePriceEditText.setOnTouchListener(mTouchListener);
        mQuantityIncrease.setOnTouchListener(mTouchListener);
        mQuantityDecrease.setOnTouchListener(mTouchListener);
        mBarcodeEditText.setOnTouchListener(mTouchListener);
        mConditionPoor.setOnTouchListener(mTouchListener);
        mConditionGood.setOnTouchListener(mTouchListener);
        mConditionGreat.setOnTouchListener(mTouchListener);
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
        mGameNameEditText = findViewById(R.id.inventory_item_add_edit_text);
        mGamePriceEditText = findViewById(R.id.game_price_edit_text);
        mGamePriceEditText.addTextChangedListener(new NumberTextWatcher(mGamePriceEditText, "##.##"));
        mBarcodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                mBarcodeEditText.removeTextChangedListener(this);
                if (text !=null && !text.toString().isEmpty()) {
                    int stringLength;
                    stringLength = mBarcodeEditText.getText().length();
                    if (stringLength == 12) {
                        getDataRefresh();
                    }
                }
                mBarcodeEditText.addTextChangedListener(this);
            }
        });
        mQuantityEditText = findViewById(R.id.inventory_quantity_edit_text);
        mQuantityIncrease = findViewById(R.id.inventory_qty_add);
        mQuantityIncrease.setOnClickListener(this);
        mQuantityDecrease = findViewById(R.id.inventory_qty_minus);
        mQuantityDecrease.setOnClickListener(this);
        mConditionRadioGroup = findViewById(R.id.condition_radio_group);
        mConditionPoor = findViewById(R.id.condition_poor);
        mConditionGood = findViewById(R.id.condition_good);
        mConditionGreat = findViewById(R.id.condition_great);
        mSystemSpinner = findViewById(R.id.inventory_system_spinner);

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
            Toast.makeText(this, "Must enter a valid title...", Toast.LENGTH_SHORT).show();
        } else {
            validGameName = true;
        }

        //Validate pricing & format
        String[] currentSalePrice = mGamePriceEditText.getText().toString().split("$");
        String finalPrice = currentSalePrice[0].trim();
        String pricingPattern = "\\$\\d{1,6}\\.\\d{2}";
        if (mGamePriceEditText.getText().toString().trim().equalsIgnoreCase("")) {
            mGamePriceEditText.setError("Cannot be blank");
        } else if (!String.valueOf(finalPrice).matches(pricingPattern)) {
            mGamePriceEditText.setError("Must be valid price format");
        } else {
            //mSuggestedPrice = getSuggestedPrice(mGamePriceEditText.getText().toString());
            validGamePrice = true;
        }

        //Checks for valid system selection
        if (mSystem == InventoryContract.InventoryEntry.SYSTEM_UNKNOWN) {
            Toast.makeText(this, "Please select a valid Game System", Toast.LENGTH_SHORT).show();
        }

        //Checks for valid quantity
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if (mQuantityEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.must_have_qty, Toast.LENGTH_SHORT).show();
        } else if (mQuantityEditText.getText().toString().equals("0")){
            deleteRecord();
        } else {
            validGameQuantity = true;
        }

        //Checks for valid game condition
        if (!mConditionGood.isChecked() && !mConditionPoor.isChecked() && !mConditionGreat.isChecked()) {
            Toast.makeText(this, "Game condition must be selected", Toast.LENGTH_SHORT).show();
        } else {
            switch (mConditionRadioGroup.getCheckedRadioButtonId()) {
                case R.id.condition_good:
                    mConditionSelected = "GOOD";
                case R.id.condition_poor:
                    mConditionSelected = "POOR";
                case R.id.condition_great:
                    mConditionSelected = "GREAT";
            }
            validGameCondition = true;
        }

        //Checks for all valid data
        if(validGameName && validGamePrice && validGameQuantity && validGameCondition) {
            mSuggestedPrice = getSuggestedPrice(mGamePriceEditText.getText().toString().trim());
            allValidData = true;
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
            values.put(InventoryContract.InventoryEntry.GAME_CONDITION, mConditionSelected);
            values.put(InventoryContract.InventoryEntry.GAME_UPC_CODE, mBarcodeEditText.getText().toString().trim());

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
                    Toast.makeText(this, "Error updating Game Details", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Successfully updated Game Details", Toast.LENGTH_SHORT).show();
                }
            }
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
            menuItem = menu.findItem(R.id.other_details);
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
            case R.id.other_details:
                if (!mGameDataChanged) {
                    seeMoreDetails();
                } else {
                    showUnsavedChangesDialog();
                }
                return true;
            case R.id.home:
                if(!mGameDataChanged) {
                    NavUtils.navigateUpFromSameTask(AddActivity.this);
                    return true;
                } else {
                    showUnsavedChangesDialog();
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
        }
        showUnsavedChangesDialog();
    }

    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder to verify if user wants to keep editing or delete changes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_are_you_sure);
        builder.setPositiveButton(R.string.trash_them, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(AddActivity.this);
            }
        });
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

    private void seeMoreDetails() {
        Intent detailsIntent = new Intent(AddActivity.this, DisplayDetailsActivity.class);
        detailsIntent.setData(mCurrentGameUri);
        startActivity(detailsIntent);
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
                    InventoryContract.InventoryEntry.GAME_CONDITION,
                    InventoryContract.InventoryEntry.GAME_UPC_CODE};
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
                int gameConditionColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_CONDITION);
                int gameUPCColumnIndex = cursorData.getColumnIndex(
                        InventoryContract.InventoryEntry.GAME_UPC_CODE);

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

                String gameCondition = cursorData.getString(gameConditionColumnIndex);
                switch (gameCondition) {
                    case "GOOD":
                        mConditionGood.setChecked(true);
                    case "POOR":
                        mConditionPoor.setChecked(true);
                    case "GREAT":
                        mConditionGreat.setChecked(true);
                }
                String gameUPC = cursorData.getString(gameUPCColumnIndex);
                mBarcodeEditText.setText(gameUPC);
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
                break;
            case R.id.inventory_qty_minus:
                mQuantityEditText.setText(decreaseQuantity());
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
        }
    }

}
