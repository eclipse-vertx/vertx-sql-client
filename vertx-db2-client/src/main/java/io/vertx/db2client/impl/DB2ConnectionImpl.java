/*
 * Copyright (C) 2019,2020 IBM Corporation
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
package io.vertx.db2client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class DB2ConnectionImpl extends SqlConnectionImpl<DB2ConnectionImpl> implements DB2Connection {

	public static void connect(Vertx vertx, DB2ConnectOptions options, Handler<AsyncResult<DB2Connection>> handler) {
		Context ctx = Vertx.currentContext();
		if (ctx != null) {
			DB2ConnectionFactory client;
			try {
				client = new DB2ConnectionFactory(ctx, false, options);
			} catch (Exception e) {
				handler.handle(Future.failedFuture(e));
				return;
			}
			client.connect(ar -> {
				if (ar.succeeded()) {
					Connection conn = ar.result();
					DB2ConnectionImpl p = new DB2ConnectionImpl(client, ctx, conn);
					conn.init(p);
					handler.handle(Future.succeededFuture(p));
				} else {
					handler.handle(Future.failedFuture(ar.cause()));
				}
			});
		} else {
			vertx.runOnContext(v -> {
				connect(vertx, options, handler);
			});
		}
	}

	private final DB2ConnectionFactory factory;

	public DB2ConnectionImpl(DB2ConnectionFactory factory, Context context, Connection conn) {
		super(context, conn);

		this.factory = factory;
	}
	
	@Override
	public void handleNotification(int processId, String channel, String payload) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DB2Connection ping(Handler<AsyncResult<Void>> handler) {
		PingCommand cmd = new PingCommand();
		cmd.handler = handler;
		schedule(cmd);
		return this;
	}

    @Override
    public DB2Connection debug(Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException("Debug command not implemented");
    }
}
