package com.glasstowerstudios.garrulo.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.comm.GarruloMessage;
import com.glasstowerstudios.garrulo.comm.GarruloMessageHandler;
import com.glasstowerstudios.garrulo.comm.AuditoryMessageHandler;

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
    mMessageHandler = new AuditoryMessageHandler();
    mCommunicator = new GarruloListeningCommunicator();

    IntentFilter filter = new IntentFilter();
    filter.addAction(getResources().getString(R.string.communicator_intent));
    registerReceiver(mCommunicator, filter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mCommunicator);
    mMessageHandler.shutdown();
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    // Dispatch to the appropriate handler
    if (mShouldListen) {
      Log.i(LOGTAG, "***** DEBUG_jwir3: onNotificationPosted: " + sbn);
      Log.i(LOGTAG, "***** DEBUG_jwir3: ID: "
                    + sbn.getId()
                    + " +++ "
                    + sbn.getNotification().tickerText
                    + " +++ "
                    + sbn.getPackageName()
                    + " +++ "
                    + sbn.getTag()
                    + " +++ "
                    + sbn.describeContents());

      // If there isn't a notification, then just return without processing it.
      if (sbn.getNotification() == null) {
        return;
      }

      switch (NotificationCompat.getCategory(sbn.getNotification())) {
        case NotificationCompat.CATEGORY_MESSAGE:
          Bundle notificationExtras = sbn.getNotification().extras;
          String messageText = notificationExtras.getString("android.text");
          String sender = notificationExtras.getString("android.title");

          GarruloMessage message = new GarruloMessage(sender, messageText);
          mMessageHandler.process(message);

          cancelNotification(sbn);
          break;
      }
    }
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    Log.i(LOGTAG, "***** DEBUG_jwir3: onNotificationRemoved: " + sbn);
    Log.i(LOGTAG, "***** DEBUG_jwir3: ID:"
                  + sbn.getId()
                  + " +++ "
                  + sbn.getNotification().tickerText
                  + " +++ "
                  + sbn.getPackageName());
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
      if (aIntent.getStringExtra("command").equals("shutdown")) {
        GeneralNotificationListenerService.this.stopListening();
      } else if (aIntent.getStringExtra("command").equals("startup")) {
        GeneralNotificationListenerService.this.startListening();
      }
    }
  }
}