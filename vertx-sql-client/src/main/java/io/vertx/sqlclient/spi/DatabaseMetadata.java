package io.vertx.sqlclient.spi;

import io.vertx.codegen.annotations.VertxGen;

/**
 * Contains static metadata about the backend database server 
 */
@VertxGen
public interface DatabaseMetadata {
  
  /**
   * @return The product name of the backend database server
   */
  public String productName();
  
  /**
   * @return The full version string for the backend database server.
   * This may be useful for for parsing more subtle aspects of the version string.
   * For simple information like database major and minor version, use {@link #majorVersion()}
   * and {@link #minorVersion()} instead.
   */
  public String fullVersion();
  
  /**
   * @return The major version of the backend database server
   */
  public int majorVersion();
  
  /**
   * @return The minor version of the backend database server
   */
  public int minorVersion();

}
