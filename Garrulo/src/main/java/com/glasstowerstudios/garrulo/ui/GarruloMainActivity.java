package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.pref.GarruloPreferences;
import com.glasstowerstudios.garrulo.service.GarruloListenerService;
import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;

/**
 * Main Activity for Garrulo application.
 *
 * This activity also contains some test data for testing TextToSpeech (TTS) capabilities.
 */
public class GarruloMainActivity
        extends Activity {

    private static final String LOGTAG = GarruloMainActivity.class.getSimpleName();

    private static final String testText1 = "There once was a man from Nantucket";
    private static final String testText2 = "Who kept all of his money in a bucket.";
    private static final String testText3 = "He had a daughter named Nan, who ran off with a Man";
    private static final String testText4 = "And, as for the money, Nantucket!";

    private MenuItem mTestMenuItem;
    private MenuItem mStopTestMenuItem;

    private boolean mShouldStop = false;

    private TTSAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "Creating Garrulo main activity");
        setContentView(R.layout.activity_garrulo_main);
        mAdapter = TTSAdapterFactory.getAdapter();
        mAdapter.init(this);
        startService(new Intent(this, GarruloListenerService.class));

        if (GarruloPreferences.getPreferences().isSuppressDefaultNotificationSound()) {
            GarruloApplication.getInstance().suppressNotifications();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOGTAG, "Destroying Garrulo main activity");
        stopService(new Intent(this, GarruloListenerService.class));
        mAdapter.shutdown();

        GarruloApplication.getInstance().unsuppressNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_garrulo_main, menu);
        mTestMenuItem = menu.findItem(R.id.action_test);
        mStopTestMenuItem = menu.findItem(R.id.action_stop_test);
        mStopTestMenuItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_test:
                runSpeakingTest();
                break;
            case R.id.action_stop_test:
                disableSpeakingTest();
                break;
            case R.id.action_quit:
                GarruloMainActivity.this.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                getParent().finish();
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    private void runSpeakingTest() {
        mTestMenuItem.setEnabled(false);
        mStopTestMenuItem.setEnabled(true);
        mShouldStop = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                long lastSpeakTime = 0;
                while(!mShouldStop) {
                    // Run every 20 ms.
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastSpeakTime >= 20000) {
                        lastSpeakTime = curTime;
                        mAdapter.speak(testText1);
                        mAdapter.speak(testText2);
                        mAdapter.speak(testText3);
                        mAdapter.speak(testText4);
                    }
                }
            }
        }).start();
    }

    private void disableSpeakingTest() {
        mShouldStop = true;
        mTestMenuItem.setEnabled(true);
        mStopTestMenuItem.setEnabled(false);
    }
}
