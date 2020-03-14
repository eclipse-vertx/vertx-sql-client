/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
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
import io.vertx.sqlclient.impl.command.CommandBase;

import java.util.Map;

public class AuthenticationCommandBase<R> extends CommandBase<R> {
  private final String username;
  private final String password;
  private final String database;
  private final String collation;
  private final Buffer serverRsaPublicKey;
  private final Map<String, String> connectionAttributes;

  public AuthenticationCommandBase(String username, String password, String database, String collation, Buffer serverRsaPublicKey, Map<String, String> connectionAttributes) {
    this.username = username;
    this.password = password;
    this.database = database;
    this.collation = collation;
    this.serverRsaPublicKey = serverRsaPublicKey;
    this.connectionAttributes = connectionAttributes;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String database() {
    return database;
  }

  public String collation() {
    return collation;
  }

  public Buffer serverRsaPublicKey() {
    return serverRsaPublicKey;
  }

  public Map<String, String> connectionAttributes() {
    return connectionAttributes;
  }
}
