package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public class LowCardinalityColumnReader extends ClickhouseColumnReader {
  public static final long SUPPORTED_SERIALIZATION_VERSION = 1;
  private final ClickhouseNativeColumnDescriptor indexDescr;
  private final ClickhouseNativeDatabaseMetadata md;
  private ClickhouseColumnReader indexColumn;
  private Long serType;
  private Long indexSize;
  private Long nKeys;
  Long keysSerializationVersion;

  private ClickhouseColumnReader keysColumn;

  public LowCardinalityColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descr);
    this.indexDescr = descr.copyWithModifiers(false, false);
    this.md = md;
  }

  @Override
  protected Object readStatePrefix(ClickhouseStreamDataSource in) {
    if (keysSerializationVersion == null) {
      if (in.readableBytes() >= 4) {
        keysSerializationVersion = in.readLongLE();
        if (keysSerializationVersion != SUPPORTED_SERIALIZATION_VERSION) {
          throw new IllegalStateException("unsupported keysSerializationVersion: " + keysSerializationVersion);
        }
      }
    }
    return keysSerializationVersion;
  }

  @Override
  protected BitSet readNullsMap(ClickhouseStreamDataSource in) {
    return null;
  }

  @Override
  protected void readData(ClickhouseStreamDataSource in) {
    if (keysSerializationVersion == null) {
      return;
    }
    if (indexSize == null) {
      if (in.readableBytes() < 8 + 8) {
        return;
      }
      serType = in.readLongLE();
      indexSize = in.readLongLE();
    }
    if (indexSize > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("low cardinality index is too big (" + indexSize + "), max " + Integer.MAX_VALUE);
    }
    if (indexColumn == null) {
      indexColumn = ClickhouseColumns.columnForSpec(indexDescr, md).reader(indexSize.intValue());
    }
    if (indexColumn.isPartial()) {
      indexColumn.readColumn(in);
      if (indexColumn.isPartial()) {
        return;
      }
    }
    if (nKeys == null) {
      if (in.readableBytes() < 8) {
        return;
      }
      nKeys = in.readLongLE();
    }
    int keyType = (int)(serType & 0xf);
    if (keysColumn == null) {
      keysColumn = uintColumn(columnDescriptor.name(), keyType).reader(nRows);
    }
    keysColumn.readColumn(in);
  }

  //called by Array column
  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (isPartial()) {
      readData(in);
    }
    return null;
  }

  @Override
  protected Object[] asObjectsArray(Class<?> desired) {
    return super.asObjectsArrayWithGetElement(desired);
  }

  @Override
  public boolean isPartial() {
    return indexSize == null || indexColumn.isPartial() || nKeys == null || keysColumn.isPartial();
  }

  @Override
  public Object getElement(int rowIdx, Class<?> desired) {
    int key = ((Number)keysColumn.getElement(rowIdx, desired)).intValue();
    if (columnDescriptor.isNullable() && key == 0) {
      return null;
    }
    return indexColumn.getElementInternal(key, desired);
  }

  static ClickhouseColumn uintColumn(String name, int code) {
    ClickhouseNativeColumnDescriptor tmp;
    //TODO smagellan: introduce immutable column descriptors for (U)Ints, reuse cached instances
    if (code == 0) {
      tmp = ClickhouseColumns.columnDescriptorForSpec("UInt8", name);
    } else if (code == 1) {
       tmp = ClickhouseColumns.columnDescriptorForSpec("UInt16", name);
    } else if (code == 2) {
       tmp = ClickhouseColumns.columnDescriptorForSpec("UInt32", name);
    } else if (code == 3) {
       tmp = ClickhouseColumns.columnDescriptorForSpec("UInt64", name);
    } else {
      throw new IllegalArgumentException("unknown low-cardinality key-column code " + code);
    }
    return ClickhouseColumns.columnForSpec(tmp, null);
  }
}
