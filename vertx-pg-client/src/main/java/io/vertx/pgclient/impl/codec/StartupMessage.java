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

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class StartupMessage {

  static final ByteBuf BUFF_USER = Unpooled.copiedBuffer("user", UTF_8).asReadOnly();
  static final ByteBuf BUFF_DATABASE = Unpooled.copiedBuffer("database", UTF_8).asReadOnly();

  final String username;
  final String database;
  final Map<String, String> properties;

  StartupMessage(String username, String database, Map<String, String> properties) {
    this.username = username;
    this.database = database;
    this.properties = properties;
  }
}
