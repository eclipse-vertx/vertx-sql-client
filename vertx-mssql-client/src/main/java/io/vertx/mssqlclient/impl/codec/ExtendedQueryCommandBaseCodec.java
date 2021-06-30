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
import io.vertx.sqlclient.data.NullValue;
import io.vertx.sqlclient.impl.TupleInternal;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mssqlclient.impl.codec.DataType.*;
import static io.vertx.mssqlclient.impl.codec.MessageType.RPC;

abstract class ExtendedQueryCommandBaseCodec<T> extends QueryCommandBaseCodec<T, ExtendedQueryCommand<T>> {

  ExtendedQueryCommandBaseCodec(TdsMessageCodec tdsMessageCodec, ExtendedQueryCommand<T> cmd) {
    super(tdsMessageCodec, cmd);
  }

  @Override
  void encode() {
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
    if (ps.handle > 0) {
      sendExecRequest();
    } else {
      sendPrepexecRequest();
    }
  }

  @Override
  protected void handleDone(short tokenType) {
    if (tokenType == TokenType.DONEPROC) {
      handleResultSetDone();
    }
  }

  @Override
  protected void handleResultSetDone() {
    super.handleResultSetDone();
    rowCount = 0;
  }

  @Override
  protected void handleReturnValue(ByteBuf payload) {
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.preparedStatement();
    if (ps.handle == 0) {
      short paramNameLength = payload.getUnsignedByte(payload.readerIndex() + 2);
      payload.skipBytes(13 + 2 * paramNameLength);
      ps.handle = payload.readIntLE();
    }
  }

  private void sendPrepexecRequest() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    // RPCReqBatch
    content.writeShortLE(0xFFFF);
    content.writeShortLE(ProcId.Sp_PrepExec);

    // Option flags
    content.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    INTN.encodeParam(content, null, true, ps.handle);

    TupleInternal params = prepexecRequestParams();

    // Param definitions
    String paramDefinitions = parseParamDefinitions(params);
    NVARCHAR.encodeParam(content, null, false, paramDefinitions);

    // SQL text
    NVARCHAR.encodeParam(content, null, false, cmd.sql());

    // Param values
    encodeParams(content, params);

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);
  }

  protected abstract TupleInternal prepexecRequestParams();

  void sendExecRequest() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    writeRpcRequestBatch(content);

    tdsMessageCodec.encoder().writeTdsMessage(RPC, content);
  }

  protected void writeRpcRequestBatch(ByteBuf packet) {
    // RPCReqBatch
    packet.writeShortLE(0xFFFF);
    packet.writeShortLE(ProcId.Sp_Execute);

    // Option flags
    packet.writeShortLE(0x0000);

    // Parameter

    // OUT Parameter
    MSSQLPreparedStatement ps = (MSSQLPreparedStatement) cmd.ps;
    INTN.encodeParam(packet, null, true, ps.handle);

    // Param values
    encodeParams(packet, execRequestParams());
  }

  protected abstract TupleInternal execRequestParams();

  private String parseParamDefinitions(TupleInternal params) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      if (i > 0) {
        stringBuilder.append(",");
      }
      stringBuilder.append("@P").append(i + 1).append(" ");
      Object param = params.getValueInternal(i);
      if (param == null) {
        stringBuilder.append(NULL.paramDefinition(null));
      } else if (param instanceof NullValue) {
        Class<?> valueClass = ((NullValue) param).type();
        DataType dataType = forValueClass(valueClass);
        stringBuilder.append(dataType.paramDefinition(null));
      } else {
        Class<?> valueClass = param.getClass();
        DataType dataType = forValueClass(valueClass);
        stringBuilder.append(dataType.paramDefinition(param));
      }
    }
    return stringBuilder.toString();
  }

  private void encodeParams(ByteBuf buffer, TupleInternal params) {
    for (int i = 0; i < params.size(); i++) {
      String name = "@P" + (i + 1);
      Object value = params.getValue(i);
      if (value == null) {
        NULL.encodeParam(buffer, name, false, null);
      } else {
        DataType dataType = DataType.forValueClass(value.getClass());
        dataType.encodeParam(buffer, name, false, value);
      }
    }
  }
}
