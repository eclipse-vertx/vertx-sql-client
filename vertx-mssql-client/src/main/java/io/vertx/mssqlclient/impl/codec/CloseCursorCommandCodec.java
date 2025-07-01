/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.impl.connection.CommandResponse;

import static io.vertx.mssqlclient.impl.codec.DataType.INTN;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;

public class CloseCursorCommandCodec extends MSSQLCommandCodec<Void, CloseCursorCommand> {

  private CursorData cursorData;
  private boolean cursorClosed;

  public CloseCursorCommandCodec(CloseCursorCommand cmd) {
    super(cmd);
  }

  @Override
  void encode() {
    cursorData = tdsMessageCodec.removeCursorData(cmd.id());
    if (cursorData != null && cursorData.serverCursorId > 0) {
      sendCursorClose();
    } else {
      tdsMessageCodec.decoder().fireCommandResponse(CommandResponse.success(null));
    }
  }

  private void sendCursorClose() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    /*
      RPCReqBatch
     */
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_CursorClose);

    // Option flags
    content.writeShortLE(0x0000);

    INTN.encodeParam(content, null, false, cursorData.serverCursorId);

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);
  }

  @Override
  protected void handleDecodingComplete() {
    if (cursorClosed) {
      super.handleDecodingComplete();
    } else {
      cursorClosed = true;
      sendUnprepare();
    }
  }

  private void sendUnprepare() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    /*
      RPCReqBatch
     */
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_Unprepare);

    // Option flags
    content.writeShortLE(0x0000);

    INTN.encodeParam(content, null, false, cursorData.preparedHandle);

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);
  }
}
