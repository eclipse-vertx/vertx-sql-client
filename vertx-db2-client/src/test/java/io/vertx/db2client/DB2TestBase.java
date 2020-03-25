/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.db2client;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.db2client.tck.ClientConfig;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.tck.Connector;

public abstract class DB2TestBase {

	@ClassRule
	public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

	protected Vertx vertx;
	protected Connector<SqlConnection> connector;
	protected DB2ConnectOptions options;

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp(TestContext ctx) throws Exception {
		System.out.println(">>> BEGIN " + testName.getMethodName());
		vertx = Vertx.vertx();
		initConnector();
		for (String table : tablesToClean())
			cleanTestTable(ctx, table);
	}

	@After
	public void tearDown(TestContext ctx) {
		connector.close();
		vertx.close(ctx.asyncAssertSuccess());
	}

	protected void initConnector() {
		options = rule.options();
		connector = ClientConfig.CONNECT.connect(vertx, options);
	}

	protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
		connector.connect(handler);
	}

	protected void cleanTestTable(TestContext ctx, String table) {
		connect(ctx.asyncAssertSuccess(conn -> {
			conn.query("DELETE FROM " + table).execute(ctx.asyncAssertSuccess(result -> {
				conn.close();
			}));
		}));
	}

	protected List<String> tablesToClean() {
		return Collections.emptyList();
	}

}
