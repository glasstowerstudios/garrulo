package com.glasstowerstudios.garrulo.util;

/**
 * An interface for interpreting messages based on locale and other factors.
 */
public interface Interpreter<T> {
    /**
     * Retrieve a formatted message, in a series of chunks, which can then be passed to a
     * {@link com.glasstowerstudios.garrulo.tts.TTSAdapter} to be spoken.
     *
     * @param aMessage A message of some kind. Every implementing class will define the type of the
     *                 message. Must not be null.
     *
     * @return An array of strings, each of which should be sent, in order, to the appropriate
     *         mechanism for conversion to speech. Assuming aMessage is not empty, this will contain
     *         at least one string object.
     */
    public String[] interpretMessage(T aMessage);
}
