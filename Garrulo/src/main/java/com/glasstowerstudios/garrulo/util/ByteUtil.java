package com.glasstowerstudios.garrulo.util;

import android.support.annotation.Nullable;

/**
 * Class for working with {@link Byte} objects (and their primitive counterparts).
 */
public class ByteUtil {
  final protected static char[] sHexCharacters = "0123456789ABCDEF".toCharArray();

  /**
   * Convert an array of {@link byte}s or {@link Byte} objects to a string of hex characters.
   *
   * @param aBytes The data which should be converted to a hexadecimal string.
   * @param aSeparator A separator which will be inserted in between each of the bytes after they
   *                   are converted to hex. May be null.
   * @return
   */
  public static String byteArrayToHexString(byte[] aBytes, @Nullable Character aSeparator) {
    char[] hexChars;
    if (aSeparator == null) {
      hexChars = new char[aBytes.length * 2];
    } else {
      hexChars = new char[aBytes.length * 3 - 1];
    }

    for ( int j = 0; j < aBytes.length; j++ ) {
      int v = aBytes[j] & 0xFF;
      if (aSeparator == null) {
        hexChars[j * 2] = sHexCharacters[v >>> 4];
        hexChars[j * 2 + 1] = sHexCharacters[v & 0x0F];
      } else {
        hexChars[j * 3] = sHexCharacters[v >>> 4];
        hexChars[j * 3 + 1] = sHexCharacters[v & 0x0F];
        if (j * 3 + 2 < hexChars.length) {
          hexChars[j * 3 + 2] = aSeparator;
        }
      }
    }

    return new String(hexChars);
  }
}
