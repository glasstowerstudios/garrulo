package com.glasstowerstudios.garrulo.util;

import com.glasstowerstudios.garrulo.comm.GarruloMessage;

/**
 * An interface for interpreting messages based on locale and other factors.
 */
public interface Interpreter {
  /**
   * Retrieve a formatted message, in a series of chunks, which can then be passed to a
   * {@link com.glasstowerstudios.garrulo.tts.TTSAdapter} to be spoken.
   *
   * @param aMessage A {@link com.glasstowerstudios.garrulo.comm.GarruloMessage}.
   *
   * @return An array of strings, each of which should be sent, in order, to the appropriate
   *         mechanism for conversion to speech. Assuming aMessage is not empty, this will contain
   *         at least one string object.
   */
  public String[] interpretMessage(GarruloMessage aMessage);
}
