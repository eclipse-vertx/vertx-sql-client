package io.vertx.db2client.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PoolBase;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class DB2PoolImpl extends PoolBase<DB2PoolImpl> implements DB2Pool {
    private final DB2ConnectionFactory factory;

    public DB2PoolImpl(Context context, boolean closeVertx, DB2ConnectOptions connectOptions, PoolOptions poolOptions) {
        super(context, closeVertx, poolOptions);
        this.factory = new DB2ConnectionFactory(context, Vertx.currentContext() != null, connectOptions);
    }

    @Override
    public void connect(Handler<AsyncResult<Connection>> completionHandler) {
        factory.connect(completionHandler);
    }

    @Override
    protected SqlConnectionImpl<?> wrap(Context context, Connection conn) {
        return new DB2ConnectionImpl(factory, context, conn);
    }

    @Override
    protected void doClose() {
        factory.close();
        super.doClose();
    }
}
