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
  private static final String LOGTAG = GarruloPreferences.class.getSimpleName();

  private static volatile GarruloPreferences sInstance;

  private GarruloPreferences() {
  }

  public boolean isNFCEnabled() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    return prefs.getBoolean(res.getString(R.string.pref_key_nfc_onoff), false);
  }

  public boolean isNFCPollingEnabled() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    return prefs.getBoolean(res.getString(R.string.pref_key_nfc_polling_onoff), false);
  }

  public int getPollingFrequencyInMinutes() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

    String rawPrefValue = prefs.getString(res.getString(R.string.pref_key_nfc_polling_frequency),
                                          "1");
    return Integer.parseInt(rawPrefValue);
  }

  public long getPollingFrequencyInMilliseconds() {
    return 1000;
//    int pollingInMinutes = getPollingFrequencyInMinutes();
//    Log.d(LOGTAG, "***** DEBUG_jwir3: Polling frequency in minutes: " + pollingInMinutes);
//    long pollingInMs = TimeUnit.MILLISECONDS.convert(pollingInMinutes, TimeUnit.MINUTES);
//    return pollingInMs;
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
