package com.glasstowerstudios.garrulo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.receiver.SMSReceiver;

/**
 * Main Garrulo listener service. Listens for events that Garrulo can handle on the system and
 * dispatches a task to perform this handling.
 */
public class GarruloListenerService extends Service {

  private static final String LOGTAG = GarruloListenerService.class.getSimpleName();

  private BroadcastReceiver mReceiver;
  private GarruloListeningCommunicator mCommunicator;

  @Override
  public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
    super.onStartCommand(aIntent, aFlags, aStartId);

    return Service.START_STICKY;
  }

  @Override
  public void onCreate() {
    mReceiver = new SMSReceiver();
    mCommunicator = new GarruloListeningCommunicator();

    startListening();

    IntentFilter shutdownFilter = new IntentFilter();
    shutdownFilter.addAction(getResources().getString(R.string.communicator_intent));
    registerReceiver(mCommunicator, shutdownFilter);
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(mReceiver);
    unregisterReceiver(mCommunicator);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * Begin listening for notifications.
   */
  public void startListening() {
    IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    smsFilter.setPriority(1000); // Make sure hangouts doesn't swallow our intents.
    registerReceiver(mReceiver, smsFilter);
  }

  /**
   * Discontinue listening for notifications.
   */
  public void stopListening() {
    unregisterReceiver(mReceiver);
  }

  /**
   * Communicator that listens for shutdown messages.
   */
  class GarruloListeningCommunicator extends BroadcastReceiver {
    @Override
    public void onReceive(Context aContext, Intent aIntent) {
      Log.d(LOGTAG, "***** DEBUG_jwir3: received intent with intent action: " + aIntent.getAction());
      if (aIntent.getStringExtra("command").equals("shutdown")) {
        GarruloListenerService.this.stopListening();
      } else if (aIntent.getStringExtra("command").equals("startup")) {
        GarruloListenerService.this.startListening();
      }
    }
  }
}
