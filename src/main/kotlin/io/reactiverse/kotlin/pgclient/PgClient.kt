package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgClient as PgClientVertxAlias
import io.reactiverse.pgclient.PgConnectOptions
import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult

suspend fun PgClientVertxAlias.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun PgClientVertxAlias.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun PgClientVertxAlias.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgClientVertxAlias.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}

object PgClient {
/**
 * Connects to the database and returns the connection if that succeeds.
 * <p/>
 * The connection interracts directly with the database is not a proxy, so closing the
 * connection will close the underlying connection to the database.
 *
 * @param vertx the vertx instance
 * @param options the connect options
 * @param handler the handler called with the connection or the failure
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgClient original] using Vert.x codegen.
 */
  suspend fun connectAwait(vertx : Vertx, options : PgConnectOptions) : PgConnection {
    return awaitResult{
      PgClientVertxAlias.connect(vertx, options, it)
    }
  }

/**
 * Like [io.reactiverse.pgclient.PgClient] with options build from the environment variables.
 *
 * @param vertx 
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgClient original] using Vert.x codegen.
 */
  suspend fun connectAwait(vertx : Vertx) : PgConnection {
    return awaitResult{
      PgClientVertxAlias.connect(vertx, it)
    }
  }

/**
 * Like [io.reactiverse.pgclient.PgClient] with options build from <code>connectionUri</code>.
 *
 * @param vertx 
 * @param connectionUri 
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgClient original] using Vert.x codegen.
 */
  suspend fun connectAwait(vertx : Vertx, connectionUri : String) : PgConnection {
    return awaitResult{
      PgClientVertxAlias.connect(vertx, connectionUri, it)
    }
  }

}
