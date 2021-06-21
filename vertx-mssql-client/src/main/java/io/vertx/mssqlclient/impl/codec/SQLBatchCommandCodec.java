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
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.nio.charset.StandardCharsets;

import static io.vertx.mssqlclient.impl.codec.MessageType.SQL_BATCH;

class SQLBatchCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {
  SQLBatchCommandCodec(TdsMessageCodec tdsMessageCodec, SimpleQueryCommand<T> cmd) {
    super(tdsMessageCodec, cmd);
  }

  @Override
  void encode() {
    ByteBuf content = tdsMessageCodec.alloc().ioBuffer();

    tdsMessageCodec.encoder().encodeHeaders(content);

    // SQLText
    content.writeCharSequence(cmd.sql(), StandardCharsets.UTF_16LE);

    tdsMessageCodec.encoder().writePacket(SQL_BATCH, content);
  }

  @Override
  protected void handleDone(short tokenType) {
    handleResultSetDone();
  }
}
