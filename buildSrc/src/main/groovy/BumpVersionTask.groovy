package org.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.internal.tasks.options.Option

class BumpVersionTask extends DefaultTask {

  // The prefix we're going to use for this repository/project. Stored for the
  // first time when getPropertyPrefix() is called.
  private static String mPropertyPrefix;

  // The name of the new version (e.g. "1.2.3", or "1.0.7-abb9812")
  private String mVersionName;

  // The properties object we'll be working on.
  private Properties mProps;

  // The file from which the properties object will be read initially.
  private File mPropsFile;

  // A flag used to indicate that the patch number should be incremented.
  private boolean mShouldIncrementPatch = false;

  // A flag used to indicate that the minor number should be incremented.
  private boolean mShouldIncrementMinor = false;

  // A flag used to indicate that the major number should be incremented.
  private boolean mShouldIncrementMajor = false;

  // A flag used to indicate whether we are creating a new beta release.
  private boolean mIsBetaRelease = false;

  // The version code. This will always increment by one, since it MUST remain
  // monotonically increasing.
  private int mVersionCode;

  @Option(option = "codeIncrement", description = "Increment the version code, but leave the version name unchanged")
  def setCodeIncrementOnly(boolean aShouldIncrementCodeOnly) {
    if (aShouldIncrementCodeOnly) {
      mShouldIncrementMajor = false;
      mShouldIncrementMinor = false;
      mShouldIncrementMinor = false;
    }
  }

  @Option(option = "betaRelease", description = "Adjust the release name to indicate this is a beta release")
  def setBetaRelease(boolean aIsBetaRelease) {
      mIsBetaRelease = aIsBetaRelease;
      if (mIsBetaRelease) {
        mShouldIncrementMajor = false;
        mShouldIncrementMinor = false;
        mShouldIncrementMinor = false;
      }
  }

  @Option(option = "versionName", description = "Full version name parameter to use")
  def setVersionName(String aVersion) {
    mVersionName = aVersion
  }

  @Option(option = "patch", description = "Bump the 'patch' portion of the version name (e.g. 'Z' in 'X.Y.Z'). This will have no effect if used in combination with --minor or --major.")
  def setShouldIncrementPatch(boolean aShouldIncrementPatch) {
    mShouldIncrementPatch = aShouldIncrementPatch;
  }

  @Option(option = "minor", description = "Bump the 'minor' portion of the version name (e.g. 'Y' in 'X.Y.Z'), and make the 'patch' component 0.")
  def setShouldIncrementMinor(boolean aShouldIncrementMinor) {
    mShouldIncrementMinor = aShouldIncrementMinor;
  }

  @Option(option = "major", description = "Bump the 'major' portion of the version name (e.g. 'X' in 'X.Y.Z'), and make the 'minor' and 'patch' components 0.")
  def setShouldIncrementMajor(boolean aShouldIncrementMajor) {
    mShouldIncrementMajor = aShouldIncrementMajor;
  }

  @TaskAction
  def bumpVersion() {
    Properties props = getProperties();
    def currentVersionCode = props.getProperty(getPropertyPrefix() + "_VERSION_CODE") as int;
    def newVersionName = "";
    if (mShouldIncrementMajor) {
      if (mVersionName != null) {
        // Inform the user that the given version name is going to be ignored.
        logger.warn("A version name, '" + mVersionName + "' was given on the command line, but is being ignored due to the --major option.")
      }
      mVersionName = getVersionNameWithIncrementedMajor();

      // Add 10 to the version code, so we have room for small bug fixes in
      // between releases.
      mVersionCode = currentVersionCode + 10;
    } else if (mShouldIncrementMinor) {
      if (mVersionName != null) {
        // Inform the user that the given version name is going to be ignored.
        logger.warn("A version name, '" + mVersionName + "' was given on the command line, but is being ignored due to the --minor option.")
      }
      mVersionName = getVersionNameWithIncrementedMinor();

      // Add 10 to the version code, so we have room for small bug fixes in
      // between releases.
      mVersionCode = currentVersionCode + 10;
    } else if (mShouldIncrementPatch) {
      mVersionName = getVersionNameWithIncrementedPatch();
      mVersionCode = currentVersionCode + 1;
    } else {
      mVersionCode = currentVersionCode + 1;
      mVersionName = getUnchangedVersionName();
    }

    if (mIsBetaRelease) {
      mVersionName = mVersionName + "-BETA";
    }

    props.setProperty(getPropertyPrefix() + "_VERSION_NAME", mVersionName as String);
    props.setProperty(getPropertyPrefix() + "_VERSION_CODE", mVersionCode as String);

    props.store(getPropertiesFile().newWriter(), null);
  }

  private String getUnchangedVersionName() {
      String[] portions = getVersionNameComponents();
      return portions.collect{ it }.join(".");
  }

  private String getVersionNameWithIncrementedPatch() {
    String[] portions = getVersionNameComponents();
    def leastSignificantPortion = portions[portions.length - 1] as int;
    leastSignificantPortion = leastSignificantPortion + 1;
    portions[portions.length - 1] = leastSignificantPortion;
    return portions.collect{ it }.join(".");
  }

  private String getVersionNameWithIncrementedMinor() {
    String[] portions = getVersionNameComponents();
    def minorPortion = portions[portions.length - 2] as int;
    minorPortion = minorPortion + 1;
    portions[portions.length - 2] = minorPortion;
    portions[portions.length - 1] = "0";
    return portions.collect{ it }.join(".");
  }

  private String getVersionNameWithIncrementedMajor() {
    String[] portions = getVersionNameComponents();
    def majorPortion = portions[portions.length - 3] as int;
    majorPortion = majorPortion + 1;
    portions[portions.length - 3] = majorPortion;
    portions[portions.length - 2] = "0";
    portions[portions.length - 1] = "0";
    return portions.collect{ it }.join(".");
  }

  private Properties getProperties() {
    if (mProps == null) {
      def propsFile = getPropertiesFile();
      mProps = new Properties();
      mProps.load(propsFile.newDataInputStream());
    }

    return mProps;
  }

  private File getPropertiesFile() {
    if (mPropsFile == null) {
      mPropsFile = new File("gradle.properties");
    }

    return mPropsFile;
  }

  private int getVersionCode() {
    Properties props = getProperties();
    return props.getProperty(getPropertyPrefix() + "_VERSION_CODE") as int;
  }

  private String[] getVersionNameComponents() {
    Properties props = getProperties();
    def currentVersionName = props.getProperty(getPropertyPrefix() + "_VERSION_NAME") as String;

    // Remove any -ALPHA or -BETA
    currentVersionName = currentVersionName.minus("-BETA").minus("-ALPHA");
    return currentVersionName.tokenize('.').toArray();
  }

  private String getPropertyPrefix() {
    if (mPropertyPrefix == null) {
      Properties props = getProperties();
      mPropertyPrefix = props.getProperty("PROPERTY_PREFIX") as String;
    }

    // If it's still null, then the user didn't specify it in the
    // gradle.properties file, so let's use the project name in all caps.
    if (mPropertyPrefix == null) {
      mPropertyPrefix = project.name.toUpperCase();
    }

    return mPropertyPrefix;
  }

}
