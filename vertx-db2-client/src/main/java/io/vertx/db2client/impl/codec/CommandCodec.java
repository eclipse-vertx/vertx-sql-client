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

import io.netty.buffer.ByteBuf;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

abstract class CommandCodec<R, C extends CommandBase<R>> {
	
    Handler<? super CommandResponse<R>> completionHandler;
    public Throwable failure;
    public R result;
    final C cmd;
    DB2Encoder encoder;

    CommandCodec(C cmd) {
        this.cmd = cmd;
    }

    abstract void decodePayload(ByteBuf payload, int payloadLength);

    void encode(DB2Encoder encoder) {
        this.encoder = encoder;
    }

    ByteBuf allocateBuffer() {
        return encoder.chctx.alloc().ioBuffer();
    }

    ByteBuf allocateBuffer(int capacity) {
        return encoder.chctx.alloc().ioBuffer(capacity);
    }

    void sendPacket(ByteBuf packet, int payloadLength) {
        if (payloadLength >= DB2Codec.PACKET_PAYLOAD_LENGTH_LIMIT) {
            /*
             * The original packet exceeds the limit of packet length, split the packet
             * here. if payload length is exactly 16MBytes-1byte(0xFFFFFF), an empty packet
             * is needed to indicate the termination.
             */
            throw new UnsupportedOperationException("Sending split packets not implemented");
//            sendSplitPacket(packet);
        } else {
            sendNonSplitPacket(packet);
        }
    }

    void sendNonSplitPacket(ByteBuf packet) {
        encoder.chctx.writeAndFlush(packet);
    }
}
