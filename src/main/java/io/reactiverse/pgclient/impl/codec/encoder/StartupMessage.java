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
import io.vertx.core.json.JsonObject;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class StartupMessage {

  public static final ByteBuf BUFF_USER = Unpooled.copiedBuffer("user", UTF_8).asReadOnly();
  public static final ByteBuf BUFF_DATABASE = Unpooled.copiedBuffer("database", UTF_8).asReadOnly();

  // default properties
  private static final String DEFAULT_APPLICATION_NAME = "reactive-pg-client";
  private static final String DEFAULT_CLIENT_ENCODING = "utf8";
  private static final String DEFAULT_DATE_STYLE = "ISO";
  private static final String DEFAULT_INTERVAL_STYLE = "postgres";
  private static final String DEFAULT_EXTRA_FLOAT_DIGITS = "2";

  public final String username;
  public final String database;
  public final JsonObject properties = new JsonObject();

  public StartupMessage(String username, String database, JsonObject properties) {
    init();

    this.username = username;
    this.database = database;

    properties.forEach(property -> {
      this.properties.put(property.getKey(), property.getValue());
    });
  }

  private void init() {
    this.properties.put("application_name", DEFAULT_APPLICATION_NAME)
      .put("client_encoding", DEFAULT_CLIENT_ENCODING)
      .put("DateStyle", DEFAULT_DATE_STYLE)
      .put("intervalStyle", DEFAULT_INTERVAL_STYLE)
      .put("extra_float_digits", DEFAULT_EXTRA_FLOAT_DIGITS);
  }
}
