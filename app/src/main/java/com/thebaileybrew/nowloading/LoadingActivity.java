package com.thebaileybrew.nowloading;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.thebaileybrew.nowloading.database.InventoryContract;
import com.thebaileybrew.nowloading.queryelements.gamesquery.QueryGameUtils;

import static android.view.View.VISIBLE;

public class LoadingActivity extends AppCompatActivity {
    private TextView welcomeMessage;
    private TextView hintOne;
    private TextView hintTwo;
    private TextView hintThree;
    private TextView hintFour;
    private Animation in;
    private Animation in2;
    private Animation in3;
    private Animation in4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        welcomeMessage = findViewById(R.id.loading_progress_text);
        hintOne = findViewById(R.id.hint_one);
        hintTwo = findViewById(R.id.hint_two);
        hintThree = findViewById(R.id.hint_three);
        hintFour = findViewById(R.id.hint_four);
        in = AnimationUtils.loadAnimation(this,R.anim.fadein);
        in2 = AnimationUtils.loadAnimation(this,R.anim.fadein);
        in3 = AnimationUtils.loadAnimation(this,R.anim.fadein);
        in4 = AnimationUtils.loadAnimation(this,R.anim.fadein);
        final Handler checkDBHandler = new Handler();
        Runnable checkForDB = new Runnable() {
            @Override
            public void run() {
                Cursor mCursor = getContentResolver().query(InventoryContract.GameEntry.CONTENT_URI,
                        null, null,null,null,null);
                Boolean rowExists = false;
                if (mCursor != null) {
                    if (mCursor.moveToFirst()) {
                        mCursor.close();
                        rowExists = true;
                        Toast.makeText(LoadingActivity.this, "Table exists already", Toast.LENGTH_SHORT).show();
                    } else {
                        mCursor.close();
                        rowExists = false;
                    }
                }
                if (!rowExists) {
                    String jsonData = QueryGameUtils.loadJSONFromAsset(LoadingActivity.this);
                    QueryGameUtils.extractDataFromJson(jsonData, LoadingActivity.this);
                }
            }
        };
        checkDBHandler.postDelayed(checkForDB, 200);

        startLoaders();
    }

    private void startLoaders() {
        final Handler mHandler = new Handler(); final Handler mHandler2 = new Handler();
        final Handler mHandler3 = new Handler(); final Handler mHandler4 = new Handler();
        Runnable delayRunnable = new Runnable() {
            @Override
            public void run() {
                hintOne.setAnimation(in);
                hintOne.setVisibility(VISIBLE);
            }
        };
        mHandler.postDelayed(delayRunnable, 2500);
        Runnable delayRunnableTwo = new Runnable() {
            @Override
            public void run() {
                hintTwo.setAnimation(in2);
                hintTwo.setVisibility(VISIBLE);
            }
        };
        mHandler2.postDelayed(delayRunnableTwo, 5000);
        Runnable delayRunnableThree = new Runnable() {
            @Override
            public void run() {
                hintThree.setAnimation(in3);
                hintThree.setVisibility(VISIBLE);
            }
        };
        mHandler3.postDelayed(delayRunnableThree, 7500);
        Runnable delayRunnableFour = new Runnable() {
            @Override
            public void run() {
                hintFour.setAnimation(in4);
                hintFour.setVisibility(VISIBLE);
            }
        };
        mHandler4.postDelayed(delayRunnableFour, 10000);

        //Run final loading of app
        final Handler mHandlerLoad = new Handler();
        Runnable delayRunnableLoader = new Runnable() {
            @Override
            public void run() {
                welcomeMessage.setText("DATABASE LOADED");
                openGamingDatabase();

            }
        };
        mHandlerLoad.postDelayed(delayRunnableLoader, 12000);
    }

    private void openGamingDatabase() {
        final Handler mHandlerLoader = new Handler();
        Runnable delayRunnableLoaderer = new Runnable() {
            @Override
            public void run() {
                welcomeMessage.setText("DATABASE LOADED");
            }
        };
        mHandlerLoader.postDelayed(delayRunnableLoaderer, 14000);
        //Intent call to main catalog
        Intent openGamingDatabase = new Intent(this, CatalogActivity.class);
        this.startActivity(openGamingDatabase);
    }
}
