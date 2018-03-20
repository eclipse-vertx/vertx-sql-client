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

package io.reactiverse.pgclient.impl.codec.encoder.message;

import io.reactiverse.pgclient.impl.codec.decoder.message.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.decoder.message.NoData;
import io.reactiverse.pgclient.impl.codec.decoder.message.ParameterDescription;
import io.reactiverse.pgclient.impl.codec.encoder.OutboundMessage;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.impl.codec.encoder.message.type.MessageType;

import java.util.Objects;

/**
 *
 * <p>
 * The message that using "statement" variant specifies the name of an existing prepared statement.
 *
 * <p>
 * The response is a {@link ParameterDescription} message describing the parameters needed by the statement,
 * followed by a {@link RowDescription} message describing the rows that will be returned when the statement is eventually
 * executed or a {@link NoData} message if the statement will not return rows.
 * {@link ErrorResponse} is issued if there is no such prepared statement.
 *
 * <p>
 * Note that since {@link Bind} has not yet been issued, the formats to be used for returned columns are not yet known to
 * the backend; the format code fields in the {@link RowDescription} message will be zeroes in this case.
 *
 * <p>
 * The message that using "portal" variant specifies the name of an existing portal.
 *
 * <p>
 * The response is a {@link RowDescription} message describing the rows that will be returned by executing the portal;
 * or a {@link NoData} message if the portal does not contain a query that will return rows; or {@link ErrorResponse}
 * if there is no such portal.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Describe implements OutboundMessage {

  private long statement;
  private String portal;

  public long getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public Describe setStatement(long statement) {
    this.statement = statement;
    return this;
  }

  public Describe setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Describe describe = (Describe) o;
    return Objects.equals(statement, describe.statement) &&
      Objects.equals(portal, describe.portal);
  }

  private static void encode(long statement, String portal, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(MessageType.DESCRIBE);
    out.writeInt(0);
    if (statement != 0) {
      out.writeByte('S');
      out.writeLong(statement);
    } else if (portal != null) {
      out.writeByte('P');
      Util.writeCStringUTF8(out, portal);
    } else {
      out.writeByte('S');
      Util.writeCStringUTF8(out, "");
    }
    out.setInt(pos + 1, out.writerIndex() - pos- 1);
  }

  @Override
  public void encode(ByteBuf out) {
    encode(statement, portal, out);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal);
  }

  @Override
  public String toString() {
    return "Describe{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      '}';
  }
}
