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

import io.reactiverse.pgclient.impl.codec.DataTypeCodec;
import io.reactiverse.pgclient.impl.codec.DataType;
import io.reactiverse.pgclient.impl.codec.decoder.message.BindComplete;
import io.reactiverse.pgclient.impl.codec.decoder.message.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.encoder.OutboundMessage;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.reactiverse.pgclient.impl.codec.encoder.message.type.MessageType.BIND;

/**
 *
 * <p>
 * The message gives the name of the prepared statement, the name of portal,
 * and the values to use for any parameter values present in the prepared statement.
 * The supplied parameter set must match those needed by the prepared statement.
 *
 * <p>
 * The response is either {@link BindComplete} or {@link ErrorResponse}.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Bind implements OutboundMessage {

  private long statement;
  private String portal;
  private List<Object> paramValues;
  private DataType[] dataTypes;
  private int[] paramFormats;

  public Bind() {
  }

  public DataType[] getDataTypes() {
    return dataTypes;
  }

  public Bind setDataTypes(DataType[] dataTypes) {
    this.dataTypes = dataTypes;
    return this;
  }

  public Bind setParamValues(List<Object> paramValues) {
    this.paramValues = paramValues;
    return this;
  }

  public Bind setParamFormats(int[] paramFormats) {
    this.paramFormats = paramFormats;
    return this;
  }

  public int[] getParamFormats() {
    return paramFormats;
  }

  public Bind setStatement(long statement) {
    this.statement = statement;
    return this;
  }

  public Bind setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public long getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public List<Object> getParamValues() {
    return paramValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bind bind = (Bind) o;
    return Objects.equals(statement, bind.statement) &&
      Objects.equals(portal, bind.portal) &&
      Objects.equals(paramValues, bind.paramValues) &&
      Arrays.equals(paramFormats, bind.paramFormats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal, paramValues, paramFormats);
  }

  private static void encode(String portal, long statement, List<Object> paramValues, DataType[] dataTypes, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(BIND);
    out.writeInt(0);
    if (portal != null) {
      out.writeCharSequence(portal, StandardCharsets.UTF_8);
    }
    out.writeByte(0);
    if (statement == 0) {
      out.writeByte(0);
    } else {
      out.writeLong(statement);
    }
    int len = paramValues.size();
    out.writeShort(len);
    // Parameter formats
    for (int c = 0;c < len;c++) {
      // for now each format is Binary
      out.writeShort(1);
    }
    out.writeShort(len);
    for (int c = 0;c < len;c++) {
      Object param = paramValues.get(c);
      if (param == null) {
        // NULL value
        out.writeInt(-1);
      } else {
        DataType dataType = dataTypes[c];
        DataTypeCodec.encodeBinary(dataType, param, out);
      }
    }

    // Result columns are all in Binary format
    out.writeShort(1);
    out.writeShort(1);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  @Override
  public void encode(ByteBuf out) {
    encode(portal, statement, paramValues, dataTypes, out);
  }

  @Override
  public String toString() {
    return "Bind{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      ", paramValues=" + paramValues +
      ", paramFormats=" + Arrays.toString(paramFormats) +
      '}';
  }
}
