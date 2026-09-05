/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.command;

import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.util.Map;

/**
 * An {@link InitCommand} carrying the MSSQL-specific bits of the login exchange.
 * <p>
 * {@link InitCommand} is shared with the other drivers, so its signature cannot grow a
 * Microsoft Entra ID access token. Subclassing keeps the token off the shared API while
 * still dispatching to {@code InitCommandCodec}, which selects the codec with
 * {@code cmd instanceof InitCommand}.
 */
public class MSSQLInitCommand extends InitCommand {

  private final String accessToken;
  private final boolean fedAuthEcho;

  public MSSQLInitCommand(SocketConnectionBase conn, String username, String password, String database,
                          Map<String, String> properties, String accessToken, boolean fedAuthEcho) {
    super(conn, username, password, database, properties);
    this.accessToken = accessToken;
    this.fedAuthEcho = fedAuthEcho;
  }

  /**
   * @return the Entra ID access token in FEDAUTH feature extension or {@code null} for SQL authentication
   */
  public String accessToken() {
    return accessToken;
  }

  /**
   * @return {@code true} when server returned {@code FEDAUTHREQUIRED} in its PRELOGIN response
   */
  public boolean fedAuthEcho() {
    return fedAuthEcho;
  }
}
