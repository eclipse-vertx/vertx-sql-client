package io.vertx.pgclient.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.encoder.message.PasswordMessage;
import io.vertx.pgclient.codec.encoder.message.Query;
import io.vertx.pgclient.codec.encoder.message.StartupMessage;
import io.vertx.pgclient.codec.encoder.message.Terminate;
import io.vertx.pgclient.codec.util.Util;

import static io.vertx.pgclient.codec.encoder.message.type.MessageType.*;
import static io.vertx.pgclient.codec.util.Util.writeCString;
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
}

