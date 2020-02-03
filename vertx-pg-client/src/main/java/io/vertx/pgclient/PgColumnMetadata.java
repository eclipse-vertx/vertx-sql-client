package io.vertx.pgclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * An object which can be used to describe the metadata of columns in Postgres:
 *
 * <ul>
 *   <li>column name</li>
 *   <li>table OID</li>
 *   <li>column attribute number</li>
 *   <li>data type OID</li>
 *   <li>type length</li>
 *   <li>type modifier</li>
 *   <li>format code</li>
 * </ul>
 */
@VertxGen
public interface PgColumnMetadata {

  /**
   * Get the field name.
   *
   * @return the name
   */
  String name();

  /**
   * If the field can be identified as a column of a specific table, the object ID of the table; otherwise zero.
   *
   * @return the table OID
   */
  int tableOID();

  /**
   * If the field can be identified as a column of a specific table, the attribute number of the column; otherwise zero.
   *
   * @return the column attribute number
   */
  short columnAttributeNumber();

  /**
   * Get the object ID (see pg_type.oid) of the field's data type.
   *
   * @return the OID
   */
  int dataTypeOID();

  /**
   * Get the data type size (see pg_type.typlen). Note that negative values denote variable-width types.
   *
   * @return the length
   */
  short typeLength();

  /**
   * Get the type modifier (see pg_attribute.atttypmod). The meaning of the modifier is type-specific.
   *
   * @return the type modifier
   */
  int typeModifier();

  /**
   * The format code being used for the field. Currently will be zero (text) or one (binary).
   *
   * @return the format code
   */
  short formatCode();
}
