/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
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
import io.vertx.oracleclient.impl.RowReader;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import oracle.jdbc.OracleConnection;

public class OracleCursorFetchCommand<C, R> extends OracleCommand<Boolean> {

  private final QueryResultHandler<R> resultHandler;
  private final int fetch;
  private final RowReader<C, R> rowReader;

  private OracleCursorFetchCommand(OracleConnection oracleConnection, ContextInternal connectionContext, ExtendedQueryCommand<R> cmd, RowReader<C, R> rowReader) {
    super(oracleConnection, connectionContext);
    resultHandler = cmd.resultHandler();
    fetch = cmd.fetch();
    this.rowReader = rowReader;
  }

  public static <U, V> OracleCursorFetchCommand<U, V> create(OracleConnection oracleConnection, ContextInternal connectionContext, ExtendedQueryCommand<V> cmd, RowReader<U, V> rowReader) {
    return new OracleCursorFetchCommand<>(oracleConnection, connectionContext, cmd, rowReader);
  }

  @Override
  protected Future<Boolean> execute() {
    return rowReader.read(fetch).compose(oracleResponse -> {
      oracleResponse.handle(resultHandler);
      return rowReader.hasMore();
    });
  }
}
