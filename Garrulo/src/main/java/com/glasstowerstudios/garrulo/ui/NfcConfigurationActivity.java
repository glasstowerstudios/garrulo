package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.glasstowerstudios.garrulo.nfc.NdefReaderTask;
import com.glasstowerstudios.garrulo.nfc.NdefTaskCompletedListener;
import com.glasstowerstudios.garrulo.nfc.NfcTagWrapper;
import com.glasstowerstudios.garrulo.pref.GarruloPreferences;
import com.glasstowerstudios.garrulo.util.ByteUtil;

import java.io.IOException;

/**
 * An activity that allows the user to configure an individual NFC tag to work with Garrulo.
 */
public class NfcConfigurationActivity
  extends Activity
  implements NdefTaskCompletedListener {
  private static final String LOGTAG = NfcConfigurationActivity.class.getSimpleName();

  private static final String GARRULO_CUSTOM_URI = "garrulo://tagid={id};version={version}";

  private NfcAdapter mNfcAdapter;

  private TextView mNfcIdField;
  private MenuItem mBindToTagAction;
  private MenuItem mUnbindFromTagAction;

  // Our tag data, while we are connected to a tag
  private NfcTagWrapper mTagWrapper;

  @Override
  public void onCreate(Bundle aSavedInstanceState) {
    super.onCreate(aSavedInstanceState);

    setContentView(R.layout.activity_nfc_configuration);

    mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

    if (mNfcAdapter == null) {
      // Nfc is disabled.
      Log.e(LOGTAG, "This device doesn't support NFC. You won't be able to use NFC with Garrulo",
            new RuntimeException());
      finish();
    }

    if (!mNfcAdapter.isEnabled()) {
      Log.e(LOGTAG, "NFC is disabled. You must enable NFC to configure tags with Garrulo",
            new RuntimeException());
      finish();
    }

    mNfcIdField = (TextView) findViewById(R.id.nfc_id);

    if (getActionBar() != null) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // This next statement would be executed if Garrulo were launched as a general utility to
    // handle NFC tags that came into range.
//    handleTag(getIntent());
  }

  @Override
  public void onResume() {
    super.onResume();
    enableForegroundDispatch();
  }

  @Override
  public void onPause() {
    super.onPause();
    disableForegroundDispatch();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_nfc_configuration, menu);

    mBindToTagAction = menu.findItem(R.id.action_bind_tag);
    mUnbindFromTagAction = menu.findItem(R.id.action_unbind_tag);

    adjustUIForData();

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem aItem) {
    switch(aItem.getItemId()) {
      case android.R.id.home:
        this.onBackPressed();
        return true;
    }

    return super.onOptionsItemSelected(aItem);
  }

  public void handleTag(Tag aTag) {
    // Dispatch our tag to the reader task
    NdefReaderTask readerTask = new NdefReaderTask(this);
    readerTask.execute(aTag);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    Tag tag = getTagFromIntent(intent);
    handleTag(tag);
  }

  private Tag getTagFromIntent(Intent aIntent) {
    Tag tag = aIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    return tag;
  }

  @Override
  public void onReadCompleted(NfcTagWrapper aTagWrapper) {
    mTagWrapper = aTagWrapper;
    adjustUIForData();
  }

  private class NfcTagPoller extends Thread {
    private Tag mTag;
    public NfcTagPoller(Tag aTag) {
      mTag = aTag;
    }

    private void closeNfcObject(TagTechnology aTechnology) {
      try {
        aTechnology.close();
      } catch (IOException e) {
        Log.e(LOGTAG, "Unable to close tag technology. This is probably due to moving too far away"
                      + " from the NFC tag.", e);
      }
    }

    @Override
    public void run() {
      boolean done = false;
      while (!done) {
        Ndef nfcObject = Ndef.get(mTag);
        if (nfcObject == null) {
          Log.w(LOGTAG, "Exiting from NFC poll loop because we don't have an Ndef object");
          done = true;
        }

        if (nfcObject != null && !nfcObject.isConnected()) {
          try {
            nfcObject.connect();
          } catch (IOException e) {
            // We were unable to connect to the Ndef tag.
            Log.i(LOGTAG, "Exiting from poll loop because it appears that we were unable to connect"
                          + " to the tag");
            done = true;
          }
        }

        if (!done) {
          Log.i(LOGTAG, "Was able to connect to Ndef tag");
        }

        closeNfcObject(nfcObject);

        try {
          long pollFrequencyMs =
            GarruloPreferences.getPreferences().getPollingFrequencyInMilliseconds();
          Thread.sleep(pollFrequencyMs);
        } catch (InterruptedException e) {
          Log.d(LOGTAG, "Interrupted while trying to sleep. Terminating thread.", e);
          done = true;
        }
      }
    }
  }

  /**
   * Enable NFC foreground dispatching.
   *
   * This allows us to have priority for handling NFC events, if they correspond to the correct
   * tech and MIME type.
   */
  private void enableForegroundDispatch() {
    // Setup a pending intent so that the Android system can give us the NFC tag details as they
    // become available.
    PendingIntent pendingIntent = PendingIntent.getActivity(
      this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    try {
      ndef.addDataType("*/*"); // TODO: We should only specify MIME types that we need
    }
    catch (IntentFilter.MalformedMimeTypeException e) {
      throw new RuntimeException("Failure while reading ndef tag: Malformed MimeType", e);
    }

    String[][] techList = new String[][] {};
    IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef, };

    mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
  }

  /**
   * Disable foreground NFC dispatching.
   *
   * This must be done when the activity is no longer in the foreground.
   */
  private void disableForegroundDispatch() {
    mNfcAdapter.disableForegroundDispatch(this);
  }

  /**
   * Adjust the user interface to show appropriate data that may or may not have been populated.
   */
  private void adjustUIForData() {
    if (mTagWrapper == null) {
      mBindToTagAction.setVisible(false);
      mUnbindFromTagAction.setVisible(false);

      mNfcIdField.setText(getResources().getString(R.string.nfc_tap_tag));

      return;
    }

    mNfcIdField.setText(getTagIDString(mTagWrapper));

    if (isGarruloTag(mTagWrapper)) {
      mBindToTagAction.setVisible(false);
      mUnbindFromTagAction.setVisible(true);
    } else {
      mBindToTagAction.setVisible(true);
      mUnbindFromTagAction.setVisible(false);
    }
  }

  /**
   * Retrieve a string representing the ID/serial number of an NFC tag.
   *
   * @param aTagWrapper The {@link NfcTagWrapper} object retrieved from a physical NFC tag.
   *
   * @return A hexidecimal-encoded string, with each byte separated by the colon character,
   *         representing the raw serial number of the NFC tag read.
   */
  private String getTagIDString(NfcTagWrapper aTagWrapper) {
    byte[] idBytes = aTagWrapper.getTag().getId();
    return ByteUtil.byteArrayToHexString(idBytes, ':');
  }

  /**
   * Determine if a particular {@link NfcTagWrapper} corresponds to a tag that was previously bound
   * to a Garrulo instance.
   *
   * @param aTagWrapper The {@link NfcTagWrapper} object retrieved from a physical NFC tag.
   *
   * @return true, if the NFC tag represented by aTagWrapper contains data indicating it was
   *         previously written to by a Garrulo instance; false, otherwise.
   */
  private boolean isGarruloTag(NfcTagWrapper aTagWrapper) {
    String applicationVersion = GarruloApplication.getInstance().getApplicationVersion();

    for (String nextData : aTagWrapper.getData()) {
      String lowercaseData = nextData.toLowerCase();
      String garruloBindString;

      if (applicationVersion.isEmpty()) {
        garruloBindString = GARRULO_CUSTOM_URI
          .replace("{id}", getTagIDString(aTagWrapper))
          .replace(";version={version}", "")
          .toLowerCase();
      } else {
        garruloBindString = GARRULO_CUSTOM_URI
          .replace("{id}", getTagIDString(aTagWrapper))
          .replace("{version}", GarruloApplication.getInstance().getApplicationVersion())
          .toLowerCase();
      }

      if (lowercaseData.contains(garruloBindString)) {
        return true;
      }
    }

    return false;
  }
}
