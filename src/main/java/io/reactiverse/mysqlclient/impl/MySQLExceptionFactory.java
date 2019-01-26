package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.MySQLException;
import io.reactiverse.mysqlclient.impl.protocol.backend.ErrPacket;

public final class MySQLExceptionFactory {
  public static MySQLException throwNewException(ErrPacket errPacket) {
    return new MySQLException(errPacket.getErrorCode(), errPacket.getErrorMessage());
  }
}
