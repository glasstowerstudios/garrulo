package com.glasstowerstudios.garrulo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.glasstowerstudios.garrulo.receiver.SMSReceiver;

/**
 * Main Garrulo listener service. Listens for events that Garrulo can handle on the system and
 * dispatches a task to perform this handling.
 */
public class GarruloListenerService extends Service {

    private static final String LOGTAG = GarruloListenerService.class.getSimpleName();

    private BroadcastReceiver mReceiver;

    @Override
    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        super.onStartCommand(aIntent, aFlags, aStartId);

        Log.d(LOGTAG, "Garrulo Listener Service started");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG, "Garrulo listener service created");
        mReceiver = new SMSReceiver();

        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        smsFilter.setPriority(1000); // Make sure hangouts doesn't swallow our intents.
        registerReceiver(mReceiver, smsFilter);
    }

    @Override
    public void onDestroy() {
        Log.d(LOGTAG, "Garrulo listener service destroyed");
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
