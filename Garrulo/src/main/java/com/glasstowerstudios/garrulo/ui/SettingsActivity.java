package com.glasstowerstudios.garrulo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings. <p/> See <a
 * href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a>
 * for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
  private static final String LOGTAG = SettingsActivity.class.getSimpleName();

  /**
   * Determines whether to always show the simplified settings UI, where settings are presented in a
   * single list. When false, settings are shown as a master/detail two-pane view on tablets. When
   * true, a single pane is shown on tablets.
   */
  private static final boolean ALWAYS_SIMPLE_PREFS = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    setupSimplePreferencesScreen();
  }

  /**
   * Shows the simplified settings UI if the device configuration if the device configuration
   * dictates that a simplified, single-pane UI should be shown.
   *
   * TODO: If we ever decide to support API <= 11, we will need to do something like this.
   */
  private void setupSimplePreferencesScreen() {
    if (!isSimplePreferences()) {
      return;
    }

    // In the simplified UI, fragments are not used at all and we instead
    // use the older PreferenceActivity APIs.

    // Add 'general' preferences.
    addPreferencesFromResource(R.xml.pref_general);
    addPreferencesFromResource(R.xml.pref_communication);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onIsMultiPane() {
    return isXLargeTablet(this) && !isSimplePreferences();
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.pref_headers, target);
  }

  @Override
  public boolean isValidFragment(String aFragmentName) {
    return GeneralPreferenceFragment.class.getName().equals(aFragmentName)
           || CommunicationPreferenceFragment.class.getName().equals(aFragmentName);
  }

  /**
   * Helper method to determine if the device has an extra-large screen. For example, 10" tablets
   * are extra-large.
   */
  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  /**
   * Determines whether the simplified settings UI should be shown. This is true if this is forced
   * via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs like {@link
   * PreferenceFragment}. In these cases, a single-pane "simplified" settings UI should be shown.
   */
  private static boolean isSimplePreferences() {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
  }

  /**
   * This fragment shows general preferences only. It is used when the activity is showing a
   * two-pane settings UI.
   */
  public static class GeneralPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_general);
    }
  }

  /**
   * This fragment shows communication preferences only. It is used when the activity is showing a
   * two-pane settings UI.
   */
  public static class CommunicationPreferenceFragment extends PreferenceFragment {
    private SwitchPreference mNFCPreference;

    private Preference.OnPreferenceChangeListener mCheckNfcPrefsListener =
      new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference aPref, Object aNewValue) {
          String key = aPref.getKey();
          if (key.equals(getResources().getString(R.string.pref_key_nfc_onoff))) {
            Boolean value = (Boolean) aNewValue;

            // If we're enabling NFC, then let's check to make sure it can be enabled.
            if (value) {
              SwitchPreference switchPref = (SwitchPreference) aPref;
              if (switchPref.getKey().equals(GarruloApplication.getInstance().getResources()
                                                               .getString(
                                                                 R.string.pref_key_nfc_onoff))) {
                if (switchPref.isEnabled()) {
                  // Verify NFC is enabled in the System Settings
                  Context appContext = GarruloApplication.getInstance();
                  Resources appResources = appContext.getResources();
                  if (!GarruloApplication.isNFCEnabled()) {
                    // NFC is available but not enabled on this device.
                    DialogInterface.OnClickListener listener =
                      new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface aDialog, int aWhich) {
                          if (aWhich == DialogInterface.BUTTON_NEGATIVE) {
                            mNFCPreference.setChecked(false);
                          } else {
                            Intent settingsIntent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            getActivity().startActivity(settingsIntent);
                          }

                          aDialog.dismiss();
                        }
                      };

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder
                      .setTitle(appResources.getString(R.string.pref_nfc_settings_alert_title));
                    dialogBuilder
                      .setMessage(appResources.getString(R.string.pref_nfc_settings_alert_message));
                    dialogBuilder.setNegativeButton(R.string.pref_nfc_settings_negative, listener);
                    dialogBuilder.setPositiveButton(R.string.pref_nfc_settings_positive, listener);
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                  }
                }
              }
            }
          }

          return true;
        }
      };

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_communication);
    }

    @Override
    public void onResume() {
      super.onResume();

      // Check if NFC is enabled
      if (!GarruloApplication.isNFCEnabled() && mNFCPreference.isChecked()) {
        mNFCPreference.setChecked(false);
      }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater aInflater,
                             ViewGroup aContainer,
                             Bundle aSavedInstanceState) {
      View view = super.onCreateView(aInflater, aContainer, aSavedInstanceState);
      mNFCPreference =
        (SwitchPreference) findPreference(getResources().getString(R.string.pref_key_nfc_onoff));
      mNFCPreference.setOnPreferenceChangeListener(mCheckNfcPrefsListener);
      if (!GarruloApplication.isNFCAvailable()) {
        mNFCPreference.setEnabled(false);
        // NFC is not available on this device.
        Log.e(LOGTAG, "Unable to enable NFC adapter - hardware unavailable");
        Toast.makeText(getActivity(), getResources().getString(R.string.pref_nfc_not_available),
                       Toast.LENGTH_LONG).show();
      }
      return view;
    }
  }
}
