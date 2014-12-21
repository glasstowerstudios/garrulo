package com.glasstowerstudios.garrulo.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

/**
 * Broadcast receiver that handles actions to be performed when an SMS message is received.
 */
public class SMSReceiver
    extends BroadcastReceiver
    implements TextToSpeech.OnInitListener {

    private static final String LOGTAG = SMSReceiver.class.getSimpleName();

    private boolean mReady = false; // Flag indicating if TTS is ready to go.
    private Deque<SmsMessage> mQueue;
    private TTSAdapter mAdapter;

    public SMSReceiver() {
        super();
        mQueue = new ArrayDeque<SmsMessage>();
    }

    @Override
    public void onReceive(Context aContext, Intent aIntent) {
        Log.d(LOGTAG, "SMSReceiver received an intent of type: " + aIntent.getAction());

        if (aIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Log.d(LOGTAG, "Received an intent for Telephony.SMS_RECEIVED");
            Bundle extras = aIntent.getExtras();
            SmsMessage messages[] = null;
            mAdapter = TTSAdapterFactory.getAdapter();
            if (extras != null) {
                // A "PDU" is a "Protocol Data Unit" - the industry spec for SMS
                Object pdus[] = (Object[]) extras.get("pdus");
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (mAdapter == null || !mAdapter.isReady()) {
                        mAdapter.addOnInitListener(this);
                        mQueue.add(messages[i]);
                        Log.d(LOGTAG, "Added message: " + messages[i] + " to queue");
                    } else {
                        Log.d(LOGTAG, "Speaking message " + messages[i]);
                        speakMessage(messages[i]);
                    }
                }
            }
        }
    }

    @Override
    public void onInit(int status) {
        Log.d(LOGTAG, "Initialized Text to Speech adapter");
        if (status == TextToSpeech.ERROR) {
            Log.d(LOGTAG, "Unable to perform text to speech conversion due to error in initialization");
        } else {
            mReady = true;
            mAdapter.removeOnInitListener(this);
//            while (!mQueue.isEmpty()) {
//                SmsMessage nextMessage = mQueue.pop();
//                Log.d(LOGTAG, "Next message processing: " + nextMessage);
//                speakMessage(nextMessage);
//            }
        }
    }

    private String getContactName(String aPhoneNumber) {
        ContentResolver cr = GarruloApplication.getInstance().getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(aPhoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    private void speakMessage(SmsMessage aMessage) {
        String phoneNumber = aMessage.getOriginatingAddress();
        String messageFrom = "New message from " + getContactName(phoneNumber);
        String messageBody = aMessage.getMessageBody();
        mAdapter.speak(messageFrom);
        mAdapter.speak(messageBody);
    }
}
