package com.glasstowerstudios.garrulo.nfc;

import android.nfc.Tag;

import java.util.Collections;
import java.util.List;

/**
 * A wrapper for {@link Tag}s, along with the data extracted from each.
 */
public class NfcTagWrapper {
  private Tag mTag;
  private List<String> mData;

  public NfcTagWrapper(Tag aTag, List<String> aData) {
    mTag = aTag;
    mData = aData;
  }

  public Tag getTag() {
    return mTag;
  }

  public List<String> getData() {
    return Collections.unmodifiableList(mData);
  }
}
