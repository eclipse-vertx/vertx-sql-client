package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.BlockInfo;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;
import io.vertx.clickhouse.clickhousenative.impl.Pair;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumns;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.*;

public class ColumnOrientedBlockReader {
  private static final Logger LOG = LoggerFactory.getLogger(ColumnOrientedBlockReader.class);

  private final int serverRevision;
  private final ClickhouseNativeDatabaseMetadata md;

  private String tempTableInfo;
  private BlockInfo blockInfo;
  private Integer nColumns;
  private Integer nRows;
  private Map<String, ClickhouseNativeColumnDescriptor> colWithTypes;
  private List<ClickhouseColumn> data;

  private String colName;
  private String colType;
  private ClickhouseColumn columnData;

  public ColumnOrientedBlockReader(ClickhouseNativeDatabaseMetadata md) {
    assert(md != null);
    this.md = md;
    this.serverRevision = md.getRevision();
  }

  public ColumnOrientedBlock readFrom(ByteBuf in) {
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES) {
      if (tempTableInfo == null) {
        tempTableInfo = ByteBufUtils.readPascalString(in);
        LOG.info("tempTableInfo: " + tempTableInfo);
        if (tempTableInfo == null) {
          return null;
        }
      }
    }

    //BlockInputStream.read
    if (blockInfo == null) {
      blockInfo = new BlockInfo();
    }
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
      if (blockInfo.isPartial()) {
        blockInfo.readFrom(in);
        if (blockInfo.isPartial()) {
          return null;
        }
      }
    }
    if (nColumns == null) {
      nColumns = ByteBufUtils.readULeb128(in);
      if (nColumns == null) {
        return null;
      }
      colWithTypes = new LinkedHashMap<>();
    }
    if (nRows == null) {
      nRows = ByteBufUtils.readULeb128(in);
      if (nRows == null) {
        return null;
      }
    }

    if (colWithTypes.size() < nColumns) {
      if (colName == null) {
        colName = ByteBufUtils.readPascalString(in);
        if (colName == null) {
          return null;
        }
      }
      if (colType == null) {
        colType = ByteBufUtils.readPascalString(in);
        if (colType == null) {
          return null;
        }
      }
      colWithTypes.put(colName, ClickhouseColumns.columnDescriptorForSpec(colType, colName));
      if (nRows > 0) {
        if (data == null) {
          data = new ArrayList<>(nColumns);
        }
        if (columnData == null) {
          LOG.info("reading column " + colName + " of type " + colType);
          columnData = ClickhouseColumns.columnForSpec(colName, colWithTypes, nRows);
          columnData.readColumn(in);

          if (columnData.isPartial()) {
            return null;
          } else {
            data.add(columnData);
            columnData = null;
          }
        }
      }
      colName = null;
      colType = null;
    }
    if (colWithTypes.size() == nColumns) {
      LOG.info("nColumns: " + nColumns + "; nRows: " + nRows);
      LOG.info("columns: " + colWithTypes);
      LOG.info("decoded: ColumnOrientedBlock");
      return new ColumnOrientedBlock(colWithTypes, data, blockInfo, md);
    }
    return null;
  }
}
