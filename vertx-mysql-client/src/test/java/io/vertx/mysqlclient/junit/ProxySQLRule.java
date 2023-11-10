package io.vertx.mysqlclient.junit;

import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ProxySQLRule extends ExternalResource {

  private static final String MONITORING_USER = "proxysql";
  private static final String MONITORING_USER_PASSWORD = "proxysql1234#";

  private final MySQLRule mySQLRule;
  private GenericContainer<?> proxySql;

  public ProxySQLRule(MySQLRule mySQLRule) {
    this.mySQLRule = mySQLRule;
  }

  @Override
  protected void before() throws Throwable {
    proxySql = new GenericContainer<>("proxysql/proxysql")
      .withLogConsumer(of -> System.out.print("[ProxySQL] " + of.getUtf8String()))
      .withCreateContainerCmdModifier(createContainerCmd -> {
        createContainerCmd.getHostConfig().withNetworkMode(mySQLRule.network());
      })
      .withExposedPorts(6032, 6033, 6070)
      .waitingFor(Wait.forLogMessage(".*Latest ProxySQL version available.*\\n", 1));

    proxySql.start();

    execStatement("UPDATE global_variables SET variable_value='false' WHERE variable_name='admin-hash_passwords'", 10);
    execStatement("LOAD ADMIN VARIABLES TO RUNTIME");

    execStatement(format("UPDATE global_variables SET variable_value='%s' WHERE variable_name='mysql-monitor_username'", MONITORING_USER));
    execStatement(format("UPDATE global_variables SET variable_value='%s' WHERE variable_name='mysql-monitor_password'", MONITORING_USER_PASSWORD));
    execStatement("UPDATE global_variables SET variable_value='2000' WHERE variable_name IN ('mysql-monitor_connect_interval','mysql-monitor_ping_interval','mysql-monitor_read_only_interval')");

    execStatement("LOAD MYSQL VARIABLES TO RUNTIME");

    execStatement(format("INSERT INTO mysql_servers(hostgroup_id, hostname, port) VALUES (0,'%s',3306)", mySQLRule.networkAlias()));
    execStatement("LOAD MYSQL SERVERS TO RUNTIME");

    execStatement(format("INSERT INTO mysql_users (username,password) VALUES ('%s','%s')", mySQLRule.options().getUser(), mySQLRule.options().getPassword()));
    execStatement("LOAD MYSQL USERS TO RUNTIME");

    for (long start = System.currentTimeMillis(); ; ) {
      ExecResult res = execStatement("SELECT connect_error FROM monitor.mysql_server_connect_log ORDER BY time_start_us LIMIT 1");
      if (res.getStdout().startsWith("NULL")) {
        break;
      }
      if (System.currentTimeMillis() - start > 30000) {
        throw new IllegalStateException(String.format("ProxySQL could not connect to backend: %s%n%s", res.getStdout(), res.getStderr()));
      }
      MILLISECONDS.sleep(500);
    }
  }

  private ExecResult execStatement(String statement) throws Exception {
    return execStatement(statement, 0);
  }

  private ExecResult execStatement(String statement, int retry) throws Exception {
    RuntimeException failure;
    for (int i = 0; ; i++) {
      ExecResult result = proxySql.execInContainer("mysql", "-u", "admin", "-padmin", "-h", "127.0.0.1", "-P", "6032", "-sN", "-e", statement);
      if (result.getExitCode() == 0) {
        return result;
      }
      if (i >= retry && !result.getStderr().contains("ERROR 2002")) {
        failure = new RuntimeException("Failed to execute statement: " + statement + "\n" + result.getStderr());
        break;
      }
      MILLISECONDS.sleep(200);
    }
    throw failure;
  }

  public MySQLConnectOptions options(MySQLConnectOptions other) {
    return new MySQLConnectOptions(other)
      .setHost(proxySql.getHost())
      .setPort(proxySql.getMappedPort(6033));
  }

  @Override
  protected void after() {
    if (proxySql != null) {
      proxySql.stop();
    }
  }
}
