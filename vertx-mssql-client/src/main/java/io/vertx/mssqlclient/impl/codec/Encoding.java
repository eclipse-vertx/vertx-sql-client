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

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public enum Encoding {

  UNICODE("UTF-16LE"),
  UTF8("UTF-8"),
  CP437("Cp437"),
  CP850("Cp850"),
  CP874("MS874"),
  CP932("MS932"),
  CP936("MS936"),
  CP949("MS949"),
  CP950("MS950"),
  CP1250("Cp1250"),
  CP1251("Cp1251"),
  CP1252("Cp1252"),
  CP1253("Cp1253"),
  CP1254("Cp1254"),
  CP1255("Cp1255"),
  CP1256("Cp1256"),
  CP1257("Cp1257"),
  CP1258("Cp1258");

  public final String charsetName;
  public final Charset charset;

  Encoding(String charsetName) {
    this.charsetName = charsetName;
    charset = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : null;
  }

  private static final int UTF8_IN_TDSCOLLATION = 0x4000000;

  public static Encoding readFrom(ByteBuf byteBuf) {
    int info = byteBuf.readIntLE(); // 4 bytes, contains: LCID ColFlags Version
    int sortId = byteBuf.readUnsignedByte(); // 1 byte, contains: SortId
    Encoding result;
    if (UTF8_IN_TDSCOLLATION == (info & UTF8_IN_TDSCOLLATION)) {
      result = Encoding.UTF8;
    } else if (sortId == 0) {
      result = WindowsLocale.forLangId(info & 0x0000FFFF).encoding;
    } else {
      result = SortOrder.forId(sortId).encoding;
    }
    return result;
  }

  @Override
  public String toString() {
    return charsetName;
  }
}
