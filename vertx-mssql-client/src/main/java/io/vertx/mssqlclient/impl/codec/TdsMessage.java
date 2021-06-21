/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

import static io.vertx.mssqlclient.impl.codec.MessageStatus.END_OF_MESSAGE;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.MIN_PACKET_LENGTH;
import static io.vertx.mssqlclient.impl.codec.TdsPacket.PACKET_HEADER_SIZE;

public class TdsMessage extends DefaultByteBufHolder {

  private final short type;

  private TdsMessage(short type, ByteBuf data) {
    super(data);
    this.type = type;
  }

  public static TdsMessage createForDecoding(ByteBufAllocator alloc, TdsPacket tdsPacket) {
    ByteBuf data;
    if (tdsPacket.status() == END_OF_MESSAGE) {
      data = tdsPacket.content();
    } else {
      data = alloc.compositeDirectBuffer().addComponent(true, tdsPacket.content());
    }
    return new TdsMessage(tdsPacket.type(), data);
  }

  public static TdsMessage createForEncoding(ByteBufAllocator alloc, short type) {
    return new TdsMessage(type, alloc.ioBuffer(MIN_PACKET_LENGTH - PACKET_HEADER_SIZE));
  }

  public int type() {
    return type;
  }

  public void aggregate(TdsPacket tdsPacket) {
    if (type != tdsPacket.type()) {
      throw new IllegalArgumentException("Message type [" + type + "] does not match packet type [" + tdsPacket.type() + "]");
    }
    CompositeByteBuf content = (CompositeByteBuf) content();
    content.addComponent(true, tdsPacket.content());
  }
}
