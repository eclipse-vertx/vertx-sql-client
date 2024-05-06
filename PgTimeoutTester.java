package io.vertx.sqlclient.templates.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import java.util.ArrayList;
import java.util.List;

public class PgTimeoutTester {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    PgConnectOptions dbConfig = new PgConnectOptions()
      .setPort(5432)
      .setConnectTimeout(2000)
      .setHost("localhost")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");

    PoolOptions poolConfig = new PoolOptions()
      .setMaxSize(1) // One connection in Pool
      .setConnectionTimeout(2); // 2 seconds

    PgPool pool = PgPool.pool(vertx, dbConfig, poolConfig);

    //connectionTimeOut(pool, vertx);
    poolTimeOut(pool, vertx);
  }

  private static void connectionTimeOut(PgPool pool, Vertx vertx) {
    //First query
    pool.getConnection()
      .onFailure(err -> {
        err.printStackTrace();
        vertx.close();
      })
      .compose(conn0 ->
          conn0.query("SELECT 1").execute()
            .onSuccess(rows -> System.out.println(rows.iterator().next().getInteger(0)))
        /*.eventually(ign -> conn0.close())*/); // Don't close connection to trigger timeout while getting one below

    //Second query
    pool.getConnection()
      .onFailure(err -> {
        err.printStackTrace();
        vertx.close();
      })
      .compose(conn0 ->
        conn0.query("SELECT 2").execute()
          .onSuccess(rows -> System.out.println(rows.iterator().next().getInteger(0)))
          .eventually(ign -> conn0.close()));
  }

  private static void poolTimeOut(PgPool pool, Vertx vertx) {
    //First query
    pool.getConnection()
      .onFailure(err -> {
        err.printStackTrace();
        vertx.close();
      })
      .compose(conn0 ->
          conn0.query("SELECT 1").execute()
            .onSuccess(rows -> System.out.println(rows.iterator().next().getInteger(0)))
        .eventually(ign -> conn0.close()));// Don't close connection to trigger timeout while getting one below

    List<Future<?>> futures = new ArrayList<>();
    //N queries
    for (int i = 2; i < 10; i++) {
      Future<?> f = pool.query("SELECT " + i).execute()
        .onSuccess(rows -> System.out.println(rows.iterator().next().getInteger(0)))
        .onFailure(err -> {
          err.printStackTrace();
          vertx.close();
        });
      futures.add(f);
    }

    Future.all(futures).onComplete(c -> vertx.close());
  }
}
