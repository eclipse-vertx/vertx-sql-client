package io.reactiverse.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import java.nio.charset.StandardCharsets;

class PgTextWriteStream extends PgCopyWriteStreamBase<Buffer> {

  PgTextWriteStream(Connection conn) {
    super(conn);
  }

  @Override
  protected void writeCopyData(Buffer data, ByteBuf buffer) {
    buffer.writeBytes(data.getByteBuf());
  }

  @Override
  protected void writeEnd(ByteBuf buffer) {
    buffer.writeCharSequence("\\.", StandardCharsets.UTF_8);
  }

  @Override
  protected void writeHeader() {

  }
}
