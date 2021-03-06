package com.glasstowerstudios.garrulo.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Adapter for TextToSpeech (TTS) services. Since TTS services need to be initialized with an
 * Activity context, an adapter is not available until the main activity has been set up.
 */
public abstract class TTSAdapter {

  /**
   * Initialize this {@link TTSAdapter} with an activity {@link Context}.
   *
   * Because {@link TextToSpeech} requires that it's bound to an activity (not a service), if you
   * use this {@link TTSAdapter} from within a {@link android.app.Service}, you will need to bind it
   * to an {@link android.app.Activity} and wait for it to be bound before attempting to do anything
   * with it.
   *
   * @param aContext The {@link android.app.Activity} context for which the {@link TextToSpeech}
   *                 implementation should be bound.
   *
   * @see #isReady()
   */
  public abstract void init(Context aContext);

  /**
   * Make the {@link TextToSpeech} implementation speak some string of text.
   *
   * @param aWhat The {@link String} to be spoken.
   */
  public abstract void speak(String aWhat);

  /**
   * Prepare the {@link TextToSpeech} implementation for shutdown.
   */
  public abstract void shutdown();

  /**
   * Retrieves whether or not this {@link com.glasstowerstudios.garrulo.tts.TTSAdapter} has been
   * paused, but not resumed.
   *
   * @return true, if this adapter has been paused but not yet resumed; false, otherwise.
   */
  public abstract boolean isPaused();

  /**
   * Pauses any speaking currently happening.
   */
  public abstract void pause();

  /**
   * Resumes speaking after a call to {@link #pause()}.
   *
   * Implementations may rewind the playback in order to account for any missed seconds during the
   * pause/resume switchover, but this functionality is not required.
   */
  public abstract void resume();

  /**
   * Determine if this adapter has been bound to an {@link android.app.Activity}.
   *
   * @return true, if this adapter is bound to an activity and onInit has been called; false,
   * otherwise.
   */
  public abstract boolean isReady();
}
