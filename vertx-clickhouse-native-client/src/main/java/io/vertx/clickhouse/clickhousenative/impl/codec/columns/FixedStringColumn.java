package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class FixedStringColumn extends ClickhouseColumn {
  protected final ClickhouseNativeDatabaseMetadata md;
  private final boolean enableStringCache;

  public FixedStringColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md, boolean enableStringCache) {
    super(descriptor);
    this.md = md;
    this.enableStringCache = enableStringCache;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new FixedStringColumnReader(nRows, descriptor, enableStringCache, md);
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
