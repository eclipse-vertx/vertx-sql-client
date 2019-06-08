package io.vertx.mysqlclient.impl.codec;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Deprecated
//TODO we may drop this class later
public final class HandshakeResponse {
  // https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html

  private static final int maxPacketSize = 0xFFFFFF;

  private final String username;
  private final Charset charset;
  private final String password;
  private final String database;
  private final byte[] scramble;
  private final int clientCapabilitiesFlags;
  private final String authMethodName;
  private final Map<String, String> clientConnectAttrs = new HashMap<>();

  public HandshakeResponse(String username,
                           Charset charset,
                           String password,
                           String database,
                           byte[] scramble,
                           int clientCapabilitiesFlags,
                           String authMethodName,
                           Map<String, String> clientConnectAttrs) {
    this.username = username;
    this.charset = charset;
    this.password = password;
    this.database = database;
    this.scramble = scramble;
    this.clientCapabilitiesFlags = clientCapabilitiesFlags;
    this.authMethodName = authMethodName;
    if (clientConnectAttrs != null) {
      this.clientConnectAttrs.putAll(clientConnectAttrs);
    }
  }

  public int getMaxPacketSize() {
    return maxPacketSize;
  }

  public String getUsername() {
    return username;
  }

  public Charset getCharset() {
    return charset;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabase() {
    return database;
  }

  public byte[] getScramble() {
    return scramble;
  }

  public int getClientCapabilitiesFlags() {
    return clientCapabilitiesFlags;
  }

  public String getAuthMethodName() {
    return authMethodName;
  }

  public Map<String, String> getClientConnectAttrs() {
    return clientConnectAttrs;
  }

  @Override
  public String toString() {
    return "HandshakeResponse{" +
      "username='" + username + '\'' +
      ", charset=" + charset +
      ", password='" + password + '\'' +
      ", database='" + database + '\'' +
      ", scramble=" + Arrays.toString(scramble) +
      ", clientCapabilitiesFlags=" + clientCapabilitiesFlags +
      ", authMethodName='" + authMethodName + '\'' +
      ", clientConnectAttrs=" + clientConnectAttrs +
      '}';
  }
}
