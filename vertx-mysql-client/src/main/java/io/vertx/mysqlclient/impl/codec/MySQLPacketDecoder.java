/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

import static io.vertx.mysqlclient.impl.protocol.Packets.PACKET_PAYLOAD_LENGTH_LIMIT;

public class MySQLPacketDecoder extends LengthFieldBasedFrameDecoder {

  public MySQLPacketDecoder() {
    super(4 + PACKET_PAYLOAD_LENGTH_LIMIT, 0, 3, 1, 0);
  }

  @Override
  protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
    return buf.getUnsignedMediumLE(offset);
  }
}
