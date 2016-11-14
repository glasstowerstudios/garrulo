package com.glasstowerstudios.garrulo.nfc;

import android.os.AsyncTask;

/**
 *
 */
public abstract class NdefIOTask<T, V> extends AsyncTask<T, Void, V> {

  private NdefTaskCompletedListener mListener;

  /**
   * Constructs an {@link NdefIOTask} without an associated {@link NdefTaskCompletedListener}.
   *
   * This is probably not what you want, given that the task won't actually do anything after
   * completion, unless you use {@link #setNdefTaskCompletedListener(NdefTaskCompletedListener)}
   * prior to calling {@link #execute(Object[])} on this object.
   *
   * @see NdefIOTask#NdefIOTask(NdefTaskCompletedListener)
   */
  public NdefIOTask() {
  }

  /**
   * Constructs an {@link NdefIOTask} with a listener for data after the task completes.
   *
   * @param aListener The {@link NdefTaskCompletedListener} which will have its
   *                  <code>onReadCompleted()</code> or <code>onWriteCompleted()</code> method
   *                  invoked after this task completes its execution.
   */
  public NdefIOTask(NdefTaskCompletedListener aListener) {
    setNdefTaskCompletedListener(aListener);
  }

  /**
   * Set the {@link NdefTaskCompletedListener} for this object.
   *
   * @param aListener The {@link NdefTaskCompletedListener} which will have its
   *                  <code>onReadCompleted()</code> or <code>onWriteCompleted()</code> method
   *                  invoked after this task completes its execution.
   */
  public void setNdefTaskCompletedListener(NdefTaskCompletedListener aListener) {
    mListener = aListener;
  }

  protected void notifyListenerReadCompleted(NfcTagWrapper aTagWrapper) {
    if (mListener != null) {
      mListener.onReadCompleted(aTagWrapper);
    }
  }

  protected void notifyListenerWriteCompleted(NfcTagWrapper aTagWrapper) {
    if (mListener != null) {
      mListener.onWriteCompleted(aTagWrapper);
    }
  }
}
