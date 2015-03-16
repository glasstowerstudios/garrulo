package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.glasstowerstudios.garrulo.BuildConfig;
import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.service.GarruloListenerService;
import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;

/**
 * Main Activity for Garrulo application.
 *
 * This activity also contains some test data for testing TextToSpeech (TTS) capabilities.
 */
public class GarruloMainActivity
  extends Activity
  implements View.OnClickListener {

  private static final String LOGTAG = GarruloMainActivity.class.getSimpleName();

  private static final String testText1 = "There once was a man from Nantucket";
  private static final String testText2 = "Who kept all of his money in a bucket.";
  private static final String testText3 = "He had a daughter named Nan, who ran off with a Man";
  private static final String testText4 = "And, as for the money, Nantucket!";

  private MenuItem mTestMenuItem;
  private MenuItem mStopTestMenuItem;
  private MenuItem mNotifyMenuItem;
  private MenuItem mStartNotificationListenerMenuItem;
  private MenuItem mStopNotificationListenerMenuItem;

  private boolean mIsListening = true;

  private TextView mServiceIndicator;
  private boolean mShouldStop = false;

  private TTSAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_garrulo_main);
    mAdapter = TTSAdapterFactory.getAdapter();
    mAdapter.init(this);
    startService(new Intent(this, GarruloListenerService.class));

    mServiceIndicator = (TextView) findViewById(R.id.service_running_indicator);

    mServiceIndicator.setOnClickListener(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopService(new Intent(this, GarruloListenerService.class));
    mAdapter.shutdown();

    GarruloApplication.getInstance().unsuppressAllNotificationSounds();
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
    mNotifyMenuItem = menu.findItem(R.id.action_notify);
    mStartNotificationListenerMenuItem = menu.findItem(R.id.action_start_notification_listener);
    mStopNotificationListenerMenuItem = menu.findItem(R.id.action_stop_notification_listener);

    // If we're not in DEBUG mode, then suppress the debug-only menu options.
    if (!BuildConfig.DEBUG) {
      mTestMenuItem.setVisible(false);
      mStopTestMenuItem.setVisible(false);
      mNotifyMenuItem.setVisible(false);
      mStartNotificationListenerMenuItem.setVisible(false);
      mStopNotificationListenerMenuItem.setVisible(false);
    }

    startListening();
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
      case R.id.action_notify:
        Log.d(LOGTAG, "Sending notification");
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle("My Notification");
        ncomp.setContentText("Notification Listener Service Example");
        ncomp.setTicker("Notification Listener Service Example");
        ncomp.setCategory(NotificationCompat.CATEGORY_EVENT);
        ncomp.setSmallIcon(R.drawable.ic_launcher);
        ncomp.setAutoCancel(true);
        nManager.notify((int) System.currentTimeMillis(), ncomp.build());
        break;
      case R.id.action_test:
        runSpeakingTest();
        break;
      case R.id.action_stop_test:
        disableSpeakingTest();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Discontinue listening for notifications.
   */
  private void stopListening() {
    int disabledTextColor = getResources().getColor(R.color.disabled_red_text);
    TextView serviceRunningIndicator = (TextView) findViewById(R.id.service_running_indicator);
    serviceRunningIndicator.setTextColor(disabledTextColor);

    Intent serviceStopIntent = new Intent(getResources().getString(R.string.communicator_intent));
    serviceStopIntent.putExtra("command", "shutdown");
    sendBroadcast(serviceStopIntent);

    mIsListening = false;

    mServiceIndicator.setBackground(getResources().getDrawable(R.drawable.garrulo_running_indicator_background_disabled));
    mServiceIndicator.setText(getResources().getString(R.string.service_disabled));
    mAdapter.pause();
  }

  /**
   * Begin listening for notifications.
   */
  private void startListening() {
    int enabledTextColor = getResources().getColor(R.color.active_green_text);
    TextView serviceRunningIndicator = (TextView) findViewById(R.id.service_running_indicator);
    serviceRunningIndicator.setTextColor(enabledTextColor);

    Intent serviceStartIntent = new Intent(getResources().getString(R.string.communicator_intent));
    serviceStartIntent.putExtra("command", "startup");
    sendBroadcast(serviceStartIntent);

    mIsListening = true;

    mServiceIndicator.setBackground(
      getResources().getDrawable(R.drawable.garrulo_running_indicator_background_enabled));
    mServiceIndicator.setText(getResources().getString(R.string.service_enabled));

    if (mAdapter.isPaused()) {
      mAdapter.resume();
    }
  }

  private void runSpeakingTest() {
    mTestMenuItem.setEnabled(false);
    mStopTestMenuItem.setEnabled(true);
    mShouldStop = false;
    new Thread(new Runnable() {

      @Override
      public void run() {
        long lastSpeakTime = 0;
        while (!mShouldStop) {
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
    mAdapter.pause();
    mTestMenuItem.setEnabled(true);
    mStopTestMenuItem.setEnabled(false);
  }

  @Override
  public void onClick(View aView) {
    if (mIsListening) {
      stopListening();
    } else {
      startListening();
    }
  }
}
