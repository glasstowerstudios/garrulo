package com.glasstowerstudios.garrulo.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.glasstowerstudios.garrulo.app.GarruloApplication;

/**
 * Container for static methods related to the population and handling of URIs custom to Garrulo.
 */
public class URIUtil {
  // All Garrulo URIs start with garrulo://
  private static final String GARRULO_SCHEME = "garrulo";

  // Possible actions for a Garrulo URI
  private static final String GARRULO_ACTION_LAUNCH = "launch";

  // Possible query parameters
  private static final String GARRULO_TAG_ID_PARAM = "tagid={id}";
  private static final String GARRULO_VERSION_PARAM = "version={version}";

  public static Uri getLaunchUriForCurrentVersion(String aTagId) {
    String currentVersion = GarruloApplication.getInstance().getApplicationVersion();
    return getLaunchUri(aTagId, currentVersion);
  }

  public static Uri getLaunchUri() {
    return getLaunchUri("", "");
  }

  public static Uri getLaunchUri(String aTagId) {
    return getLaunchUri(aTagId, "");
  }

  public static Uri getLaunchUri(@NonNull String aTagId, @NonNull String aVersion) {
    Uri.Builder bldr = getURIBuilder();
    bldr.authority(GARRULO_ACTION_LAUNCH);
    if (!aTagId.isEmpty()) {
      bldr.appendQueryParameter(GARRULO_TAG_ID_PARAM, aTagId);
    }

    if (!aVersion.isEmpty()) {
      bldr.appendQueryParameter(GARRULO_VERSION_PARAM, aVersion);
    }

    return bldr.build();
  }

  private static Uri.Builder getURIBuilder() {
    Uri.Builder bldr = new Uri.Builder();
    bldr.scheme(GARRULO_SCHEME);
    return bldr;
  }
}
