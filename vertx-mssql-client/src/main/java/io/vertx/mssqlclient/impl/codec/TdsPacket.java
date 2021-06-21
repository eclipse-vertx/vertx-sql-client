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
import io.netty.buffer.DefaultByteBufHolder;

public class TdsPacket extends DefaultByteBufHolder {

  public static final int PACKET_HEADER_SIZE = 8;
  public static final int MIN_PACKET_LENGTH = 512;
  public static final int MAX_PACKET_LENGTH = 32767;

  private final short type;
  private final short status;
  private final int length;

  public TdsPacket(short type, short status, int length, ByteBuf data) {
    super(data);
    this.type = type;
    this.status = status;
    this.length = length;
  }

  public short type() {
    return type;
  }

  public short status() {
    return status;
  }

  public int length() {
    return length;
  }
}
