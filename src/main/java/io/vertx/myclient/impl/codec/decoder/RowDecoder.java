package io.vertx.myclient.impl.codec.decoder;

import io.netty.buffer.ByteBuf;

public interface RowDecoder {

  void decodeRow(int len, ByteBuf in);

}
