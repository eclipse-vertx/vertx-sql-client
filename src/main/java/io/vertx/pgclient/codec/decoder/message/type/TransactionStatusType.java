package io.vertx.pgclient.codec.decoder.message.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class TransactionStatusType {
  public static final byte NOT_BLOCK = 'I';
  public static final byte BLOCK = 'T';
  public static final byte FAILED = 'E';
}
