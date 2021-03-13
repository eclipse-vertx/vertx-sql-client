package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;


public class LowCardinalityColumn extends ClickhouseColumn {
  private final ClickhouseNativeColumnDescriptor indexDescr;
  private ClickhouseColumn indexColumn;
  private Long serType;
  private Long indexSize;
  private Long nKeys;
  private Long keysSerializationVersion;

  private ClickhouseColumn keysColumn;

  public LowCardinalityColumn(int nRows, ClickhouseNativeColumnDescriptor descr) {
    super(nRows, descr);
    this.indexDescr = descr.copyWithModifiers(false, false);
  }

  @Override
  protected void readStatePrefix(ClickhouseStreamDataSource in) {
    if (keysSerializationVersion == null) {
      if (in.readableBytes() >= 4) {
        keysSerializationVersion = in.readLongLE();
        if (keysSerializationVersion != 1) {
          throw new IllegalStateException("unsupported keysSerializationVersion: " + keysSerializationVersion);
        }
      }
    }
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
      indexColumn = ClickhouseColumns.columnForSpec(indexDescr, indexSize.intValue());
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
      keysColumn = uintColumn(keyType);
    }
    keysColumn.readColumn(in);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    return null;
  }

  @Override
  public boolean isPartial() {
    return indexSize == null || indexColumn.isPartial() || nKeys == null || keysColumn.isPartial();
  }

  @Override
  public Object getElement(int rowIdx) {
    int key = ((Number)keysColumn.getElement(rowIdx)).intValue();
    if (columnDescriptor.isNullable() && key == 0) {
      return null;
    }
    return indexColumn.getElementInternal(key);
  }

  private ClickhouseColumn uintColumn(int code) {
    ClickhouseNativeColumnDescriptor tmp;
    String name = columnDescriptor.name();
    //TODO smagellan: introduce immutable column readers, reuse cached instances
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
    return ClickhouseColumns.columnForSpec(tmp, nRows);
  }
}
