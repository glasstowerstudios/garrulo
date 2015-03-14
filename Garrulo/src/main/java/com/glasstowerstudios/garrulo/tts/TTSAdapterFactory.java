package com.glasstowerstudios.garrulo.tts;

import android.util.Log;

/**
 * Factory class for adapting TextToSpeech (TTS) services to be used within Garrulo.
 */
public abstract class TTSAdapterFactory {
  private static final String LOGTAG = TTSAdapterFactory.class.getSimpleName();

  private static TTSAdapter sInstance;

  /**
   * Retrieve an instance of the {@link TTSAdapter}, or create one if one is not yet available.
   *
   * This method currently is bound to {@link QueuedSpeakingAdapter}, but if additional implementations are
   * created in the future, it can be refactored to take a class parameter instead.
   *
   * @return A static instance of {@link TTSAdapter}.
   */
  public static TTSAdapter getAdapter() {
    if (sInstance == null) {
      try {
        Class<? extends TTSAdapter> clazz =
          Class.forName(TTSAdapterFactory.class.getPackage().getName()
                        + "."
                        + QueuedSpeakingAdapter.class.getSimpleName())
               .asSubclass(TTSAdapter.class);
        sInstance = clazz.newInstance();
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
        Log.e(LOGTAG,
              "Unable to instantiate instance of class: " + QueuedSpeakingAdapter.class.getSimpleName(),
              e);
        throw new IllegalArgumentException(e);
      }
    }

    return sInstance;
  }
}
