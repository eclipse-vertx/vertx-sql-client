package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;

public class BlockStreamProfileInfo {
  private Integer rows;
  private Integer blocks;
  private Integer bytes;
  private Boolean appliedLimit;
  private Integer rowsBeforeLimit;
  private Boolean calculatedRowsBeforeLimit;

  public void readFrom(ByteBuf in) {
    if (rows == null) {
      rows = ByteBufUtils.readULeb128(in);
      if (rows == null) {
        return;
      }
    }
    if (blocks == null) {
      blocks = ByteBufUtils.readULeb128(in);
      if (blocks == null) {
        return;
      }
    }
    if (bytes == null) {
      bytes = ByteBufUtils.readULeb128(in);
      if (bytes == null) {
        return;
      }
    }
    if (appliedLimit == null) {
      if (in.readableBytes() == 0) {
        return;
      }
      appliedLimit = in.readBoolean();
    }
    if (rowsBeforeLimit == null) {
      rowsBeforeLimit = ByteBufUtils.readULeb128(in);
      if (rowsBeforeLimit == null) {
        return;
      }
    }
    if (calculatedRowsBeforeLimit == null) {
      if (in.readableBytes() == 0) {
        return;
      }
      calculatedRowsBeforeLimit = in.readBoolean();
    }
  }

  public Integer getRows() {
    return rows;
  }

  public Integer getBlocks() {
    return blocks;
  }

  public Integer getBytes() {
    return bytes;
  }

  public Boolean getAppliedLimit() {
    return appliedLimit;
  }

  public Integer getRowsBeforeLimit() {
    return rowsBeforeLimit;
  }

  public Boolean getCalculatedRowsBeforeLimit() {
    return calculatedRowsBeforeLimit;
  }

  public boolean isComplete() {
    return !isPartial();
  }

  public boolean isPartial() {
    return calculatedRowsBeforeLimit == null;
  }

  @Override
  public String toString() {
    return "BlockStreamProfileInfo{" +
      "rows=" + rows +
      ", blocks=" + blocks +
      ", bytes=" + bytes +
      ", appliedLimit=" + appliedLimit +
      ", rowsBeforeLimit=" + rowsBeforeLimit +
      ", calculatedRowsBeforeLimit=" + calculatedRowsBeforeLimit +
      '}';
  }
}
