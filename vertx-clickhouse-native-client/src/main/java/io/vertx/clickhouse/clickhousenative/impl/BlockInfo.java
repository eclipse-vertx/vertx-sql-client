package io.vertx.clickhouse.clickhousenative.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ByteBufUtils;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class BlockInfo {
  private static final Logger LOG = LoggerFactory.getLogger(BlockInfo.class);

  private Boolean isOverflows;
  private Integer bucketNum;
  private boolean complete;
  private Integer fieldNum;

  public BlockInfo() {
    isOverflows = false;
    bucketNum = -1;
  }

  public BlockInfo(Boolean isOverflows, Integer bucketNum) {
    this.isOverflows = isOverflows;
    this.bucketNum = bucketNum;
  }

  public void serializeTo(ByteBuf buf) {
    ByteBufUtils.writeULeb128(1, buf);
    buf.writeByte(isOverflows ? 1 : 0);
    ByteBufUtils.writeULeb128(2, buf);
    buf.writeIntLE(bucketNum);
    ByteBufUtils.writeULeb128(0, buf);
  }

  public boolean isComplete() {
    return complete;
  }

  public boolean isPartial() {
    return !complete;
  }

  public void readFrom(ByteBuf buf) {
    while (isPartial()) {
      if (fieldNum == null) {
        fieldNum = ByteBufUtils.readULeb128(buf);
        if (fieldNum == null) {
          return;
        }
      }

      LOG.info("fieldNum: " + fieldNum);
      if (fieldNum == 0) {
        complete = true;
        return;
      }
      if (fieldNum == 1) {
        if (buf.readableBytes() >= 1) {
          isOverflows = buf.readBoolean();
          fieldNum = null;
          LOG.info("isOverflows: " + isOverflows);
        } else {
          return;
        }
      } else if (fieldNum == 2) {
        int readable = buf.readableBytes();
        if (readable >= 4) {
          bucketNum = buf.readIntLE();
          fieldNum = null;
          LOG.info("bucketNum: " + bucketNum);
        } else {
          return;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "BlockInfo{" +
      "isOverflows=" + isOverflows +
      ", bucketNum=" + bucketNum +
      '}';
  }
}
