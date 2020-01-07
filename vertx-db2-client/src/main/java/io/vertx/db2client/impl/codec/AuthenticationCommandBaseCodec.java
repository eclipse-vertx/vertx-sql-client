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
