package io.vertx.pgclient.codec;

/**
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public enum TransactionStatus {
  IDLE((byte) 'I'),
  ACTIVE((byte)'T'),
  FAILED((byte)'E');
  final byte id;
  TransactionStatus(byte id) {
    this.id = id;
  }
  public static TransactionStatus valueOf(byte id) {
    if(id == 'I') {
      return TransactionStatus.IDLE;
    } else if(id == 'T') {
      return TransactionStatus.ACTIVE;
    } else {
      return TransactionStatus.FAILED;
    }
  }
}