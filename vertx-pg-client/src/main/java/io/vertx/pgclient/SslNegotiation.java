/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * SSL negotiation mode for PostgreSQL connections.
 * <p>
 * Determines how SSL/TLS is negotiated with the PostgreSQL server.
 * This setting controls the negotiation protocol, while {@link SslMode}
 * controls whether encryption is required.
 *
 * @see <a href="https://www.postgresql.org/docs/current/libpq-connect.html#LIBPQ-CONNECT-SSLNEGOTIATION">PostgreSQL SSL Negotiation</a>
 */
@VertxGen
public enum SslNegotiation {

  /**
   * Traditional PostgreSQL SSL negotiation (default).
   * <p>
   * The client sends an SSL request message and waits for the server's
   * response before establishing the SSL connection. This mode works with
   * all PostgreSQL versions.
   */
  POSTGRES("postgres"),

  /**
   * Direct SSL negotiation (PostgreSQL 17+).
   * <p>
   * The client establishes an SSL connection immediately without sending
   * an SSL request message. This mode:
   * <ul>
   * <li>Reduces connection latency by one round-trip</li>
   * <li>Enables standard SSL proxies (nginx, haproxy) to terminate TLS</li>
   * <li>Supports SNI/ALPN protocol negotiation</li>
   * </ul>
   * <p>
   * <b>Note:</b> Requires PostgreSQL 17 or later. Attempting to use this mode
   * with earlier PostgreSQL versions will result in an SSL handshake failure.
   */
  DIRECT("direct");

  public static final SslNegotiation[] VALUES = SslNegotiation.values();

  public final String value;

  SslNegotiation(String value) {
    this.value = value;
  }

  /**
   * Parse the SSL negotiation mode from a string value.
   *
   * @param value the string value (case-insensitive)
   * @return the corresponding SSL negotiation mode
   * @throws IllegalArgumentException if the value is not recognized
   */
  public static SslNegotiation of(String value) {
    for (SslNegotiation sslNegotiation : VALUES) {
      if (sslNegotiation.value.equalsIgnoreCase(value)) {
        return sslNegotiation;
      }
    }

    throw new IllegalArgumentException("Could not find an appropriate SSL negotiation mode for the value [" + value + "].");
  }
}
