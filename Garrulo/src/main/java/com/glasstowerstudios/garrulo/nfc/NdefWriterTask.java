package com.glasstowerstudios.garrulo.nfc;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.IOException;

/**
 *
 */
public class NdefWriterTask extends NdefIOTask<NdefOutputData, NfcTagWrapper> {
  private static final String LOGTAG = NdefWriterTask.class.getSimpleName();

  public NdefWriterTask() {
    super();
  }

  public NdefWriterTask(NdefTaskCompletedListener aListener) {
    super(aListener);
  }

  @Override
  protected NfcTagWrapper doInBackground(NdefOutputData... aParams) {
    NfcTagWrapper tagWrapper = aParams[0].getTag();
    NdefMessage message = aParams[0].getMessage();

    Ndef ndef = Ndef.get(tagWrapper.getTag());
    try {
      ndef.connect();
      if (!ndef.isWritable()) {
        // TODO: What should we do here?
        return null;
      }

      ndef.writeNdefMessage(message);

      ndef.close();
    } catch (IOException e) {
      // TODO: What should we do here?
      Log.e(LOGTAG, "Encountered an IOException while trying to write tag data", e);
      return null;
    } catch (FormatException e) {
      // TODO: What should we do here?
      Log.e(LOGTAG, "Encountered a FormatException while trying to write tag data", e);
      return null;
    }

    try {
      ndef.connect();
      NdefMessage rereadMessage = ndef.getNdefMessage();
      ndef.close();
      return new NfcTagWrapper(ndef.getTag(), rereadMessage.getRecords());
    } catch (FormatException e) {
      Log.e(LOGTAG, "Unable to re-read NDEF tag after writing due to malformed data", e);
    } catch (IOException e) {
      Log.e(LOGTAG, "Unable to re-read NDEF tag after writing", e);
    }

    return null;
  }

  @Override
  protected void onPostExecute(NfcTagWrapper aTagWrapper) {
    notifyListenerWriteCompleted(aTagWrapper);
  }
}
