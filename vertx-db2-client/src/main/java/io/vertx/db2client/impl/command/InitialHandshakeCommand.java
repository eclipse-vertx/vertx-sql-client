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
