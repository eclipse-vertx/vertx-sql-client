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
	@Ignore		// TODO - Need to figure out why this is blowing up in parseACCSECreply()
	public void testConnectInvalidDatabase(TestContext ctx) {
	  options.setDatabase("DOES_NOT_EXIST");
	  DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
		  ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
//		  DB2Exception ex = (DB2Exception) err;
//		  ctx.assertTrue(ex.getMessage().contains("Invalid database"), "The SQL error message returned is not correct.  It should have contained \"Invalid database\", but instead it said \"" + ex.getMessage() + "\"");
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
		  System.out.println(err.getMessage());
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
			  System.out.println(err.getMessage());
		  }));
	}
	
	@Test
	@Ignore  // TODO - @GJW I want to validate we would always want to fail a test for a blank user name.  If so, we want to catch this *before* we connect to the DB2 server
	public void testConnectBlankUsername(TestContext ctx) {
	  options.setUser("");
	  DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
		  ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
		  ctx.assertTrue("Missing userid, verify a user value was supplied".equalsIgnoreCase(err.getMessage()), "The text of the returned error message is incorrect.  It should say \"Missing userid, verify a user value was supplied\", but instead, it says \"" + err.getMessage() + "\"");
	  }));
	}
	
	@Test
	@Ignore  // TODO - @GJW I want to validate we would always want to fail a test for a blank password.  If so, we want to catch this *before* we connect to the DB2 server 
	public void testConnectBlankPassword(TestContext ctx) {
		options.setPassword("");
		  DB2Connection.connect(vertx, options, ctx.asyncAssertFailure(err -> {
              ctx.assertTrue(err instanceof DB2Exception, "The error message returned is of the wrong type.  It should be a DB2Exception, but it was of type " + err.getClass().getSimpleName());
              ctx.assertTrue("Missing password, verify a password value was supplied".equalsIgnoreCase(err.getMessage()), "The text of the returned error message is incorrect.  It should say \"Missing password, verify a password value was supplied\", but instead, it says \"" + err.getMessage() + "\"");		  
		  }));
	}
	
	@Test
	@Ignore		// TODO - complete
	public void testConnectInvalidQuery(TestContext ctx) {
	}
}
