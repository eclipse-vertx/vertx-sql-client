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
package io.vertx.mysqlclient.impl.codec;

import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.mysqlclient.impl.MySQLSocketConnection;

import java.util.ArrayDeque;

public class MySQLCodec extends CombinedChannelDuplexHandler<MySQLDecoder, MySQLEncoder> {

  private final ArrayDeque<CommandCodec<?, ?>> inflight = new ArrayDeque<>();

  public MySQLCodec(MySQLSocketConnection mySQLSocketConnection) {
    MySQLEncoder encoder = new MySQLEncoder(inflight, mySQLSocketConnection);
    MySQLDecoder decoder = new MySQLDecoder(inflight, encoder);
    init(decoder, encoder);
  }
}
