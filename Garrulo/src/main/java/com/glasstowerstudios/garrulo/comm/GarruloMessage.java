package com.glasstowerstudios.garrulo.comm;

import android.util.Log;

/**
 * Implementation of a general message. Can be overridden for more specific message types.
 */
public class GarruloMessage {
  private static final String LOGTAG = GarruloMessage.class.getSimpleName();

  private CharSequence mTextContent;
  private String mSender;

  public GarruloMessage(String aSender, CharSequence aContent) {
    mTextContent = aContent;
    Log.d(LOGTAG, "Text Content: " + mTextContent);
    mSender = aSender;
  }

  public CharSequence getTextContent() {
    return mTextContent;
  }

  public String getSender() {
    return mSender;
  }
}
