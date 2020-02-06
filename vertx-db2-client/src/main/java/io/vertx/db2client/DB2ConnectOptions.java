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
package io.vertx.db2client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * Connect options for configuring {@link DB2Connection} or {@link DB2Pool}.
 */
@DataObject(generateConverter = true)
public class DB2ConnectOptions extends SqlConnectOptions {
    
    /**
     * Provide a {@link DB2ConnectOptions} configured from a connection URI.
     *
     * @param connectionUri the connection URI to configure from
     * @return a {@link DB2ConnectOptions} parsed from the connection URI
     * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
     */
    public static DB2ConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented");
//      JsonObject parsedConfiguration = DB2ConnectionUriParser.parse(connectionUri);
//      return new DB2ConnectOptions(parsedConfiguration);
    }

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 50000;
    public static final String DEFAULT_USER = "root";
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_SCHEMA = "";
    public static final String DEFAULT_CHARSET = "utf8";
    public static final boolean DEFAULT_USE_AFFECTED_ROWS = false;
    public static final Map<String, String> DEFAULT_CONNECTION_ATTRIBUTES;

    static {
        Map<String, String> defaultAttributes = new HashMap<>();
        defaultAttributes.put("_client_name", "vertx-db2-client");
        DEFAULT_CONNECTION_ATTRIBUTES = Collections.unmodifiableMap(defaultAttributes);
    }

    public DB2ConnectOptions() {
        super();
    }

    public DB2ConnectOptions(String uri) {
        super();
        throw new UnsupportedOperationException("TODO @AGG need to implement uri parsing");
    }

    public DB2ConnectOptions(JsonObject json) {
        super(json);
    }

    public DB2ConnectOptions(DB2ConnectOptions other) {
        super(other);
    }

    @Override
    public DB2ConnectOptions setHost(String host) {
        return (DB2ConnectOptions) super.setHost(host);
    }

    @Override
    public DB2ConnectOptions setPort(int port) {
        return (DB2ConnectOptions) super.setPort(port);
    }

    @Override
    public DB2ConnectOptions setUser(String user) {
        return (DB2ConnectOptions) super.setUser(user);
    }

    @Override
    public DB2ConnectOptions setPassword(String password) {
        return (DB2ConnectOptions) super.setPassword(password);
    }

    @Override
    public DB2ConnectOptions setDatabase(String database) {
        return (DB2ConnectOptions) super.setDatabase(database);
    }

    @Override
    public DB2ConnectOptions setProperties(Map<String, String> properties) {
        return (DB2ConnectOptions) super.setProperties(properties);
    }

    @GenIgnore
    @Override
    public DB2ConnectOptions addProperty(String key, String value) {
        return (DB2ConnectOptions) super.addProperty(key, value);
    }

    /**
     * Initialize with the default options.
     */
    protected void init() {
        this.setHost(DEFAULT_HOST);
        this.setPort(DEFAULT_PORT);
        this.setUser(DEFAULT_USER);
        this.setPassword(DEFAULT_PASSWORD);
        this.setDatabase(DEFAULT_SCHEMA);
        this.setProperties(new HashMap<>(DEFAULT_CONNECTION_ATTRIBUTES));
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        return json;
    }
}
