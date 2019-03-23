package io.vertx.mysqlclient.impl;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class CapabilitiesNegotiator {
  /**
   * Determine the capabilities flags to use.
   *
   * @param serverCapabilitiesFlags capabilities received from server
   * @param database                database schema
   * @return negotiated capabilities flags
   */
  public static int negotiate(int serverCapabilitiesFlags, String database) {
    int clientCapabilitiesFlags = clientSupportedCapabilitiesFlags();

    if (database != null && !database.isEmpty()) {
      clientCapabilitiesFlags |= CLIENT_CONNECT_WITH_DB;
    }

    return clientCapabilitiesFlags & serverCapabilitiesFlags;
  }

  /**
   * Define which capabilities this client supports.
   *
   * @return client supported capabilities
   */
  private static int clientSupportedCapabilitiesFlags() {
    int capabilities = 0x00000000;

    capabilities |= CLIENT_PLUGIN_AUTH
      | CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA
      | CLIENT_SECURE_CONNECTION
      | CLIENT_PROTOCOL_41
      | CLIENT_DEPRECATE_EOF
      | CLIENT_TRANSACTIONS
      | CLIENT_MULTI_STATEMENTS
      | CLIENT_MULTI_RESULTS;

    return capabilities;
  }
}
