package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class LowCardinalityColumn extends ClickhouseColumn {
  private final ClickhouseNativeColumnDescriptor indexDescriptor;
  private final ClickhouseColumn indexColumn;
  private final ClickhouseNativeDatabaseMetadata md;

  public LowCardinalityColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md) {
    super(descriptor);
    this.md = md;
    this.indexDescriptor = descriptor.copyWithModifiers(false, false);
    this.indexColumn = ClickhouseColumns.columnForSpec(indexDescriptor, md);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new LowCardinalityColumnReader(nRows, descriptor, indexDescriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new LowCardinalityColumnWriter(data, descriptor, md, columnIndex);
  }

  @Override
  public Object nullValue() {
    return indexColumn.nullValue();
  }

  @Override
  public Object[] emptyArray() {
    return indexColumn.emptyArray();
  }
}
