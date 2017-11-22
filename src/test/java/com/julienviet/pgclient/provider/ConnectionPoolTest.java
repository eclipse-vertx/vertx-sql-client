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

package com.julienviet.pgclient.provider;

import com.julienviet.pgclient.impl.provider.ConnectionPoolProvider;
import com.julienviet.pgclient.impl.provider.ConnectionProvider;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectionPoolTest {

  @Test
  public void testSimple() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    provider.acquire(holder);
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
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    provider.acquire(holder1);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder1.init();
    SimpleHolder holder2 = new SimpleHolder();
    provider.acquire(holder2);
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
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    provider.acquire(holder1);
    SimpleHolder holder2 = new SimpleHolder();
    provider.acquire(holder2);
    assertEquals(1, queue.size()); // Check that we won't create more connection than max size
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    assertTrue(holder1.isConnected());
    assertEquals(0, queue.size());
  }

  @Test
  public void testConnClose() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder1 = new SimpleHolder();
    provider.acquire(holder1);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder1.init();
    SimpleHolder holder2 = new SimpleHolder();
    provider.acquire(holder2);
    assertFalse(holder2.isComplete());
    assertEquals(0, queue.size());
    conn.close();
    assertEquals(1, holder1.closed());
    assertEquals(1, queue.size());
    assertFalse(holder2.isComplete());
  }

  @Test
  public void testDoubleConnectionClose() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    provider.acquire(holder);
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
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    provider.acquire(holder);
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
    ConnectionProvider provider = new ConnectionPoolProvider(queue, 1);
    SimpleHolder holder = new SimpleHolder();
    provider.acquire(holder);
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    holder.init();
    try {
      holder.init();
      fail();
    } catch (IllegalStateException ignore) {
    }
  }
}
