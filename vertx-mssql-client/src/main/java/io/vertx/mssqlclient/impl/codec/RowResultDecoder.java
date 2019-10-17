package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.MSSQLRowImpl;
import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  final MSSQLRowDesc desc;

  RowResultDecoder(Collector<Row, C, R> collector, MSSQLRowDesc desc) {
    super(collector);
    this.desc = desc;
  }

  @Override
  public Row decodeRow(int len, ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    for (int c = 0; c < len; c++) {
      Object decoded = null;
      ColumnData columnData = desc.columnDatas[c];
      decoded = MSSQLDataTypeCodec.decode(columnData.dataType(), in);
      row.addValue(decoded);
    }
    return row;
  }
}
