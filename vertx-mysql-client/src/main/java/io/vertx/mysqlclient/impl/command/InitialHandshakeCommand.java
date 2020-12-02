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

package io.vertx.mysqlclient.impl.command;

import io.vertx.core.buffer.Buffer;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.SslMode;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;

import java.nio.charset.Charset;
import java.util.Map;

public class InitialHandshakeCommand extends AuthenticationCommandBase<Connection> {
  private final SocketConnectionBase conn;
  private final SslMode sslMode;
  private final int initialCapabilitiesFlags;
  private final Charset charsetEncoding;
  private final MySQLAuthenticationPlugin authenticationPlugin;

  public InitialHandshakeCommand(SocketConnectionBase conn,
                                 String username,
                                 String password,
                                 String database,
                                 MySQLCollation collation,
                                 Buffer serverRsaPublicKey,
                                 Map<String, String> connectionAttributes,
                                 SslMode sslMode,
                                 int initialCapabilitiesFlags,
                                 Charset charsetEncoding,
                                 MySQLAuthenticationPlugin authenticationPlugin) {
    super(username, password, database, collation, serverRsaPublicKey, connectionAttributes);
    this.conn = conn;
    this.sslMode = sslMode;
    this.initialCapabilitiesFlags = initialCapabilitiesFlags;
    this.charsetEncoding = charsetEncoding;
    this.authenticationPlugin = authenticationPlugin;
  }

  public SocketConnectionBase connection() {
    return conn;
  }

  public SslMode sslMode() {
    return sslMode;
  }

  public int initialCapabilitiesFlags() {
    return initialCapabilitiesFlags;
  }

  public Charset charsetEncoding() {
    return charsetEncoding;
  }

  public MySQLAuthenticationPlugin authenticationPlugin() {
    return authenticationPlugin;
  }
}
