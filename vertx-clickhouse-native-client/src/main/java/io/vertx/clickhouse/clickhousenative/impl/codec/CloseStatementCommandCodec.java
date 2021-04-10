/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

public class CloseStatementCommandCodec extends ClickhouseNativeCommandCodec<Void, CloseStatementCommand> {
  public CloseStatementCommandCodec(CloseStatementCommand cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd);
  }

  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);
    ClickhouseNativePreparedStatement stmt = (ClickhouseNativePreparedStatement) cmd.statement();
    if (stmt.isSentQuery()) {
      encoder.getConn().releasePs(stmt.getPsId());
    }
    completionHandler.handle(CommandResponse.success(null));
  }


  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}
