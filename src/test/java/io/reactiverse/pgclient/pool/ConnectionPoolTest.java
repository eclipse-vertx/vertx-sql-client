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

package io.reactiverse.pgclient.pool;

import io.reactiverse.pgclient.impl.ConnectionPool;
import io.vertx.core.Future;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectionPoolTest {

  @Test
  public void testSimple() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    pool.acquire(holder);
    assertEquals(1, queue.size());
    assertFalse(holder.isComplete());
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    assertTrue(holder.isConnected());
    assertNotNull(conn.holder);
    assertNotSame(conn, holder.connection());
    holder.init();
    holder.close();
  }

  @Test
  public void testRecycle() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder1.init();
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    assertFalse(holder2.isComplete());
    assertEquals(0, queue.size());
    holder1.close();
    assertEquals(0, conn.closed);
    assertEquals(0, holder1.closed());
    assertTrue(holder2.isConnected());
    assertEquals(0, queue.size());
  }

  @Test
  public void testConnectionCreation() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    assertEquals(1, queue.size()); // Check that we won't create more connection than max size
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    assertTrue(holder1.isConnected());
    assertEquals(0, queue.size());
  }

  @Test
  public void testConnClose() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder1.init();
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    assertFalse(holder2.isComplete());
    assertEquals(0, queue.size());
    conn.close();
    assertEquals(1, holder1.closed());
    assertEquals(1, queue.size());
    assertFalse(holder2.isComplete());
  }

  @Test
  public void testConnectionCloseInPool() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    pool.acquire(holder);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder.init();
    holder.close();
    conn.close();
    assertEquals(0, pool.available());
  }

  @Test
  public void testDoubleConnectionClose() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    pool.acquire(holder);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder.init();
    conn.close();
    try {
      conn.close();
      fail();
    } catch (IllegalStateException ignore) {
    }
  }

  @Test
  public void testDoubleConnectionRelease() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    pool.acquire(holder);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder.init();
    holder.close();
    try {
      holder.close();
      fail();
    } catch (IllegalStateException ignore) {
    }
  }

  @Test
  public void testDoubleConnectionAcquire() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    pool.acquire(holder);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder.init();
    try {
      holder.init();
      fail();
    } catch (IllegalStateException ignore) {
    }
  }

  @Test
  public void testReleaseConnectionWhenWaiterQueueIsEmpty() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 2);
    // Acquire a connection from the pool for holder1
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleConnection conn1 = new SimpleConnection();
    queue.connect(conn1);
    holder1.init();
    // Acquire a connection from the pool for holder2
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    // Release the first connection so the second waiter gets the connection
    holder1.close();
    // The connection should be put back in the pool
    assertEquals(1, pool.available());
    // Satisfy the holder with connection it actually asked for
    SimpleConnection conn2 = new SimpleConnection();
    queue.connect(conn2);
    holder2.init();
  }

  @Test
  public void testReleaseClosedConnectionShouldNotAddBackTheConnectionToThePool() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1);
    // Acquire a connection from the pool for holder1
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleConnection conn1 = new SimpleConnection();
    queue.connect(conn1);
    holder1.init();
    // Close connection
    conn1.close();
    holder1.close();
    assertEquals(pool.available(), 0);
  }

  @Test
  public void testMaxQueueSize1() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1, 0);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder1.init();
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    assertTrue(holder2.isFailed());
  }

  @Test
  public void testMaxQueueSize2() {
    SimpleHolder holder2 = new SimpleHolder();
    SimpleConnection conn = new SimpleConnection();
    ConnectionPool[] poolRef = new ConnectionPool[1];
    ConnectionPool pool = new ConnectionPool(ar -> {
      poolRef[0].acquire(holder2);
      assertFalse(holder2.isComplete());
      ar.handle(Future.succeededFuture(conn));
      assertFalse(holder2.isComplete());
    }, 1, 0);
    poolRef[0] = pool;
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    assertTrue(holder1.isComplete());
    assertTrue(holder2.isFailed());
  }

  @Test
  public void testConnectionFailure() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 1, 0);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    Exception cause = new Exception();
    queue.fail(cause);
    assertTrue(holder1.isFailed());
    assertSame(cause, holder1.failure());
    assertEquals(0, pool.available());
    assertEquals(0, pool.size());
    SimpleHolder holder2 = new SimpleHolder();
    pool.acquire(holder2);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    assertTrue(holder2.isConnected());
    assertEquals(0, pool.available());
    assertEquals(1, pool.size());
  }

  @Test
  public void testAcquireOnlyConnectOnce() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionPool pool = new ConnectionPool(queue, 10, 0);
    SimpleHolder holder1 = new SimpleHolder();
    pool.acquire(holder1);
    assertEquals(1, queue.size());
  }
}
