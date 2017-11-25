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
import com.julienviet.pgclient.codec.decoder.message.NoticeResponse;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.EmptyQueryResponse;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

import static com.julienviet.pgclient.codec.encoder.message.type.MessageType.QUERY;

/**
 * <p>
 * This message includes an SQL command (or commands) expressed as a text string.
 *
 * <p>
 * The possible response messages from the backend are
 * {@link CommandComplete}, {@link RowDescription}, {@link DataRow}, {@link EmptyQueryResponse}, {@link ErrorResponse},
 * {@link ReadyForQuery} and {@link NoticeResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Query implements OutboundMessage {

  final String sql;

  public Query(String sql) {
    this.sql = sql;
  }

  public String getQuery() {
    return sql;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Query that = (Query) o;
    return Objects.equals(sql, that.sql);
  }

  @Override
  public void encode(ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(QUERY);
    out.writeInt(0);
    Util.writeCStringUTF8(out, getQuery());
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sql);
  }

  @Override
  public String toString() {
    return "Query{" +
      "sql='" + sql + '\'' +
      '}';
  }
}
