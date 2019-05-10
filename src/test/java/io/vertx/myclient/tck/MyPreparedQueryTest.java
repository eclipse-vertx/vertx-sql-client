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
package io.vertx.myclient.tck;

import io.vertx.myclient.junit.MyRule;
import io.vertx.sqlclient.PreparedQueryTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyPreparedQueryTest extends PreparedQueryTestBase {

  @ClassRule
  public static MyRule rule = new MyRule();

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Ignore
  @Override
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    // Does not pass, we can't achieve this feature on MySQL for now, see io.reactiverse.pgclient.impl.my.codec.MyParamDesc#prepare for reasons.
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }
}
