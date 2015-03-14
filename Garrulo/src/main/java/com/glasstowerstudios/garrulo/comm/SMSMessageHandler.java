package com.glasstowerstudios.garrulo.comm;

import android.util.Log;

import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;
import com.glasstowerstudios.garrulo.util.Interpreter;
import com.glasstowerstudios.garrulo.util.SMSInterpreter;

/**
 * Broadcast receiver that handles actions to be performed when an SMS message is received.
 */
public class SMSMessageHandler
  implements GarruloMessageHandler {

  private static final String LOGTAG = SMSMessageHandler.class.getSimpleName();

  private TTSAdapter mAdapter;

  public SMSMessageHandler() {
    super();
    mAdapter = TTSAdapterFactory.getAdapter();
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
