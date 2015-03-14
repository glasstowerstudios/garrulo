package com.glasstowerstudios.garrulo.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

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
  implements TextToSpeech.OnInitListener {

  private static final String LOGTAG = TTSAdapterFactory.class.getSimpleName();

  private boolean mReady = false;
  private TextToSpeech mTts;
  private Deque<String> mSpeakingQueue;
  private Thread mSpeakingThread;

  @Override
  public void init(Context aContext) {
    mTts = new TextToSpeech(aContext, this);
    mSpeakingQueue = new ConcurrentLinkedDeque<>();

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
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            Log.d(LOGTAG, "Interrupted while waiting to speak. Breaking from speaking loop");
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
    if (mSpeakingQueue.isEmpty()) {
      return;
    }

    String nextThingToSpeak = mSpeakingQueue.peekFirst();
    if (mReady) {
      mTts.speak(nextThingToSpeak, TextToSpeech.QUEUE_ADD, null);
      mSpeakingQueue.pop();
    }

    // If we weren't ready, it's ok, because we'll speak at the next go-around.
  }
}
