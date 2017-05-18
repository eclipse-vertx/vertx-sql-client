package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.TransactionStatus;
import io.vertx.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.pgclient.codec.decoder.message.ReadyForQuery;

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

  @Override
  public String toString() {
    return "Sync{}";
  }
}
