package com.glasstowerstudios.garrulo.app;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Main Application class for Garrulo. Static instance is held and used for situations where a
 * UI-independent context is required.
 */
public class GarruloApplication extends Application {
  private static final String LOGTAG = GarruloApplication.class.getSimpleName();

  private boolean mAreNotificationsSuppressed = false;

  private static GarruloApplication sInstance;

  @Override
  public void onCreate() {
    sInstance = this;
  }

  public static GarruloApplication getInstance() {
    if (sInstance == null) {
      Log.e(LOGTAG, "Attempted to retrieve GarruloApplication instance before onCreate!");
    }

    return sInstance;
  }

  /**
   * Suppress notifications from playing the default notification ringtone when Garrulo is active.
   *
   * Note: This affects ALL notifications.
   */
  public void suppressAllNotificationSounds() {
    if (!mAreNotificationsSuppressed) {
      AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
      mAreNotificationsSuppressed = true;
    }
  }

  /**
   * Turn off suppression of notifications that was enabled with {@link
   * #suppressAllNotificationSounds()}.
   *
   * Note: This affects ALL notifications.
   */
  public void unsuppressAllNotificationSounds() {
    if (mAreNotificationsSuppressed) {
      AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
      mAreNotificationsSuppressed = false;
    }
  }
}
