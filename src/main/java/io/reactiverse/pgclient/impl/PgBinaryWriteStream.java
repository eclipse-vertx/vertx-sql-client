package io.reactiverse.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.copy.CopyTuple;
import io.reactiverse.pgclient.impl.codec.DataTypeCodec;
import java.util.ArrayList;
import java.util.List;

class PgBinaryWriteStream extends PgCopyWriteStreamBase<CopyTuple> {

  private List<DataType> tupleTypes;

  PgBinaryWriteStream(Connection conn) {
    super(conn);
  }

  public void writeHeader() {
    conn.schedule(new CopyDataCommand(this::writeCopyHeader, r -> {
      if (r.failed()) {
        expHandler.handle(r.cause());
      }
    }));
  }

  private void writeCopyHeader(ByteBuf buffer) {
    buffer.writeByte('P');
    buffer.writeByte('G');
    buffer.writeByte('C');
    buffer.writeByte('O');
    buffer.writeByte('P');
    buffer.writeByte('Y');
    buffer.writeByte('\n');
    buffer.writeByte('\377');
    buffer.writeByte('\r');
    buffer.writeByte('\n');
    buffer.writeZero(1);

    buffer.writeInt(0); //no custom flags
    buffer.writeInt(0); //no fields
  }

  @Override
  protected void writeCopyData(CopyTuple tuple, ByteBuf buffer) {
    int copySize = tuple.size();
    synchronized (this) {
      if (tupleTypes == null) { //cache this, it has to be the same each time
        ArrayList<DataType> types = new ArrayList<>(copySize);
        for (int c = 0; c < copySize; c++) {
          types.add(tuple.getDataType(c));
        }
        tupleTypes = types;
      }
    }
    buffer.writeShort((short)copySize);
    for (int c = 0; c < copySize; c++) {
      Object param = tuple.getValue(c);
      if (param == null) {
        // NULL value
        buffer.writeInt(-1);
      } else {
        DataType dataType = tupleTypes.get(c);
        if (dataType.supportsBinary) {
          int idx = buffer.writerIndex();
          buffer.writeInt(0);
          DataTypeCodec.encodeBinary(dataType, param, buffer);
          buffer.setInt(idx, buffer.writerIndex() - idx - 4);
        } else {
          DataTypeCodec.encodeText(dataType, param, buffer);
        }
      }
    }
  }

  @Override
  protected void writeEnd(ByteBuf buffer) {
    buffer.writeShort(-1);
  }
}
