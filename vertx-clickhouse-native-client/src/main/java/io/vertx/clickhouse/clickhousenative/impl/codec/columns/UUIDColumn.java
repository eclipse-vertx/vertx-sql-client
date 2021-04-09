package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.UUID;

public class UUIDColumn extends ClickhouseColumn {
  public static final UUID[] EMPTY_UUID_ARRAY = new UUID[0];

  public static final UUID ZERO_UUID = new UUID(0, 0);
  public static final int ELEMENT_SIZE = 16;

  public UUIDColumn(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new UUIDColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new UUIDColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return ZERO_UUID;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_UUID_ARRAY;
  }
}
