package com.glasstowerstudios.garrulo.comm;

import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;

import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;
import com.glasstowerstudios.garrulo.util.Interpreter;
import com.glasstowerstudios.garrulo.util.SMSInterpreter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Broadcast receiver that handles actions to be performed when an SMS message is received.
 */
public class SMSMessageHandler
  implements TextToSpeech.OnInitListener,
  GarruloMessageHandler {

  private static final String LOGTAG = SMSMessageHandler.class.getSimpleName();

  private boolean mReady = false; // Flag indicating if TTS is ready to go.
  private Deque<SmsMessage> mQueue;
  private TTSAdapter mAdapter;

  public SMSMessageHandler() {
    super();
    mQueue = new ArrayDeque<SmsMessage>();
    mAdapter = TTSAdapterFactory.getAdapter();
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

  @Override
  public void process(GarruloMessage aMessage) {
    Interpreter interpreter = new SMSInterpreter();
    String[] messages = interpreter.interpretMessage(aMessage);
    for (String message : messages) {
      Log.d(LOGTAG, "Speaking message: " + message);
      mAdapter.speak(message);
    }
  }

  @Override
  public void shutdown() {
    mAdapter.shutdown();
  }
}
