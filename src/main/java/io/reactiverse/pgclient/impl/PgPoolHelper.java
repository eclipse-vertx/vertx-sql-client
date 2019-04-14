package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

/**
 * This class handles sharing the pool client instances by using a local shared map.
 *
 * @author <a href="https://github.com/mystdeim">Roman Novikov</a>
 */
public class PgPoolHelper {
  private static final String DS_LOCAL_MAP_NAME = "__vertx.PostgreSQL.pools";

  public static PgPool getOrCreate(Vertx vertx, PgPoolOptions options, String poolName) {
    synchronized (vertx) {
      LocalMap<String, PgPoolHolder> map = vertx.sharedData().getLocalMap(DS_LOCAL_MAP_NAME);
      PgPoolHolder theHolder = map.get(poolName);
      if (theHolder == null) {
        theHolder = new PgPoolHolder(vertx, options, () -> removeFromMap(vertx, map, poolName));
        map.put(poolName, theHolder);
      } else {
        theHolder.incRefCount();
      }
      return new PgPoolWrapper(theHolder);
    }
  }

  private static void removeFromMap(Vertx vertx, LocalMap<String, PgPoolHolder> map, String poolName) {
    synchronized (vertx) {
      map.remove(poolName);
      if (map.isEmpty()) {
        map.close();
      }
    }
  }
}
