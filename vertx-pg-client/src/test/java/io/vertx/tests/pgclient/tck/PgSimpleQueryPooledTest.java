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
package io.vertx.tests.pgclient.tck;

import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.tests.sqlclient.tck.SimpleQueryTestBase;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgSimpleQueryPooledTest extends SimpleQueryTestBase {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }
}
