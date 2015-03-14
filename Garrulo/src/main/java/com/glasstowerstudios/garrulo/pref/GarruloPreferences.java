package com.glasstowerstudios.garrulo.pref;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;

/**
 * Encapsulation of preferences specific to Garrulo.
 *
 * An instance of {@link GarruloPreferences} is immutable. That is, if you need to change a
 * preference in this set, you will need to change it in the backend and then retrieve a new
 * instance of this object.
 */
public class GarruloPreferences {
  private static volatile GarruloPreferences sInstance;

  private GarruloPreferences() {
  }

  public boolean isNFCEnabled() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    return prefs.getBoolean(res.getString(R.string.pref_key_nfc_onoff), false);
  }

  public boolean shouldSuppressDefaultNotificationSound() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    return prefs.getBoolean(res.getString(R.string.pref_key_suppress_notification_sound), false);
  }

  public boolean shouldAllowDucking() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    return prefs.getBoolean(res.getString(R.string.pref_key_allow_ducking), false);
  }

  public static GarruloPreferences getPreferences() {
    // This variable seems useless, but is actually necessary for the double-checked locking to
    // work correctly.
    GarruloPreferences prefs = sInstance;
    if (prefs == null) {
      synchronized(GarruloPreferences.class) {
        prefs = sInstance;
        if (prefs == null) {
          prefs = sInstance = new GarruloPreferences();
        }
      }
    }

    return prefs;
  }
}
