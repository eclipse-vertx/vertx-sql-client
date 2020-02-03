package io.vertx.pgclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.PropertyKind;

/**
 * An interface to define Postgres specific constants or behaviors.
 */
@VertxGen
public interface PgClient {
  /**
   * SqlResult Property for Postgres ResultSet metadata.
   */
  PropertyKind<PgResultSetMetadata> RESULTSET_METADATA = () -> PgResultSetMetadata.class;
}
