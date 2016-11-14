package com.glasstowerstudios.garrulo.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Background task for reading the data from an NFC tag.
 */
public class NdefReaderTask extends NdefIOTask<Tag, NfcTagWrapper> {

  private static final String LOGTAG = NdefReaderTask.class.getSimpleName();

  private static final String UTF8 = "UTF-8";
  private static final String UTF16 = "UTF-16";

  public NdefReaderTask() {
    super();
  }

  public NdefReaderTask(NdefTaskCompletedListener aListener) {
    super(aListener);
  }

  @Override
  protected NfcTagWrapper doInBackground(Tag... params) {
    List<String> data = new ArrayList<>();

    Tag tag = params[0];

    Ndef ndef = Ndef.get(tag);
    if (ndef == null) {
      // NDEF is not supported by this Tag.
      return null;
    }

    NdefMessage ndefMessage = ndef.getCachedNdefMessage();
    NdefRecord[] records = ndefMessage.getRecords();
    for (NdefRecord ndefRecord : records) {
      if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
          && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
        try {
          data.add(readText(ndefRecord));
        } catch (UnsupportedEncodingException e) {
          Log.e(LOGTAG, "Encountered an unsupported encoding while reading from NFC tag", e);
        }
      }
    }

    return new NfcTagWrapper(tag, records);
  }

  /**
   * Retrieve a string of text from an {@link NdefRecord}.
   *
   * @param aRecord The {@link NdefRecord} that contains the text to be read.
   *
   * @return A string with the text contained in the {@link NdefRecord}, if it is of the appropriate
   *         encoding.
   *
   * @throws UnsupportedEncodingException If the text was encoded with something other than UTF-8 or
   *         UTF-16.
   */
  private String readText(NdefRecord aRecord) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

    byte[] payload = aRecord.getPayload();

    // Get the Text Encoding
    String textEncoding = ((payload[0] & 128) == 0) ? UTF8 : UTF16;

    // Get the Language Code
    int languageCodeLength = payload[0] & 0063;

    // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
    // e.g. "en"

    // Get the Text
    return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
  }

  @Override
  protected void onPostExecute(NfcTagWrapper aTagWrapper) {
    notifyListenerReadCompleted(aTagWrapper);
  }

}
