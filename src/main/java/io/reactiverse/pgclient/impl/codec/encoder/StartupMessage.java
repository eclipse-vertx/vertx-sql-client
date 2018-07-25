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

package io.reactiverse.pgclient.impl.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class StartupMessage {

  public static final ByteBuf BUFF_USER = Unpooled.copiedBuffer("user", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_DATABASE = Unpooled.copiedBuffer("database", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_APPLICATION_NAME = Unpooled.copiedBuffer("application_name", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_VERTX_PG_CLIENT = Unpooled.copiedBuffer("vertx-pg-client", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_CLIENT_ENCODING = Unpooled.copiedBuffer("client_encoding", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_UTF8 = Unpooled.copiedBuffer("utf8", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_DATE_STYLE = Unpooled.copiedBuffer("DateStyle", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_ISO = Unpooled.copiedBuffer("ISO", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_INTERVAL_STYLE = Unpooled.copiedBuffer("intervalStyle", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_INTERVAL_STYLE_TYPE = Unpooled.copiedBuffer("postgres", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_EXTRA_FLOAT_DIGITS = Unpooled.copiedBuffer("extra_float_digits", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_2 = Unpooled.copiedBuffer("2", UTF_8).asReadOnly();

  public final String username;
  public final String database;

  public StartupMessage(String username, String database) {
    this.username = username;
    this.database = database;
  }
}
