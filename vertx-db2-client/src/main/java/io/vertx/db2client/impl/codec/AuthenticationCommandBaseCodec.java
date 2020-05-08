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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.command.AuthenticationCommandBase;
import io.vertx.db2client.impl.util.BufferUtils;

abstract class AuthenticationCommandBaseCodec<R, C extends AuthenticationCommandBase<R>> extends CommandCodec<R, C> {

    AuthenticationCommandBaseCodec(C cmd) {
        super(cmd);
    }

    protected final void encodeConnectionAttributes(Map<String, String> clientConnectionAttributes, ByteBuf packet) {
        ByteBuf kv = encoder.chctx.alloc().ioBuffer();
        for (Map.Entry<String, String> attribute : clientConnectionAttributes.entrySet()) {
            BufferUtils.writeLengthEncodedString(kv, attribute.getKey(), StandardCharsets.UTF_8);
            BufferUtils.writeLengthEncodedString(kv, attribute.getValue(), StandardCharsets.UTF_8);
        }
        BufferUtils.writeLengthEncodedInteger(packet, kv.readableBytes());
        packet.writeBytes(kv);
    }
}
