package com.glasstowerstudios.garrulo.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.glasstowerstudios.garrulo.R;
import com.glasstowerstudios.garrulo.nfc.NdefOutputData;
import com.glasstowerstudios.garrulo.nfc.NdefReaderTask;
import com.glasstowerstudios.garrulo.nfc.NdefTaskCompletedListener;
import com.glasstowerstudios.garrulo.nfc.NdefWriterTask;
import com.glasstowerstudios.garrulo.nfc.NfcTagWrapper;

import java.nio.charset.Charset;

/**
 * An activity that allows the user to configure an individual NFC tag to work with Garrulo.
 */
public class NfcConfigurationActivity
  extends Activity
  implements NdefTaskCompletedListener, MenuItem.OnMenuItemClickListener {
  private static final String LOGTAG = NfcConfigurationActivity.class.getSimpleName();

  private NfcAdapter mNfcAdapter;

  private TextView mNfcIdField;
  private MenuItem mBindToTagAction;
  private MenuItem mUnbindFromTagAction;

  private ProgressDialog mProgressDialog;

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
    mBindToTagAction.setOnMenuItemClickListener(this);
    mUnbindFromTagAction = menu.findItem(R.id.action_unbind_tag);
    mUnbindFromTagAction.setOnMenuItemClickListener(this);

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

  @Override
  public void onWriteCompleted(NfcTagWrapper aWrapper) {
    if (aWrapper != null) {
      mTagWrapper = aWrapper;
    }
    adjustUIForData();
    mProgressDialog.dismiss();
  }

  @Override
  public boolean onMenuItemClick(MenuItem aItem) {
    switch(aItem.getItemId()) {
      case R.id.action_bind_tag:
        bindTag(mTagWrapper);
        return true;

      case R.id.action_unbind_tag:
        unbindTag(mTagWrapper);
        return true;
    }

    return false;
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

    // We pass null for both the filters and techlists to ensure we're listening for ALL tags.
    mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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

    mNfcIdField.setText(mTagWrapper.getTagIDString());

    if (isGarruloTag(mTagWrapper)) {
      mBindToTagAction.setVisible(false);
      mUnbindFromTagAction.setVisible(true);
    } else {
      mBindToTagAction.setVisible(true);
      mUnbindFromTagAction.setVisible(false);
    }
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
    String expectedPayload = getApplication().getPackageName();
    for (NdefRecord nextRecord : aTagWrapper.getNdefRecords()) {
      String nextRecordPayload = new String(nextRecord.getPayload(), Charset.forName("UTF-8"));
      Log.d(LOGTAG, "***** DEBUG_jwir3: Saw payload: " + nextRecordPayload);
      Log.d(LOGTAG, "***** DEBUG_jwir3: Comparison:  " + expectedPayload);
      if (nextRecordPayload.equalsIgnoreCase(expectedPayload)) {
        return true;
      }
    }

    return false;
  }

  private void bindTag(NfcTagWrapper aTagWrapper) {
    String nfcWriting = getResources().getString(R.string.nfc_writing);
    mProgressDialog = ProgressDialog.show(this, null, nfcWriting, true, false);

    NdefRecord extRecord = NdefRecord.createExternal(this.getPackageName(), "externalType",
                                                     "garrulo-launch".getBytes());

    NdefRecord appRecord = NdefRecord.createApplicationRecord(this.getPackageName());
    NdefRecord[] recordsToWrite = { extRecord, appRecord };

    NdefMessage messageToWrite = new NdefMessage(recordsToWrite);
    NdefOutputData data = new NdefOutputData(aTagWrapper, messageToWrite);
    NdefWriterTask outputTask = new NdefWriterTask(this);
    outputTask.execute(data);
  }

  private void unbindTag(NfcTagWrapper aTagWrapper) {
    String nfcWriting = getResources().getString(R.string.nfc_writing);
    mProgressDialog = ProgressDialog.show(this, null, nfcWriting, true, false);

    NdefRecord emptyRecord = new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null);

    NdefRecord[] recordsToWrite = { emptyRecord };

    NdefMessage messageToWrite = new NdefMessage(recordsToWrite);
    NdefOutputData data = new NdefOutputData(aTagWrapper, messageToWrite);
    NdefWriterTask outputTask = new NdefWriterTask(this);
    outputTask.execute(data);
  }
}
