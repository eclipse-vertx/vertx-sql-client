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

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.datatype.DataFormat;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import static io.vertx.mysqlclient.impl.protocol.Packets.*;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommand<R>> extends QueryCommandBaseCodec<R, C> {

  protected final MySQLPreparedStatement statement;

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd, DataFormat.BINARY);
    statement = (MySQLPreparedStatement) cmd.preparedStatement();
  }

  @Override
  protected void handleInitPacket(ByteBuf payload) {
    // may receive ERR_Packet, OK_Packet, Binary Protocol Resultset
    int firstByte = payload.getUnsignedByte(payload.readerIndex());
    if (firstByte == OK_PACKET_HEADER) {
      OkPacket okPacket = decodeOkPacketPayload(payload);
      handleSingleResultsetDecodingCompleted(okPacket.serverStatusFlags(), okPacket.affectedRows(), okPacket.lastInsertId());
    } else if (firstByte == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else {
      handleResultsetColumnCountPacketBody(payload);
    }
  }

  @Override
  protected void handleAllResultsetDecodingCompleted() {
    // Close prepare statement
    MySQLPreparedStatement ps = (MySQLPreparedStatement) this.cmd.ps;
    if (ps.closeAfterUsage) {
      CloseStatementCommand cmd = new CloseStatementCommand(ps);
      CloseStatementCommandCodec stmt = new CloseStatementCommandCodec(cmd);
      stmt.completionHandler = ar -> {};
      stmt.encode(encoder);
    }
    super.handleAllResultsetDecodingCompleted();
  }
}
