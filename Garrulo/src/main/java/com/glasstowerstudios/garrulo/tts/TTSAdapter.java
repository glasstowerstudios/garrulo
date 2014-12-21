package com.glasstowerstudios.garrulo.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Adapter for TextToSpeech (TTS) services. Since TTS services need to be initialized with an Activity
 * context, an adapter is not available until the main activity has been set up.
 */
public abstract class TTSAdapter {

    /**
     * Initialize this {@link TTSAdapter} with an activity {@link Context}.
     *
     * Because {@link TextToSpeech} requires that it's bound to an activity (not a service), if you
     * use this {@link TTSAdapter} from within a {@link android.app.Service}, you will need to bind it to an
     * {@link android.app.Activity} and wait for it to be bound before attempting to do anything with it.
     *
     * @param aContext The {@link android.app.Activity} context for which the {@link TextToSpeech} implementation
     *                 should be bound.
     *
     * @see #isReady()
     */
    public abstract void init(Context aContext);

    /**
     * Make the {@link TextToSpeech} implementation speak some string of text.
     *
     * @param aWhat The {@link String} to be spoken.
     *
     */
    public abstract void speak(String aWhat);

    /**
     * Prepare the {@link TextToSpeech} implementation for shutdown.
     */
    public abstract void shutdown();

    /**
     * Add a {@link android.speech.tts.TextToSpeech.OnInitListener} to receive notifications when
     * initialization has happened.
     *
     * @param aListener The {@link android.speech.tts.TextToSpeech.OnInitListener} to receive
     *                  notifications of initialization when they occur.
     */
    public abstract void addOnInitListener(TextToSpeech.OnInitListener aListener);

    /**
     * Remove a previously added {@link android.speech.tts.TextToSpeech.OnInitListener} and prevent
     * it from receiving notifications when initialization has happened.
     *
     * @param aListener The {@link android.speech.tts.TextToSpeech.OnInitListener} that was previously
     *                  added to the list of listeners to receive notifications of initialization
     *                  when they occur.
     */
    public abstract void removeOnInitListener(TextToSpeech.OnInitListener aListener);

    /**
     * Determine if this adapter has been bound to an {@link android.app.Activity}.
     *
     * @return true, if this adapter is bound to an activity and onInit has been called;
     *         false, otherwise.
     */
    public abstract boolean isReady();
}
