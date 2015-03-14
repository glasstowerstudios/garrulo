package com.glasstowerstudios.garrulo.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.comm.GarruloMessageHandler;
import com.glasstowerstudios.garrulo.comm.SMSMessageHandler;

/**
 * Main Garrulo listener service. Listens for events that Garrulo can handle on the system and
 * dispatches a task to perform this handling.
 */
public class GeneralNotificationListenerService extends NotificationListenerService {

  private static final String LOGTAG = GeneralNotificationListenerService.class.getSimpleName();

  private GarruloMessageHandler mMessageHandler;
  private BroadcastReceiver mCommunicator;

  // Whether or not we should be listening for notifications.
  private boolean mShouldListen = false;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(LOGTAG, "***** DEBUG_jwir3: Creating service");
    mMessageHandler = new SMSMessageHandler();
    mCommunicator = new GarruloListeningCommunicator();

    IntentFilter filter = new IntentFilter();
    filter.addAction(getResources().getString(R.string.communicator_intent));
    registerReceiver(mCommunicator, filter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mCommunicator);
    Log.d(LOGTAG, "***** DEBUG_jwir3: Destroying GarruloListeningService");
    mMessageHandler.shutdown();
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    // Dispatch to the appropriate handler
    if (mShouldListen) {
      Log.i(LOGTAG,"***** DEBUG_jwir3: onNotificationPosted: " + sbn);
      Log.i(LOGTAG,"***** DEBUG_jwir3: ID: " + sbn.getId() + " +++ " + sbn.getNotification().tickerText + " +++ " + sbn.getPackageName());

      switch (NotificationCompat.getCategory(sbn.getNotification())) {
        case NotificationCompat.CATEGORY_MESSAGE:
          // We don't want to do anything with this category, because it will be handled by the
          // listener of SMS_RECEIVED.
          break;
      }
    }
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    Log.i(LOGTAG,"***** DEBUG_jwir3: onNotificationRemoved: " + sbn);
    Log.i(LOGTAG,"***** DEBUG_jwir3: ID:" + sbn.getId() + " +++ " + sbn.getNotification().tickerText + " +++ " + sbn.getPackageName());
  }

  @TargetApi(21)
  public void cancelNotification(StatusBarNotification aStatusBarNotification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cancelNotification(aStatusBarNotification.getKey());
    } else {
      cancelNotification(aStatusBarNotification.getPackageName(),
                         aStatusBarNotification.getTag(),
                         aStatusBarNotification.getId());
    }
  }

  public void stopListening() {
    mShouldListen = false;
  }

  public void startListening() {
    mShouldListen = true;
  }

  class GarruloListeningCommunicator extends BroadcastReceiver {
    @Override
    public void onReceive(Context aContext, Intent aIntent) {
      Log.d(LOGTAG, "***** DEBUG_jwir3: Received command");
      if (aIntent.getStringExtra("command").equals("shutdown")) {
        Log.d(LOGTAG, "***** DEBUG_jwir3: Received shutdown command");
        GeneralNotificationListenerService.this.stopListening();
      } else if (aIntent.getStringExtra("command").equals("startup")) {
        Log.d(LOGTAG, "***** DEBUG_jwir3: Received startup command");
        GeneralNotificationListenerService.this.startListening();
      }
    }
  }
}