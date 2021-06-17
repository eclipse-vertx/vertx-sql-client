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
import io.vertx.core.Handler;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.function.Consumer;

import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedByteLengthString;
import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.readUnsignedShortLengthString;

abstract class MSSQLCommandCodec<R, C extends CommandBase<R>> {
  final C cmd;
  public MSSQLException failure;
  public R result;
  Handler<? super CommandResponse<R>> completionHandler;
  TdsMessageEncoder encoder;

  MSSQLCommandCodec(C cmd) {
    this.cmd = cmd;
  }

  void encode(TdsMessageEncoder encoder) {
    this.encoder = encoder;
  }

  void encodeMessage(MessageType type, MessageStatus status, int processId, Consumer<ByteBuf> payloadEncoder) {
    // FIXME split large message into packets
  }

  abstract void decodeMessage(TdsMessage message, TdsMessageEncoder encoder);

  void handleErrorToken(ByteBuf buffer) {
    // token value has been processed
    int length = buffer.readUnsignedShortLE();

    int number = buffer.readIntLE();
    byte state = buffer.readByte();
    byte severity = buffer.readByte();
    String message = readUnsignedShortLengthString(buffer);
    String serverName = readUnsignedByteLengthString(buffer);
    String procedureName = readUnsignedByteLengthString(buffer);
    int lineNumber = buffer.readIntLE();

    MSSQLException failure = new MSSQLException(number, state, severity, message, serverName, procedureName, lineNumber);

    if (this.failure == null) {
      this.failure = failure;
    } else {
      this.failure.add(failure);
    }
  }

  void complete() {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(failure);
    } else {
      resp = CommandResponse.success(result);
    }
    completionHandler.handle(resp);
  }

}
