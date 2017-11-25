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

import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class SSLRequest implements OutboundMessage {

  public static final SSLRequest INSTANCE = new SSLRequest();

  private final int code = 80877103;

  public int getCode() {
    return code;
  }

  private SSLRequest() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SSLRequest that = (SSLRequest) o;
    return code == that.code;
  }

  @Override
  public void encode(ByteBuf out) {
    int pos = out.writerIndex();
    out.writeInt(0);
    out.writeInt(code);
//    out.writeInt(0x12345679);
    out.setInt(pos, out.writerIndex() - pos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  @Override
  public String toString() {
    return "SSLRequest{" +
      "code=" + code +
      '}';
  }
}
