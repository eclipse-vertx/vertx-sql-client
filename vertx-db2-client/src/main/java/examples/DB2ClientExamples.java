package examples;

import java.sql.Connection;
import java.sql.DriverManager;

import io.vertx.core.AsyncResult;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

/**
 * To Run DB2 in a docker container thaty is compatible with this sample, run
 * the script at scripts/db2.sh
 *
 * @author aguibert
 */
public class DB2ClientExamples {

    public static void runJDBC() throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:db2://192.168.1.22:32772/test", "db2inst1", "foobar1234")) {
            con.createStatement().execute("DROP TABLE IF exists mutable");
            con.createStatement().execute("CREATE TABLE mutable\n" + 
                    "            (\n" + 
                    "              id  integer       NOT NULL,\n" + 
                    "              val varchar(2048) NOT NULL,\n" + 
                    "              PRIMARY KEY (id)\n" + 
                    "            )");
            
            con.createStatement().execute("DROP TABLE IF EXISTS immutable");
                    con.createStatement().execute("CREATE TABLE immutable (" +
            "id      integer       NOT NULL, " +
              "message varchar(2048) NOT NULL," +
              "PRIMARY KEY (id)" +
              ")");

            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (1, 'fortune: No such file or directory');");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (3, 'After enough decimal places, nobody gives a damn.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (7, 'Any program that runs right is obsolete.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (9, 'Feature: A bug with seniority.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (10, 'Computers make very fast, very accurate mistakes.')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (11, '<script>alert(\"This should not be displayed in a browser alert box.\");</script>')");
            con.createStatement().execute("INSERT INTO immutable (id, message) VALUES (12, 'フレームワークのベンチマーク')");
//             con.createStatement().execute("CREATE TABLE users ( id varchar(50) )");
//             con.createStatement().execute("INSERT INTO users VALUES ('andy')");
//             con.createStatement().execute("INSERT INTO users VALUES ('julien')");
//             con.createStatement().execute("INSERT INTO users VALUES ('bob')");
//             con.createStatement().execute("INSERT INTO users VALUES ('chuck')");
////            Statement stmt = con.createStatement();
//            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id=?");
//            ps.setString(1, "andy");
//            ResultSet rs = ps.executeQuery();
//            // ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id='andy'");
////            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
//            while (rs.next())
//                System.out.println("Got JDBC result: " + rs.getString(1));
        }
    }

    public static void main(String args[]) throws Exception {
//         runJDBC();
//         if (true)
//         return;
        
        // Connect options
        DB2ConnectOptions connectOptions = new DB2ConnectOptions()//
                .setPort(32772)//
                .setHost("192.168.1.22")//
                .setDatabase("test")//
                .setUser("db2inst1")//
                .setPassword("foobar1234");

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
//
        client.query("SELECT id, message from immutable", ar -> {
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
                    System.out.println("    " + row);
                }
            } else {
                System.out.println("Failure: " + ar.cause().getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void selectAll(DB2Pool client) {
        client.query("SELECT * FROM users", ar -> {
            try {
                if (ar.succeeded()) {
                    RowSet<Row> result = ar.result();
                    System.out.println("result=" + result);
                    System.out.println("  rows=" + result.rowCount());
                    System.out.println("  size=" + result.size());
                    System.out.println(" names=" + result.columnsNames());
                    for (Row row : result) {
                        System.out.println("  row=" + row);
                        System.out.println("    name=" + row.getColumnName(0));
                        System.out.println("    value=" + row.getString(0));
                    }
                } else {
                    System.out.println("Failure: " + ar.cause().getMessage());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
