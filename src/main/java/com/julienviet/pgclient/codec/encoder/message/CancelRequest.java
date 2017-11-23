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

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.encoder.OutboundMessage;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class CancelRequest implements OutboundMessage {

  private final int code = 80877102;
  private final int processId;
  private final int secretKey;

  public CancelRequest(int processId, int secretKey) {
    this.processId = processId;
    this.secretKey = secretKey;
  }

  public int getCode() {
    return code;
  }

  public int getProcessId() {
    return processId;
  }

  public int getSecretKey() {
    return secretKey;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CancelRequest that = (CancelRequest) o;
    return code == that.code &&
      processId == that.processId &&
      secretKey == that.secretKey;
  }

  @Override
  public void encode(ByteBuf out) {
    int pos = out.writerIndex();
    out.writeInt(0);
    out.writeInt(code);
    out.writeInt(processId);
    out.writeInt(secretKey);
    out.setInt(pos, out.writerIndex() - pos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, processId, secretKey);
  }

  @Override
  public String toString() {
    return "CancelRequest{" +
      "code=" + code +
      ", processId=" + processId +
      ", secretKey=" + secretKey +
      '}';
  }
}
