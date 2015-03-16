package com.glasstowerstudios.garrulo.app;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
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

  /**
   * Determines if the hardware on this device allows for near-field communication.
   *
   * @return true, if this device supports NFC; false, otherwise.
   */
  public static boolean isNFCAvailable() {
    Context appContext = GarruloApplication.getInstance();
    final NfcManager manager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
    NfcAdapter adapter = manager.getDefaultAdapter();
    return !(adapter == null);
  }

  /**
   * Determines if this device supports near-field communication, and if so, if near-field
   * communication is enabled.
   *
   * @return true, if this device supports NFC and NFC is enabled; false, otherwise.
   */
  public static boolean isNFCEnabled() {
    if (!isNFCAvailable()) {
      return false;
    }

    Context appContext = GarruloApplication.getInstance();
    final NfcManager manager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
    NfcAdapter adapter = manager.getDefaultAdapter();
    return adapter.isEnabled();
  }
}
