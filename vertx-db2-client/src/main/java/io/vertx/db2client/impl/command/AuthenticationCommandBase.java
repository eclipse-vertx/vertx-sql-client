package io.vertx.db2client.impl.command;

import java.util.Map;

import io.vertx.sqlclient.impl.command.CommandBase;

public class AuthenticationCommandBase<R> extends CommandBase<R> {
    private final String username;
    private final String password;
    private final String database;
    private final Map<String, String> connectionAttributes;

    public AuthenticationCommandBase(String username, String password, String database,
            Map<String, String> connectionAttributes) {
        this.username = username;
        this.password = password;
        this.database = database;
        this.connectionAttributes = connectionAttributes;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String database() {
        return database;
    }

    public Map<String, String> connectionAttributes() {
        return connectionAttributes;
    }
}
