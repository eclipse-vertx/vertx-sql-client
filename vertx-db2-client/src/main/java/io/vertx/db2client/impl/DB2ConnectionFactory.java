package io.vertx.db2client.impl;

import java.util.Collections;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.sqlclient.impl.Connection;

public class DB2ConnectionFactory {
    private final NetClient netClient;
    private final Context context;
    private final boolean registerCloseHook;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final Map<String, String> connectionAttributes;
    private final boolean cachePreparedStatements;
    private final int preparedStatementCacheSize;
    private final int preparedStatementCacheSqlLimit;
    private final Closeable hook;

    public DB2ConnectionFactory(Context context, boolean registerCloseHook, DB2ConnectOptions options) {
        NetClientOptions netClientOptions = new NetClientOptions(options);

        this.context = context;
        this.registerCloseHook = registerCloseHook;
        this.hook = this::close;
        if (registerCloseHook) {
            context.addCloseHook(hook);
        }

        this.host = options.getHost();
        this.port = options.getPort();
        this.username = options.getUser();
        this.password = options.getPassword();
        this.database = options.getDatabase();
        this.connectionAttributes = Collections.unmodifiableMap(options.getProperties());

        this.cachePreparedStatements = options.getCachePreparedStatements();
        this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
        this.preparedStatementCacheSqlLimit = options.getPreparedStatementCacheSqlLimit();

        this.netClient = context.owner().createNetClient(netClientOptions);
    }

    // Called by hook
    private void close(Handler<AsyncResult<Void>> completionHandler) {
        netClient.close();
        completionHandler.handle(Future.succeededFuture());
    }

    void close() {
        if (registerCloseHook) {
            context.removeCloseHook(hook);
        }
        netClient.close();
    }

    public void connect(Handler<AsyncResult<Connection>> handler) {
        Promise<NetSocket> promise = Promise.promise();
        promise.future().setHandler(ar1 -> {
            if (ar1.succeeded()) {
                NetSocketInternal socket = (NetSocketInternal) ar1.result();
                DB2SocketConnection conn = new DB2SocketConnection(socket, cachePreparedStatements,
                        preparedStatementCacheSize, preparedStatementCacheSqlLimit, context);
                conn.init();
                conn.sendStartupMessage(username, password, database, connectionAttributes, handler);
            } else {
                handler.handle(Future.failedFuture(ar1.cause()));
            }
        });
        netClient.connect(port, host, promise);
    }
}
