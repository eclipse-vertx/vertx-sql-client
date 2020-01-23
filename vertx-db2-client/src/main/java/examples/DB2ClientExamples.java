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
package examples;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

/**
 * To Run DB2 in a docker container thaty is compatible with this sample, run
 * the script at scripts/db2.sh
 */
public class DB2ClientExamples {
	
	static final String HOST = "localhost";
	static final int PORT = 50000;
	static final String DB_NAME = "vertx_db";
	static final String DB_USER = "db2user";
	static final String DB_PASS = "db2pass";
    
    static {
        System.setProperty("java.util.logging.config.file", Paths.get("src", "test", "resources", "vertx-default-jul-logging.properties").toString());
    }
    
    public static void runJDBC() throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:db2://" + HOST + ":" + PORT + "/" + DB_NAME, DB_USER, DB_PASS)) {
//            runInitSql(con);
//            
//            // Insert lots of data to immutable table
//            for (int i = 13; i < 100; i++)
//                con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (" + i + ", 'Sample data " + i + "')");
            
            PreparedStatement ps = con.prepareStatement("SELECT * FROM immutable WHERE id=?");
            ps.setInt(1, 5);
            ps.executeQuery();
            
            ps.setInt(1, 6);
            ps.executeQuery();
            
            ps.setInt(1, 7);
            ps.executeQuery();
            
            ps = con.prepareStatement("SELECT message FROM immutable WHERE id=?");
            ps.setInt(1, 8);
            ps.executeQuery();
            
            ps.setInt(1, 9);
            ps.executeQuery();
            
//            getDB2Message(con, -804, "01", "07002", -2146303980, 20, 0, 0, -1550, 0);
            
