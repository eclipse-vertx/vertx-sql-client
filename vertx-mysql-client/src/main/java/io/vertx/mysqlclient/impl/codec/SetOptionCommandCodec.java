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
import io.vertx.mysqlclient.impl.command.SetOptionCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;

class SetOptionCommandCodec extends CommandCodec<Void, SetOptionCommand> {
  private static final int PAYLOAD_LENGTH = 3;

  SetOptionCommandCodec(SetOptionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendSetOptionCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    handleOkPacketOrErrorPacketPayload(payload);
  }

  private void sendSetOptionCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_SET_OPTION);
    packet.writeShortLE(cmd.option().ordinal());

    sendNonSplitPacket(packet);
  }
}
