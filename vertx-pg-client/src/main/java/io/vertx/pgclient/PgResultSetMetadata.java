package io.vertx.pgclient;

import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * An object that can be used to represent metadata of the Postgres ResultSet which includes
 * metadata information of all the fields.
 */
@VertxGen
public interface PgResultSetMetadata {

  /**
   * get all the {@link PgColumnMetadata} in this resultset.
   *
   * @return the column metadata list
   */
  List<PgColumnMetadata> columnMetadataList();
}
