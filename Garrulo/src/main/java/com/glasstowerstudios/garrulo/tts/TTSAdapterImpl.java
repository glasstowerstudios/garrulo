package com.glasstowerstudios.garrulo.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of an adapter for TextToSpeech (TTS) services.
 */
public class TTSAdapterImpl
  extends TTSAdapter
  implements TextToSpeech.OnInitListener {

  private static final String LOGTAG = TTSAdapterFactory.class.getSimpleName();

  private boolean mReady = false;
  private TextToSpeech mTts;
  private static List<TextToSpeech.OnInitListener> mListeners =
    new ArrayList<TextToSpeech.OnInitListener>();

  @Override
  public void init(Context aContext) {
    mTts = new TextToSpeech(aContext, this);
  }

  @Override
  public void speak(String aWhat) {
    if (mReady) {
      mTts.speak(aWhat, TextToSpeech.QUEUE_ADD, null);
    }
  }

  @Override
  public void addOnInitListener(TextToSpeech.OnInitListener aListener) {
    if (!mListeners.contains(aListener)) {
      if (mReady) {
        aListener.onInit(TextToSpeech.SUCCESS);
      } else {
        mListeners.add(aListener);
      }
    }
  }

  @Override
  public void removeOnInitListener(TextToSpeech.OnInitListener aListener) {
    mListeners.remove(aListener);
  }

  @Override
  public boolean isReady() {
    return mReady;
  }

  @Override
  public void shutdown() {
    mTts.shutdown();
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      mReady = true;
      for (TextToSpeech.OnInitListener listener : mListeners) {
        listener.onInit(status);
      }
    } else {
      Log.e(LOGTAG, "Encountered an error while setting up text to speech");
    }
  }
}
