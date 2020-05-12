/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl.codec;

import java.util.ArrayDeque;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.DB2Exception;
import io.vertx.sqlclient.impl.command.CommandResponse;

class DB2Decoder extends ByteToMessageDecoder {

  private static final Logger LOG = LoggerFactory.getLogger(DB2Decoder.class);

  private final ArrayDeque<CommandCodec<?, ?>> inflight;

  DB2Decoder(ArrayDeque<CommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    int payloadLength = computeLength(in);
    if (payloadLength >= DB2Codec.PACKET_PAYLOAD_LENGTH_LIMIT)
      throw new UnsupportedOperationException("TODO @AGG split package decoding not implemented");
    if (payloadLength <= 4)
      throw new IllegalStateException("Illegal payload length: " + payloadLength);
    if (payloadLength > in.readableBytes()) {
      // wait until we have more bytes to read
      LOG.debug("Waiting for more bytes to be available. payload=" + payloadLength + " > readable=" + in.readableBytes());
      return;
    }
    decodePayload(in.readRetainedSlice(payloadLength), payloadLength);
  }

  private int computeLength(ByteBuf in) {
    int ridx = in.readerIndex();
    int index = 0;
    final int readableBytes = in.readableBytes();
    boolean dssContinues = true;
    while (dssContinues && index < readableBytes) {
      if (readableBytes >= index + 3)
        dssContinues &= (in.getByte(ridx + index + 3) & 0x40) == 0x40;
      else
        dssContinues = false;
      short dssLen = 11; // minimum length of DRDA message
      if (readableBytes >= index + 2)
        dssLen = in.getShort(ridx + index);
      index += dssLen;
    }
    return index;
  }

  private void decodePayload(ByteBuf payload, int payloadLength) {
    CommandCodec<?, ?> ctx = inflight.peek();
    int startIndex = payload.readerIndex();
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("<<< DECODE " + ctx + " (" + payloadLength + " bytes)");
      ctx.decodePayload(payload, payloadLength);
    } catch (DB2Exception connex) {
      // A common connection error occurred, so don't bother with a hex dump and
      // generic error message
      ctx.completionHandler.handle(CommandResponse.failure(connex));
    } catch (Throwable t) {
      int i = payload.readerIndex();
      payload.readerIndex(startIndex);
      StringBuilder sb = new StringBuilder(
          "FATAL: Error parsing buffer at index " + i + " / 0x" + Integer.toHexString(i) + "\n");
      ByteBufUtil.appendPrettyHexDump(sb, payload);
      LOG.error(sb.toString(), t);
      ctx.completionHandler.handle(CommandResponse.failure(t));
    } finally {
      payload.clear();
      payload.release();
    }
  }
}
