package com.glasstowerstudios.garrulo.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * Factory class for adapting TextToSpeech (TTS) services to be used within Garrulo.
 */
public abstract class TTSAdapterFactory {
    private static final String LOGTAG = TTSAdapterFactory.class.getSimpleName();

    private static TTSAdapterFactory sInstance;

    public abstract void init(Context aContext);

    public abstract void speak(String aWhat);

    public abstract void shutdown();

    public abstract void addOnInitListener(TextToSpeech.OnInitListener aListener);

    public abstract void removeOnInitListener(TextToSpeech.OnInitListener aListener);

    public abstract boolean isReady();

    public static TTSAdapterFactory getInstance() {
        if (sInstance == null) {
            String className =  "TTSAdapterFactoryImpl";
            try {
                Class<? extends TTSAdapterFactory> clazz =
                        Class.forName(TTSAdapterFactory.class.getPackage().getName() + "." + className)
                        .asSubclass(TTSAdapterFactory.class);
                sInstance = clazz.newInstance();
            } catch (ClassNotFoundException|IllegalAccessException|InstantiationException e) {
                Log.e(LOGTAG, "Unable to instantiate instance of class: " + className, e);
                throw new IllegalArgumentException(e);
            }
        }

        return sInstance;
    }
}
