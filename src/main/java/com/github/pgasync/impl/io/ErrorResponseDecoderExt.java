package com.github.pgasync.impl.io;

import com.github.pgasync.impl.message.ErrorResponse;

import java.nio.ByteBuffer;

import static com.github.pgasync.impl.io.IO.getCString;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ErrorResponseDecoderExt implements Decoder<ErrorResponse> {

  private final byte[] field = new byte[2048];

  @Override
  public byte getMessageId() {
    return 'E';
  }

  @Override
  public ErrorResponse read(ByteBuffer buffer) {

    String level = null;
    String code = null;
    String message = null;

    for (byte type = buffer.get(); type != 0; type = buffer.get()) {
      String value = getCString(buffer, field);
      if (type == (byte) 'S') {
        level = value;
      } else if (type == 'C') {
        code = value;
      } else if (type == 'M') {
        message = value;
      }
    }

    return new ErrorResponse(level, code, message);
  }

}
