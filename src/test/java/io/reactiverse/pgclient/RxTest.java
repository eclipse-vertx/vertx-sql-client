/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.reactiverse.pgclient;

import io.reactiverse.reactivex.pgclient.PgStream;
import io.reactiverse.reactivex.pgclient.Row;
import io.reactiverse.reactivex.pgclient.Tuple;
import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactiverse.reactivex.pgclient.PgClient;
import io.reactivex.Flowable;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RunWith(VertxUnitRunner.class)
public class RxTest extends PgTestBase {

  Vertx vertx;
  PgConnectOptions options;
  PgPool pool;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(PgTestBase.options);
    pool = PgClient.pool(vertx, new PgPoolOptions(options).setMaxSize(1));
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  private Flowable<Row> createFlowable(String sql) {
    return pool.rxBegin()
      .flatMapPublisher(tx -> tx.rxPrepare(sql)
        .flatMapPublisher(preparedQuery -> {
          // Fetch 50 rows at a time
          PgStream<io.reactiverse.reactivex.pgclient.Row> stream = preparedQuery.createStream(50, Tuple.tuple());
          return stream.toFlowable();
        })
        .doAfterTerminate(tx::commit));
  }

  @Test
  public void testFlowableCommit(TestContext ctx) {
    Async async = ctx.async();
    Flowable<Row> flowable = createFlowable("SELECT id, randomnumber from WORLD");
    flowable.subscribe(new Subscriber<io.reactiverse.reactivex.pgclient.Row>() {

      private Subscription sub;
      private Set<Integer> ids = new HashSet<>();

      {
        // Expected ids
        for (int i = 1;i <= 10000;i++) {
          ids.add(i);
        }
      }
      @Override
      public void onSubscribe(Subscription subscription) {
        sub = subscription;
        subscription.request(1);
      }
      @Override
      public void onNext(Row row) {
        ids.remove(row.getInteger("id"));
        sub.request(1);
      }
      @Override
      public void onError(Throwable err) {
        ctx.fail(err);
      }
      @Override
      public void onComplete() {
        ctx.assertEquals(Collections.emptySet(), ids);
        // Check the pool is back in the pool by the commit
        pool.rxGetConnection().subscribe(conn -> {
          conn.close();
          async.complete();
        }, ctx::fail);
      }
    });
  }

  @Test
  public void testFlowableError(TestContext ctx) {
    Async async = ctx.async();
    Flowable<Row> flowable = createFlowable("invalid SQL");
    flowable.subscribe(row -> {
      ctx.fail("Should not get rows");
    }, error -> {
      // Check the pool is back in the pool after implicit transaction rollback
      pool.rxGetConnection().subscribe(conn -> {
        conn.close();
        async.complete();
      }, ctx::fail);
    });
  }
}
