package io.vertx.pgclient.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.encoder.message.PasswordMessage;
import io.vertx.pgclient.codec.encoder.message.Query;
import io.vertx.pgclient.codec.encoder.message.StartupMessage;
import io.vertx.pgclient.codec.encoder.message.Terminate;

import static io.vertx.pgclient.codec.utils.Utils.*;
import static java.nio.charset.StandardCharsets.*;


/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageEncoder extends MessageToByteEncoder<Message> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {

    if (message.getClass() == StartupMessage.class) {

      StartupMessage startup = (StartupMessage) message;

      out.writeInt(0);
      // protocol version
      out.writeShort(3);
      out.writeShort(0);

      writeCString(out, "user", UTF_8);
      writeCString(out, startup.getUsername(), UTF_8);
      writeCString(out, "database", UTF_8);
      writeCString(out, startup.getDatabase(), UTF_8);
      writeCString(out, "application_name", UTF_8);
      writeCString(out, "vertx-pg-client", UTF_8);
      writeCString(out, "client_encoding", UTF_8);
      writeCString(out, "utf8", UTF_8);

      out.writeByte(0);
      out.setInt(0, out.writerIndex());

    } else if (message.getClass() == PasswordMessage.class) {

      PasswordMessage password = (PasswordMessage) message;
      out.writeByte('p');
      out.writeInt(0);
      out.writeBytes(password.getPasswordHash() != null ? password.getPasswordHash() : password.getPassword());
      out.writeByte(0);
      out.setInt(1, out.writerIndex() - 1);


    } else if(message.getClass() == Query.class) {

      Query query = (Query) message;
      out.writeByte('Q');
      out.writeInt(0);
      out.writeBytes(query.getQuery().getBytes(UTF_8));
      out.writeByte(0);
      out.setInt(1, out.writerIndex() - 1);

    } else if(message.getClass() == Terminate.class) {

      out.writeByte('X');
      out.writeInt(4);

    }

  }
}

