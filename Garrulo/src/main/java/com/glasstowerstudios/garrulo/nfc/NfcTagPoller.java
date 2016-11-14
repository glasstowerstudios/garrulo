package com.glasstowerstudios.garrulo.nfc;

import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.TagTechnology;
import android.support.annotation.NonNull;
import android.util.Log;

import com.glasstowerstudios.garrulo.comm.GarruloCommunicationChannel;
import com.glasstowerstudios.garrulo.pref.GarruloPreferences;

import java.io.IOException;

/**
 *
 */
public class NfcTagPoller
  extends Thread {
  private static final String LOGTAG = NfcTagPoller.class.getSimpleName();
  private Tag mTag;

  private GarruloCommunicationChannel mCommChannel;

  public NfcTagPoller(@NonNull Tag aTag, @NonNull GarruloCommunicationChannel aChannel) {
    mTag = aTag;
    mCommChannel = aChannel;
  }

  private void closeNfcObject(TagTechnology aTechnology) {
    try {
      aTechnology.close();
    } catch (IOException e) {
      Log.e(LOGTAG, "Unable to close tag technology. This is probably due to moving too far away"
                    + " from the NFC tag.", e);
    }
  }

  @Override
  public void run() {
    boolean done = false;
    while (!done) {
      Ndef nfcObject = Ndef.get(mTag);
      if (nfcObject == null) {
        Log.w(LOGTAG, "Exiting from NFC poll loop because we don't have an Ndef object");
        done = true;
      }

      if (nfcObject != null && !nfcObject.isConnected()) {
        try {
          nfcObject.connect();
        } catch (IOException e) {
          // We were unable to connect to the Ndef tag.
          Log.i(LOGTAG, "Exiting from poll loop because it appears that we were unable to connect"
                        + " to the tag");
          done = true;
          mCommChannel.communicateCommand(GarruloCommunicationChannel.GarruloCommand.SHUTDOWN);
        }
      }

      if (!done) {
        Log.i(LOGTAG, "Was able to connect to Ndef tag");
      }

      closeNfcObject(nfcObject);

      try {
        long pollFrequencyMs =
          GarruloPreferences.getPreferences().getPollingFrequencyInMilliseconds();
        Thread.sleep(pollFrequencyMs);
      } catch (InterruptedException e) {
        Log.d(LOGTAG, "Interrupted while trying to sleep. Terminating thread.", e);
        done = true;
      }
    }
  }
}

//public class NfcTagPoller extends Thread {
//
//  private Tag mNfcTag;
//  private long mNfcPollingFrequency;
//
//  public NfcTagPoller(Tag aTag) {
//    mNfcTag = aTag;
//    GarruloPreferences prefs = GarruloPreferences.getPreferences();
//    mNfcPollingFrequency = prefs.getPollingFrequencyInMilliseconds();
//    Log.d(LOGTAG, "***** DEBUG_jwir3: Polling every " + mNfcPollingFrequency + "ms");
//  }
//
//  public void run() {
//    while (true) {
//      // Wake up every X ms and check to see if the nfc tag is available and whether polling is
//      // enabled (if polling was disabled between now and the last wakeup time, then we should
//      // simply stop this thread).
//      try {
//        Thread.sleep(mNfcPollingFrequency);
//      } catch (InterruptedException e) {
//        Log.e(LOGTAG, "NFC polling thread was interrupted while sleeping", e);
//        break;
//      }
//
//      if (!GarruloPreferences.getPreferences().isNFCPollingEnabled()) {
//        // NFC polling was disabled, so just exit the thread.
//        break;
//      }
//
//      // If the nfc tag is unavailable, then we should stop Garrulo from listening and terminate
//      // this thread.
//      Ndef ndefTag = Ndef.get(mNfcTag);
//      try {
//        if (!ndefTag.isConnected()) {
//          ndefTag.connect();
//        }
//      } catch (IOException e) {
//        Log.d(LOGTAG, "***** DEBUG_jwir3: NDEF tag apparently went out of scope");
//        break;
//      }
//    }
//  }
//}
