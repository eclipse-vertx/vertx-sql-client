package io.vertx.mysqlclient;

/**
 * This parameter specifies the desired security state of the connection to the server.
 * More information can be found in <a href="https://dev.mysql.com/doc/refman/8.0/en/connection-options.html#option_general_ssl-mode">MySQL Reference Manual</a>
 */
public enum SslMode {

  /**
   * establish an unencrypted connection.
   */
  DISABLED("disabled"),

  /**
   * establish an encrypted connection if the server supports encrypted connections, falling back to an unencrypted connection if an encrypted connection cannot be established.
   */
  PREFERRED("preferred"),

  /**
   * establish an encrypted connection if the server supports encrypted connections. The connection attempt fails if an encrypted connection cannot be established.
   */
  REQUIRED("required"),

  /**
   * Like REQUIRED, but additionally verify the server Certificate Authority (CA) certificate against the configured CA certificates. The connection attempt fails if no valid matching CA certificates are found.
   */
  VERIFY_CA("verify_ca"),

  /**
   * Like VERIFY_CA, but additionally perform host name identity verification by checking the host name the client uses for connecting to the server against the identity in the certificate that the server sends to the client.
   */
  VERIFY_IDENTITY("verify_identity");

  public static final SslMode[] VALUES = SslMode.values();

  public final String value;

  SslMode(String value) {
    this.value = value;
  }

  public static SslMode of(String value) {
    for (SslMode sslMode : VALUES) {
      if (sslMode.value.equalsIgnoreCase(value)) {
        return sslMode;
      }
    }

    throw new IllegalArgumentException("Could not find an appropriate SSL mode for the value [" + value + "].");
  }
}
