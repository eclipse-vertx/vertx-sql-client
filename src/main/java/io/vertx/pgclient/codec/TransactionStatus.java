package io.vertx.pgclient.codec;

/**
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public enum TransactionStatus {
  IDLE,
  ACTIVE,
  FAILED
}