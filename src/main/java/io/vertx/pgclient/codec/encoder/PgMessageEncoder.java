package io.vertx.pgclient.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.encoder.message.PasswordMessage;
import io.vertx.pgclient.codec.encoder.message.QueryMessage;
import io.vertx.pgclient.codec.encoder.message.StartupMessage;
import io.vertx.pgclient.codec.encoder.message.TerminateMessage;

import static java.nio.charset.StandardCharsets.UTF_8;


public class PgMessageEncoder extends MessageToByteEncoder<Message> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {

    if (message.getClass() == StartupMessage.class) {

      StartupMessage startup = (StartupMessage) message;
      out.writeInt(0);
      out.writeShort(3);
      out.writeShort(0);
      for (String s : new String[]{
        "user", startup.getUsername(),
        "database", startup.getDatabase(),
        "application_name", "vertx-pg-client",
        "client_encoding", "utf8"}) {
        byte[] params = s.getBytes(UTF_8);
        out.writeBytes(params);
        out.writeByte(0);
      }
      out.writeByte(0);
      out.setInt(0, out.writerIndex());

    } else if (message.getClass() == PasswordMessage.class) {

      PasswordMessage password = (PasswordMessage) message;
      out.writeByte('p');
      out.writeInt(0);
      out.writeBytes(password.getPasswordHash() != null ? password.getPasswordHash() : password.getPassword().getBytes());
      out.writeByte(0);
      out.setInt(1, out.writerIndex() - 1);


    } else if(message.getClass() == QueryMessage.class) {

      QueryMessage query = (QueryMessage) message;
      out.writeByte('Q');
      out.writeInt(0);
      out.writeBytes(query.getQuery().getBytes(UTF_8));
      out.writeByte(0);
      out.setInt(1, out.writerIndex() - 1);

    } else if(message.getClass() == TerminateMessage.class) {

      out.writeByte('X');
      out.writeInt(4);

    }

  }
}

