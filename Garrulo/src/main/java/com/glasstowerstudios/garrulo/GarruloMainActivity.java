package com.glasstowerstudios.garrulo;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class GarruloMainActivity
        extends Activity
        implements TextToSpeech.OnInitListener {

    private static final String LOGTAG = GarruloMainActivity.class.getSimpleName();

    private static final String testText1 = "There once was a man from Nantucket";
    private static final String testText2 = "Who kept all of his money in a bucket.";
    private static final String testText3 = "He had a daughter named Nan, who ran off with a Man";
    private static final String testText4 = "And, as for the money, Nantucket!";

    // TODO: At some point, we'll probably want to convert this into a service.
    private TextToSpeech mTTSConvertorInstance;
    private boolean mReady; // Flag indicating whether we're ready to perform TTS conversions.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garrulo_main);
        mReady = false;
        mTTSConvertorInstance = new TextToSpeech(this, this);
    }

    @Override
    protected void onDestroy() {
        if (mReady && mTTSConvertorInstance != null) {
            mTTSConvertorInstance.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_garrulo_main, menu);
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
                return true;
            case R.id.action_test:
                runSpeakingTest();
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
        new Thread(new Runnable() {

            @Override
            public void run() {
                long lastSpeakTime = 0;
                while(true) {
                    // Run every 20 ms.
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastSpeakTime >= 20000 && mReady) {
                        lastSpeakTime = curTime;
                        mTTSConvertorInstance.speak(testText1, TextToSpeech.QUEUE_ADD, null);
                        mTTSConvertorInstance.speak(testText2, TextToSpeech.QUEUE_ADD, null);
                        mTTSConvertorInstance.speak(testText3, TextToSpeech.QUEUE_ADD, null);
                        mTTSConvertorInstance.speak(testText4, TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.ERROR) {
            Log.d(LOGTAG, "Unable to perform text to speech conversion due to error in initialization");
        } else {
            mReady = true;
        }
    }
}
