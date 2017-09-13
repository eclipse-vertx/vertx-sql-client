package com.julienviet.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public enum PoolingMode {

  /**
   * A physical connection is borrowed for the life of the pool's connection.
   */
  CONNECTION,

  /**
   * A physical connection is borrwoed for the execution of each statement.
   */
  STATEMENT

}
