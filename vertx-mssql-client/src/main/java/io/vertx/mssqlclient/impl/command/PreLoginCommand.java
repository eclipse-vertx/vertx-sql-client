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

import io.vertx.sqlclient.impl.command.CommandBase;

public class PreLoginCommand extends CommandBase<PreLoginResponse> {

  private final boolean ssl;
  private final boolean fedAuth;

  public PreLoginCommand(boolean ssl) {
    this(ssl, false);
  }

  public PreLoginCommand(boolean ssl, boolean fedAuth) {
    this.ssl = ssl;
    this.fedAuth = fedAuth;
  }

  public boolean sslRequired() {
    return ssl;
  }

  /**
   * @return {@code true} if the client should advertise the {@code FEDAUTHREQUIRED} PRELOGIN option
   */
  public boolean fedAuthRequested() {
    return fedAuth;
  }
}
