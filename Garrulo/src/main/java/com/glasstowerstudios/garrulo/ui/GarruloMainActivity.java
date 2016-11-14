package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.glasstowerstudios.garrulo.BuildConfig;
import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.comm.GarruloCommunicationChannel;
import com.glasstowerstudios.garrulo.comm.GarruloCommunicationChannelResponder;
import com.glasstowerstudios.garrulo.nfc.NdefUtils;
import com.glasstowerstudios.garrulo.nfc.NfcTagPoller;
import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;

/**
 * Main Activity for Garrulo application.
 *
 * This activity also contains some test data for testing TextToSpeech (TTS) capabilities.
 */
public class GarruloMainActivity
  extends Activity
  implements View.OnClickListener, GarruloCommunicationChannelResponder {

  private static final String LOGTAG = GarruloMainActivity.class.getSimpleName();

  private static final String testText1 = "There once was a man from Nantucket";
  private static final String testText2 = "Who kept all of his money in a bucket.";
  private static final String testText3 = "He had a daughter named Nan, who ran off with a Man";
  private static final String testText4 = "And, as for the money, Nantucket!";

  private MenuItem mTestMenuItem;
  private MenuItem mStopTestMenuItem;

  private boolean mIsListening = true;

  private TextView mServiceIndicator;
  private boolean mShouldStop = false;

  private GarruloCommunicationChannel mCommChannel;

  private TTSAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent launchIntent = getIntent();

    setContentView(R.layout.activity_garrulo_main);
    mAdapter = TTSAdapterFactory.getAdapter();
    mAdapter.init(this);

    mServiceIndicator = (TextView) findViewById(R.id.service_running_indicator);
    mServiceIndicator.setOnClickListener(this);

    mCommChannel = new GarruloCommunicationChannel(this, this);

    handleLaunchFromNfc(launchIntent, mCommChannel);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mAdapter.shutdown();
    mCommChannel.disconnect();

    GarruloApplication.getInstance().unsuppressAllNotificationSounds();
  }

  @Override
  public void onResume() {
    super.onResume();
    ensureNotificationAccessGranted();
    mCommChannel.communicateCommand(GarruloCommunicationChannel.GarruloCommand.STARTUP);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_garrulo_main, menu);

    mTestMenuItem = menu.findItem(R.id.action_test);
    mStopTestMenuItem = menu.findItem(R.id.action_stop_test);
    MenuItem notifyMenuItem = menu.findItem(R.id.action_notify);
    MenuItem startNotificationListenerMenuItem =
      menu.findItem(R.id.action_start_notification_listener);
    MenuItem stopNotificationListenerMenuItem =
      menu.findItem(R.id.action_stop_notification_listener);

    // If we're not in DEBUG mode, then suppress the debug-only menu options.
    if (!BuildConfig.DEBUG) {
      mTestMenuItem.setVisible(false);
      mStopTestMenuItem.setVisible(false);
      notifyMenuItem.setVisible(false);
      startNotificationListenerMenuItem.setVisible(false);
      stopNotificationListenerMenuItem.setVisible(false);
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

    mIsListening = true;

    mServiceIndicator.setBackground(
      getResources().getDrawable(R.drawable.garrulo_running_indicator_background_enabled));
    mServiceIndicator.setText(getResources().getString(R.string.service_enabled));

    if (mAdapter.isPaused()) {
      mAdapter.resume();
    }
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
    mAdapter.pause();
    mTestMenuItem.setEnabled(true);
    mStopTestMenuItem.setEnabled(false);
  }

  @Override
  public void onClick(View aView) {
    if (mIsListening) {
      mCommChannel.communicateCommand(GarruloCommunicationChannel.GarruloCommand.SHUTDOWN);
    } else {
      mCommChannel.communicateCommand(GarruloCommunicationChannel.GarruloCommand.STARTUP);
    }
  }

  private void handleLaunchFromNfc(Intent aIntent, @NonNull GarruloCommunicationChannel aChannel) {
    Bundle extras = aIntent.getExtras();
    if (extras != null) {
      if (extras.keySet().contains(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
        Parcelable[] ndefMsgs = extras.getParcelableArray(NfcAdapter.EXTRA_NDEF_MESSAGES);
        for (Parcelable parcel : ndefMsgs) {
          if (parcel instanceof NdefMessage) {
            NdefMessage ndefParcel = (NdefMessage) parcel;
            if (NdefUtils.wasLaunchedFromNFC(ndefParcel)) {
              // Start our NFC tag poller if we can retrieve the tag.
              Tag nfcTag = (Tag) extras.get(NfcAdapter.EXTRA_TAG);
              new NfcTagPoller(nfcTag, aChannel).start();
              return;
            }
          }
        }
      }
    }
  }

  @Override
  public void onShutdown() {
    stopListening();
  }

  @Override
  public void onStartup() {
    startListening();
  }
}
