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

import static io.vertx.db2client.junit.TestUtil.assertContains;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.impl.drda.SQLState;
import io.vertx.db2client.impl.drda.SqlCode;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class DB2ErrorMessageTest extends DB2TestBase {

	@Test
	public void testConnectInvalidDatabase(TestContext ctx) {
		options.setDatabase("DB_DOES_NOT_EXIST");
		DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
			ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
			DB2Exception ex = (DB2Exception) err;
			assertContains(ctx, ex.getMessage(), "provided was not found", "The connection was closed by the database server");
			ctx.assertTrue(ex.getErrorCode() == SqlCode.DATABASE_NOT_FOUND ||
					ex.getErrorCode() == SqlCode.CONNECTION_REFUSED, 
					"Wrong SQL code received.  Expecting " + SqlCode.DATABASE_NOT_FOUND + " or " + SqlCode.CONNECTION_REFUSED + ", but received " + ex.getErrorCode());
			assertContains(ctx, ex.getSqlState(), "2E000", SQLState.AUTH_DATABASE_CONNECTION_REFUSED);
		}));
	}

	@Test
	public void testConnectInvalidUsername(TestContext ctx) {
		options.setUser("INVALID_USER_FOR_TESTING");
		DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
			ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
			DB2Exception ex = (DB2Exception) err;
			assertContains(ctx, ex.getMessage(), "Invalid credentials");
			ctx.assertEquals(SqlCode.INVALID_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.NET_CONNECT_AUTH_FAILED, ex.getSqlState());
		}));
	}

	@Test
	public void testConnectInvalidPassword(TestContext ctx) {
		options.setPassword("INVALID_PASSWORD_FOR_TESTING");
		DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
			ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
			DB2Exception ex = (DB2Exception) err;
			assertContains(ctx, ex.getMessage(), "Invalid credentials");
			ctx.assertEquals(SqlCode.INVALID_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.NET_CONNECT_AUTH_FAILED, ex.getSqlState());
		}));
	}

	@Test
	public void testQueryBlankDatabase(TestContext ctx) {
		try {
			options.setDatabase("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (DB2Exception ex) {
			assertContains(ctx, ex.getMessage(), "The database name cannot be blank or null");
			ctx.assertEquals(SqlCode.DATABASE_NOT_FOUND, ex.getErrorCode());
			ctx.assertEquals(SQLState.DATABASE_NOT_FOUND, ex.getSqlState());
		}
	}

	@Test
	public void testQueryBlankUsername(TestContext ctx) {
		try {
			options.setUser("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (DB2Exception ex) {
		    assertContains(ctx, ex.getMessage(), "The user cannot be blank or null");
			ctx.assertEquals(SqlCode.MISSING_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.CONNECT_USERID_ISNULL, ex.getSqlState());
		}
	}

	@Test
	public void testQueryBlankPassword(TestContext ctx) {
		try {
			options.setPassword("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (DB2Exception ex) {
		    assertContains(ctx, ex.getMessage(), "The password cannot be blank or null");
			ctx.assertEquals(SqlCode.MISSING_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.CONNECT_PASSWORD_ISNULL, ex.getSqlState());
		}
	}

	@Test
	// This should cause sqlCode=-104 sqlState=42601 to be returned from the server
	public void testQueryBlankTable(TestContext ctx) {
		DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT id, message FROM ").execute(ctx.asyncAssertFailure(err -> {
				ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
				DB2Exception ex = (DB2Exception) err;
				assertContains(ctx, ex.getMessage(), "The SQL syntax provided was invalid");
				ctx.assertEquals(SqlCode.INVALID_SQL_STATEMENT, ex.getErrorCode());
				ctx.assertEquals("42601", ex.getSqlState());
			}));
		}));
	}

	@Test
	// This should cause sqlCode=-204 sqlState=42704 to be returned from the server
	public void testInvalidTableQuery(TestContext ctx) {
		DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT id, message FROM TABLE_DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(err -> {
				ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
				DB2Exception ex = (DB2Exception) err;
				assertContains(ctx, ex.getMessage(), "provided is not defined");
				ctx.assertEquals(SqlCode.OBJECT_NOT_DEFINED, ex.getErrorCode());
				ctx.assertEquals("42704", ex.getSqlState());
			}));
		}));
	}

	@Test
	// This should cause sqlCode=-206 sqlState=42703 to be returned from the server
	public void testInvalidColumnQuery(TestContext ctx) {
		DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT INVALID_COLUMN FROM immutable").execute(ctx.asyncAssertFailure(err -> {
				ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
				DB2Exception ex = (DB2Exception) err;
				assertContains(ctx, ex.getMessage(), "provided does not exist");
				ctx.assertEquals(SqlCode.COLUMN_DOES_NOT_EXIST, ex.getErrorCode());
				ctx.assertEquals("42703", ex.getSqlState());
			}));
		}));
	}

	@Test
	// This should cause sqlCode=-104 sqlState=42601 to be returned from the server
	public void testInvalidQuery(TestContext ctx) {
		DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
			conn.query("KJHDKJAHDQWEUWHQDDA:SHDL:KASHDJ").execute(ctx.asyncAssertFailure(err -> {
				ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
				DB2Exception ex = (DB2Exception) err;
				assertContains(ctx, ex.getMessage(), "The SQL syntax provided was invalid");
				ctx.assertEquals(SqlCode.INVALID_SQL_STATEMENT, ex.getErrorCode());
				ctx.assertEquals("42601", ex.getSqlState());
			}));
		}));
	}

	/**
	 * Incorrect number of inserted columns (2) and inserted values (3)
	 * Should force sqlcode -117
	 */
	@Test
	public void testMismatchingInsertedColumns(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("INSERT INTO basicdatatype (id, test_int_2) VALUES (99,24,25)").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "An INSERT statement contained a different number of insert columns from the number of insert values that were supplied");
            ctx.assertEquals(SqlCode.MISMATCHING_COLUMNS_AND_VALUES, ex.getErrorCode());
          }));
      }));
    }
	
	/**
	 * Inserted column is repeated more than once
	 * Should force sqlcode -121
	 */
	@Test
    public void testRepeatedColumnReference(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("INSERT INTO basicdatatype (id, test_int_2, test_int_2) VALUES (99,24,25)").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "An INSERT or UPDATE statement listed the same column name (TEST_INT_2) more than one time in its update list");
            ctx.assertEquals(SqlCode.REPEATED_COLUMN_REFERENCE, ex.getErrorCode());
          }));
      }));
	}
	
	/**
	 * A query with joins from two tables that both have columns with the same name, 
	 * but the column names are not qualified by the table prefix
	 * Should for sqlcode -203
	 */
    @Test
    public void testAmbiguousColumnName(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT immutable.id AS IMM_ID," +
            "Fortune.id AS FORT_ID," +
            "message FROM immutable " +
            "INNER JOIN Fortune ON immutable.id = Fortune.id " +
            "WHERE immutable.id=1").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "A reference to the column name 'MESSAGE' is ambiguous");
            ctx.assertEquals(SqlCode.AMBIGUOUS_COLUMN_NAME, ex.getErrorCode());
          }));
      }));
    }
    
    /**
     * Insert a new row that would violate a unique column constraint
     * Should force sqlcode -803
     */
    @Test
    public void testDuplicateKeys(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("INSERT INTO immutable (id, message) VALUES (1, 'a duplicate key')").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "Duplicate keys were detected on table " + options.getUser().toUpperCase() + ".IMMUTABLE");
            ctx.assertEquals(SqlCode.DUPLICATE_KEYS_DETECTED, ex.getErrorCode());
          }));
      }));
    }
    
    /**
     * Tables created with a PRIMARY KEY column must also specify NOT NULL
     * Should force sqlcode -542
     */
    @Test
    public void testCreateTableNullPrimaryKey(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("CREATE TABLE fruits (id INTEGER PRIMARY KEY, name VARCHAR(50) NOT NULL)").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "The column 'ID' cannot be a column of a primary key because it can contain null values");
            ctx.assertEquals(SqlCode.PRIMARY_KEY_CAN_BE_NULL, ex.getErrorCode());
          }));
      }));
    }
    
    // 
    /**
     * Try inserting a specific value into a column that is declared GENERATED ALWAYS
     * Should force sqlcode -798
     */
    @Test
    public void testInsertIntoGeneratedAlwaysColumn(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("INSERT INTO Fortune (id,message) VALUES (25, 'hello world')").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "A value cannot be specified for column 'ID' which is identified as GENERATED ALWAYS");
            ctx.assertEquals(SqlCode.INSERT_INTO_GENERATED_ALWAYS, ex.getErrorCode());
          }));
      }));
    }
    
    @Test
    public void testDuplicateObject(TestContext ctx) {
      DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.query("CREATE TABLE Fortune (\n" + 
            "  id       integer       NOT NULL GENERATED AS IDENTITY (START WITH 1, INCREMENT BY 1),\n" + 
            "  message  varchar(2048),\n" + 
            "  PRIMARY KEY  (id)\n" + 
            ")").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
            DB2Exception ex = (DB2Exception) err;
            assertContains(ctx, ex.getMessage(), "The object with the name '");
            assertContains(ctx, ex.getMessage(), "FORTUNE'");
            assertContains(ctx, ex.getMessage(), "of type 'TABLE' already exists");
            ctx.assertEquals(SqlCode.OBJECT_ALREADY_EXISTS, ex.getErrorCode());
          }));
      }));
    }
	}
