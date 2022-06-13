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
import io.vertx.sqlclient.impl.TupleInternal;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mssqlclient.impl.codec.DataType.INTN;
import static io.vertx.mssqlclient.impl.codec.DataType.NVARCHAR;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;

class ExtendedCursorQueryCommandCodec<T> extends ExtendedQueryCommandBaseCodec<T> {

  private final CursorData cursorData;

  ExtendedCursorQueryCommandCodec(TdsMessageCodec tdsMessageCodec, ExtendedQueryCommand<T> cmd) {
    super(tdsMessageCodec, cmd);
    cursorData = tdsMessageCodec.getOrCreateCursorData(cmd.cursorId());
  }

  @Override
  void encode() {
    if (cursorData.preparedHandle == 0) {
      sendCursorPrepExec();
    } else {
      rowResultDecoder = new RowResultDecoder<>(cmd.collector(), cursorData.mssqlRowDesc);
      sendCursorFetch();
    }
  }

  private void sendCursorPrepExec() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    // RPCReqBatch
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_CursorPrepExec);

    // Option flags
    content.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    INTN.encodeParam(content, null, true, 0); // prepared handle
    INTN.encodeParam(content, null, true, 0); // cursor

    TupleInternal params = prepexecRequestParams();

    // Param definitions
    String paramDefinitions = parseParamDefinitions(params);
    NVARCHAR.encodeParam(content, null, false, paramDefinitions);

    // SQL text
    NVARCHAR.encodeParam(content, null, false, cmd.sql());

    int scrollOpt = params.size() == 0 ? 0x8 : 0x1008;
    INTN.encodeParam(content, null, false, scrollOpt);

    int ccOpt = 0x1 | 0x2000;
    INTN.encodeParam(content, null, false, ccOpt);

    INTN.encodeParam(content, null, true, 0); // rowcount

    // Param values
    encodeParams(content, params);

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);
  }

  @Override
  protected void handleResultSetDone() {
    if (cursorData.fetchSent) {
      super.handleResultSetDone();
    }
  }

  @Override
  protected void handleDecodingComplete() {
    if (!cursorData.fetchSent) {
      sendCursorFetch();
    } else {
      result = cursorData.hasMore();
      complete();
    }
  }

  @Override
  protected MSSQLRowDesc createRowDesc(ColumnData[] columnData) {
    return (cursorData.mssqlRowDesc = MSSQLRowDesc.create(columnData, true));
  }

  private void sendCursorFetch() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    // RPCReqBatch
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_CursorFetch);

    // Option flags
    content.writeByte(0x02); // NO METADATA
    content.writeByte(0x00);

    // Parameter

    // OUT Parameter
    INTN.encodeParam(content, null, false, cursorData.serverCursorId); // cursor
    INTN.encodeParam(content, null, false, 0x0002); // NEXT
    INTN.encodeParam(content, null, false, 0x0000); // start row (ignored)
    INTN.encodeParam(content, null, false, cmd.fetch()); // numrows

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);

    if (!cursorData.fetchSent) {
      cursorData.fetchSent = true;
    }
  }

  @Override
  protected void handleReturnValue(ByteBuf payload) {
    short paramNameLength = payload.getUnsignedByte(payload.readerIndex() + 2);
    payload.skipBytes(12 + 2 * paramNameLength);
    Number value = (Number) INTN.decodeValue(payload, null);
    if (value != null) {
      if (cursorData.preparedHandle == 0) {
        cursorData.preparedHandle = value.intValue();
      } else if (cursorData.serverCursorId == 0) {
        cursorData.serverCursorId = value.intValue();
      } else {
        cursorData.rowsTotal = value.intValue();
      }
    }
  }

  @Override
  protected void handleRow(ByteBuf payload) {
    cursorData.rowsFetched++;
    super.handleRow(payload);
  }

  @Override
  protected void handleNbcRow(ByteBuf payload) {
    cursorData.rowsFetched++;
    super.handleNbcRow(payload);
  }

  @Override
  protected TupleInternal prepexecRequestParams() {
    return (TupleInternal) cmd.params();
  }

  @Override
  protected TupleInternal execRequestParams() {
    throw new UnsupportedOperationException();
  }
}
