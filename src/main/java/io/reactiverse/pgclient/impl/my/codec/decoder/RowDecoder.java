package io.reactiverse.pgclient.impl.my.codec.decoder;

import io.netty.buffer.ByteBuf;

public interface RowDecoder {

  void decodeRow(int len, ByteBuf in);

}
