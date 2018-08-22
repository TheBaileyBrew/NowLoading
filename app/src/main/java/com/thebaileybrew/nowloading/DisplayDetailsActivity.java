package com.thebaileybrew.nowloading;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.thebaileybrew.nowloading.database.InventoryContract;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView gameNameText;
    private TextView gameSystemText;
    private TextView gameGenreText;
    private TextView gameReleaseText;
    private TextView gameQuantityText;
    private TextView gameSuggestedText;
    private TextView gameDeveloperText;
    private TextView gameDevHomeText;
    private TextView gameDevEstText;
    private TextView gameDevPhoneText;
    private Uri mCurrentGameUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initViews();
        //Get Intent Data from parent activities
        Intent displayIntent = getIntent();
        mCurrentGameUri = displayIntent.getData();
        getDataFromInventoryTable();
        getDataFromGameTable();
    }

    /*
    * Load data from the Game Table in the Database
    */
    private void getDataFromGameTable() {
        String gameNameData = gameNameText.getText().toString().trim();
        gameNameData = gameNameData.replaceAll("'","''");
        String[] projection = {
                InventoryContract.GameEntry._ID,
                InventoryContract.GameEntry.GAME_NAME,
                InventoryContract.GameEntry.GAME_GENRE,
                InventoryContract.GameEntry.GAME_SYSTEM,
                InventoryContract.GameEntry.GAME_RELEASE_DATE,
                InventoryContract.GameEntry.GAME_DEVELOPER,
                InventoryContract.GameEntry.GAME_DEV_ESTB,
                InventoryContract.GameEntry.GAME_DEV_HQ,
                InventoryContract.GameEntry.GAME_DEV_COUNTRY};
        Cursor mGameCursor = getContentResolver().query(
                InventoryContract.GameEntry.CONTENT_URI,
                projection,
                InventoryContract.GameEntry.GAME_NAME + " = " + "'" + gameNameData + "'",
                null,
                null);
        assert mGameCursor != null;
        if (mGameCursor.moveToFirst()) {
            String gameGenre = mGameCursor.getString(
                    mGameCursor.getColumnIndex("gamegenre"));
            if (gameGenre.equals("null")){ gameGenre = ""; }
            String gameRelease = mGameCursor.getString(
                    mGameCursor.getColumnIndex("gamerelease"));
            if (gameRelease.equals("null")){ gameRelease = "Unknown"; }
            String gameDeveloper = mGameCursor.getString(
                    mGameCursor.getColumnIndex("devname"));
            if (gameDeveloper.equals("null")){ gameDeveloper = "Unknown"; }
            String gameDeveloperEst = mGameCursor.getString(
                    mGameCursor.getColumnIndex("devestablish"));
            if (gameDeveloperEst.equals("null")){ gameDeveloperEst = "Unknown"; }
            String gameDeveloperCountry = mGameCursor.getString(
                    mGameCursor.getColumnIndex("devcountry"));
            if (gameDeveloperCountry.equals("null")){ gameDeveloperCountry = "Unknown"; }
            String gamePhone = mGameCursor.getString(
                    mGameCursor.getColumnIndex("devhq"));
            if (gamePhone.equals("null")){ gamePhone = ""; }
            gameGenreText.setText(gameGenre);
            gameReleaseText.setText(gameRelease);
            gameDeveloperText.setText(gameDeveloper);
            gameDevEstText.setText(gameDeveloperEst);
            gameDevPhoneText.setText(gamePhone);
            gameDevHomeText.setText(gameDeveloperCountry);
        }
        mGameCursor.close();
    }

    /*
    * Load data from the Inventory Table in the Database
    */
    private void getDataFromInventoryTable() {
        String[] projection = {
            InventoryContract.InventoryEntry._ID,
            InventoryContract.InventoryEntry.GAME_NAME,
            InventoryContract.InventoryEntry.GAME_SYSTEM,
            InventoryContract.InventoryEntry.GAME_QUANTITY,
            InventoryContract.InventoryEntry.GAME_SALE_PRICE,
            InventoryContract.InventoryEntry.GAME_SUGGESTED_PRICE,
            InventoryContract.InventoryEntry.GAME_CONDITION};
        Cursor mInventoryCursor = getContentResolver().query(mCurrentGameUri,
                projection, null, null, null);
        assert mInventoryCursor != null;
        if(mInventoryCursor.moveToFirst()) {
            String gameName = mInventoryCursor.getString(
                    mInventoryCursor.getColumnIndex("game"));
            String gameSystem = mInventoryCursor.getString(
                    mInventoryCursor.getColumnIndex("system"));
            switch(gameSystem) {
                case "0":
                    gameSystemText.setText(R.string.sps3);
                    break;
                case "1":
                    gameSystemText.setText(R.string.sps4);
                    break;
                case "2":
                    gameSystemText.setText(R.string.mxbox);
                    break;
                case "3":
                    gameSystemText.setText(R.string.n3ds);
                    break;
                case "4":
                    gameSystemText.setText(R.string.nswitch);
                    break;
            }
            String gameQuantity = mInventoryCursor.getString(
                    mInventoryCursor.getColumnIndex("quantity"));
            String gamePrice = mInventoryCursor.getString(
                    mInventoryCursor.getColumnIndex("suggested"));
            gameNameText.setText(gameName);
            gameQuantityText.setText(gameQuantity);
            gameSuggestedText.setText(gamePrice);
        }
        mInventoryCursor.close();

    }

    private void initViews() {
        gameNameText = findViewById(R.id.game_name);
        gameSystemText = findViewById(R.id.game_system);
        gameGenreText = findViewById(R.id.game_genre_details);
        gameReleaseText = findViewById(R.id.game_released_details);
        gameQuantityText = findViewById(R.id.quantity_details);
        gameSuggestedText = findViewById(R.id.suggested_details);
        gameDeveloperText = findViewById(R.id.developer_details);
        gameDevHomeText = findViewById(R.id.developer_home_details);
        gameDevEstText = findViewById(R.id.developer_est_details);
        gameDevPhoneText = findViewById(R.id.developer_phone_details);
    }

    @Override
    public void onClick(View v) {
        //For future enhancement
    }


}
