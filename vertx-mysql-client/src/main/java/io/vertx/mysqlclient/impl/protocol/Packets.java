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

package io.vertx.mysqlclient.impl.protocol;

/**
 * MySQL Packets.
 */
public final class Packets {
  public static final int OK_PACKET_HEADER = 0x00;
  public static final int EOF_PACKET_HEADER = 0xFE;
  public static final int ERROR_PACKET_HEADER = 0xFF;
  public static final int PACKET_PAYLOAD_LENGTH_LIMIT = 0xFFFFFF;

  public static final class OkPacket {

    private final int affectedRows;
    private final long lastInsertId;
    private final int serverStatusFlags;

    public OkPacket(int affectedRows, long lastInsertId, int serverStatusFlags) {
      this.affectedRows = affectedRows;
      this.lastInsertId = lastInsertId;
      this.serverStatusFlags = serverStatusFlags;
    }

    public int affectedRows() {
      return affectedRows;
    }

    public long lastInsertId() {
      return lastInsertId;
    }

    public int serverStatusFlags() {
      return serverStatusFlags;
    }
  }

  public static final class EofPacket {

    private final int numberOfWarnings;
    private final int serverStatusFlags;

    public EofPacket(int numberOfWarnings, int serverStatusFlags) {
      this.numberOfWarnings = numberOfWarnings;
      this.serverStatusFlags = serverStatusFlags;
    }

    public int numberOfWarnings() {
      return numberOfWarnings;
    }

    public int serverStatusFlags() {
      return serverStatusFlags;
    }
  }

  @SuppressWarnings("unused")
  public static final class ServerStatusFlags {
    /*
      https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad
     */

    public static final int SERVER_STATUS_IN_TRANS = 0x0001;
    public static final int SERVER_STATUS_AUTOCOMMIT = 0x0002;
    public static final int SERVER_MORE_RESULTS_EXISTS = 0x0008;
    public static final int SERVER_STATUS_NO_GOOD_INDEX_USED = 0x0010;
    public static final int SERVER_STATUS_NO_INDEX_USED = 0x0020;
    public static final int SERVER_STATUS_CURSOR_EXISTS = 0x0040;
    public static final int SERVER_STATUS_LAST_ROW_SENT = 0x0080;
    public static final int SERVER_STATUS_DB_DROPPED = 0x0100;
    public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 0x0200;
    public static final int SERVER_STATUS_METADATA_CHANGED = 0x0400;
    public static final int SERVER_QUERY_WAS_SLOW = 0x0800;
    public static final int SERVER_PS_OUT_PARAMS = 0x1000;
    public static final int SERVER_STATUS_IN_TRANS_READONLY = 0x2000;
    public static final int SERVER_SESSION_STATE_CHANGED = 0x4000;
  }

  @SuppressWarnings("unused")
  public static final class EnumCursorType {
    public static final byte CURSOR_TYPE_NO_CURSOR = 0;
    public static final byte CURSOR_TYPE_READ_ONLY = 1;

    // not supported by the server for now
    public static final byte CURSOR_TYPE_FOR_UPDATE = 2;
    public static final byte CURSOR_TYPE_SCROLLABLE = 4;
  }

  @SuppressWarnings("unused")
  public static final class ParameterFlag {
    public static final int UNSIGNED = 0x80;
  }
}
