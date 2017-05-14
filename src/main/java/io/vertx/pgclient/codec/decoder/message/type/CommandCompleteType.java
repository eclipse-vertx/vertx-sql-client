package io.vertx.pgclient.codec.decoder.message.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class CommandCompleteType {
  public static final String INSERT = "INSERT";
  public static final String DELETE = "DELETE";
  public static final String UPDATE = "UPDATE";
  public static final String SELECT = "SELECT";
  public static final String MOVE = "MOVE";
  public static final String FETCH = "FETCH";
  public static final String COPY = "COPY";
}
