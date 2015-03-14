package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
  private MenuItem mStartListening;
  private MenuItem mStopListening;

  private boolean mShouldStop = false;

  private TTSAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    stopService(new Intent(this, GarruloListenerService.class));
    mAdapter.shutdown();

    GarruloApplication.getInstance().unsuppressNotifications();
  }

  @Override
  public void onResume() {
    super.onResume();
    ensureNotificationAccessGranted();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_garrulo_main, menu);
    mTestMenuItem = menu.findItem(R.id.action_test);
    mStopTestMenuItem = menu.findItem(R.id.action_stop_test);

    mStartListening = menu.findItem(R.id.action_start_notification_listener);
    mStopListening = menu.findItem(R.id.action_stop_notification_listener);
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
      case R.id.action_start_notification_listener:
        // Start the listener
        startListening();
        break;
      case R.id.action_stop_notification_listener:
        // Stop the listener
        stopListening();
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
      case R.id.action_quit:
        stopListening();
        GarruloMainActivity.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        getParent().finish();
        System.exit(0);
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Discontinue listening for notifications.
   */
  private void stopListening() {
    Intent serviceStopIntent = new Intent(getResources().getString(R.string.communicator_intent));
    serviceStopIntent.putExtra("command", "shutdown");
    Log.d(LOGTAG, "***** DEBUG_jwir3: Sending intent: " + serviceStopIntent);
    sendBroadcast(serviceStopIntent);
    mStartListening.setEnabled(true);
    mStopListening.setEnabled(false);
  }

  /**
   * Begin listening for notifications.
   */
  private void startListening() {
    Intent serviceStartIntent = new Intent(getResources().getString(R.string.communicator_intent));
    serviceStartIntent.putExtra("command", "startup");
    Log.d(LOGTAG, "***** DEBUG_jwir3: Sending intent: " + serviceStartIntent);
    sendBroadcast(serviceStartIntent);
    mStartListening.setEnabled(false);
    mStopListening.setEnabled(true);
  }

  /**
   * Verifies that access to listen to notifications has been granted by the user, and if not,
   * prompts the user to navigate to the Settings application to enable this permission.
   */
  private void ensureNotificationAccessGranted() {
    if (!GarruloApplication.isNotificationListenerAccessGranted()) {
      DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface aDialog, int aWhich) {
          switch (aWhich) {
            case Dialog.BUTTON_NEGATIVE:
              aDialog.dismiss();
              break;

            case Dialog.BUTTON_POSITIVE:
              Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
              startActivity(intent);
              aDialog.dismiss();
              break;
          }
        }
      };

      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
      dialogBuilder.setTitle(getResources().getString(R.string.notification_listener_access_title));
      dialogBuilder
        .setMessage(getResources().getString(R.string.notification_listener_access_warning));
      dialogBuilder.setNegativeButton(R.string.notification_listener_response_negative, listener);
      dialogBuilder.setPositiveButton(R.string.notification_listener_response_positive, listener);
      AlertDialog dialog = dialogBuilder.create();
      dialog.show();

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
    mTestMenuItem.setEnabled(true);
    mStopTestMenuItem.setEnabled(false);
  }
}
