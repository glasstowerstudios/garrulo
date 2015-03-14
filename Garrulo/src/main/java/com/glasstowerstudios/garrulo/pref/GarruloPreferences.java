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
  private boolean mNFCEnabled;
  private boolean mSuppressDefaultNotificationSound;

  private GarruloPreferences() {
    GarruloApplication app = GarruloApplication.getInstance();
    Resources res = app.getResources();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
    mNFCEnabled = prefs.getBoolean(res.getString(R.string.pref_key_nfc_onoff), false);
    mSuppressDefaultNotificationSound =
      prefs.getBoolean(res.getString(R.string.pref_key_suppress_notification_sound), false);
  }

  public boolean isNFCEnabled() {
    return mNFCEnabled;
  }

  public boolean isSuppressDefaultNotificationSound() {
    return mSuppressDefaultNotificationSound;
  }

  public static GarruloPreferences getPreferences() {
    return new GarruloPreferences();
  }
}
