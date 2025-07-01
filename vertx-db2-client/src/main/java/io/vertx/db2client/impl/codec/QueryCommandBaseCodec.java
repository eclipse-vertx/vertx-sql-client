/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.sqlclient.internal.RowDescriptor;
import io.vertx.sqlclient.spi.protocol.QueryCommandBase;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends CommandCodec<Boolean, C> {

  protected ColumnMetaData columnDefinitions;
  protected final boolean isQuery;

  QueryCommandBaseCodec(C cmd) {
    super(cmd);
    this.isQuery = DRDAQueryRequest.isQuery(cmd.sql());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append("@");
    sb.append(Integer.toHexString(hashCode()));
    sb.append(" sql=" + cmd.sql());
    if (!isQuery)
      sb.append(", autoCommit=" + cmd.autoCommit());
    return sb.toString();
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);

    ByteBuf packet = allocateBuffer();
    int packetStartIdx = packet.writerIndex();
    DRDAQueryRequest req = new DRDAQueryRequest(packet, encoder.socketConnection.connMetadata);
    if (isQuery) {
      encodeQuery(req);
    } else {
      encodeUpdate(req);
    }
    req.completeCommand();

    sendPacket(packet, packet.writerIndex() - packetStartIdx);
  }

  abstract void encodeQuery(DRDAQueryRequest req);

  abstract void encodeUpdate(DRDAQueryRequest req);

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    if (isQuery) {
      decodeQuery(payload);
    } else {
      decodeUpdate(payload);
    }
  }

  abstract void decodeQuery(ByteBuf payload);

  abstract void decodeUpdate(ByteBuf payload);

  void handleQueryResult(RowResultDecoder<?, T> decoder) {
    Throwable failure = decoder.complete();
    T result = decoder.result();
    RowDescriptor rowDescriptor = decoder.rowDesc;
    int size = decoder.size();
    int updatedCount = decoder.size();
    decoder.reset();
    cmd.resultHandler().handleResult(updatedCount, size, rowDescriptor, result, failure);
  }

}
