package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class FixedStringColumn extends ClickhouseColumn {
  protected final ClickhouseNativeDatabaseMetadata md;

  public FixedStringColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md) {
    super(descriptor);
    this.md = md;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new FixedStringColumnReader(nRows, descriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new FixedStringColumnWriter(data, descriptor, md.getStringCharset(), columnIndex);
  }

  @Override
  public Object nullValue() {
    return StringColumn.EMPTY;
  }
}
