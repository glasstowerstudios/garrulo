package com.glasstowerstudios.garrulo.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.glasstowerstudios.garrulo.R;

/**
 * Communicator that listens for shutdown messages.
 */
public class GarruloCommunicationChannel extends BroadcastReceiver {
  private static final String LOGTAG = GarruloCommunicationChannel.class.getSimpleName();

  public static String COMMAND_STARTUP = "startup";
  public static String COMMAND_SHUTDOWN = "shutdown";

  public enum GarruloCommand {

    UNKNOWN("unknown"),
    STARTUP("startup"),
    SHUTDOWN("shutdown");

    private String mCommandString;

    GarruloCommand(String aCommand) {
      mCommandString = aCommand;
    }

    public String getCommandString() {
      return mCommandString;
    }

    public static void addCommandToIntent(GarruloCommand aCommand, Intent aIntent) {
      aIntent.putExtra("command", aCommand.getCommandString());
    }

    public static GarruloCommand getFromIntent(Intent aIntent) {
      if (aIntent.hasExtra("command")) {
        if (aIntent.getStringExtra("command").equals(SHUTDOWN.getCommandString())) {
          return SHUTDOWN;
        } else if (aIntent.getStringExtra("command").equals(STARTUP.getCommandString())) {
          return STARTUP;
        }
      }

      return UNKNOWN;
    }
  }

  private Context mContext;

  private GarruloCommunicationChannelResponder mResponder;

  public GarruloCommunicationChannel(Context aContext,
                                     GarruloCommunicationChannelResponder aResponder) {
    mContext = aContext;
    mResponder = aResponder;

    IntentFilter communicatorIntentFilter = new IntentFilter();
    communicatorIntentFilter.addAction(
      mContext.getResources().getString(R.string.communicator_intent));
    mContext.registerReceiver(this, communicatorIntentFilter);
  }

  public void communicateCommand(GarruloCommand aCommand) {
    Log.d(LOGTAG, "***** DEBUG_jwir3: Communicating command: " + aCommand.getCommandString());
    Intent commIntent = new Intent(mContext.getResources().getString(R.string.communicator_intent));
    GarruloCommand.addCommandToIntent(aCommand, commIntent);
    mContext.sendBroadcast(commIntent);
  }

  @Override
  public void onReceive(Context aContext, Intent aIntent) {
    switch (GarruloCommand.getFromIntent(aIntent)) {
      case STARTUP:
        mResponder.onStartup();
        break;

      case SHUTDOWN:
        mResponder.onShutdown();
        break;

      default:
        Log.w(LOGTAG, "Unknown Garrulo command, or no command found. Skipping.");
    }
  }

  public void disconnect() {
    mContext.unregisterReceiver(this);
  }
}