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
package io.vertx.db2client.tck;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.SimpleQueryTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2SimpleQueryTest extends SimpleQueryTestBase {

    @ClassRule
    public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

    @Override
    protected void initConnector() {
        connector = ClientConfig.CONNECT.connect(vertx, rule.options());
    }

    @Before
    public void setUp(TestContext ctx) throws Exception {
        vertx = Vertx.vertx();
        initConnector();
        cleanTestTable(ctx);
    }

    private void cleanTestTable(TestContext ctx) {
        // use DELETE FROM because DB2 does not support TRUNCATE TABLE
        connect(ctx.asyncAssertSuccess(conn -> {
            conn.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {
                conn.close();
            }));
        }));
    }
    
    @Override
    @Test
    @Ignore // TODO implement error path handling properly
    public void testQueryError(TestContext ctx) {
        super.testQueryError(ctx);
    }
}
