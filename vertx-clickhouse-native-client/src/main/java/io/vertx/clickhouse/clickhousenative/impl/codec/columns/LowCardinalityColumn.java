package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class LowCardinalityColumn extends ClickhouseColumn {
  private final ClickhouseColumn nested;
  private final ClickhouseNativeDatabaseMetadata md;

  public LowCardinalityColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md) {
    super(descriptor);
    this.md = md;
    ClickhouseNativeColumnDescriptor tmp = ClickhouseColumns.columnDescriptorForSpec(descriptor.getNestedType(), descriptor.name());
    nested = ClickhouseColumns.columnForSpec(tmp, md);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new LowCardinalityColumnReader(nRows, descriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new LowCardinalityColumnWriter(data, descriptor, md, columnIndex);
  }

  @Override
  public Object nullValue() {
    return nested.nullValue();
  }

  @Override
  public Object[] emptyArray() {
    return nested.emptyArray();
  }
}
