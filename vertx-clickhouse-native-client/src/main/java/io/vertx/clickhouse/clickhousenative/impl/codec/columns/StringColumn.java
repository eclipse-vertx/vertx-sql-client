package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class StringColumn extends ClickhouseColumn {
  public static final byte[] EMPTY = new byte[0];
  private final boolean enableStringCache;

  private final ClickhouseNativeDatabaseMetadata md;
  public StringColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md, boolean enableStringCache) {
    super(descriptor);
    this.md = md;
    this.enableStringCache = enableStringCache;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new StringColumnReader(nRows, descriptor, enableStringCache, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new StringColumnWriter(data, descriptor, md.getStringCharset(), columnIndex);
  }

  @Override
  public Object nullValue() {
    return EMPTY;
  }
}
