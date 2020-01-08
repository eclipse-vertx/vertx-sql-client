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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.CommandResponse;

class DB2Decoder extends ByteToMessageDecoder {
	
	private static final Logger LOG = LoggerFactory.getLogger(CloseConnectionCommandCodec.class);

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
            return;
        }
        if (LOG.isDebugEnabled())
        	LOG.debug("received " + payloadLength + " bytes for " + inflight.peek());
        decodePayload(in.readRetainedSlice(payloadLength), payloadLength, in.getShort(in.readerIndex() + 4));
    }
    
    private int computeLength(ByteBuf in) {
        int index = 0;
        final int readableBytes = in.readableBytes();
        boolean dssContinues = true;
        while (dssContinues && index < readableBytes) {
            if (readableBytes >= index + 3)
                dssContinues &= (in.getByte(index + 3) & 0x40) == 0x40;
            else
                dssContinues = false;
            short dssLen = in.getShort(index);
            index += dssLen;
//                System.out.println("  DSS=" + dssLen + " total=" + index);
        }
        return index;
    }

    private void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
        CommandCodec<?,?> ctx = inflight.peek();
        ctx.sequenceId = sequenceId + 1;
        int startIndex = payload.readerIndex();
        try {
            ctx.decodePayload(payload, payloadLength);
        } catch (Throwable t) {
            int i = payload.readerIndex();
            LOG.error("FATAL: Error parsing buffer at index " + i + " / 0x" + Integer.toHexString(i), t);
            payload.readerIndex(startIndex);
            DB2Codec.dumpBuffer(payload, payloadLength, i);
            ctx.completionHandler.handle(CommandResponse.failure(t));
        }
        payload.clear();
        payload.release();
    }
}
