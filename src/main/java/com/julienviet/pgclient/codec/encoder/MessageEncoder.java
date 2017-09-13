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

package com.julienviet.pgclient.codec.encoder;

import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Close;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.PasswordMessage;
import com.julienviet.pgclient.codec.encoder.message.Query;
import com.julienviet.pgclient.codec.encoder.message.StartupMessage;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import com.julienviet.pgclient.codec.encoder.message.Terminate;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.util.Util;

import static com.julienviet.pgclient.codec.encoder.message.type.MessageType.*;
import static com.julienviet.pgclient.codec.util.Util.writeCString;
import static java.nio.charset.StandardCharsets.*;


/**
 *
 * Encoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageEncoder extends MessageToByteEncoder<Message> {

  private static final ByteBuf BUFF_USER = Unpooled.copiedBuffer("user", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_DATABASE = Unpooled.copiedBuffer("database", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_APPLICATION_NAME = Unpooled.copiedBuffer("application_name", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_VERTX_PG_CLIENT = Unpooled.copiedBuffer("vertx-pg-client", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_CLIENT_ENCODING = Unpooled.copiedBuffer("client_encoding", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_UTF8 = Unpooled.copiedBuffer("utf8", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_DATE_STYLE = Unpooled.copiedBuffer("DateStyle", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_ISO = Unpooled.copiedBuffer("ISO", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_EXTRA_FLOAT_DIGITS = Unpooled.copiedBuffer("extra_float_digits", UTF_8).asReadOnly();
  private static final ByteBuf BUFF_2 = Unpooled.copiedBuffer("2", UTF_8).asReadOnly();

  @Override
  protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
    if (message.getClass() == StartupMessage.class) {
      encodeStartupMessage(message, out);
    } else if (message.getClass() == PasswordMessage.class) {
      encodePasswordMessage(message, out);
    } else if(message.getClass() == Query.class) {
      encodeQuery(message, out);
    } else if(message.getClass() == Terminate.class) {
      encodeTerminate(out);
    } else if(message.getClass() == Parse.class) {
      encodeParse(message , out);
    } else if(message.getClass() == Bind.class) {
      encodeBind(message , out);
    } else if(message.getClass() == Describe.class) {
      encodeDescribe(message , out);
    } else if(message.getClass() == Execute.class) {
      encodeExecute(message , out);
    } else if(message.getClass() == Close.class) {
      encodeClose(message , out);
    } else if(message.getClass() == Sync.class) {
      encodeSync(out);
    }
  }

  private void encodeStartupMessage(Message message, ByteBuf out) {

    StartupMessage startup = (StartupMessage) message;

    out.writeInt(0);
    // protocol version
    out.writeShort(3);
    out.writeShort(0);

    writeCString(out, BUFF_USER);
    Util.writeCStringUTF8(out, startup.getUsername());
    writeCString(out, BUFF_DATABASE);
    Util.writeCStringUTF8(out, startup.getDatabase());
    writeCString(out, BUFF_APPLICATION_NAME);
    writeCString(out, BUFF_VERTX_PG_CLIENT);
    writeCString(out, BUFF_CLIENT_ENCODING);
    writeCString(out, BUFF_UTF8);
    writeCString(out, BUFF_DATE_STYLE);
    writeCString(out, BUFF_ISO);
    writeCString(out, BUFF_EXTRA_FLOAT_DIGITS);
    writeCString(out, BUFF_2);

    out.writeByte(0);
    out.setInt(0, out.writerIndex());
  }

  private void encodePasswordMessage(Message message, ByteBuf out) {
    PasswordMessage password = (PasswordMessage) message;
    out.writeByte(PASSWORD_MESSAGE);
    out.writeInt(0);
    Util.writeCStringUTF8(out, password.getHash());
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeQuery(Message message, ByteBuf out) {
    Query query = (Query) message;
    out.writeByte(QUERY);
    out.writeInt(0);
    Util.writeCStringUTF8(out, query.getQuery());
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeTerminate(ByteBuf out) {
    out.writeByte(TERMINATE);
    out.writeInt(4);
  }

  private void encodeParse(Message message, ByteBuf out) {
    Parse parse = (Parse) message;
    int[] paramDataTypes = parse.getParamDataTypes();
    out.writeByte(PARSE);
    out.writeInt(0);
    if(parse.getStatement() == null) {
      out.writeByte(0);
    } else {
      Util.writeCStringUTF8(out, parse.getStatement());
    }
    Util.writeCStringUTF8(out, parse.getQuery());
    // no parameter data types (OIDs)
    if(paramDataTypes == null) {
      out.writeShort(0);
    } else {
      // Parameter data types (OIDs)
      out.writeShort(paramDataTypes.length);
      for (int c = 0; c < paramDataTypes.length; ++c) {
        out.writeInt(paramDataTypes[c]);
      }
    }
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeBind(Message message, ByteBuf out) {
    Bind bind = (Bind) message;
    byte[][] paramValues = bind.getParamValues();
    int[] paramFormats = bind.getParamFormats();
    out.writeByte(BIND);
    out.writeInt(0);
    if(bind.getPortal() == null) {
      out.writeByte(0);
    } else {
      Util.writeCStringUTF8(out, bind.getPortal());
    }
    if(bind.getStatement() == null) {
      out.writeByte(0);
    } else {
      Util.writeCStringUTF8(out, bind.getStatement());
    }
    if(paramValues == null) {
      // No parameter formats
      out.writeShort(0);
      // No parameter values
      out.writeShort(0);
    } else {
      // Parameter formats
      out.writeShort(paramValues.length);
      for (int c = 0; c < paramValues.length; ++c) {
        // for now each format is TEXT
        out.writeShort(0);
      }
      out.writeShort(paramValues.length);
      for (int c = 0; c < paramValues.length; ++c) {
        if (paramValues[c] == null) {
          // NULL value
          out.writeInt(-1);
        } else {
          // Not NULL value
          out.writeInt(paramValues[c].length);
          out.writeBytes(paramValues[c]);
        }
      }
    }
    // Result columns are all in TEXT format
    out.writeShort(0);
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeDescribe(Message message, ByteBuf out) {
    Describe describe = (Describe) message;
    out.writeByte(DESCRIBE);
    out.writeInt(0);
    if (describe.getStatement() != null) {
      out.writeByte('S');
      Util.writeCStringUTF8(out, describe.getStatement());
    } else if (describe.getPortal() != null) {
      out.writeByte('P');
      Util.writeCStringUTF8(out, describe.getPortal());
    } else {
      out.writeByte('S');
      Util.writeCStringUTF8(out, "");
    }
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeExecute(Message message, ByteBuf out) {
    Execute execute = (Execute) message;
    out.writeByte(EXECUTE);
    out.writeInt(0);
    Util.writeCStringUTF8(out, execute.getPortal() != null ? execute.getPortal() : "");
    out.writeInt(execute.getRowCount()); // Zero denotes "no limit" maybe for ReadStream<Row>
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeClose(Message message, ByteBuf out) {
    Close close = (Close) message;
    out.writeByte(CLOSE);
    out.writeInt(0);
    out.writeByte('S'); // 'S' to close a prepared statement or 'P' to close a portal
    Util.writeCStringUTF8(out, close.getStatement() != null ? close.getStatement() : "");
    out.setInt(1, out.writerIndex() - 1);
  }

  private void encodeSync(ByteBuf out) {
    out.writeByte(SYNC);
    out.writeInt(4);
  }
}

