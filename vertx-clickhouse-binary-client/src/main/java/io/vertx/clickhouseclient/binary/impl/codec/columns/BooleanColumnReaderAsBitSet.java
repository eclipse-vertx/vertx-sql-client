package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public class BooleanColumnReaderAsBitSet extends ClickhouseColumnReader {
  protected BooleanColumnReaderAsBitSet(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return ((BitSet)itemsArray).get(rowIdx);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= BooleanColumnReaderAsArray.ELEMENT_SIZE * nRows) {
      BitSet data = new BitSet(nRows);
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data.set(i, in.readBoolean());
        } else {
          in.skipBytes(BooleanColumnReaderAsArray.ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object[] allocateTwoDimArray(Class<?> desired, int dim1, int dim2) {
    if (desired == boolean.class) {
      return new boolean[dim1][dim2];
    }
    return new Boolean[dim1][dim2];
  }

  @Override
  protected Object allocateOneDimArray(Class<?> desired, int length) {
    if (desired == boolean.class) {
      return new boolean[length];
    }
    return new Boolean[length];
  }
}
