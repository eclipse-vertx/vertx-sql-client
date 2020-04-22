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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

class PrepareStatementCodec extends MSSQLCommandCodec<PreparedStatement, PrepareStatementCommand> {
  PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    // we use sp_prepexec instead of sp_prepare + sp_exec
    PreparedStatement preparedStatement = new MSSQLPreparedStatement(cmd.sql(), null, cmd.cacheable());
    completionHandler.handle(CommandResponse.success(preparedStatement));

  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {

  }
}
