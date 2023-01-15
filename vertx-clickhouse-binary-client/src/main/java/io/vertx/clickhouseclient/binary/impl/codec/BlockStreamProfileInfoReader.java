/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class BlockStreamProfileInfoReader {
  private static final Logger LOG = LoggerFactory.getLogger(BlockStreamProfileInfoReader.class);

  private Integer rows;
  private Integer blocks;
  private Integer bytes;
  private Boolean appliedLimit;
  private Integer rowsBeforeLimit;
  private Boolean calculatedRowsBeforeLimit;

  public BlockStreamProfileInfo readFrom(ByteBuf in) {
    int idxStart = in.readerIndex();
    if (rows == null) {
      rows = ByteBufUtils.readULeb128(in);
      if (rows == null) {
        return null;
      }
    }
    if (blocks == null) {
      blocks = ByteBufUtils.readULeb128(in);
      if (blocks == null) {
        return null;
      }
    }
    if (bytes == null) {
      bytes = ByteBufUtils.readULeb128(in);
      if (bytes == null) {
        return null;
      }
    }
    if (appliedLimit == null) {
      if (in.readableBytes() == 0) {
        return null;
      }
      appliedLimit = in.readBoolean();
    }
    if (rowsBeforeLimit == null) {
      rowsBeforeLimit = ByteBufUtils.readULeb128(in);
      if (rowsBeforeLimit == null) {
        return null;
      }
    }
    if (calculatedRowsBeforeLimit == null) {
      if (in.readableBytes() == 0) {
        return null;
      }
      calculatedRowsBeforeLimit = in.readBoolean();
    }
    if (LOG.isDebugEnabled()) {
      int idxEnd = in.readerIndex();
      String bufferAsStringConsumed = ByteBufUtil.hexDump(in, idxStart, idxEnd - idxStart);
      LOG.debug("bufferAsStringConsumed: " + bufferAsStringConsumed);
    }
    return new BlockStreamProfileInfo(rows, blocks, bytes, appliedLimit, rowsBeforeLimit, calculatedRowsBeforeLimit);
  }
}