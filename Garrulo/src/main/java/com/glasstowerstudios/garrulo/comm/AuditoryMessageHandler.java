package com.glasstowerstudios.garrulo.comm;

import com.glasstowerstudios.garrulo.tts.TTSAdapter;
import com.glasstowerstudios.garrulo.tts.TTSAdapterFactory;
import com.glasstowerstudios.garrulo.util.Interpreter;
import com.glasstowerstudios.garrulo.util.SMSInterpreter;

/**
 * Broadcast receiver that handles actions necessary to process messages via auditory output (i.e.
 * text to speech).
 */
public class AuditoryMessageHandler
  implements GarruloMessageHandler {

  private TTSAdapter mAdapter;

  public AuditoryMessageHandler() {
    super();
    mAdapter = TTSAdapterFactory.getAdapter();
  }

  @Override
  public void process(GarruloMessage aMessage) {
    Interpreter interpreter = new SMSInterpreter();
    String[] messages = interpreter.interpretMessage(aMessage);
    for (String message : messages) {
      mAdapter.speak(message);
    }
  }

  @Override
  public void shutdown() {
    mAdapter.shutdown();
  }
}
