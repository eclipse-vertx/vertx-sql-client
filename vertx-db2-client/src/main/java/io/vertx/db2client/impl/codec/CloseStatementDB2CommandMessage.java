/*
 * Copyright (C) 2020 IBM Corporation
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
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.codec.CommandResponse;

class CloseStatementDB2CommandMessage extends DB2CommandMessage<Void, CloseStatementCommand> {

  CloseStatementDB2CommandMessage(CloseStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    DB2PreparedStatement statement = (DB2PreparedStatement) cmd.statement();
    statement.close();
    fireCommandSuccess(null);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    // no statement response
  }
}
