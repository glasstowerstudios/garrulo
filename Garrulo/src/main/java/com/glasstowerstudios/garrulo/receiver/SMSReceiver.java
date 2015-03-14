package com.glasstowerstudios.garrulo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.glasstowerstudios.garrulo.comm.GarruloMessage;
import com.glasstowerstudios.garrulo.comm.GarruloMessageHandler;
import com.glasstowerstudios.garrulo.comm.SMSMessageHandler;

/**
 * Broadcast receiver that handles actions to be performed when an SMS message is received.
 */
public class SMSReceiver
  extends BroadcastReceiver {

  private static final String LOGTAG = SMSReceiver.class.getSimpleName();

  private boolean mReady = false; // Flag indicating if TTS is ready to go.
  private GarruloMessageHandler mMessageHandler;

  public SMSReceiver() {
    super();
    mMessageHandler = new SMSMessageHandler();
  }

  @Override
  public void onReceive(Context aContext, Intent aIntent) {
    Log.d(LOGTAG, "SMSReceiver received an intent of type: " + aIntent.getAction());

    if (aIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      Log.d(LOGTAG, "Received an intent for Telephony.SMS_RECEIVED");
      Bundle extras = aIntent.getExtras();
      SmsMessage messages[] = null;
      if (extras != null) {
        // A "PDU" is a "Protocol Data Unit" - the industry spec for SMS
        Object pdus[] = (Object[]) extras.get("pdus");
        messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
          messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

          String sender = messages[i].getOriginatingAddress();
          String text = messages[i].getMessageBody();
          GarruloMessage msg = new GarruloMessage(sender, text);
          Log.d(LOGTAG, "***** DEBUG_jwir3: About to process message from: " + sender);
          mMessageHandler.process(msg);
        }
      }
    }
  }
}
