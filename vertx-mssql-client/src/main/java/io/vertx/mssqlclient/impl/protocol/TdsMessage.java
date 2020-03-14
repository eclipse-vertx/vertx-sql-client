/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public final class TdsMessage extends DefaultByteBufHolder {
  private final MessageType type;
  private final MessageStatus status;
  private final int processId;

  private TdsMessage(MessageType type, MessageStatus status, int processId, ByteBuf data) {
    super(data);
    this.type = type;
    this.status = status;
    this.processId = processId;
  }

  public static TdsMessage newTdsMessage(MessageType type, MessageStatus status, int processId, ByteBuf data) {
    return new TdsMessage(type, status, processId, data.retainedSlice());
  }

  public static TdsMessage newTdsMessageFromSinglePacket(TdsPacket tdsPacket) {
    ByteBuf packetData = tdsPacket.content().slice();
    return newTdsMessage(tdsPacket.type(), tdsPacket.status(), tdsPacket.processId(), packetData);
  }

  public MessageType type() {
    return type;
  }

  public MessageStatus status() {
    return status;
  }

  public int processId() {
    return processId;
  }
}
