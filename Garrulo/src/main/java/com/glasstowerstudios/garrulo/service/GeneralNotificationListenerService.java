package com.glasstowerstudios.garrulo.service;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.glasstowerstudios.garrulo.comm.AuditoryMessageHandler;
import com.glasstowerstudios.garrulo.comm.GarruloCommunicationChannel;
import com.glasstowerstudios.garrulo.comm.GarruloCommunicationChannelResponder;
import com.glasstowerstudios.garrulo.comm.GarruloMessage;
import com.glasstowerstudios.garrulo.comm.GarruloMessageHandler;

/**
 * Main Garrulo listener service. Listens for events that Garrulo can handle on the system and
 * dispatches a task to perform this handling.
 */
public class GeneralNotificationListenerService
  extends NotificationListenerService
  implements GarruloCommunicationChannelResponder {

  private static final String LOGTAG = GeneralNotificationListenerService.class.getSimpleName();

  private GarruloMessageHandler mMessageHandler;
  private GarruloCommunicationChannel mCommChannel;

  // Whether or not we should be listening for notifications.
  private boolean mShouldListen = false;

  @Override
  public void onCreate() {
    super.onCreate();
    mMessageHandler = new AuditoryMessageHandler();
    mCommChannel = new GarruloCommunicationChannel(this, this);
//    mCommunicator = new GarruloListeningCommunicator();
//
//    IntentFilter filter = new IntentFilter();
//    filter.addAction(getResources().getString(R.string.communicator_intent));
//    registerReceiver(mCommunicator, filter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
//    unregisterReceiver(mCommunicator);
    mCommChannel.disconnect();
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
          Log.d(LOGTAG, "***** DEBUG_jwir3: Sending message: " + messageText + ", from sender: " + sender);
          mMessageHandler.process(message);

          cancelNotification(sbn);
          break;
      }
    } else {
      Log.d(LOGTAG, "Garrulo is not listening for notifications.");
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

  @Override
  public void onShutdown() {
    stopListening();
  }

  @Override
  public void onStartup() {
    startListening();
  }
}