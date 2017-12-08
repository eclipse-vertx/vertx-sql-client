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

package com.julienviet.pgclient.impl.codec.encoder.message;

import com.julienviet.pgclient.impl.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.impl.codec.TransactionStatus;
import com.julienviet.pgclient.impl.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.impl.codec.encoder.OutboundMessage;
import io.netty.buffer.ByteBuf;

import static com.julienviet.pgclient.impl.codec.encoder.message.type.MessageType.SYNC;

/**
 *
 * <p>
 * The purpose of this message is to provide a resynchronization point for error recovery.
 * When an error is detected while processing any extended-query message, the backend issues {@link ErrorResponse},
 * then reads and discards messages until this message is reached, then issues {@link ReadyForQuery} and returns to normal
 * message processing.
 *
 * <p>
 * Note that no skipping occurs if an error is detected while processing this message which ensures that there is one
 * and only one {@link ReadyForQuery} sent for each of this message.
 *
 * <p>
 * Note this message does not cause a transaction block opened with BEGIN to be closed. It is possible to detect this
 * situation in {@link ReadyForQuery#transactionStatus()} that includes {@link TransactionStatus} information.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Sync implements OutboundMessage {

  public static final Sync INSTANCE = new Sync();

  private Sync() {}

  @Override
  public void encode(ByteBuf out) {
    out.writeByte(SYNC);
    out.writeInt(4);
  }

  @Override
  public String toString() {
    return "Sync{}";
  }
}
