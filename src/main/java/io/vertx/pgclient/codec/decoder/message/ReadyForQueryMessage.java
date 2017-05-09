package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.TransactionStatus;

import java.util.Objects;

/**
 *
 * Every ReadyForQueryMessage returned from the backend has transaction status that would be one of the following
 *
 * NOT_BLOCK : Not in a transaction block
 * BLOCK : In transaction block
 * FAILED : Failed transaction block (queries will be rejected until block is ended)
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public class ReadyForQueryMessage implements Message {

  private final TransactionStatus transactionStatus;

  public ReadyForQueryMessage(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReadyForQueryMessage that = (ReadyForQueryMessage) o;
    return transactionStatus == that.transactionStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus);
  }


  @Override
  public String toString() {
    return "ReadyForQueryMessage{" +
      "transactionStatus=" + transactionStatus +
      '}';
  }
}
