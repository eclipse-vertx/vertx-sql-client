package io.reactiverse.myclient.impl.protocol.backend;

import java.util.Arrays;

public final class InitialHandshakePacket {
  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html
   */

  private final byte protocolVersion = 10;
  private final String serverVersion;
  private final long connectionId;
  private final int serverCapabilitiesFlags;
  private final short characterSet;
  private final int serverStatusFlags;
  private final byte[] scramble;
  private final String authMethodName;


  public InitialHandshakePacket(String serverVersion,
                                long connectionId,
                                int serverCapabilitiesFlags,
                                short characterSet,
                                int serverStatusFlags,
                                byte[] scramble,
                                String authMethodName) {
    this.serverVersion = serverVersion;
    this.connectionId = connectionId;
    this.serverCapabilitiesFlags = serverCapabilitiesFlags;
    this.characterSet = characterSet;
    this.serverStatusFlags = serverStatusFlags;
    this.scramble = scramble;
    this.authMethodName = authMethodName;
  }

  public byte getProtocolVersion() {
    return protocolVersion;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public long getConnectionId() {
    return connectionId;
  }

  public int getServerCapabilitiesFlags() {
    return serverCapabilitiesFlags;
  }

  public short getCharacterSet() {
    return characterSet;
  }

  public int getServerStatusFlags() {
    return serverStatusFlags;
  }

  public byte[] getScramble() {
    return scramble;
  }

  public String getAuthMethodName() {
    return authMethodName;
  }

  @Override
  public String toString() {
    return "InitialHandshakePacket{" +
      "protocolVersion=" + protocolVersion +
      ", serverVersion='" + serverVersion + '\'' +
      ", connectionId=" + connectionId +
      ", serverCapabilitiesFlags=" + serverCapabilitiesFlags +
      ", characterSet=" + characterSet +
      ", serverStatusFlags=" + serverStatusFlags +
      ", scramble=" + Arrays.toString(scramble) +
      ", authMethodName='" + authMethodName + '\'' +
      '}';
  }
}
