/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {
  private static final int PAYLOAD_LENGTH = 1;

  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendQuitCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    // connection will be terminated later
  }

  private void sendQuitCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(encoder.sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_QUIT);

    sendNonSplitPacket(packet);
  }
}
