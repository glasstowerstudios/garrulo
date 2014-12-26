package com.glasstowerstudios.garrulo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.pref.GarruloPreferences;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String LOGTAG = SettingsActivity.class.getSimpleName();

    // We store a reference to the NFC preference so we can switch it off again if the user doesn't
    // enable NFC.
    private SwitchPreference mNFCPreference;

    private Preference.OnPreferenceChangeListener mCheckSystemSettingsListener =
            new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference aPref, Object aNewValue) {
                    String key = aPref.getKey();
                    if (key.equals(getResources().getString(R.string.pref_key_nfc_onoff))) {
                        Boolean value = (Boolean) aNewValue;

                        // If we're enabling NFC, then let's check to make sure it can be enabled.
                        if (value.booleanValue()) {
                            SwitchPreference switchPref = (SwitchPreference) aPref;
                            if (switchPref.getKey().equals(GarruloApplication.getInstance().getResources().getString(R.string.pref_key_nfc_onoff))) {
                                if (switchPref.isEnabled()) {
                                    // Verify NFC is enabled in the System Settings
                                    Context appContext = GarruloApplication.getInstance();
                                    Resources appResources = appContext.getResources();
                                    if (!isNFCEnabled()) {
                                        // NFC is available but not enabled on this device.
                                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface aDialog, int aWhich) {
                                                if (aWhich == DialogInterface.BUTTON_NEGATIVE) {
                                                    mNFCPreference.setChecked(false);
                                                } else {
                                                    Intent settingsIntent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                                    SettingsActivity.this.startActivity(settingsIntent);
                                                }

                                                aDialog.dismiss();
                                            }
                                        };

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                                        dialogBuilder.setTitle(appResources.getString(R.string.pref_nfc_settings_alert_title));
                                        dialogBuilder.setMessage(appResources.getString(R.string.pref_nfc_settings_alert_message));
                                        dialogBuilder.setNegativeButton(R.string.pref_nfc_settings_negative, listener);
                                        dialogBuilder.setPositiveButton(R.string.pref_nfc_settings_positive, listener);
                                        AlertDialog dialog = dialogBuilder.create();
                                        dialog.show();
                                    }
                                }
                            }
                        }
                    } else if (key.equals(getResources().getString(R.string.pref_key_suppress_notification_sound))) {
                        SwitchPreference suppressPref = (SwitchPreference) aPref;
                        GarruloPreferences allPrefs = GarruloPreferences.getPreferences();
                        if (suppressPref.isChecked() != allPrefs.isSuppressDefaultNotificationSound()) {
                            GarruloApplication app = GarruloApplication.getInstance();
                            if (suppressPref.isChecked()) {
                                app.suppressNotifications();
                            } else {
                                app.unsuppressNotifications();
                            }
                        }
                    }

                    return true;
                }
            };
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if NFC is enabled
        if (!isNFCEnabled() && mNFCPreference.isChecked()) {
            mNFCPreference.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
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
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        mNFCPreference = (SwitchPreference)findPreference(getResources().getString(R.string.pref_key_nfc_onoff));
        mNFCPreference.setOnPreferenceChangeListener(mCheckSystemSettingsListener);
        if (!isNFCAvailable()) {
            mNFCPreference.setEnabled(false);
            // NFC is not available on this device.
            Log.e(LOGTAG, "Unable to enable NFC adapter - hardware unavailable");
            Toast.makeText(this, getResources().getString(R.string.pref_nfc_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * Determines if this device supports near-field communication, and if so, if near-field
     * communication is enabled.
     *
     * @return true, if this device supports NFC and NFC is enabled; false, otherwise.
     */
    private boolean isNFCEnabled() {
        if (!isNFCAvailable()) {
            return false;
        }

        Context appContext = GarruloApplication.getInstance();
        final NfcManager manager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        return adapter.isEnabled();
    }

    /**
     * Determines if the hardware on this device allows for near-field communication.
     *
     * @return true, if this device supports NFC; false, otherwise.
     */
    private boolean isNFCAvailable() {
        Context appContext = GarruloApplication.getInstance();
        final NfcManager manager = (NfcManager) appContext.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        return !(adapter == null);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }
}
