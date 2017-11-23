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

import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.EmptyQueryResponse;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

import static com.julienviet.pgclient.codec.encoder.message.type.MessageType.EXECUTE;

/**
 *
 * <p>
 * The message specifies the portal and a maximum row count (zero meaning "fetch all rows") of the result.
 *
 * <p>
 * The row count of the result is only meaningful for portals containing commands that return row sets;
 * in other cases the command is always executed to completion, and the row count of the result is ignored.
 *
 * <p>
 * The possible responses to this message are the same as {@link Query} message, except that
 * it doesn't cause {@link ReadyForQuery} or {@link RowDescription} to be issued.
 *
 * <p>
 * If Execute terminates before completing the execution of a portal, it will send a {@link PortalSuspended} message;
 * the appearance of this message tells the frontend that another Execute should be issued against the same portal to
 * complete the operation. The {@link CommandComplete} message indicating completion of the source SQL command
 * is not sent until the portal's execution is completed. Therefore, This message is always terminated by
 * the appearance of exactly one of these messages: {@link CommandComplete},
 * {@link EmptyQueryResponse}, {@link ErrorResponse} or {@link PortalSuspended}.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Execute implements OutboundMessage {

  private String portal;
  private int rowCount;


  public String getPortal() {
    return portal;
  }

  public int getRowCount() {
    return rowCount;
  }

  public Execute setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public Execute setRowCount(int rowCount) {
    this.rowCount = rowCount;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Execute execute = (Execute) o;
    return rowCount == execute.rowCount &&
      Objects.equals(portal, execute.portal);
  }

  private static void encode(String portal, int rowCount, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(EXECUTE);
    out.writeInt(0);
    Util.writeCStringUTF8(out, portal != null ? portal : "");
    out.writeInt(rowCount); // Zero denotes "no limit" maybe for ReadStream<Row>
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  @Override
  public void encode(ByteBuf out) {
    encode(portal, rowCount, out);
  }

  @Override
  public int hashCode() {
    return Objects.hash(portal, rowCount);
  }

  @Override
  public String toString() {
    return "Execute{" +
      ", portal='" + portal + '\'' +
      ", rowCount=" + rowCount +
      '}';
  }

}
