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

import static org.junit.Assume.assumeTrue;

import java.sql.RowId;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

@RunWith(VertxUnitRunner.class)
public class DB2DataTypeTest extends DB2TestBase {
	
	/**
	 * In DB2 the FLOAT and DOUBLE column types both map to an 8-byte 
	 * double-precision column (i.e. Java double). Ensure that a Java
	 * float can still be inserted and selected from such a column
	 */
	@Test
	public void testInsertFloatColumn(TestContext ctx) {
		  connect(ctx.asyncAssertSuccess(conn -> {
			    // Insert some data
			    conn.preparedQuery("INSERT INTO db2_types (id,test_float) VALUES (?, ?)")
			      .execute(Tuple.of(1, 5.0f), ctx.asyncAssertSuccess(insertResult -> {
			         conn.query("SELECT * FROM db2_types WHERE id = 1")
			           .execute(ctx.asyncAssertSuccess(rows -> {
			        	   ctx.assertEquals(1, rows.size());
			        	   Row row = rows.iterator().next();
			        	   ctx.assertEquals(1, row.getInteger(0));
			        	   ctx.assertEquals(5.0f, row.getFloat(1));
			           }));
			      }));
			  }));
	}
	
	@Test
	public void testRowId(TestContext ctx) {
	  assumeTrue("Only DB2/Z supports the ROWID column type", rule.isZOS());
	  
	  final String msg = "insert data for testRowId";
	  connect(ctx.asyncAssertSuccess(conn -> {
	    // Insert some data
	    conn.query("INSERT INTO ROWTEST (message) VALUES ('" + msg + "')")
	      .execute(ctx.asyncAssertSuccess(insertResult -> {
	         // Find it by msg
	         conn.query("SELECT * FROM ROWTEST WHERE message = '" + msg + "'")
	           .execute(ctx.asyncAssertSuccess(rows -> {
	             RowId rowId = verifyRowId(ctx, rows, msg);
	             // Now find it by rowid
	             conn.preparedQuery("SELECT * FROM ROWTEST WHERE id = ?")
	               .execute(Tuple.of(rowId), ctx.asyncAssertSuccess(rows2 -> {
	                 verifyRowId(ctx, rows2, msg);
	               }));
	           }));
	      }));
	  }));
	}
	
  private RowId verifyRowId(TestContext ctx, RowSet<Row> rows, String msg) {
    ctx.assertEquals(1, rows.size());
    Row row = rows.iterator().next();
    ctx.assertEquals(msg, row.getString(1));
    RowId rowid = row.get(RowId.class, 0);
    ctx.assertNotNull(rowid);
    ctx.assertEquals(22, rowid.getBytes().length);
    return rowid;
  }
}
