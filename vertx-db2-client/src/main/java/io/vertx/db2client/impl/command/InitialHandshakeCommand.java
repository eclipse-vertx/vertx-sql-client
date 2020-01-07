package io.vertx.db2client.impl.command;

import java.util.Map;

import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;

public class InitialHandshakeCommand extends AuthenticationCommandBase<Connection> {
    private final SocketConnectionBase conn;

    public InitialHandshakeCommand(SocketConnectionBase conn, String username, String password, String database,
            Map<String, String> connectionAttributes) {
        super(username, password, database, connectionAttributes);
        this.conn = conn;
    }

    public SocketConnectionBase connection() {
        return conn;
    }

}
