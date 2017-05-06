package com.github.pgasync.impl.netty;

import com.github.pgasync.impl.io.AuthenticationDecoder;
import com.github.pgasync.impl.io.CommandCompleteDecoder;
import com.github.pgasync.impl.io.DataRowDecoder;
import com.github.pgasync.impl.io.Decoder;
import com.github.pgasync.impl.io.ErrorResponseDecoder;
import com.github.pgasync.impl.io.NotificationResponseDecoder;
import com.github.pgasync.impl.io.ReadyForQueryDecoder;
import com.github.pgasync.impl.io.RowDescriptionDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ByteBufMessageDecoderExt extends ByteBufMessageDecoder {

  static final Map<Byte,Decoder<?>> DECODERS = new HashMap<>();
  static {
    for (Decoder<?> decoder : new Decoder<?>[] {
      new ErrorResponseDecoder(),
      new AuthenticationDecoder(),
      new ReadyForQueryDecoder(),
      new RowDescriptionDecoder(),
      new CommandCompleteDecoder(),
      new DataRowDecoder(),
      new NotificationResponseDecoder() }) {
      DECODERS.put(decoder.getMessageId(), decoder);
    }
  }

  private Object previous;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() < 5) {
      return;
    }

    byte id = in.readByte();
    int length = in.readInt();

    Decoder<?> decoder = DECODERS.get(id);
    try {
      if (decoder != null) {
        ByteBuffer buffer = in.nioBuffer();
        Object read = decoder.read(buffer);
        previous = read;
        out.add(read);
        in.skipBytes(buffer.position());
      } else {
        System.out.println("UNKNOWN decoder " + id + " " + previous);
        in.skipBytes(length - 4);
      }
    } catch (Throwable t) {
      // broad catch as otherwise the exception is silently dropped
      ctx.fireExceptionCaught(t);
    }
  }
}
