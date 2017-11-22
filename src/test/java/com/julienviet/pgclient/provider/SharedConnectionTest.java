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

import com.julienviet.pgclient.impl.provider.ConnectionProvider;
import com.julienviet.pgclient.impl.provider.SharedConnectionProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotSame;

public class SharedConnectionTest {

  @Test
  public void testSimple() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new SharedConnectionProvider(queue);
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
  public void testShare() {
    int times = 10;
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new SharedConnectionProvider(queue);
    List<SimpleHolder> holders = new ArrayList<>();
    for (int i = 0;i < times;i++) {
      SimpleHolder holder = new SimpleHolder();
      provider.acquire(holder);
      assertEquals(1, queue.size());
      assertFalse(holder.isComplete());
      holders.add(holder);
    }
    SimpleConnection conn = new SimpleConnection();
    queue.connect(conn);
    for (SimpleHolder holder : holders) {
      assertTrue(holder.isConnected());
      assertNotNull(conn.holder);
      assertNotSame(conn, holder.connection());
      holder.init();
    }
  }

  @Test
  public void testDoubleConnectionClose() {
    ConnectionQueue queue = new ConnectionQueue();
    ConnectionProvider provider = new SharedConnectionProvider(queue);
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
    ConnectionProvider provider = new SharedConnectionProvider(queue);
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
    ConnectionProvider provider = new SharedConnectionProvider(queue);
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
