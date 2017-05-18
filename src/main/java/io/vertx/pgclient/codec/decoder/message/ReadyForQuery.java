package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.TransactionStatus;

import java.util.Objects;

/**
 *
 * <p>
 * The frontend can issue commands. Every message returned from the backend has transaction status
 * that would be one of the following
 *
 * <p>
 * IDLE : Not in a transaction block
 * <p>
 * ACTIVE : In transaction block
 * <p>
 * FAILED : Failed transaction block (queries will be rejected until block is ended)
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public class ReadyForQuery implements Message {

  private final TransactionStatus transactionStatus;

  public ReadyForQuery(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReadyForQuery that = (ReadyForQuery) o;
    return transactionStatus == that.transactionStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus);
  }


  @Override
  public String toString() {
    return "ReadyForQuery{" +
      "transactionStatus=" + transactionStatus +
      '}';
  }
}
