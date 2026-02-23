/*
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
 */
package io.vertx.tests.pgclient;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.TargetServerType;
import io.vertx.pgclient.impl.PgPoolOptions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ServerTypeAwareConnectionFactoryTest {

  @Test
  public void testPgPoolOptionsCopyConstructor() {
    PgPoolOptions original = new PgPoolOptions();
    original.setPipelined(true);
    original.setTargetServerType(TargetServerType.PREFER_SECONDARY);
    List<PgConnectOptions> servers = Arrays.asList(
      new PgConnectOptions().setHost("host1").setPort(5432)
    );
    original.setServers(servers);
    PgPoolOptions copy = new PgPoolOptions(original);
    assertTrue(copy.isPipelined());
    assertEquals(TargetServerType.PREFER_SECONDARY, copy.getTargetServerType());
    assertEquals(servers, copy.getServers());
  }

  @Test
  public void testPgPoolOptionsCopyFromPlainPoolOptions() {
    io.vertx.sqlclient.PoolOptions plain = new io.vertx.sqlclient.PoolOptions().setMaxSize(10);
    PgPoolOptions copy = new PgPoolOptions(plain);
    assertEquals(10, copy.getMaxSize());
    assertFalse(copy.isPipelined());
    assertEquals(TargetServerType.ANY, copy.getTargetServerType());
    assertNull(copy.getServers());
  }
}
