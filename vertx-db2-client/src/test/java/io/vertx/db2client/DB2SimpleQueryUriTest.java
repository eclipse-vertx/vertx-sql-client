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

import org.junit.runner.RunWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.db2client.tck.DB2SimpleQueryTest;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.tck.Connector;

/*
 * Runs the tck SimpleQueryTest, but with a uri instead of connection properties.
 */
@RunWith(VertxUnitRunner.class)
public class DB2SimpleQueryUriTest extends DB2SimpleQueryTest {
    
  @Override
  protected void initConnector() {
    String uri = "db2://"+rule.options().getUser()+":"
                         +rule.options().getPassword()+"@"
                         +rule.options().getHost()+":"
                         +rule.options().getPort()+"/"
                         +rule.options().getDatabase();
		
    connector = new Connector<SqlConnection>() {
      @Override
      public void connect(Handler<AsyncResult<SqlConnection>> handler) {
        DB2Connection.connect(vertx, uri, ar -> {
          if (ar.succeeded()) {
            handler.handle(Future.succeededFuture(ar.result()));
          } else {
            handler.handle(Future.failedFuture(ar.cause()));
          }
        });
      }

      @Override
      public void close() {
      }
    };
  }	
}
