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

import org.junit.Ignore;
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
			ctx.assertTrue(ex.getMessage().contains("provided was not found") ||
					ex.getMessage().contains("The connection was closed by the database server"), 
					"The SQL error message returned is not correct.  It should have contained \"provided was not found\" or \"The connection was closed by the database server\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertTrue(ex.getErrorCode() == SqlCode.DATABASE_NOT_FOUND ||
					ex.getErrorCode() == SqlCode.CONNECTION_REFUSED, 
					"Wrong SQL code received");
			ctx.assertTrue(ex.getSqlState().equalsIgnoreCase("2E000") ||
					ex.getSqlState() == SQLState.AUTH_DATABASE_CONNECTION_REFUSED,
					"Wrong SQL state received");
//			System.out.println(ex.getMessage());
		}));
	}

	@Test
	public void testConnectInvalidUsername(TestContext ctx) {
		options.setUser("INVALID_USER_FOR_TESTING");
		DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
			ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
			DB2Exception ex = (DB2Exception) err;
			ctx.assertTrue(ex.getMessage().contains("Invalid credentials"), "The SQL error message returned is not correct.  It should have contained \"Invalid credentials\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertEquals(SqlCode.INVALID_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.NET_CONNECT_AUTH_FAILED, ex.getSqlState());
//			System.out.println(ex.getMessage());
		}));
	}

	@Test
	public void testConnectInvalidPassword(TestContext ctx) {
		options.setPassword("INVALID_PASSWORD_FOR_TESTING");
		DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
			ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
			DB2Exception ex = (DB2Exception) err;
			ctx.assertTrue(ex.getMessage().contains("Invalid credentials"), "The SQL error message returned is not correct.  It should have contained \"Invalid credentials\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertEquals(SqlCode.INVALID_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.NET_CONNECT_AUTH_FAILED, ex.getSqlState());
//			System.out.println(ex.getMessage());
		}));
	}

	@Test
	@Ignore
	public void testQueryBlankDatabase(TestContext ctx) {
		try {
			options.setDatabase("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (Exception e) {
			ctx.assertTrue(e instanceof DB2Exception, "Expected a DB2Exception to be thrown");
			DB2Exception ex = (DB2Exception) e;
			ctx.assertTrue(ex.getMessage().contains("The database name cannot be blank or null"), "The SQL error message returned is not correct.  It should have contained \"The database name cannot be blank or null\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertEquals(SqlCode.DATABASE_NOT_FOUND, ex.getErrorCode());
			ctx.assertEquals(SQLState.DATABASE_NOT_FOUND, ex.getSqlState());
//			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void testQueryBlankUsername(TestContext ctx) {
		try {
			options.setUser("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (Exception e) {
			ctx.assertTrue(e instanceof DB2Exception, "Expected a DB2Exception to be thrown");
			DB2Exception ex = (DB2Exception) e;
			ctx.assertTrue(ex.getMessage().contains("The user cannot be blank or null"), "The SQL error message returned is not correct.  It should have contained \"The user cannot be blank or null\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertEquals(SqlCode.MISSING_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.CONNECT_USERID_ISNULL, ex.getSqlState());
//			System.out.println(ex.getMessage());
		}
	}

	@Test
	public void testQueryBlankPassword(TestContext ctx) {
		try {
			options.setPassword("");
			ctx.fail("Expected a DB2Exception to be thrown");
		} catch (Exception e) {
			ctx.assertTrue(e instanceof DB2Exception, "Expected a DB2Exception to be thrown");
			DB2Exception ex = (DB2Exception) e;
			ctx.assertTrue(ex.getMessage().contains("The password cannot be blank or null"), "The SQL error message returned is not correct.  It should have contained \"The password cannot be blank or null\", but instead it said \"" + ex.getMessage() + "\"");
			ctx.assertEquals(SqlCode.MISSING_CREDENTIALS, ex.getErrorCode());
			ctx.assertEquals(SQLState.CONNECT_PASSWORD_ISNULL, ex.getSqlState());
//			System.out.println(ex.getMessage());
		}
	}

	@Test
	// This should cause sqlCode=-104 sqlState=42601 to be returned from the server
	public void testQueryBlankTable(TestContext ctx) {
		DB2Connection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
			conn.query("SELECT id, message FROM ").execute(ctx.asyncAssertFailure(err -> {
				ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
				DB2Exception ex = (DB2Exception) err;
				ctx.assertTrue(ex.getMessage().contains("The SQL syntax provided was invalid"), "The SQL error message returned is not correct.  It should have contained \"The SQL syntax provided was invalid\", but instead it said \"" + ex.getMessage() + "\"");
				ctx.assertEquals(SqlCode.INVALID_SQL_STATEMENT, ex.getErrorCode());
				ctx.assertEquals("42601", ex.getSqlState());
//				System.out.println(ex.getMessage());
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
				ctx.assertTrue(ex.getMessage().contains("provided is not defined"), "The SQL error message returned is not correct.  It should have contained \"provided is not defined\", but instead it said \"" + ex.getMessage() + "\"");
				ctx.assertEquals(SqlCode.OBJECT_NOT_DEFINED, ex.getErrorCode());
				ctx.assertEquals("42704", ex.getSqlState());
//				System.out.println(ex.getMessage());
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
				ctx.assertTrue(ex.getMessage().contains("provided does not exist"), "The SQL error message returned is not correct.  It should have contained \"provided does not exist\", but instead it said \"" + ex.getMessage() + "\"");
				ctx.assertEquals(SqlCode.COLUMN_DOES_NOT_EXIST, ex.getErrorCode());
				ctx.assertEquals("42703", ex.getSqlState());
//				System.out.println(ex.getMessage());
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
				ctx.assertTrue(ex.getMessage().contains("The SQL syntax provided was invalid"), "The SQL error message returned is not correct.  It should have contained \"The SQL syntax provided was invalid\", but instead it said \"" + ex.getMessage() + "\"");
				ctx.assertEquals(SqlCode.INVALID_SQL_STATEMENT, ex.getErrorCode());
				ctx.assertEquals("42601", ex.getSqlState());
//				System.out.println(ex.getMessage());
			}));
		}));
	}
}
