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
import io.vertx.clickhouseclient.binary.ClickhouseConstants;
import io.vertx.clickhouseclient.binary.impl.ClickhouseBinaryDatabaseMetadata;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class QueryProgressInfoReader {
  private static final Logger LOG = LoggerFactory.getLogger(QueryProgressInfoReader.class);

  private final int serverRevision;

  private Integer rows;
  private Integer bytes;
  private Integer totalRows;
  private Integer writtenRows;
  private Integer writtenBytes;

  public QueryProgressInfoReader(ClickhouseBinaryDatabaseMetadata md) {
    this.serverRevision = md.getRevision();
  }

  public QueryProgressInfo readFrom(ByteBuf in) {
    int idxStart = in.readerIndex();
    if (rows == null) {
      rows = ByteBufUtils.readULeb128(in);
      if (rows == null) {
        return null;
      }
    }

    if (bytes == null) {
      bytes = ByteBufUtils.readULeb128(in);
      if (bytes == null) {
        return null;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TOTAL_ROWS_IN_PROGRESS) {
      if (totalRows == null) {
        totalRows = ByteBufUtils.readULeb128(in);
      }
      if (totalRows == null) {
        return null;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_WRITE_INFO) {
      if (writtenRows == null) {
        writtenRows = ByteBufUtils.readULeb128(in);
      }
      if (writtenRows == null) {
        return null;
      }
      if (writtenBytes == null) {
        writtenBytes = ByteBufUtils.readULeb128(in);
        if (writtenBytes == null) {
          return null;
        }
      }
    }
    int idxEnd = in.readerIndex();
    String bufferAsStringConsumed = ByteBufUtil.hexDump(in, idxStart, idxEnd - idxStart);
    if (LOG.isDebugEnabled()) {
      LOG.debug("bufferAsStringConsumed: " + bufferAsStringConsumed);
    }
    return new QueryProgressInfo(rows, bytes, totalRows, writtenRows, writtenBytes);
  }
}
