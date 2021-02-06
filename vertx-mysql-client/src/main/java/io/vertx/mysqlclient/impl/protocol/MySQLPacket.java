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

package io.vertx.mysqlclient.impl.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

/**
 * Represents a MySQL packet
 */
public class MySQLPacket extends DefaultByteBufHolder {

  private final int payloadLength;
  private final short sequenceId;

  public MySQLPacket(int payloadLength, short sequenceId, ByteBuf content) {
    super(content);
    this.payloadLength = payloadLength;
    this.sequenceId = sequenceId;
  }

  public int payloadLength() {
    return payloadLength;
  }

  public short sequenceId() {
    return sequenceId;
  }
}
