package com.glasstowerstudios.garrulo.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.TagTechnology;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.pref.GarruloPreferences;
import com.glasstowerstudios.garrulo.util.ByteUtil;
import com.glasstowerstudios.garrulo.util.MimeUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * An activity that allows the user to configure an individual NFC tag to work with Garrulo.
 */
public class NfcConfigurationActivity extends Activity {
  private static final String LOGTAG = NfcConfigurationActivity.class.getSimpleName();

  private TextView mNfcIdField;
  private MenuItem mBindToTagAction;

  // Our tag data
  private Tag mTag;

  @Override
  public void onCreate(Bundle aSavedInstanceState) {
    super.onCreate(aSavedInstanceState);

    setContentView(R.layout.activity_nfc_configuration);

    mNfcIdField = (TextView) findViewById(R.id.nfc_id);

    if (getActionBar() != null) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    setupActionBar();
    handleIntent(getIntent());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_nfc_configuration, menu);

    mBindToTagAction = menu.findItem(R.id.action_bind_tag);

    if (mTag != null) {
      if (isGarruloTag(mTag)) {
        mBindToTagAction.setVisible(false);
      }
    }

    return true;
  }

    /**
     * Enables functionality from the action bar.
     */
  private void setupActionBar() {
    ActionBar actionBar = getActionBar();
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

  public void handleIntent(Intent aIntent) {
    String action = aIntent.getAction();
    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

      String type = aIntent.getType();
      if (MimeUtil.TEXT_PLAIN.equals(type)) {

        Tag tag = aIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        new NdefReaderTask().execute(tag);

      } else {
        Log.d(LOGTAG, "An incorrect MIME type was seen: " + type);
      }
    } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

      // In case we would still use the Tech Discovered Intent
      Tag tag = aIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
      String[] techList = tag.getTechList();
      String searchedTech = Ndef.class.getName();

      for (String tech : techList) {
        if (searchedTech.equals(tech)) {
          new NdefReaderTask().execute(tag);
          break;
        }
      }
    }
  }

  /**
   * Determine whether or not a {@link Tag} is bound to Garrulo.
   *
   * @param aTag The {@link Tag} which should be read for data.
   *
   * @return true, if the tag has previously been written/bound by Garrulo; false, otherwise.
   */
  private boolean isGarruloTag(Tag aTag) {
    return true;
  }

  /**
   * Background task for reading the data from an NFC tag.
   */
  private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

    @Override
    protected String doInBackground(Tag... params) {
      Tag tag = params[0];

//      Ndef ndef = Ndef.get(tag);
//      if (ndef == null) {
//        // NDEF is not supported by this Tag.
//        return null;
//      }
//
//      NdefMessage ndefMessage = ndef.getCachedNdefMessage();
//      NdefRecord[] records = ndefMessage.getRecords();
//      for (NdefRecord ndefRecord : records) {
//        if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
//            && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
//          try {
//            return readText(ndefRecord);
//          } catch (UnsupportedEncodingException e) {
//            Log.e(LOGTAG, "Unsupported Encoding", e);
//          }
//        }
//      }
//
//      return null;

      // Start our thread which will poll for the nfc tags' removal.
      new NfcTagPoller(tag).start();

      byte[] idBytes = tag.getId();

      return ByteUtil.byteArrayToHexString(idBytes, ':');
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

      byte[] payload = record.getPayload();

      // Get the Text Encoding
      String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

      // Get the Language Code
      int languageCodeLength = payload[0] & 0063;

      // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
      // e.g. "en"

      // Get the Text
      return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    /**
     * Binds an NDEF tag to Garrulo by writing appropriate data to the tag.
     */
    private void bindTag(Tag aTag) {

    }

    @Override
    protected void onPostExecute(String result) {
      if (result != null) {
        mNfcIdField.setText(result);
      }
    }
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
        Log.d(LOGTAG, "Unable to close TagTechnology!", e);
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
}
