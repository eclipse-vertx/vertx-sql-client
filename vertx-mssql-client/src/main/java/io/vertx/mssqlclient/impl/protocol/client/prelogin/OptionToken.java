package io.vertx.mssqlclient.impl.protocol.client.prelogin;

public abstract class OptionToken {
  private final byte type;
  private final int optionLength;

  public OptionToken(byte type, int optionLength) {
    this.type = type;
    this.optionLength = optionLength;
  }

  public byte tokenType() {
    return type;
  }

  public int optionLength() {
    return optionLength;
  }
}
