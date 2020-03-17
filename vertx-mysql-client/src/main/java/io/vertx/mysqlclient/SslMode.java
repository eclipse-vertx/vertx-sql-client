/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * This parameter specifies the desired security state of the connection to the server.
 * More information can be found in <a href="https://dev.mysql.com/doc/refman/8.0/en/connection-options.html#option_general_ssl-mode">MySQL Reference Manual</a>
 */
@VertxGen
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
