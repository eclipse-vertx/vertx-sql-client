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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.impl.connection.CommandResponse;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;

class PrepareStatementCodec extends MSSQLCommandCodec<PreparedStatement, PrepareStatementCommand> {
  PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode() {
    // we use sp_prepexec instead of sp_prepare + sp_exec
    PreparedStatement preparedStatement = new MSSQLPreparedStatement(cmd.sql());
    tdsMessageCodec.decoder().fireCommandResponse(CommandResponse.success(preparedStatement));
  }
}
