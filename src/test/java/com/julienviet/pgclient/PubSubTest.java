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
package com.julienviet.pgclient;

import com.julienviet.pgclient.pubsub.PgSubscriber;
import com.julienviet.pgclient.pubsub.PgChannel;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class PubSubTest extends PgTestBase {

  Vertx vertx;
  PgSubscriber subscriber;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    if (subscriber != null) {
      subscriber.close();
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testNotify(TestContext ctx) {
    Async async = ctx.async(2);
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("LISTEN the_channel").execute(ctx.asyncAssertSuccess(result1 -> {
        conn.notificationHandler(notification -> {
          ctx.assertEquals("the_channel", notification.getChannel());
          ctx.assertEquals("the message", notification.getPayload());
          async.countDown();
        });
        conn.query("NOTIFY the_channel, 'the message'", ctx.asyncAssertSuccess(result2 -> {
          async.countDown();
        }));
      }));
    }));
  }

  @Test
  public void testConnect(TestContext ctx) {
    subscriber = PgSubscriber.subscriber(vertx, options);
    Async notifiedLatch = ctx.async();
    PgChannel sub1 = subscriber.channel("channel1");
    PgChannel sub2 = subscriber.channel("channel2");
    sub1.handler(notif -> {
      ctx.assertEquals("msg1", notif);
      notifiedLatch.countDown();
    });
    sub2.handler(notif -> {
      ctx.assertEquals("msg2", notif);
      notifiedLatch.countDown();
    });
    Async connectLatch = ctx.async();
    subscriber.connect(ctx.asyncAssertSuccess(v -> connectLatch.complete()));
    connectLatch.awaitSuccess(10000);
    subscriber.actualConnection().query("NOTIFY channel1, 'msg1'", ctx.asyncAssertSuccess());
    subscriber.actualConnection().query("NOTIFY channel2, 'msg2'", ctx.asyncAssertSuccess());
    notifiedLatch.awaitSuccess(10000);
  }

  @Test
  public void testSubscribe(TestContext ctx) {
    subscriber = PgSubscriber.subscriber(vertx, options);
    Async connectLatch = ctx.async();
    subscriber.connect(ctx.asyncAssertSuccess(v -> connectLatch.complete()));
    connectLatch.awaitSuccess(10000);
    PgChannel channel = subscriber.channel("the_channel");
    Async subscribedLatch = ctx.async();
    ctx.assertEquals(channel, channel.subscribeHandler(v -> subscribedLatch.complete()));
    Async notifiedLatch = ctx.async();
    channel.handler(notif -> {
      ctx.assertEquals("msg", notif);
      notifiedLatch.countDown();
    });
    subscribedLatch.awaitSuccess(10000);
    subscriber.actualConnection().query("NOTIFY the_channel, 'msg'", ctx.asyncAssertSuccess());
    notifiedLatch.awaitSuccess(10000);
  }

  @Test
  public void testUnsubscribe(TestContext ctx) {
    subscriber = PgSubscriber.subscriber(vertx, options);
    Async connectLatch = ctx.async();
    subscriber.connect(ctx.asyncAssertSuccess(v -> connectLatch.complete()));
    connectLatch.awaitSuccess(10000);
    PgChannel sub = subscriber.channel("the_channel");
    Async endLatch = ctx.async();
    sub.endHandler(v -> endLatch.complete());
    Async subscribedLatch = ctx.async();
    sub.subscribeHandler(v -> subscribedLatch.complete());
    sub.handler(notif -> {
    });
    subscribedLatch.awaitSuccess(10000);
    sub.handler(null);
    endLatch.awaitSuccess(10000);
  }

  @Test
  public void testReconnect(TestContext ctx) {
    PgConnectOptions options = new PgConnectOptions(PgTestBase.options);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> connRef = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      connRef.set(conn);
      conn.connect();
    });
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      options.setPort(8080).setHost("localhost");
      listenLatch.complete();
    }));
    listenLatch.awaitSuccess(10000);
    subscriber = PgSubscriber.subscriber(vertx, options);
    PgChannel sub = subscriber.channel("the_channel");
    Async connect1Latch = ctx.async();
    Async connect2Latch = ctx.async();
    Async connect3Latch = ctx.async();
    AtomicInteger times = new AtomicInteger();
    sub.subscribeHandler(v -> {
      switch (times.getAndIncrement()) {
        case 0:
          connect1Latch.complete();
          break;
        case 1:
          connect2Latch.complete();
          break;
        case 2:
          connect3Latch.complete();
          break;
      }
    });
    subscriber.connect(ar -> { });
    sub.handler(notif -> { });
    connect1Latch.awaitSuccess(10000);
    AtomicInteger count = new AtomicInteger();
    subscriber.reconnectPolicy(retries -> {
      ctx.assertEquals(0, retries);
      ctx.assertFalse(subscriber.closed());
      if (count.getAndIncrement() < 2) {
        return 100L;
      } else {
        return -1L;
      }
    });
    Async closeLatch = ctx.async();
    subscriber.closeHandler(v -> closeLatch.complete());
    connRef.get().close();
    connect2Latch.awaitSuccess(10000);
    connRef.get().close();
    connect3Latch.awaitSuccess(10000);
    connRef.get().close();
    closeLatch.awaitSuccess(10000);
    ctx.assertEquals(3, count.get());
    ctx.assertTrue(subscriber.closed());
  }

  @Test
  public void testClose(TestContext ctx) {
    PgSubscriber subscriber = PgSubscriber.subscriber(vertx, options);
    PgChannel sub = subscriber.channel("the_channel");
    Async endLatch = ctx.async();
    sub.endHandler(v -> endLatch.complete());
    sub.handler(notif -> {
    });
    Async connectLatch = ctx.async();
    subscriber.connect(ctx.asyncAssertSuccess(v -> connectLatch.complete()));
    connectLatch.awaitSuccess(10000);
    Async closeLatch = ctx.async();
    subscriber.closeHandler(v -> closeLatch.complete());
    subscriber.close();
    endLatch.awaitSuccess(10000);
    closeLatch.awaitSuccess(10000);
  }
}
