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

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mssqlclient.impl.codec.DataType.INTN;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;

class CloseStatementCommandCodec extends MSSQLCommandCodec<Void, CloseStatementCommand> {

  CloseStatementCommandCodec(TdsMessageCodec tdsMessageCodec, CloseStatementCommand cmd) {
    super(tdsMessageCodec, cmd);
  }

  @Override
  void encode() {
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.statement();
    if (ps.handle > 0) {
      sendUnprepareRequest();
    } else {
      completionHandler.handle(CommandResponse.success(null));
    }
  }

  private void sendUnprepareRequest() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    /*
      RPCReqBatch
     */
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_Unprepare);

    // Option flags
    content.writeShortLE(0x0000);

    INTN.encodeParam(content, null, false, ((MSSQLPreparedStatement) cmd.statement()).handle);

    tdsMessageCodec.encoder().writePacket(RPC, content);
  }
}
