package com.glasstowerstudios.garrulo.nfc;

/**
 * An observer object for receiving notifications when an {@link NdefReaderTask} has been completed.
 */
public interface NdefTaskCompletedListener {

  /**
   * Called when a read operation in an {@link NdefReaderTask} has been completed.
   *
   * @param aTagWrapper The {@link NfcTagWrapper} object created after an operation has been
   *                    completed.
   */
  void onReadCompleted(NfcTagWrapper aTagWrapper);

  void onWriteCompleted(NfcTagWrapper aTagWrapper);
}
