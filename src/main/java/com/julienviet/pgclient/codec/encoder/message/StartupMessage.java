/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.julienviet.pgclient.codec.encoder.message;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

import static com.julienviet.pgclient.codec.encoder.message.type.MessageType.TERMINATE;
import static com.julienviet.pgclient.codec.util.Util.writeCString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class StartupMessage implements OutboundMessage {

  private static final ByteBuf BUFF_USER = Unpooled.copiedBuffer("user", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_DATABASE = Unpooled.copiedBuffer("database", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_APPLICATION_NAME = Unpooled.copiedBuffer("application_name", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_VERTX_PG_CLIENT = Unpooled.copiedBuffer("vertx-pg-client", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_CLIENT_ENCODING = Unpooled.copiedBuffer("client_encoding", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_UTF8 = Unpooled.copiedBuffer("utf8", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_DATE_STYLE = Unpooled.copiedBuffer("DateStyle", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_ISO = Unpooled.copiedBuffer("ISO", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_EXTRA_FLOAT_DIGITS = Unpooled.copiedBuffer("extra_float_digits", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_2 = Unpooled.copiedBuffer("2", UTF_8).asReadOnly();

  final String username;
  final String database;

  public StartupMessage(String username, String database) {
    this.username = username;
    this.database = database;
  }

  public String getUsername() {
    return username;
  }

  public String getDatabase() {
    return database;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StartupMessage that = (StartupMessage) o;
    return Objects.equals(username, that.username) &&
      Objects.equals(database, that.database);
  }

  @Override
  public void encode(ByteBuf out) {

    int pos = out.writerIndex();

    out.writeInt(0);
    // protocol version
    out.writeShort(3);
    out.writeShort(0);

    writeCString(out, BUFF_USER);
    Util.writeCStringUTF8(out, username);
    writeCString(out, BUFF_DATABASE);
    Util.writeCStringUTF8(out, database);
    writeCString(out, BUFF_APPLICATION_NAME);
    writeCString(out, BUFF_VERTX_PG_CLIENT);
    writeCString(out, BUFF_CLIENT_ENCODING);
    writeCString(out, BUFF_UTF8);
    writeCString(out, BUFF_DATE_STYLE);
    writeCString(out, BUFF_ISO);
    writeCString(out, BUFF_EXTRA_FLOAT_DIGITS);
    writeCString(out, BUFF_2);

    out.writeByte(0);
    out.setInt(pos, out.writerIndex() - pos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, database);
  }

  @Override
  public String toString() {
    return "StartupMessage{" +
      "username='" + username + '\'' +
      ", database='" + database + '\'' +
      '}';
  }
}