            System.out.println("Done with JDBC");
        }
    }

    public static void main(String args[]) throws Exception {
//         runJDBC();
//         if (true)
//         return;
        
        // Connect options
        DB2ConnectOptions connectOptions = new DB2ConnectOptions()//
                .setPort(PORT)//
                .setHost(HOST)//
                .setDatabase(DB_NAME)//
                .setUser(DB_USER)//
                .setPassword(DB_PASS);

        // Pool options
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        
        // Create the client pool
        DB2Pool client = DB2Pool.pool(connectOptions, poolOptions);

//        client.query("CREATE TABLE IF NOT EXISTS users ( id varchar(50) )", ar -> {
//            if (ar.succeeded()) {
//                System.out.println("Created table");
//            } else {
//                System.out.println("Create failed: " + ar.cause());
//            }
//        });
        
//        client.getConnection(connResult -> {
//        	final SqlConnection conn = connResult.result(); 
//        	conn.preparedQuery("INSERT INTO mutable (id, val) VALUES (2, 'Whatever')", insert -> {
//        		System.out.println("@AGG inside INSERT result. Row count = " + insert.result().rowCount()); // 1
//        		conn.preparedQuery("UPDATE mutable SET val = 'Rocks!' WHERE id = 2", update -> {
//        			System.out.println("@AGG inside UPDATE result. Row count = " + update.result().rowCount()); // 1
//                    conn.preparedQuery("SELECT val FROM mutable WHERE id = 2", select -> {
//                    	System.out.println("@AGG inside select result is: " + select.result().iterator().next().getValue(0)); // Rocks!
//                    	conn.close();
//                    });
//        		});
//        	});
//        });
        
//        // Tests incremental data fetching
//        client.getConnection(connResult -> {
//        	final SqlConnection conn = connResult.result(); 
//        	conn.prepare("SELECT * FROM immutable WHERE id=? OR id=? OR id=? OR id=? OR id=? OR id=?", select -> {
//        		System.out.println("@AGG inside SELECT");
//        		Cursor query = select.result().cursor(Tuple.of(1, 8, 4, 11, 2, 9));
//        		query.read(4, result -> {
//        			System.out.println("@AGG inside query read");
//        			System.out.println("  size=" + result.result().size()); // 4
//        			System.out.println("  hasMore=" + query.hasMore()); // true
//        			for (Row r : result.result())
//        			    System.out.println("Got row: " + r.getInteger(0) + "  " + r.getString(1));
//        			query.read(4, moreResults -> {
//        				System.out.println("@AGG second read");
//        				System.out.println("  size=" + moreResults.result().size());
//        				System.out.println("  hasMore=" + query.hasMore());
//        				for (Row r : moreResults.result())
//        				    System.out.println("Got row: " + r.getInteger(0) + "  " + r.getString(1));
//        				conn.close();
//        			});
//        		});
//        	});
//        });
        
//        client.getConnection(ar -> {
//            System.out.println("@AGG got connection");
//            SqlConnection con = ar.result();
//            con.closeHandler(onClose -> {
//                System.out.println("@AGG connection is closed");
//            });
//            con.prepare("SELECT id, message from immutable", ar2 -> {
//                System.out.println("@AGG inside prepare");
//                PreparedQuery pq = ar2.result();
//                Cursor cursor = pq.cursor();
//                cursor.read(10, ar3 -> {
//                    System.out.println("@AGG inside cursor read");
//                    dumpResults(ar3);
//                });
//            });
//        });
        
        client.preparedQuery("SELECT id, message FROM immutable", ar -> {
            System.out.println("@AGG inside PS lambda");
            dumpResults(ar);
        });
        
//        client.preparedQuery("SELECT id, message FROM immutable WHERE id=? OR id=?", Tuple.of(1, 2), ar -> {
//            System.out.println("@AGG inside PS lambda");
//            dumpResults(ar);
//          });
        
//        client.preparedQuery("INSERT INTO immutable (id, message) VALUES (?, ?)", Tuple.of(14, "Hello updates!"), ar -> {
//            System.out.println("@AGG inside PS update lambda");
//            dumpResults(ar);
//          });
        
        waitFor(3000);
        client.close();
        waitFor(500);
        System.out.println("Done");
    }
    
    private static void stressTest(int NUM_QUERIES, DB2Pool client) throws Exception {
        long allStart = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(NUM_QUERIES);
        
        for (int i = 0; i < NUM_QUERIES; i++) {
            final int queryNum = i + 1;
            long queryStart = System.currentTimeMillis();
            client.query("SELECT id, message from immutable WHERE id=" + queryNum, ar -> {
                latch.countDown();
                if (!ar.succeeded()) {
                    System.out.println("QUERY " + queryNum + " FAILED");
                    ar.cause().printStackTrace();
                } else {
                    System.out.println("Query " + queryNum + " complete in " + (System.currentTimeMillis() - queryStart) + "ms");
                }
//                dumpResults(ar);
            });
        }
        
        if (!latch.await(15, TimeUnit.SECONDS)) {
            System.out.println("Queries timed out.");
        } else {
            System.out.println("All queries complete in " + (System.currentTimeMillis() - allStart) + "ms");
        }
    }
    
    private static void waitFor(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void dumpResults(AsyncResult<RowSet<Row>> ar) {
        try {
            if (ar.succeeded()) {
                RowSet<Row> result = ar.result();
                System.out.println("result=" + result);
                System.out.println("  rowCount=" + result.rowCount());
                System.out.println("  size=" + result.size());
                System.out.println("  names=" + result.columnsNames());
                for (Tuple row : result) {
                    System.out.println("    id=" + row.getInteger(0) + " message=" + row.getString(1));
                }
            } else {
                System.out.println("Failure: " + ar.cause().getMessage());
                ar.cause().printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void runInitSql(Connection con) throws Exception {
        String currentLine = "";
        for (String sql : Files.readAllLines(Paths.get("src", "test", "resources", "init.sql"))) {
            if (sql.startsWith("--"))
                continue;
            currentLine += sql;
            if (sql.endsWith(";")) {
                System.out.println("Run SQL: " + currentLine);
                con.createStatement().execute(currentLine);
                currentLine = "";
            }
        }
    }
    
    private static void getDB2Message(Connection con, int sqlCode, String sqlErrmc, String sqlState, int... sqlerrd) throws Exception {
        if (sqlerrd != null && sqlerrd.length != 6 && sqlerrd.length != 0)
            throw new IllegalArgumentException("Sqlerrd array must be either length 0 or 6");
        if (sqlerrd == null || sqlerrd.length == 0)
            sqlerrd = new int[6];
        
        CallableStatement cs = con.prepareCall("call SYSIBM.SQLCAMESSAGE(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        cs.setInt(1, sqlCode);// 0 or sqlcode
        cs.setShort(2, (short) (sqlErrmc == null ? 0 : sqlErrmc.length())); // 0 or sqlerrmc length
        cs.setString(3, sqlErrmc); // errmc tokens as string
        cs.setString(4, "SQLRA144"); // sqlerrp
        // sqlerrd: sql internal error codes
        cs.setInt(5, sqlerrd[0]);
        cs.setInt(6, sqlerrd[1]);
        cs.setInt(7, sqlerrd[2]);
        cs.setInt(8, sqlerrd[3]);
        cs.setInt(9, sqlerrd[4]);
        cs.setInt(10, sqlerrd[5]);
        cs.setString(11, null); // sql warn
        cs.setString(12, sqlState); // sql state string
        cs.setString(13, null); // message file name
        cs.setString(14, Locale.getDefault().toString());
        cs.registerOutParameter(14, Types.VARCHAR);
        cs.registerOutParameter(15, Types.LONGVARCHAR);
        cs.registerOutParameter(16, Types.INTEGER);
        cs.execute();
        
        if (cs.getInt(16) == 0) {
            // Return msg text
            System.out.println(cs.getString(15));
        } else {
            System.out.println("Unable to get DB2 message text");
        }
    }
}
