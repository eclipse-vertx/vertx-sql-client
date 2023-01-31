package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;


public class BooleanColumnReader extends ClickhouseColumnReader {
  public static final int ELEMENT_SIZE = 1;
  protected BooleanColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return columnDescriptor.isArray() ? super.getElementInternal(rowIdx, desired) : ((BitSet)itemsArray).get(rowIdx);
  }


  protected Object readItems(ClickhouseStreamDataSource in) {
    return columnDescriptor.isArray() ? readItemsArray(in) : readItemsBitSet(in);
  }

  protected Object readItemsBitSet(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      BitSet data = new BitSet(nRows);
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data.set(i, in.readBoolean());
        } else {
          in.skipBytes(ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }

  protected Object readItemsArray(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      boolean[] data = new boolean[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data[i] = in.readBoolean();
        } else {
          in.skipBytes(ELEMENT_SIZE);
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
