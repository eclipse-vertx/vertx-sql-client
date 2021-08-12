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
package io.vertx.oracleclient.impl.commands;

import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.oracleclient.OracleConnectOptions;
import oracle.jdbc.OracleConnection;

import java.sql.SQLException;

public class PingCommand extends AbstractCommand<Integer> {
  public PingCommand(OracleConnectOptions options) {
    super(options);
  }

  @Override
  public Future<Integer> execute(OracleConnection conn, ContextInternal context) {
    return context.executeBlocking(p -> {
        int result;
        try {
          result = conn.pingDatabase();

        } catch (SQLException throwables) {
          p.fail(throwables);
          return;
        }
        p.complete(result);
      }
    );
  }
}
