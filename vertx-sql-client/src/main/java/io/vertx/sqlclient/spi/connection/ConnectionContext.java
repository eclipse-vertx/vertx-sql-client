package io.vertx.sqlclient.spi.connection;

/**
 * <p>The connection context that handles connection interactions with the outer world, e.g. handling an event.</p>
 *
 * <p>The connection context allows a connection to signal inner changes to its context (the client state machine).</p>
 */
public interface ConnectionContext {

  /**
   * Signals a generic database event.
   *
   * @param event the event.
   */
  void handleEvent(Object event);

  /**
   * Signals the connection is closed.
   */
  void handleClosed();

  /**
   * Signals a failure.
   *
   * @param failure the failure
   */
  void handleException(Throwable failure);

}
