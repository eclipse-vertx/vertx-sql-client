package com.julienviet.pgclient.codec.encoder.message;

import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.TransactionStatus;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;

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
 * situation in {@link ReadyForQuery#getTransactionStatus()} that includes {@link TransactionStatus} information.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Sync implements Message {

  public static final Sync INSTANCE = new Sync();

  private Sync() {}

  @Override
  public String toString() {
    return "Sync{}";
  }
}
