package io.vertx.db2client.impl.codec;

import java.util.stream.Collector;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.DB2RowImpl;
import io.vertx.db2client.impl.drda.Cursor;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {

    final DB2RowDesc rowDesc;
    final Cursor cursor;
    final DRDAQueryResponse response;

    RowResultDecoder(Collector<Row, C, R> collector, DB2RowDesc rowDesc, Cursor cursor, DRDAQueryResponse resp) {
        super(collector);
        this.rowDesc = rowDesc;
        this.cursor = cursor;
        this.response = resp;
    }

    public boolean isQueryComplete() {
        return response.isQueryComplete();
    }

    public boolean next() {
        response.readOpenQueryData();
        return cursor.next();
    }

    @Override
    protected Row decodeRow(int len, ByteBuf in) {
        Row row = new DB2RowImpl(rowDesc);
        for (int i = 1; i < rowDesc.columnDefinitions().columns_ + 1; i++) {
            Object o = cursor.getObject(i);
            row.addValue(o);
        }
        return row;
    }
}
