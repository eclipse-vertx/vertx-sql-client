package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public class LowCardinalityColumnReader extends ClickhouseColumnReader {
  public static final long SUPPORTED_SERIALIZATION_VERSION = 1;
  public static final ClickhouseColumn UINT8_KEY_COLUMN = ClickhouseColumns.columnForSpec("UInt8", "lcKeyColumn", null);
  public static final ClickhouseColumn UINT16_KEY_COLUMN = ClickhouseColumns.columnForSpec("UInt16", "lcKeyColumn", null);
  public static final ClickhouseColumn UINT32_KEY_COLUMN = ClickhouseColumns.columnForSpec("UInt32", "lcKeyColumn", null);
  public static final ClickhouseColumn UINT64_KEY_COLUMN = ClickhouseColumns.columnForSpec("UInt64", "lcKeyColumn", null);

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
      keysColumn = uintColumn(keyType).reader(nRows);
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

  static ClickhouseColumn uintColumn(int code) {
    ClickhouseColumn tmp;
    if (code == 0) {
      tmp = UINT8_KEY_COLUMN;
    } else if (code == 1) {
       tmp = UINT16_KEY_COLUMN;
    } else if (code == 2) {
       tmp = UINT32_KEY_COLUMN;
    } else if (code == 3) {
       tmp = UINT64_KEY_COLUMN;
    } else {
      throw new IllegalArgumentException("unknown low-cardinality key-column code " + code);
    }
    return tmp;
  }
}
