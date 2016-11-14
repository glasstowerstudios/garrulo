package com.glasstowerstudios.garrulo.nfc;

import android.nfc.NdefMessage;

/**
 *
 */
public class NdefOutputData {
  private NfcTagWrapper mTag;
  private NdefMessage mMessage;

  public NdefOutputData(NfcTagWrapper aTag, NdefMessage aMessage) {
    mTag = aTag;
    mMessage = aMessage;
  }

  public NfcTagWrapper getTag() {
    return mTag;
  }

  public NdefMessage getMessage() {
    return mMessage;
  }
}
