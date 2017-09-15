package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.PgClientOptions

/**
 * A function providing a DSL for building [com.julienviet.pgclient.PgClientOptions] objects.
 *
 *
 * @param cachePreparedStatements 
 * @param database 
 * @param host 
 * @param password 
 * @param pipeliningLimit 
 * @param port 
 * @param ssl 
 * @param username 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [com.julienviet.pgclient.PgClientOptions original] using Vert.x codegen.
 */
fun PgClientOptions(
  cachePreparedStatements: Boolean? = null,
  database: String? = null,
  host: String? = null,
  password: String? = null,
  pipeliningLimit: Int? = null,
  port: Int? = null,
  ssl: Boolean? = null,
  username: String? = null): PgClientOptions = com.julienviet.pgclient.PgClientOptions().apply {

  if (cachePreparedStatements != null) {
    this.setCachePreparedStatements(cachePreparedStatements)
  }
  if (database != null) {
    this.setDatabase(database)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (password != null) {
    this.setPassword(password)
  }
  if (pipeliningLimit != null) {
    this.setPipeliningLimit(pipeliningLimit)
  }
  if (port != null) {
    this.setPort(port)
  }
  if (ssl != null) {
    this.setSsl(ssl)
  }
  if (username != null) {
    this.setUsername(username)
  }
}

