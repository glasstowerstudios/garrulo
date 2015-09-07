package com.glasstowerstudios.garrulo.comm;

/**
 * Handler for Garrulo messages.
 */
public interface GarruloMessageHandler {

  /**
   * Process this message by performing necessary interpretation via an {@link
   * com.glasstowerstudios.garrulo.util.Interpreter} and then passing it to a {@link
   * com.glasstowerstudios.garrulo.tts.TTSAdapter} for output to speech.
   *
   * @param aMessage The {@link GarruloMessage} to be processed.
   */
  void process(GarruloMessage aMessage);

  /**
   * Shut down this {@link com.glasstowerstudios.garrulo.comm.GarruloMessageHandler} so that it is
   * no longer bound to a {@link com.glasstowerstudios.garrulo.tts.TTSAdapter}.
   */
  void shutdown();
}
