package com.glasstowerstudios.garrulo.nfc;

import android.nfc.NdefRecord;
import android.nfc.Tag;

import com.glasstowerstudios.garrulo.util.ByteUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper for {@link Tag}s, along with the data extracted from each.
 */
public class NfcTagWrapper {
  private Tag mTag;
  private List<NdefRecord> mRecords;

  public NfcTagWrapper(Tag aTag, NdefRecord[] aRecords) {
    mTag = aTag;
    mRecords = new ArrayList<>();
    for (NdefRecord record : aRecords) {
      mRecords.add(record);
    }
  }

  public Tag getTag() {
    return mTag;
  }

  public List<NdefRecord> getNdefRecords() {
    return Collections.unmodifiableList(mRecords);
  }

  /**
   * Retrieve a string representing the ID/serial number of the NFC tag represented by this
   * {@link NfcTagWrapper}.
   *
   * @return A hexidecimal-encoded string, with each byte separated by the colon character,
   *         representing the raw serial number of the NFC tag read.
   */
  public String getTagIDString() {
    byte[] idBytes = getTag().getId();
    return ByteUtil.byteArrayToHexString(idBytes, ':');
  }
}
