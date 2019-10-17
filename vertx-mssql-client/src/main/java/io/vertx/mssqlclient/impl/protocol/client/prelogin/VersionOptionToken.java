package io.vertx.mssqlclient.impl.protocol.client.prelogin;

public final class VersionOptionToken extends OptionToken {
  public static final byte TYPE = 0x00;

  private final short majorVersion;
  private final short minorVersion;
  private final int buildNumber;
  private final int subBuildNumber;

  public VersionOptionToken(short majorVersion, short minorVersion, int buildNumber, int subBuildNumber) {
    super(TYPE, 6);
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.buildNumber = buildNumber;
    this.subBuildNumber = subBuildNumber;
  }

  public short majorVersion() {
    return majorVersion;
  }

  public short minorVersion() {
    return minorVersion;
  }

  public int buildNumber() {
    return buildNumber;
  }

  public int subBuildNumber() {
    return subBuildNumber;
  }
}
