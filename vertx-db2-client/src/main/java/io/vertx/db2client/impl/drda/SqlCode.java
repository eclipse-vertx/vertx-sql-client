package io.vertx.db2client.impl.drda;

public class SqlCode {
    private int code_;

    SqlCode(int code) {
        code_ = code;
    }

    /**
     * Return the SQL code represented by this instance.
     *
     * @return an SQL code
     */
    public final int getCode() {
        return code_;
    }

    public final static SqlCode queuedXAError = new SqlCode(-4203);

    final static SqlCode disconnectError = new SqlCode(40000);

    /** SQL code for SQL state 02000 (end of data). DRDA does not
     * specify the SQL code for this SQL state, but Derby uses 100. */
    public final static SqlCode END_OF_DATA = new SqlCode(100);
}