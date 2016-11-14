package com.glasstowerstudios.garrulo.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

/**
 *
 */
public class NdefUtils {
  public static final String GARRULO_NFC_EXTERNAL_TYPE = "com.glasstowerstudios.garrulo:externaltype";
  public static final String GARRULO_LAUNCHED_FROM_NFC_VALUE = "garrulo-launch";

  public static boolean wasLaunchedFromNFC(NdefMessage aMessage) {
    return containsExternalTypeValue(aMessage, GARRULO_LAUNCHED_FROM_NFC_VALUE);
  }

  public static boolean containsExternalTypeValue(NdefMessage aMessage, String aValue) {
    for (NdefRecord nextRecord : aMessage.getRecords()) {
      String typeString = new String(nextRecord.getType());
      String payloadString = new String(nextRecord.getPayload());
      if (typeString.equals(GARRULO_NFC_EXTERNAL_TYPE) && payloadString.equals(aValue)) {
        return true;
      }
    }

    return false;
  }
}
