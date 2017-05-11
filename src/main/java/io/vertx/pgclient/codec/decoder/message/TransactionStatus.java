package io.vertx.pgclient.codec.decoder.message;

/**
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public enum TransactionStatus {
  NOT_BLOCK,
  BLOCK,
  FAILED
}