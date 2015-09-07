package com.glasstowerstudios.garrulo.tts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.glasstowerstudios.garrulo.pref.GarruloPreferences;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Implementation of an adapter for TextToSpeech (TTS) services that enqueues messages when {@link
 * #speak(String)} is called, and sends them to the TextToSpeech (TTS) engine at given intervals,
 * once the engine is ready.
 *
 * The sending of the messages to the TTS engine happens on a separate thread which periodically
 * polls for new messages in the queueg.
 */
public class QueuedSpeakingAdapter
  extends TTSAdapter
  implements TextToSpeech.OnInitListener, AudioManager.OnAudioFocusChangeListener {

  private static final String LOGTAG = TTSAdapterFactory.class.getSimpleName();

  private boolean mReady = false;
  private TextToSpeech mTts;
  private Deque<String> mSpeakingQueue;
  private Thread mSpeakingThread;
  private AudioManager mAudioManager;
  private boolean mHasAudioFocus = false;
  private String mCurrentlyBeingSpoken;
  private boolean mPaused = false;

  // ConcurrentLinkedDeque is actually in the Android API as far back as API 16, but it wasn't
  // apparently "ready". As such, we can ignore the API warnings for it, provided we don't use a
  // min API < 16.
  @SuppressLint("NewApi")
  @Override
  public void init(Context aContext) {
    mSpeakingQueue = new ConcurrentLinkedDeque<>();
    mTts = new TextToSpeech(aContext, this);
    mAudioManager = (AudioManager) aContext.getSystemService(Context.AUDIO_SERVICE);

    // Start a new thread that's job is to speak whatever is in the queue every
    // 500ms.
    mSpeakingThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          // Note that this currently has a 500ms break between items in the queue, so if we have
          // many items that are designed to be spaced together, this will incur at least a 500ms
          // gap between them.
          speakOneItemFromQueue();
          releaseAudioFocusIfNotNeeded();
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            Log.d(LOGTAG, "Interrupted while waiting to speak. Breaking from speaking loop");
            releaseAudioFocus();
            break;
          }
        }
      }
    });

    mSpeakingThread.start();
  }

  @Override
  public void speak(String aWhat) {
    mSpeakingQueue.add(aWhat);
  }

  @Override
  public boolean isReady() {
    return mReady;
  }

  @Override
  public void shutdown() {
    mTts.shutdown();
    mSpeakingThread.interrupt();
  }

  @Override
  public void pause() {
    if (mTts.isSpeaking()) {
      mTts.stop();
      mSpeakingQueue.addFirst(mCurrentlyBeingSpoken);
      mCurrentlyBeingSpoken = null;
      mPaused = true;
    }
  }

  @Override
  public void resume() {
    mPaused = false;
  }

  @Override
  public boolean isPaused() {
    return mPaused;
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      mReady = true;
    } else {
      Log.e(LOGTAG, "Encountered an error while setting up text to speech");
    }
  }

  /**
   * Send one item (whatever is next in the speaking queue) to the Text-to-Speech engine.
   */
  private void speakOneItemFromQueue() {
    // If the speaking queue is empty, then we don't have any work to do.
    if (mSpeakingQueue.isEmpty()) {
      return;
    }

    // Make sure we can get audio focus before we do anything.
    if (mHasAudioFocus) {
      if (mReady && !isPaused() && !mTts.isSpeaking()) {
        // If we aren't ready, it's ok, because we'll speak at the next go-around.
        mCurrentlyBeingSpoken = mSpeakingQueue.pop();
        mTts.speak(mCurrentlyBeingSpoken, TextToSpeech.QUEUE_ADD, null);
      }
    } else {
      requestAudioFocus();
    }
  }

  /**
   * Perform the necessary interactions with the {@link AudioManager} that will request audio focus
   * for Garrulo.
   *
   * @return The integer result from {@link
   * AudioManager#requestAudioFocus(android.media.AudioManager.OnAudioFocusChangeListener, int, int)}.
   */
  private int requestAudioFocus() {
    GarruloPreferences prefs = GarruloPreferences.getPreferences();
    int hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;

    if (prefs.shouldAllowDucking()) {
      hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
    }

    int audioFocus = mAudioManager.requestAudioFocus(this, AudioManager.USE_DEFAULT_STREAM_TYPE,
                                                     hint);

    if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      mHasAudioFocus = true;
    }

    return audioFocus;
  }

  /**
   * Perform the necessary interactions with the {@link AudioManager} that will relinquish audio
   * focus for Garrulo.
   *
   * @return The integer result from {@link
   * AudioManager#abandonAudioFocus(android.media.AudioManager.OnAudioFocusChangeListener)}.
   */
  private int releaseAudioFocus() {
    int audioFocus = mAudioManager.abandonAudioFocus(this);

    if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      mHasAudioFocus = false;
    }

    return audioFocus;
  }

  /**
   * Relinquish audio focus by calling {@link #releaseAudioFocus()} if it is no longer needed for
   * Garrulo's functionality.
   */
  private void releaseAudioFocusIfNotNeeded() {
    boolean isAudioFocusNeeded = !mSpeakingQueue.isEmpty() || mTts.isSpeaking();
    if (mHasAudioFocus &&
        (!isAudioFocusNeeded || !mReady)) {
      // If the speaking queue is now empty, or we weren't ready, then let's release the audio
      // focus.
      releaseAudioFocus();
    }
  }

  @Override
  public void onAudioFocusChange(int focusChange) {
    switch(focusChange) {
      case AudioManager.AUDIOFOCUS_GAIN:
        mHasAudioFocus = true;
        if (isPaused()) {
          resume();
        }

        break;

      case AudioManager.AUDIOFOCUS_LOSS:
        mHasAudioFocus = false;
        pause();
        break;
    }
  }
}
