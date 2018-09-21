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
package examples;

import io.reactiverse.reactivex.pgclient.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.docgen.Source;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Source
public class RxExamples {

  public void simpleQuery01Example(PgPool pool) {

    // A simple query
    Single<PgRowSet> single = pool.rxQuery("SELECT * FROM users WHERE id='julien'");

    // Execute the query
    single.subscribe(result -> {
      System.out.println("Got " + result.size() + " rows ");
    }, err -> {
      System.out.println("Failure: " + err.getMessage());
    });
  }

  public void streamingQuery01Example(PgPool pool) {

    // Create a flowable
    Observable<Row> observable = pool.rxGetConnection()
      .flatMapObservable(conn -> conn
        .rxPrepare("SELECT * FROM users WHERE first_name LIKE $1")
        .flatMapObservable(pq -> {
          // Fetch 50 rows at a time
          PgStream<Row> stream = pq.createStream(50, Tuple.of("julien"));
          return stream.toObservable();
        })
        // Close the connection after usage
        .doAfterTerminate(conn::close));

    // Then subscribe
    observable.subscribe(row -> {
      System.out.println("User: " + row.getString("last_name"));
    }, err -> {
      System.out.println("Error: " + err.getMessage());
    }, () -> {
      System.out.println("End of stream");
    });
  }

  public void streamingQuery02Example(PgPool pool) {

    // Create a flowable
    Flowable<Row> flowable = pool.rxGetConnection()
      .flatMapPublisher(conn -> conn.rxPrepare("SELECT * FROM users WHERE first_name LIKE $1")
        .flatMapPublisher(pq -> {
          // Fetch 50 rows at a time
          PgStream<Row> stream = pq.createStream(50, Tuple.of("julien"));
          return stream.toFlowable();
        }));

    // Then subscribe
    flowable.subscribe(new Subscriber<Row>() {

      private Subscription sub;

      @Override
      public void onSubscribe(Subscription subscription) {
        sub = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(Row row) {
        sub.request(1);
        System.out.println("User: " + row.getString("last_name"));
      }

      @Override
      public void onError(Throwable err) {
        System.out.println("Error: " + err.getMessage());
      }

      @Override
      public void onComplete() {
        System.out.println("End of stream");
      }
    });
  }

  public void transaction01Example(PgPool pool) {

    Completable completable = pool
      .rxBegin()
      .flatMapCompletable(tx -> tx
        .rxQuery("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
        .flatMap(result -> tx.rxQuery("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')"))
        .flatMapCompletable(result -> tx.rxCommit()));

    completable.subscribe(() -> {
      // Transaction succeeded
    }, err -> {
      // Transaction failed
    });
  }
}
