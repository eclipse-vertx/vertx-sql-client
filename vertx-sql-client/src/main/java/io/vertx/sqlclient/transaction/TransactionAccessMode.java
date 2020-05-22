package io.vertx.sqlclient.transaction;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Transaction access mode is the transaction characteristic which can be used to control whether the transaction is read/write or read only.
 */
@VertxGen
public enum TransactionAccessMode {

  /**
   * Indicate the transaction is in read write mode
   */
  READ_WRITE,

  /**
   * Indicate the transaction is in read only mode
   */
  READ_ONLY;

}
