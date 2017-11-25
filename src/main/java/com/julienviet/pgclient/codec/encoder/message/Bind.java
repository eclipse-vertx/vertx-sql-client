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

import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.julienviet.pgclient.codec.encoder.message.type.MessageType.BIND;

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

  private String statement;
  private String portal;
  private List<Object> paramValues;
  private int[] paramFormats;

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

  public Bind setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Bind setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public String getStatement() {
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

  private static void encode(String portal, String statement, List<Object> paramValues, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(BIND);
    out.writeInt(0);
    if(portal == null) {
      out.writeByte(0);
    } else {
      Util.writeCStringUTF8(out, portal);
    }
    if(statement == null) {
      out.writeByte(0);
    } else {
      Util.writeCStringUTF8(out, statement);
    }
    if(paramValues == null) {
      // No parameter formats
      out.writeShort(0);
      // No parameter values
      out.writeShort(0);
    } else {
      byte[][] foobar = Util.paramValues(paramValues);
      // Parameter formats
      out.writeShort(foobar.length);
      for (int c = 0; c < foobar.length; ++c) {
        // for now each format is TEXT
        out.writeShort(0);
      }
      out.writeShort(foobar.length);
      for (int c = 0; c < foobar.length; ++c) {
        if (foobar[c] == null) {
          // NULL value
          out.writeInt(-1);
        } else {
          // Not NULL value
          out.writeInt(foobar[c].length);
          out.writeBytes(foobar[c]);
        }
      }
    }
    // Result columns are all in TEXT format
    out.writeShort(0);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  @Override
  public void encode(ByteBuf out) {
    encode(portal, statement, paramValues, out);
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
