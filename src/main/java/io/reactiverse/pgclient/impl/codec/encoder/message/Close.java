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

import io.reactiverse.pgclient.impl.codec.decoder.message.CloseComplete;
import io.reactiverse.pgclient.impl.codec.decoder.message.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.encoder.OutboundMessage;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.impl.codec.encoder.message.type.MessageType;

import java.util.Objects;

/**
 *
 * <p>
 * The message closes an existing prepared statement or portal and releases resources.
 * Note that closing a prepared statement implicitly closes any open portals that were constructed from that statement.
 *
 * <p>
 * The response is either {@link CloseComplete} or {@link ErrorResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Close implements OutboundMessage {

  private String statement;
  private String portal;


  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }


  public Close setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Close setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Close close = (Close) o;
    return Objects.equals(statement, close.statement) &&
      Objects.equals(portal, close.portal);
  }

  @Override
  public void encode(ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(MessageType.CLOSE);
    out.writeInt(0);
    out.writeByte('S'); // 'S' to close a prepared statement or 'P' to close a portal
    Util.writeCStringUTF8(out, statement != null ? statement : "");
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal);
  }

  @Override
  public String toString() {
    return "Close{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      '}';
  }
}
