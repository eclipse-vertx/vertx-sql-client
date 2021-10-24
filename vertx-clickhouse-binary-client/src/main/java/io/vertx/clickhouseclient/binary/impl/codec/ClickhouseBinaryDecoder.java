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

package io.vertx.clickhouseclient.binary.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinarySocketConnection;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.ArrayDeque;
import java.util.List;

public class ClickhouseBinaryDecoder extends ByteToMessageDecoder {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseBinaryDecoder.class);

  private final ArrayDeque<ClickhouseBinaryCommandCodec<?, ?>> inflight;
  private final ClickhouseBinarySocketConnection conn;
  private Handler<? super CommandResponse<Connection>> initHandler;
  private PacketReader packetReader;
  private Boolean hasException;
  private Object errorPacket;

  public ClickhouseBinaryDecoder(ArrayDeque<ClickhouseBinaryCommandCodec<?, ?>> inflight, ClickhouseBinarySocketConnection conn) {
    this.inflight = inflight;
    this.conn = conn;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (inflight.peek() != null) {
      if (inflight.peek() instanceof InitCommandCodec) {
        InitCommandCodec tmp = (InitCommandCodec) inflight.peek();
        initHandler = tmp.completionHandler;
      } else {
        initHandler = null;
      }
      inflight.peek().decode(ctx, in);
    } else {
      LOG.warn("received abandoned data, probably non-existent DB exception after successful login");
      if (hasException == null) {
        hasException = PacketReader.hasException(in);
      }
      if (hasException == null) {
        return;
      }
      if (hasException) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("has exception: " + hasException);
        }
        if (packetReader == null) {
          packetReader = new PacketReader(conn.getDatabaseMetaData(), null, null, conn.lz4Factory());
        }
        errorPacket = packetReader.receivePacket(ctx.alloc(), in);
        if (errorPacket == null) {
          return;
        }

        if (LOG.isDebugEnabled()) {
          LOG.debug("error packet: " + errorPacket);
        }
        Exception ex = (Exception) errorPacket;
        errorPacket = null;
        packetReader = null;
        hasException = null;
        if (initHandler != null) {
          initHandler.handle(CommandResponse.failure(ex));
        }
        throw ex;
      } else {
        LOG.error("unknown abandoned data");
        throw new IllegalStateException("unknown abandoned data");
      }
    }
  }
}
