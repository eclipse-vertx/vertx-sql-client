/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient.it;

import io.reactiverse.pgclient.PgConnectOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnvTest {

  @Test
  public void testFoo() {
    PgConnectOptions options = PgConnectOptions.fromEnv();
    assertEquals("test_host", options.getHost());
    assertEquals("test_database", options.getDatabase());
    assertEquals("test_user", options.getUser());
    assertEquals("test_password", options.getPassword());
  }
}
