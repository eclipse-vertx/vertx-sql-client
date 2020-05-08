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
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {

  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    ByteBuf packet = allocateBuffer();
    DRDAQueryRequest closeCursor = new DRDAQueryRequest(packet, encoder.connMetadata);
    closeCursor.buildRDBCMM();
    closeCursor.completeCommand();
    sendNonSplitPacket(packet);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
      DRDAQueryResponse closeCursor = new DRDAQueryResponse(payload, encoder.connMetadata);
      closeCursor.readLocalCommit();
      encoder.chctx.channel().close();
  }
}
