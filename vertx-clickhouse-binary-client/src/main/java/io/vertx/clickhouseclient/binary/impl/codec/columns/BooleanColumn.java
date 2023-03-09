package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class BooleanColumn extends ClickhouseColumn {
  public static final Boolean[] EMPTY_BOOLEAN_ARRAY = new Boolean[0];
  public BooleanColumn(ClickhouseBinaryColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new BooleanColumnReaderAsBitSet(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new BooleanColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return Boolean.FALSE;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_BOOLEAN_ARRAY;
  }
}
