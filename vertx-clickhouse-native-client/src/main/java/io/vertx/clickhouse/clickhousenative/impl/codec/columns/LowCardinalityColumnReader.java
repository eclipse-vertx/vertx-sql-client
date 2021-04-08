package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public class LowCardinalityColumnReader extends ClickhouseColumnReader {
  public static final long SUPPORTED_SERIALIZATION_VERSION = 1;

  private static final ClickhouseColumn[] KEY_COLUMNS = new ClickhouseColumn[] {
    ClickhouseColumns.columnForSpec("UInt8", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt16", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt32", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt64", "lcKeyColumn", null)
  };

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
    int key = ((Number)keysColumn.getElement(rowIdx, Number.class)).intValue();
    if (columnDescriptor.isNullable() && key == 0) {
      return null;
    }
    //TODO: maybe introduce cache here if String encoding was requested (for VARCHAR where desired == String.class || desired == Object.class)
    return indexColumn.getElementInternal(key, desired);
  }

  static ClickhouseColumn uintColumn(int code) {
    if (code < 0 || code >= KEY_COLUMNS.length) {
      throw new IllegalArgumentException("unknown low-cardinality key-column code " + code);
    }
    return KEY_COLUMNS[code];
  }
}
