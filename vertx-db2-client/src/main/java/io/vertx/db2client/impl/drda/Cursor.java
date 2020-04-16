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
package io.vertx.db2client.impl.drda;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class Cursor {

    //-----------------------------varchar representations------------------------

    public final static int STRING = 0;
    public final static int VARIABLE_STRING = 2;       // uses a 2-byte length indicator
    public final static int VARIABLE_SHORT_STRING = 1; // aka Pascal L; uses a 1-byte length indicator
    public final static int NULL_TERMINATED_STRING = 3;

    public final static int BYTES = 4;
    // unused protocol element: VARIABLE_BYTES = 5;
    // unused protocol element: VARIABLE_SHORT_BYTES = 6;
    public final static int NULL_TERMINATED_BYTES = 7;

    // Charsets
    static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    static final Charset UTF_8 = Charset.forName("UTF-8");
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    
    private static final DateTimeFormatter db2TimeFormat = DateTimeFormatter.ofPattern("HH.mm.ss");

    // unused protocol element: SBCS_CLOB = 8;
    // unused protocol element: MBCS_CLOB = 9;
    // unused protocol element: DBCS_CLOB = 10;
    //-----------------------------internal state---------------------------------

    //-------------Structures for holding and scrolling the data -----------------
    // TODO: Encapsulate this field
    public ByteBuf dataBuffer_;
//    public byte[] dataBuffer_;
//    public ByteArrayOutputStream dataBufferStream_ = new ByteArrayOutputStream();
//    public int position_; // This is the read head
    public int lastValidBytePosition_;
    public boolean hasLobs_; // is there at least one LOB column?

    // Current row positioning
    protected int currentRowPosition_;
    private int nextRowPosition_;
    // Let's new up a 2-dimensional array based on fetch-size and reuse so that
    protected int[] columnDataPosition_;

    // This is the actual, computed lengths of varchar fields, not the max length from query descriptor or DA
    protected int[] columnDataComputedLength_;
    // populate this for

    // All the data is in the buffers, but user may not have necessarily stepped to the last row yet.
    // This flag indicates that the server has returned all the rows, and is positioned
    // after last, for both scrollable and forward-only cursors.
    // For singleton cursors, this memeber will be set to true as soon as next is called.
    private boolean allRowsReceivedFromServer_;

    // Total number of rows read so far.
    // This should never exceed this.statement.maxRows
    long rowsRead_;

    // Maximum column size limit in bytes.
    int maxFieldSize_ = 0;

    // Row positioning for all cached rows
    // For scrollable result sets, these lists hold the offsets into the cached rowset buffer for each row of data.
    protected ArrayList<int[]> columnDataPositionCache_ = new ArrayList<int[]>();
    protected ArrayList<int[]> columnDataLengthCache_ = new ArrayList<int[]>();
    protected ArrayList<boolean[]> columnDataIsNullCache_ = new ArrayList<boolean[]>();
    ArrayList<Boolean> isUpdateDeleteHoleCache_ = new ArrayList<Boolean>();
    boolean isUpdateDeleteHole_;

    // State to keep track of when a row has been updated,
    // cf. corresponding set and get accessors.  Only implemented for
    // scrollable updatable insensitive result sets for now.
    private boolean isRowUpdated_;

    final static Boolean ROW_IS_NULL = Boolean.TRUE;
    private final static Boolean ROW_IS_NOT_NULL = Boolean.FALSE;

    // For the net, this data comes from the query descriptor.

    public int[] jdbcTypes_;
    public int columns_;
    public boolean[] nullable_;
    public Charset[] charset_;
    public boolean[] isNull_;
    public int[] fdocaLength_; // this is the max length for

    //----------------------------------------------------------------------------

    public int[] ccsid_;
    private char[] charBuffer_;
    
//    NetResultSet netResultSet_;
//    private NetAgent netAgent_;

    Typdef qrydscTypdef_;

    int maximumRowSize_ = 0;
    boolean blocking_;  // if true, multiple rows may be "blocked" in a single reply

    // Raw fdoca column meta data.
    int[] typeToUseForComputingDataLength_;
    boolean[] isGraphic_;

    // key = column position, value = index into extdtaData_
    HashMap<Integer, Integer> extdtaPositions_ = new HashMap<Integer, Integer>();

    /**
     * Queue to hold EXTDTA data that hasn't been correlated to its
     * column number.
     */
    ArrayList<byte[]> extdtaData_ = new ArrayList<byte[]>();


    boolean rtnextrow_ = true;

    /** Flag indicating whether the result set on the server is
     * implicitly closed when end-of-data is received. */
    private boolean qryclsimpEnabled_;
    
    private final ConnectionMetaData metadata;

    //-----------------------------constants--------------------------------------

    //---------------------constructors/finalizer---------------------------------

    Cursor(ConnectionMetaData metadata) {
      this.metadata = metadata;
    }

    //---------------------constructors/finalizer---------------------------------

    public void setNumberOfColumns(int numberOfColumns) {
        columnDataPosition_ = new int[numberOfColumns];
        columnDataComputedLength_ = new int[numberOfColumns];

        columns_ = numberOfColumns;
        nullable_ = new boolean[numberOfColumns];
        charset_ = new Charset[numberOfColumns];

        ccsid_ = new int[numberOfColumns];

        isNull_ = new boolean[numberOfColumns];
        jdbcTypes_ = new int[numberOfColumns];
    }

    /**
     * Makes the next row the current row. Returns true if the current
     * row position is a valid row position.
     *
     * @param allowServerFetch if false, don't fetch more data from
     * the server even if more data is needed
     * @return {@code true} if current row position is valid
     * @exception SQLException if an error occurs
     */
    protected boolean stepNext(boolean allowServerFetch) {
        // reset lob data
        // clears out Cursor.lobs_ calculated for the current row when cursor is moved.
        clearLobData_();

        // mark the start of a new row.
        makeNextRowPositionCurrent();
        
        // Moving out of the hole, set isUpdateDeleteHole to false
        isUpdateDeleteHole_ = false;

        isRowUpdated_ = false;

        // Drive the CNTQRY outside of calculateColumnOffsetsForRow() if the dataBuffer_
        // contains no data since it has no abilities to handle replies other than
        // the QRYDTA, i.e. ENDQRYRM when the result set contains no more rows.
        while (!dataBufferHasUnprocessedData()) {
            if (allRowsReceivedFromServer_) {
                return false;
            }
            return false;
            // @AGG not implementing this code path, let other objects flow a fetch
            // getMoreData_();
        }

        // The parameter passed in here is used as an index into the cached rowset for
        // scrollable cursors, for the arrays to be reused.  It is not used for forward-only
        // cursors, so just pass in 0.
        boolean rowPositionIsValid = calculateColumnOffsetsForRow_(0, allowServerFetch);
        markNextRowPosition();
        return rowPositionIsValid;
    }

    /**
     * Makes the next row the current row. Returns true if the current
     * row position is a valid row position.
     *
     * @return {@code true} if current row position is valid
     */
    public boolean next() {
        return stepNext(true);
    }

    //--------------------------reseting cursor state-----------------------------

    /**
     * Return {@code true} if all rows are received from the
     * server.
     *
     * @return {@code true} if all rows are received from the
     * server.
     */
    public final boolean allRowsReceivedFromServer() {
        return allRowsReceivedFromServer_;
    }

    final boolean currentRowPositionIsEqualToNextRowPosition() {
        return (currentRowPosition_ == nextRowPosition_);
    }

    // reset the beginning and ending position in the data buffer to 0
    // reset the currentRowPosition and nextRowPosition to 0
    // reset lastRowReached and sqlcode100Received to false
    // clear the column data offsets cache
    public final void resetDataBuffer() {
        dataBuffer_.resetReaderIndex();
        lastValidBytePosition_ = 0;
        currentRowPosition_ = 0;
        nextRowPosition_ = 0;
        setAllRowsReceivedFromServer(false);
    }

    final boolean dataBufferHasUnprocessedData() {
        return dataBuffer_ != null && (lastValidBytePosition_ - dataBuffer_.readerIndex()) > 0;
    }

    public final void setIsUpdataDeleteHole(int row, boolean isRowNull) {
        isUpdateDeleteHole_ = isRowNull;
        Boolean nullIndicator = (isUpdateDeleteHole_ == true) ? ROW_IS_NULL : ROW_IS_NOT_NULL;
        if (isUpdateDeleteHoleCache_.size() == row) {
            isUpdateDeleteHoleCache_.add(nullIndicator);
        } else {
            isUpdateDeleteHoleCache_.set(row, nullIndicator);
        }
    }

    /**
     * Keep track of updated status for this row.
     *
     * @param isRowUpdated true if row has been updated
     *
     * @see Cursor#getIsRowUpdated
     */
    public final void setIsRowUpdated(boolean isRowUpdated) {
        isRowUpdated_ = isRowUpdated;
    }

    /**
     * Get updated status for this row. 
     * Minion of ResultSet#rowUpdated.
     *
     * @see Cursor#setIsRowUpdated
     */
    public final boolean getIsRowUpdated() {
        return isRowUpdated_;
    }

    /**
     * Get deleted status for this row. 
     * Minion of ResultSet#rowDeleted.
     *
     * @see Cursor#setIsUpdataDeleteHole
     */
    public final boolean getIsUpdateDeleteHole() {
        return isUpdateDeleteHole_;
    }
    
    //---------------------------cursor positioning-------------------------------

    protected final void markNextRowPosition() {
        nextRowPosition_ = dataBuffer_.readerIndex();
    }

    protected final void makeNextRowPositionCurrent() {
        currentRowPosition_ = nextRowPosition_;
    }

    // This tracks the total number of rows read into the client side buffer for
    // this result set, irregardless of scrolling.
    // Per jdbc semantics, this should never exceed statement.maxRows.
    // This event should be generated in the materialized cursor's implementation
    // of calculateColumnOffsetsForRow().
    public final void incrementRowsReadEvent() {
        rowsRead_++;
    }

    //------- the following getters are called on known column types -------------
    // Direct conversions only, cross conversions are handled by another set of getters.

    // Build a Java boolean from a 1-byte signed binary representation.
    private boolean get_BOOLEAN(int column) {
    	// @AGG force Little Endian
    	// @AGG In DB2 BOOLEAN columns appear to be encoded as two bytes
      if (metadata.isZos())
        return dataBuffer_.getShort(columnDataPosition_[column - 1]) != 0;
      else
    	return dataBuffer_.getShortLE(columnDataPosition_[column - 1]) != 0;
//        if ( SignedBinary.getByte( dataBuffer_, columnDataPosition_[column - 1] ) == 0 )
//        { return false; }
//        else { return true; }
    }

    // Build a Java short from a 2-byte signed binary representation.
    private final short get_SMALLINT(int column) {
        // @AGG force Little Endian
      if (metadata.isZos())
        return dataBuffer_.getShort(columnDataPosition_[column - 1]);
      else
        return dataBuffer_.getShortLE(columnDataPosition_[column - 1]);
//        return SignedBinary.getShort(dataBuffer_,
//                columnDataPosition_[column - 1]);
    }

    // Build a Java int from a 4-byte signed binary representation.
    protected final int get_INTEGER(int column) {
        // @AGG had to get integer as Little Endian
      if (metadata.isZos())
        return dataBuffer_.getInt(columnDataPosition_[column - 1]);
      else
        return dataBuffer_.getIntLE(columnDataPosition_[column - 1]);
//        return SignedBinary.getInt(dataBuffer_,
//                columnDataPosition_[column - 1]);
    }

    // Build a Java long from an 8-byte signed binary representation.
    private final long get_BIGINT(int column) {
        // @AGG force Little Endian
      if (metadata.isZos())
        return dataBuffer_.getLong(columnDataPosition_[column - 1]);
      else
        return dataBuffer_.getLongLE(columnDataPosition_[column - 1]);
//        return SignedBinary.getLong(dataBuffer_,
//                columnDataPosition_[column - 1]);
    }

    // Build a Java float from a 4-byte floating point representation.
    private final float get_FLOAT(int column) {
        // @AGG force Little Endian
      if (metadata.isZos()) {
        byte[] bytes = new byte[4];
        dataBuffer_.getBytes(columnDataPosition_[column - 1], bytes);
        return FloatingPoint.getFloat_hex(bytes, 0);
//        return dataBuffer_.getFloat(columnDataPosition_[column - 1]);
      } else {
        return dataBuffer_.getFloatLE(columnDataPosition_[column - 1]);
//        return FloatingPoint.getFloat(dataBuffer_,
//                columnDataPosition_[column - 1]);
      }
    }

    // Build a Java double from an 8-byte floating point representation.
    private final double get_DOUBLE(int column) {
      if (metadata.isZos()) {
        byte[] bytes = new byte[8];
        dataBuffer_.getBytes(columnDataPosition_[column - 1], bytes);
        return FloatingPoint.getDouble_hex(bytes, 0);
        //return dataBuffer_.getDouble(columnDataPosition_[column - 1]);
      } else {
        return dataBuffer_.getDoubleLE(columnDataPosition_[column - 1]);
//        return FloatingPoint.getDouble(dataBuffer_,
//                columnDataPosition_[column - 1]);
      }
    }
    
    // Build a java.math.BigDecimal from a fixed point decimal byte representation.
    private final BigDecimal get_DECIMAL(int column) {
        return Decimal.getBigDecimal(dataBuffer_,
                columnDataPosition_[column - 1],
                getColumnPrecision(column - 1),
                getColumnScale(column - 1));
    }


    // Build a Java double from a fixed point decimal byte representation.
    private double getDoubleFromDECIMAL(int column) {
        return Decimal.getDouble(dataBuffer_,
                columnDataPosition_[column - 1],
                getColumnPrecision(column - 1),
                getColumnScale(column - 1));
    }

    // Build a Java long from a fixed point decimal byte representation.
    private long getLongFromDECIMAL(int column, String targetType) {
        try {
            return Decimal.getLong(dataBuffer_,
                    columnDataPosition_[column - 1],
                    getColumnPrecision(column - 1),
                    getColumnScale(column - 1));
        } catch (ArithmeticException | IllegalArgumentException e) {
            throw new IllegalArgumentException("SQLState.LANG_OUTSIDE_RANGE_FOR_DATATYPE " + targetType, e);
        }
    }
    
    // Build a Java String from a database VARCHAR or LONGVARCHAR field.
    //
    // Depending on the ccsid, length is the number of chars or number of bytes.
    // For 2-byte character ccsids, length is the number of characters,
    // for all other cases length is the number of bytes.
    // The length does not include the null terminator.
    private String get_VARCHAR(int column) {
        if (ccsid_[column - 1] == 1200) {
            return getStringWithoutConvert(columnDataPosition_[column - 1] + 2,
                    columnDataComputedLength_[column - 1] - 2);
        }

        // check for null encoding is needed because the net layer
        // will no longer throw an exception if the server didn't specify
        // a mixed or double byte ccsid (ccsid = 0).  this check for null in the
        // cursor is only required for types which can have mixed or double
        // byte ccsids.
        if (charset_[column - 1] == null) {
            throw new IllegalStateException("SQLState.CHARACTER_CONVERTER_NOT_AVAILABLE");
        }

        int dataLength = columnDataComputedLength_[column - 1] - 2;
        if (maxFieldSize_ != 0 && maxFieldSize_ < dataLength)
        	dataLength = maxFieldSize_;
        return dataBuffer_.getCharSequence(columnDataPosition_[column - 1] + 2, 
        		dataLength, charset_[column - 1]).toString();
//        String tempString = new String(dataBuffer_,
//                columnDataPosition_[column - 1] + 2,
//                columnDataComputedLength_[column - 1] - 2,
//                charset_[column - 1]);
//        return (maxFieldSize_ == 0) ? tempString :
//                tempString.substring(0, Math.min(maxFieldSize_, tempString.length()));
    }

    // Build a Java String from a database CHAR field.
    private String get_CHAR(int column) {
        if (ccsid_[column - 1] == 1200) {
            return getStringWithoutConvert(columnDataPosition_[column - 1], columnDataComputedLength_[column - 1]);
        }

        // check for null encoding is needed because the net layer
        // will no longer throw an exception if the server didn't specify
        // a mixed or double byte ccsid (ccsid = 0).  this check for null in the
        // cursor is only required for types which can have mixed or double
        // byte ccsids.
        if (charset_[column - 1] == null) {
            throw new IllegalStateException("SQLState.CHARACTER_CONVERTER_NOT_AVAILABLE");
        }

        int dataLength = columnDataComputedLength_[column - 1];
        if (maxFieldSize_ != 0 && maxFieldSize_ < dataLength)
        	dataLength = maxFieldSize_;
        return dataBuffer_.getCharSequence(columnDataPosition_[column - 1], 
        		dataLength, charset_[column - 1]).toString();
//        String tempString = new String(dataBuffer_,
//                columnDataPosition_[column - 1],
//                columnDataComputedLength_[column - 1],
//                charset_[column - 1]);
//        return (maxFieldSize_ == 0) ? tempString :
//                tempString.substring(0, Math.min(maxFieldSize_,
//                                                 tempString.length()));
    }

    // Build a JDBC Date object from the ISO DATE field.
    private LocalDate get_DATE(int column) {
        // DATE column is always 10 chars long
        String dateString = dataBuffer_.getCharSequence(columnDataPosition_[column - 1], 
        		10, charset_[column - 1]).toString();
        return LocalDate.parse(dateString);
//        return DateTime.dateBytesToDate(dataBuffer_,
//            columnDataPosition_[column - 1],
//            cal,
//            charset_[column - 1]);
    }

    // Build a JDBC Time object from the ISO TIME field.
    private LocalTime get_TIME(int column) {
        // Time column is always 8 chars long
        String timeString = dataBuffer_.getCharSequence(columnDataPosition_[column - 1], 
        		8, charset_[column - 1]).toString();
        return LocalTime.parse(timeString, db2TimeFormat);
//        return DateTime.timeBytesToTime(dataBuffer_,
//                columnDataPosition_[column - 1],
//                cal,
//                charset_[column - 1]);
    }

//    // Build a JDBC Timestamp object from the ISO TIMESTAMP field.
//    private final Timestamp getTIMESTAMP(int column, Calendar cal)
//            throws SQLException {
//        return DateTime.timestampBytesToTimestamp(
//            dataBuffer_,
//            columnDataPosition_[column - 1],
//            cal,
//            charset_[column - 1],
//            agent_.connection_.serverSupportsTimestampNanoseconds());
//    }
//
//    // Build a JDBC Timestamp object from the ISO DATE field.
//    private final Timestamp getTimestampFromDATE(
//            int column, Calendar cal) throws SQLException {
//        return DateTime.dateBytesToTimestamp(dataBuffer_,
//                columnDataPosition_[column - 1],
//                cal,
//                charset_[column -1]);
//    }
//
//    // Build a JDBC Timestamp object from the ISO TIME field.
//    private final Timestamp getTimestampFromTIME(
//            int column, Calendar cal) throws SQLException {
//        return DateTime.timeBytesToTimestamp(dataBuffer_,
//                columnDataPosition_[column - 1],
//                cal,
//                charset_[column -1]);
//    }
//
//    // Build a JDBC Date object from the ISO TIMESTAMP field.
//    private final Date getDateFromTIMESTAMP(int column, Calendar cal)
//            throws SQLException {
//        return DateTime.timestampBytesToDate(dataBuffer_,
//                columnDataPosition_[column - 1],
//                cal,
//                charset_[column -1]);
//    }
//
//    // Build a JDBC Time object from the ISO TIMESTAMP field.
//    private final Time getTimeFromTIMESTAMP(int column, Calendar cal)
//            throws SQLException {
//        return DateTime.timestampBytesToTime(dataBuffer_,
//                columnDataPosition_[column - 1],
//                cal,
//                charset_[column -1]);
//    }
//
//    private String getStringFromDATE(int column) throws SQLException {
//        return getDATE(column, getRecyclableCalendar()).toString();
//    }
//
//    // Build a string object from the byte TIME representation.
//    private String getStringFromTIME(int column) throws SQLException {
//        return getTIME(column, getRecyclableCalendar()).toString();
//    }
//
//    // Build a string object from the byte TIMESTAMP representation.
//    private String getStringFromTIMESTAMP(int column) throws SQLException {
//        return getTIMESTAMP(column, getRecyclableCalendar()).toString();
//    }

    // Extract bytes from a database Types.BINARY field.
    // This is the type CHAR(n) FOR BIT DATA.
    private byte[] get_CHAR_FOR_BIT_DATA(int column) {
        // There is no limit to the size of a column if maxFieldSize is zero.
        // Otherwise, use the smaller of maxFieldSize and the actual column length.
        int columnLength = (maxFieldSize_ == 0) ? columnDataComputedLength_[column - 1] :
                Math.min(maxFieldSize_, columnDataComputedLength_[column - 1]);

        byte[] bytes = new byte[columnLength];
        //System.arraycopy(dataBuffer_, columnDataPosition_[column - 1], bytes, 0, columnLength);
        dataBuffer_.getBytes(columnDataPosition_[column - 1], bytes);
        return bytes;
    }

    // Extract bytes from a database Types.VARBINARY or LONGVARBINARY field.
    // This includes the types:
    //   VARCHAR(n) FOR BIT DATA
    //   LONG VARCHAR(n) FOR BIT DATA
    private byte[] get_VARCHAR_FOR_BIT_DATA(int column) {
        byte[] bytes;
        int columnLength =
            (maxFieldSize_ == 0) ? columnDataComputedLength_[column - 1] - 2 :
            Math.min(maxFieldSize_, columnDataComputedLength_[column - 1] - 2);
        bytes = new byte[columnLength];
//        System.arraycopy(dataBuffer_, columnDataPosition_[column - 1] + 2, bytes, 0, bytes.length);
        dataBuffer_.getBytes(columnDataPosition_[column - 1] + 2, bytes);
        return bytes;
    }

    // Deserialize a UDT from a database Types.JAVA_OBJECT field.
    // This is used for user defined types.
    private Object get_UDT(int column) {
        byte[] bytes;
        int columnLength =
            (maxFieldSize_ == 0) ? columnDataComputedLength_[column - 1] - 2 :
            Math.min(maxFieldSize_, columnDataComputedLength_[column - 1] - 2);
        bytes = new byte[columnLength];
        //System.arraycopy(dataBuffer_, columnDataPosition_[column - 1] + 2, bytes, 0, bytes.length);
        dataBuffer_.getBytes(columnDataPosition_[column - 1] + 2, bytes);

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            ObjectInputStream ois = new ObjectInputStream( bais );
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("SQLState.NET_MARSHALLING_UDT_ERROR", e);
        }
    }
    
    private DB2RowId get_ROWID(int column) {
      int columnLength = maxFieldSize_ == 0
          ? columnDataComputedLength_[column - 1] - 2
          : Math.min(maxFieldSize_, columnDataComputedLength_[column - 1] - 2);
      byte[] bytes = new byte[columnLength];
      dataBuffer_.getBytes(columnDataPosition_[column - 1] + 2, bytes);
      return new DB2RowId(bytes);
    }

//    /**
//     * Instantiate an instance of Calendar that can be re-used for getting
//     * Time, Timestamp, and Date values from this cursor.  Assumption is
//     * that all users of the returned Calendar object will clear it as
//     * appropriate before using it.
//     */
//    private Calendar getRecyclableCalendar()
//    {
//        if (recyclableCalendar_ == null)
//            recyclableCalendar_ = new GregorianCalendar();
//
//        return recyclableCalendar_;
//    }

//    /**
//     * Returns a reference to the locator procedures.
//     * <p>
//     * These procedures are used to operate on large objects referenced on the
//     * server by locators.
//     *
//     * @return The locator procedures object.
//     */
//    CallableLocatorProcedures getLocatorProcedures() {
//        return agent_.connection_.locatorProcedureCall();
//    }

    //------- the following getters perform any necessary cross-conversion _------

    final boolean getBoolean(int column) {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return get_BOOLEAN(column);
        case Types.SMALLINT:
            return get_SMALLINT(column) != 0;
        case Types.INTEGER:
            return get_INTEGER(column) != 0;
        case Types.BIGINT:
            return get_BIGINT(column) != 0;
        case Types.REAL:
            return get_FLOAT(column) != 0;
        case Types.DOUBLE:
            return get_DOUBLE(column) != 0;
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return getLongFromDECIMAL(column, "boolean") != 0;
        case Types.CHAR:
            String trimmedChar = get_CHAR(column);
            return !(trimmedChar.equals("0") || trimmedChar.equals("false"));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            String trimmed = get_VARCHAR(column);
            return !(trimmed.equals("0") || trimmed.equals("false"));
        default:
            throw coercionError( "boolean", column );
        }
    }

    final byte getByte(int column) {
        // This needs to be changed to use jdbcTypes[]
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getByteFromBoolean(get_BOOLEAN(column));
        case Types.SMALLINT:
            int smallInt = get_SMALLINT(column);
            if (smallInt > Byte.MAX_VALUE || smallInt < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + smallInt);
            return (byte) smallInt;
        case Types.INTEGER:
            int i = get_INTEGER(column);
            if (i > Byte.MAX_VALUE || i < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + i);
            return (byte) i;
        case Types.BIGINT:
            long l = get_BIGINT(column);
            if (l > Byte.MAX_VALUE || l < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + l);
            return (byte) l;
        case Types.REAL:
            float f = get_FLOAT(column);
            if (f > Byte.MAX_VALUE || f < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + f);
            return (byte) f;
        case Types.DOUBLE:
            double d = get_DOUBLE(column);
            if (d > Byte.MAX_VALUE || d < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + d);
            return (byte) d;
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            long ld = getLongFromDECIMAL(column, "byte");
            if (ld > Byte.MAX_VALUE || ld < Byte.MIN_VALUE)
                throw new IllegalArgumentException("Value outside of byte range: " + ld);
            return (byte) ld;
        case Types.CHAR:
            return CrossConverters.getByteFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getByteFromString(get_VARCHAR(column));
        default:
            throw coercionError( "byte", column );
        }
    }

    public final short getShort(int column) throws SQLException {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getShortFromBoolean(get_BOOLEAN(column));
        case Types.SMALLINT:
            return get_SMALLINT(column);
        case Types.INTEGER:
            return CrossConverters.getShortFromInt(get_INTEGER(column));
        case Types.BIGINT:
            return CrossConverters.getShortFromLong(get_BIGINT(column));
        case Types.REAL:
            return CrossConverters.getShortFromFloat(get_FLOAT(column));
        case Types.DOUBLE:
            return CrossConverters.getShortFromDouble(get_DOUBLE(column));
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return CrossConverters.getShortFromLong(
                getLongFromDECIMAL(column, "short"));
        case Types.CHAR:
            return CrossConverters.getShortFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getShortFromString(get_VARCHAR(column));
        default:
            throw coercionError( "short", column );
        }
    }

    public final int getInt(int column) throws SQLException {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getIntFromBoolean(get_BOOLEAN(column));
        case Types.SMALLINT:
            return (int) get_SMALLINT(column);
        case Types.INTEGER:
            return get_INTEGER(column);
        case Types.BIGINT:
            return CrossConverters.getIntFromLong(get_BIGINT(column));
        case Types.REAL:
            return CrossConverters.getIntFromFloat(get_FLOAT(column));
        case Types.DOUBLE:
            return CrossConverters.getIntFromDouble(get_DOUBLE(column));
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return CrossConverters.getIntFromLong(
                getLongFromDECIMAL(column, "int"));
        case Types.CHAR:
            return CrossConverters.getIntFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getIntFromString(get_VARCHAR(column));
        default:
            throw coercionError(  "int", column );
        }
    }

    public final long getLong(int column) throws SQLException {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getLongFromBoolean(get_BOOLEAN(column));
        case Types.SMALLINT:
            return (long) get_SMALLINT(column);
        case Types.INTEGER:
            return (long) get_INTEGER(column);
        case Types.BIGINT:
            return get_BIGINT(column);
        case Types.REAL:
            return CrossConverters.getLongFromFloat(get_FLOAT(column));
        case Types.DOUBLE:
            return CrossConverters.getLongFromDouble(get_DOUBLE(column));
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return getLongFromDECIMAL(column, "long");
        case Types.CHAR:
            return CrossConverters.getLongFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getLongFromString(get_VARCHAR(column));
        default:
            throw coercionError( "long", column );
        }
    }

    public final float getFloat(int column) throws SQLException {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getFloatFromBoolean(get_BOOLEAN(column));
        case Types.REAL:
            return get_FLOAT(column);
        case Types.DOUBLE:
            return CrossConverters.getFloatFromDouble(get_DOUBLE(column));
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return CrossConverters.getFloatFromDouble(getDoubleFromDECIMAL(column));
        case Types.SMALLINT:
            return (float) get_SMALLINT(column);
        case Types.INTEGER:
            return (float) get_INTEGER(column);
        case Types.BIGINT:
            return (float) get_BIGINT(column);
        case Types.CHAR:
            return CrossConverters.getFloatFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getFloatFromString(get_VARCHAR(column));
        default:
            throw coercionError( "float", column );
        }
    }

    public final double getDouble(int column) throws SQLException {
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return CrossConverters.getDoubleFromBoolean(get_BOOLEAN(column));
        case Types.REAL:
            double d = (double) get_FLOAT(column);
            return d;
            //return (double) get_FLOAT (column);
        case Types.DOUBLE:
            return get_DOUBLE(column);
        case Types.DECIMAL:
            // For performance we don't materialize the BigDecimal, but convert directly from decimal bytes to a long.
            return getDoubleFromDECIMAL(column);
        case Types.SMALLINT:
            return (double) get_SMALLINT(column);
        case Types.INTEGER:
            return (double) get_INTEGER(column);
        case Types.BIGINT:
            return (double) get_BIGINT(column);
        case Types.CHAR:
            return CrossConverters.getDoubleFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getDoubleFromString(get_VARCHAR(column));
        default:
            throw coercionError( "double", column );
        }
    }

    public final BigDecimal getBigDecimal(int column) throws SQLException {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return BigDecimal.valueOf(getLong(column));
        case Types.DECIMAL:
            return get_DECIMAL(column);
        case Types.REAL:
            // Can't use the following commented out line because it changes precision of the result.
            //return new java.math.BigDecimal (get_FLOAT (column));
            float f = get_FLOAT(column);
            return new BigDecimal(String.valueOf(f));
        case Types.DOUBLE:
            return BigDecimal.valueOf(get_DOUBLE(column));
        case Types.SMALLINT:
            return BigDecimal.valueOf(get_SMALLINT(column));
        case Types.INTEGER:
            return BigDecimal.valueOf(get_INTEGER(column));
        case Types.BIGINT:
            return BigDecimal.valueOf(get_BIGINT(column));
        case Types.CHAR:
            return CrossConverters.getBigDecimalFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getBigDecimalFromString(get_VARCHAR(column));
        default:
            throw coercionError( "java.math.BigDecimal", column );
        }
    }

    public final LocalDate getDate(int column) {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.DATE:
            return get_DATE(column);
        case Types.TIMESTAMP:
//            return getDateFromTIMESTAMP(column, cal);
            throw new UnsupportedOperationException();
        case Types.CHAR:
            return CrossConverters.getDateFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getDateFromString(get_VARCHAR(column));
        default:
            throw coercionError( "java.time.LocalDate", column );
        }
    }

    public final LocalTime getTime(int column) {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.TIME:
            return get_TIME(column);
        case Types.TIMESTAMP:
//            return getTimeFromTIMESTAMP(column, cal);
            throw new UnsupportedOperationException();
        case Types.CHAR:
            return CrossConverters.getTimeFromString(get_CHAR(column));
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getTimeFromString(get_VARCHAR(column));
        default:
            throw coercionError( "java.sql.Time", column );
        }
    }

    public final Timestamp getTimestamp(int column, Calendar cal) {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.TIMESTAMP:
//            return getTIMESTAMP(column, cal);
        case Types.DATE:
//            return getTimestampFromDATE(column, cal);
        case Types.TIME:
//            return getTimestampFromTIME(column, cal);
            throw new UnsupportedOperationException();
        case Types.CHAR:
            return CrossConverters.getTimestampFromString(get_CHAR(column), cal);
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return CrossConverters.getTimestampFromString(get_VARCHAR(column), cal);
        default:
            throw coercionError( "java.sql.Timestamp", column );
        }
    }

    public final String getString(int column) {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            if (get_BOOLEAN(column)) {
                return Boolean.TRUE.toString();
            } else {
                return Boolean.FALSE.toString();
            }
        case Types.CHAR:
            return get_CHAR(column);
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return get_VARCHAR(column);

        case Types.SMALLINT:
            return String.valueOf(get_SMALLINT(column));
        case Types.INTEGER:
            return String.valueOf(get_INTEGER(column));
        case Types.BIGINT:
            return String.valueOf(get_BIGINT(column));
        case Types.REAL:
            return String.valueOf(get_FLOAT(column));
        case Types.DOUBLE:
            return String.valueOf(get_DOUBLE(column));
        case Types.DECIMAL:
//                // We could get better performance here if we didn't materialize the BigDecimal,
//                // but converted directly from decimal bytes to a string.
//                return String.valueOf(get_DECIMAL(column));
        case Types.DATE:
//                return getStringFromDATE(column);
        case Types.TIME:
//                return getStringFromTIME(column);
        case Types.TIMESTAMP:
//                return getStringFromTIMESTAMP(column);
        case ClientTypes.BINARY:
//                tempString = CrossConverters.getStringFromBytes(get_CHAR_FOR_BIT_DATA(column));
//                return (maxFieldSize_ == 0) ? tempString
//                        : tempString.substring(0, Math.min(maxFieldSize_, tempString.length()));
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
//                tempString = CrossConverters.getStringFromBytes(get_VARCHAR_FOR_BIT_DATA(column));
//                return (maxFieldSize_ == 0) ? tempString
//                        : tempString.substring(0, Math.min(maxFieldSize_, tempString.length()));
        case Types.JAVA_OBJECT:
//                Object obj = get_UDT(column);
//                if (obj == null) {
//                    return null;
//                } else {
//                    return obj.toString();
//                }
        case Types.BLOB:
//                ClientBlob b = getBlobColumn_(column, agent_, false);
//                tempString = CrossConverters.getStringFromBytes(b.getBytes(1, (int) b.length()));
//                return tempString;
        case Types.CLOB:
//                ClientClob c = getClobColumn_(column, agent_, false);
//                tempString = c.getSubString(1, (int) c.length());
//                return tempString;
            throw new UnsupportedOperationException();
        default:
            throw coercionError("String", column);
        }
    }

    public final byte[] getBytes(int column) {
            switch (jdbcTypes_[column - 1]) {
            case Types.BINARY:
                return get_CHAR_FOR_BIT_DATA(column);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return get_VARCHAR_FOR_BIT_DATA(column);
            case Types.BLOB:
                throw new UnsupportedOperationException();
//                ClientBlob b = getBlobColumn_(column, agent_, false);
//                byte[] bytes = b.getBytes(1, (int) b.length());
//                return bytes;
            default:
                throw coercionError( "byte[]", column );
            }
    }
    
    public final DB2RowId getRowID(int column) {
      switch (jdbcTypes_[column - 1]) {
      case Types.ROWID:
        return get_ROWID(column);
      default:
        throw coercionError("RowId", column);
      }
    }

    final InputStream getBinaryStream(int column)
    {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
            case Types.BINARY:
                return new ByteArrayInputStream(get_CHAR_FOR_BIT_DATA(column));
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return
                    new ByteArrayInputStream(get_VARCHAR_FOR_BIT_DATA(column));
            case Types.BLOB:
                throw new UnsupportedOperationException();
//                ClientBlob b = getBlobColumn_(column, agent_, false);
//                if (b.isLocator()) {
//                    BlobLocatorInputStream is 
//                            = new BlobLocatorInputStream(agent_.connection_, b);
//                    return new BufferedInputStream(is);
//                } else {
//                    return b.getBinaryStreamX();
//                }
            default:
                throw coercionError( "java.io.InputStream", column );
        }
    }

    final InputStream getAsciiStream(int column)
    {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
            case Types.CLOB:
                throw new UnsupportedOperationException();
//                ClientClob c = getClobColumn_(column, agent_, false);
//                if (c.isLocator()) {
//                    ClobLocatorInputStream is 
//                            = new ClobLocatorInputStream(agent_.connection_, c);
//                    return new BufferedInputStream(is);
//                } else {
//                    return c.getAsciiStreamX();
//                }
            case Types.CHAR:
                return new ByteArrayInputStream(
                        get_CHAR(column).getBytes(ISO_8859_1));
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return new ByteArrayInputStream(
                        get_VARCHAR(column).getBytes(ISO_8859_1));
            case Types.BINARY:
                return new ByteArrayInputStream(get_CHAR_FOR_BIT_DATA(column));
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return
                    new ByteArrayInputStream(get_VARCHAR_FOR_BIT_DATA(column));
            case Types.BLOB:
                return getBinaryStream(column);
            default:
                throw coercionError( "java.io.InputStream", column );
        }
    }
 
    final Reader getCharacterStream(int column)
            throws SQLException 
    {
        switch (jdbcTypes_[column - 1]) {
            case Types.CLOB:
                throw new UnsupportedOperationException();
//                ClientClob c = getClobColumn_(column, agent_, false);
//                if (c.isLocator()) {
//                    ClobLocatorReader reader
//                            = new ClobLocatorReader(agent_.connection_, c);
//                    return new BufferedReader(reader);
//                } else {
//                    return c.getCharacterStreamX();
//                }
            case Types.CHAR:
                return new StringReader(get_CHAR(column));
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return new StringReader(get_VARCHAR(column));
            case Types.BINARY:
                return new InputStreamReader(
                    new ByteArrayInputStream(
                        get_CHAR_FOR_BIT_DATA(column)), UTF_16BE);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return new InputStreamReader(
                    new ByteArrayInputStream(
                        get_VARCHAR_FOR_BIT_DATA(column)), UTF_16BE);
            case Types.BLOB:
                return new InputStreamReader(getBinaryStream(column), UTF_16BE);
            default:
                throw coercionError( "java.io.Reader", column );
            }
    }

    public final Blob getBlob(int column) {
    	if (isNull(column))
    		return null;
        throw new UnsupportedOperationException();
//        switch (jdbcTypes_[column - 1]) {
//        case ClientTypes.BLOB:
//            return getBlobColumn_(column, agent_, true);
//        default:
//            throw coercionError( "java.sql.Blob", column );
//        }
    }

    public final Clob getClob(int column) {
    	if (isNull(column))
    		return null;
        throw new UnsupportedOperationException();
//        switch (jdbcTypes_[column - 1]) {
//        case ClientTypes.CLOB:
//            return getClobColumn_(column, agent_, true);
//        default:
//            throw coercionError( "java.sql.Clob", column );
//        }
    }

//    final Array getArray(int column) throws SQLException {
//        throw new SQLException(agent_.logWriter_, 
//            new ClientMessageId (SQLState.NOT_IMPLEMENTED),
//            "getArray(int)");
//    }
//
//    final Ref getRef(int column) throws SQLException {
//        throw new SQLException(agent_.logWriter_, 
//            new ClientMessageId (SQLState.NOT_IMPLEMENTED), "getRef(int)");
//    }
    
    private boolean isNull(int column) {
    	return nullable_[column - 1] && isNull_[column - 1];
    }

    public final Object getObject(int column) {
    	if (isNull(column))
    		return null;
        switch (jdbcTypes_[column - 1]) {
        case Types.BOOLEAN:
            return get_BOOLEAN(column);
        case Types.SMALLINT:
            // See Table 4 in JDBC 1 spec (pg. 932 in jdbc book)
            // @AGG since this is not JDBC, just return as a short
            //return Integer.valueOf(get_SMALLINT(column));
            return get_SMALLINT(column);
        case Types.INTEGER:
            return get_INTEGER(column);
        case Types.BIGINT:
            return get_BIGINT(column);
        case Types.REAL:
            return get_FLOAT(column);
        case Types.DOUBLE:
            return get_DOUBLE(column);
        case Types.DECIMAL:
            return get_DECIMAL(column);
        case Types.DATE:
            return get_DATE(column);
        case Types.TIME:
            return get_TIME(column);
//        case Types.TIMESTAMP:
//            return getTIMESTAMP(column, getRecyclableCalendar());
        case Types.CHAR:
            return get_CHAR(column);
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return get_VARCHAR(column);
        case ClientTypes.BINARY:
            return get_CHAR_FOR_BIT_DATA(column);
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return get_VARCHAR_FOR_BIT_DATA(column);
        case Types.JAVA_OBJECT:
            return get_UDT( column );
        case Types.ROWID:
            return get_ROWID(column);
//        case Types.BLOB:
//            return getBlobColumn_(column, agent_, true);
//        case Types.CLOB:
//            return getClobColumn_(column, agent_, true);
        default:
            throw coercionError("Object type: ", column );
        }
    }

    public final void allocateCharBuffer() {
        // compute the maximum char length
        int maxCharLength = 0;
        for (int i = 0; i < columns_; i++) {
            switch (jdbcTypes_[i]) {
            case ClientTypes.CHAR:
            case ClientTypes.VARCHAR:
            case ClientTypes.LONGVARCHAR:
                if (fdocaLength_[i] > maxCharLength) {
                    maxCharLength = fdocaLength_[i];
                }
            }
        }

        // allocate char buffer to accomodate largest result column
        charBuffer_ = new char[maxCharLength];
    }

    private String getStringWithoutConvert(int position, int actualLength) {
        int start = position;
        int end = position + actualLength;

        int charCount = 0;
        while (start < end) {
            charBuffer_[charCount++] = dataBuffer_.getChar(start);
            //charBuffer_[charCount++] = (char) (((dataBuffer_[start] & 0xff) << 8) | (dataBuffer_[start + 1] & 0xff));
            start += 2;
        }

        return new String(charBuffer_, 0, charCount);
    }

    //private ColumnTypeConversionException coercionError
    private IllegalStateException coercionError
        ( String targetType, int sourceColumn )
    {
        return new IllegalStateException("Unknown target type for " + targetType + 
                ClientTypes.getTypeString(jdbcTypes_[sourceColumn -1]) +
                " value=" + jdbcTypes_[sourceColumn -1]);
    }

    private int getColumnPrecision(int column) {
        return ((fdocaLength_[column] >> 8) & 0xff);
    }

    private int getColumnScale(int column) {
        return (fdocaLength_[column] & 0xff);
    }
    
    // @AGG following code is from NetCursor
    
    //-----------------------------parsing the data buffer------------------------

    /**
     * Calculate the column offsets for a row.
     * <p>
     * Pseudo-code:
     * <ol>
     * <li>parse thru the current row in dataBuffer computing column
     * offsets</li>
     * <li>if (we hit the super.lastValidBytePosition, ie. encounter
     * partial row)
     *   <ol>
     *     <li>shift partial row bytes to beginning of dataBuffer
     *     (this.shiftPartialRowToBeginning())</li>
     *     <li>reset current row position (also done by
     *     this.shiftPartialRowToBeginning())</li>
     *     <li>send and recv continue-query into commBuffer
     *     (rs.flowContinueQuery())</li>
     *     <li>parse commBuffer up to QRYDTA
     *     (rs.flowContinueQuery())</li>
     *     <li>copy query data from reply's commBuffer to our
     *     dataBuffer (this.copyQrydta())</li>
     *   </ol>
     * </ol>
     *
     * @param rowIndex row index
     * @param allowServerFetch if true, allow fetching more data from
     * server
     * @return <code>true</code> if the current row position is a
     * valid row position.
     */
    protected boolean calculateColumnOffsetsForRow_(int rowIndex, boolean allowServerFetch)
    {
        int daNullIndicator = CodePoint.NULLDATA;
        int colNullIndicator = CodePoint.NULLDATA;
        int length;

        int[] columnDataPosition = null;
        int[] columnDataComputedLength = null;
        boolean[] columnDataIsNull = null;
        boolean receivedDeleteHoleWarning = false;
        boolean receivedRowUpdatedWarning = false;

        // @AGG assume NOT scrollable
//        if ((position_ == lastValidBytePosition_) &&
//                (netResultSet_ != null) && (netResultSet_.scrollable_)) {
//            return false;
//        }

        if (hasLobs_) {
            extdtaPositions_.clear();  // reset positions for this row
        }

        NetSqlca[] netSqlca = parseSQLCARD(qrydscTypdef_);
        // If we don't have at least one byte in the buffer for the DA null indicator,
        // then we need to send a CNTQRY request to fetch the next block of data.
        // Read the DA null indicator. Do this before we close mark the statement
        // closed on the server.
        daNullIndicator = readFdocaOneByte();
        
        if (netSqlca != null) {
            for (int i=0;i<netSqlca.length; i++) {
                int sqlcode = netSqlca[i].getSqlCode();
                if (sqlcode < 0) {
                    throw new IllegalStateException(//netAgent_.logWriter_, 
                            netSqlca[i].toString());
                } else {
                    if (sqlcode == SqlCode.END_OF_DATA.getCode()) {
                        setAllRowsReceivedFromServer(true);
                        if (//netResultSet_ != null && 
                                netSqlca[i].containsSqlcax()) {
                            throw new UnsupportedOperationException();
//                            netResultSet_.setRowCountEvent(
//                                    netSqlca[i].getRowCount());
                        }
                    } else if (/*netResultSet_ != null && */ sqlcode > 0) {
                        String sqlState = netSqlca[i].getSqlState();
                        if (!sqlState.equals(SQLState.ROW_DELETED) && 
                                !sqlState.equals(SQLState.ROW_UPDATED)) {
//                            netResultSet_.accumulateWarning(
//                                    new SqlWarning(agent_.logWriter_, 
//                                        netSqlca[i]));
                        } else {
                            receivedDeleteHoleWarning 
                                    |= sqlState.equals(SQLState.ROW_DELETED);
                            receivedRowUpdatedWarning 
                                    |= sqlState.equals(SQLState.ROW_UPDATED);
                        }
                    }
                }
            }
        }

        setIsUpdataDeleteHole(rowIndex, receivedDeleteHoleWarning);
        setIsRowUpdated(receivedRowUpdatedWarning);
        
        

        // In the case for held cursors, the +100 comes back as part of the QRYDTA, and as
        // we are parsing through the row that contains the SQLCA with +100, we mark the
        // nextRowPosition_ which is the lastValidBytePosition_, but we don't mark the
        // currentRowPosition_ until the next time next() is called causing the check
        // cursor_.currentRowPositionIsEqualToNextRowPosition () to fail in getRow() and thus
        // not returning 0 when it should. So we need to mark the current row position immediately
        // in order for getRow() to be able to pick it up.

        // markNextRowPosition() is called again once this method returns, but it is ok
        // since it's only resetting nextRowPosition_ to position_ and position_ will
        // not change again from this point.

        if (allRowsReceivedFromServer() &&
            //(position_ == lastValidBytePosition_)) {
            (dataBuffer_.readerIndex() == lastValidBytePosition_)) {
            markNextRowPosition();
            makeNextRowPositionCurrent();
            return false;
        }

        // If data flows....
        if (daNullIndicator == 0x0) {

            incrementRowsReadEvent();

            // netResultSet_ is null if this method is invoked from Lob.position()
            // If row has exceeded the size of the ArrayList, new up a new int[] and add it to the
            // ArrayList, otherwise just reuse the int[].
            // @AGG assume NOT scrollable
//            if (netResultSet_ != null && netResultSet_.scrollable_) {
//                columnDataPosition = allocateColumnDataPositionArray(rowIndex);
//                columnDataComputedLength = allocateColumnDataComputedLengthArray(rowIndex);
//                columnDataIsNull = allocateColumnDataIsNullArray(rowIndex);
//                // Since we are no longer setting the int[]'s to null for a delete/update hole, we need
//                // another way of keeping track of the delete/update holes.
//                setIsUpdataDeleteHole(rowIndex, false);
//            } else {
                // Use the arrays defined on the Cursor for forward-only cursors.
                // can they ever be null
                if (columnDataPosition_ == null || columnDataComputedLength_ == null || isNull_ == null) {
                    allocateColumnOffsetAndLengthArrays();
                }
                columnDataPosition = columnDataPosition_;
                columnDataComputedLength = columnDataComputedLength_;
                columnDataIsNull = isNull_;
//            }

            // Loop through the columns
            for (int index = 0; index < columns_; index++) {
                // If column is nullable, read the 1-byte null indicator.
                if (nullable_[index])
                // Need to pass the column index so all previously calculated offsets can be
                // readjusted if the query block splits on a column null indicator.

                // null indicators from FD:OCA data
                // 0 to 127: a data value will flow.
                // -1 to -128: no data value will flow.
                {
                    colNullIndicator = readFdocaOneByte(index);
                }

                // If non-null column data
                if (!nullable_[index] || (colNullIndicator >= 0 && colNullIndicator <= 127)) {

                    // Set the isNull indicator to false
                    columnDataIsNull[index] = false;

                    switch (typeToUseForComputingDataLength_[index]) {
                    // for fixed length data
                    case Typdef.FIXEDLENGTH:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        if (isGraphic_[index]) {
                            columnDataComputedLength[index] = skipFdocaBytes(fdocaLength_[index] * 2, index);
                        } else {
                            columnDataComputedLength[index] = skipFdocaBytes(fdocaLength_[index], index);
                        }
                        break;

                        // for variable character string and variable byte string,
                        // there are 2-byte of length in front of the data
                    case Typdef.TWOBYTELENGTH:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        length = readFdocaTwoByteLength(index);
                        // skip length + the 2-byte length field
                        if (isGraphic_[index]) {
                            columnDataComputedLength[index] = skipFdocaBytes(length * 2, index) + 2;
                        } else {
                            columnDataComputedLength[index] = skipFdocaBytes(length, index) + 2;
                        }
                        break;

                        // For decimal columns, determine the precision, scale, and the representation
                    case Typdef.DECIMALLENGTH:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        columnDataComputedLength[index] = skipFdocaBytes(getDecimalLength(index), index);
                        break;

                    case Typdef.LOBLENGTH:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        columnDataComputedLength[index] = skipFdocaBytes(fdocaLength_[index] & 0x7fff, index);
                        break;

                        // for short variable character string and short variable byte string,
                        // there is a 1-byte length in front of the data
                    case Typdef.ONEBYTELENGTH:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        length = readFdocaOneByte(index);
                        // skip length + the 1-byte length field
                        if (isGraphic_[index]) {
                            columnDataComputedLength[index] = skipFdocaBytes(length * 2, index) + 1;
                        } else {
                            columnDataComputedLength[index] = skipFdocaBytes(length, index) + 1;
                        }
                        break;

                    default:
                        columnDataPosition[index] = dataBuffer_.readerIndex();
                        if (isGraphic_[index]) {
                            columnDataComputedLength[index] = skipFdocaBytes(fdocaLength_[index] * 2, index);
                        } else {
                            columnDataComputedLength[index] = skipFdocaBytes(fdocaLength_[index], index);
                        }
                        break;
                    }
                } else if ((colNullIndicator & 0x80) == 0x80) {
                    // Null data. Set the isNull indicator to true.
                    columnDataIsNull[index] = true;
                }
            }

            // set column offsets for the current row.
            columnDataPosition_ = columnDataPosition;
            columnDataComputedLength_ = columnDataComputedLength;
            isNull_ = columnDataIsNull;

            if (!allRowsReceivedFromServer()) {
                calculateLobColumnPositionsForRow();
                // Flow another CNTQRY if we are blocking, are using rtnextrow, and expect
                // non-trivial EXTDTAs for forward only cursors.  Note we do not support
                // EXTDTA retrieval for scrollable cursors.
                // if qryrowset was sent on excsqlstt for a sp call, which is only the case
                // @AGG assume NOT scrollable
                if (blocking_ && rtnextrow_ && /* !netResultSet_.scrollable_ && */ !extdtaPositions_.isEmpty()) {
                    if (allowServerFetch) {
                        throw new UnsupportedOperationException();
                        //netResultSet_.flowFetch();
                    } else {
                        return false;
                    }
                }
            }
        } else {
            // @AGG assume NOT scrollable
//            if (netResultSet_ != null && netResultSet_.scrollable_) {
//                if (receivedDeleteHoleWarning) {
//                    setIsUpdataDeleteHole(rowIndex, true);
//                }
//            }
        }

        // If blocking protocol is used, we could have already received an ENDQRYRM,
        // which sets allRowsReceivedFromServer_ to true.  It's safe to assume that all of
        // our QRYDTA's have been successfully copied to the dataBuffer.  And even though
        // the flag for allRowsReceivedFromServer_ is set, we still want to continue to parse through
        // the data in the dataBuffer.
        // But in the case where fixed row protocol is used,
        if (!blocking_ && allRowsReceivedFromServer() &&
            daNullIndicator == 0xFF) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Scan the data buffer to see if end of data (SQL state 02000)
     * has been received. This method should only be called when the
     * cursor is being closed since the pointer to the current row can
     * be modified.
     */
    void scanDataBufferForEndOfData() {
        while (!allRowsReceivedFromServer() &&
               (dataBuffer_.readerIndex() != lastValidBytePosition_)) {
            stepNext(false);
        }
    }

    private int readFdocaInt() {
        checkForSplitRowAndComplete(4);
        if (metadata.isZos())
          return dataBuffer_.readInt();
        else
          return dataBuffer_.readIntLE();
//        int i = SignedBinary.getInt(dataBuffer_, position_);
//        position_ += 4;
//        return i;
    }

    // Reads 1-byte from the dataBuffer from the current position.
    // If position is already at the end of the buffer, send CNTQRY to get more data.
    private int readFdocaOneByte() {
        checkForSplitRowAndComplete(1);
        //return dataBuffer_[position_++] & 0xff;
        return dataBuffer_.readUnsignedByte();
    }

    // Reads 1-byte from the dataBuffer from the current position.
    // If position is already at the end of the buffer, send CNTQRY to get more data.
    private int readFdocaOneByte(int index) {

        checkForSplitRowAndComplete(1, index);
        //return dataBuffer_[position_++] & 0xff;
        return dataBuffer_.readByte();
    }

    // Reads <i>length</i> number of bytes from the dataBuffer starting from the
    // current position.  Returns a new byte array which contains the bytes read.
    // If current position plus length goes past the lastValidBytePosition, send
    // CNTQRY to get more data.
    private byte[] readFdocaBytes(int length) {

        checkForSplitRowAndComplete(length);

        byte[] b = new byte[length];
        dataBuffer_.readBytes(b);
//        System.arraycopy(dataBuffer_, position_, b, 0, length);
//        position_ += length;
//
        return b;
    }

    // Reads 2-bytes from the dataBuffer starting from the current position, and
    // returns an integer constructed from the 2-bytes.  If current position plus
    // 2 bytes goes past the lastValidBytePosition, send CNTQRY to get more data.
    private int readFdocaTwoByteLength() {

        checkForSplitRowAndComplete(2);
        return dataBuffer_.readShort();
//        return
//                ((dataBuffer_[position_++] & 0xff) << 8) +
//                ((dataBuffer_[position_++] & 0xff) << 0);
    }

    private int readFdocaTwoByteLength(int index) {

        checkForSplitRowAndComplete(2, index);
        return dataBuffer_.readShort();
//        return
//                ((dataBuffer_[position_++] & 0xff) << 8) +
//                ((dataBuffer_[position_++] & 0xff) << 0);
    }

    // Check if position plus length goes past the lastValidBytePosition.
    // If so, send CNTQRY to get more data.
    // length - number of bytes to skip
    // returns the number of bytes skipped
    private int skipFdocaBytes(int length) {

        checkForSplitRowAndComplete(length);
        dataBuffer_.skipBytes(length);
//        position_ += length;
        return length;
    }

    private int skipFdocaBytes(int length, int index) {

        checkForSplitRowAndComplete(length, index);
//        position_ += length;
        dataBuffer_.skipBytes(length);
        return length;
    }

    // Shift partial row bytes to beginning of dataBuffer,
    // and resets current row position, and lastValidBytePosition.
    // When we shift partial row, we'll have to recalculate column offsets
    // up to this column.
    private void shiftPartialRowToBeginning() {
        if(true)
            throw new UnsupportedOperationException("Need to step through this method");
//        // Get the length to shift from the beginning of the partial row.
//        int length = lastValidBytePosition_ - currentRowPosition_;
//
//        // shift the data in the dataBufferStream
//        dataBufferStream_.reset();
//        if (dataBuffer_ != null) {
//            dataBufferStream_.write(dataBuffer_, currentRowPosition_, length);
//        }
//
//        for (int i = 0; i < length; i++) {
//            dataBuffer_[i] = dataBuffer_[currentRowPosition_ + i];
//        }
//
//        position_ = length - (lastValidBytePosition_ - position_);
//        lastValidBytePosition_ = length;
    }

    /**
     * Adjust column offsets after fetching the next part of a split row.
     * @param index the index of the column that was split, or -1 when not
     * fetching column data
     */
    private void adjustColumnOffsetsForColumnsPreviouslyCalculated(int index) {
        for (int j = 0; j <= index; j++) {
            columnDataPosition_[j] -= currentRowPosition_;
        }
    }

    private void resetCurrentRowPosition() {
        currentRowPosition_ = 0;
    }

    // Calculates the column index for Lob objects constructed from EXTDTA data.
    // Describe information isn't sufficient because we have to check
    // for trivial values (nulls or zero-length) and exclude them.
    // Need also to check whether locator was returned since in that case
    // there will be no EXTDTA data for the LOB column.
    void calculateLobColumnPositionsForRow() {
        int currentPosition = 0;

        for (int i = 0; i < columns_; i++) {
            if ((isNonTrivialDataLob(i)) 
                && (locator(i + 1) == -1)) // Lob.INVALID_LOCATOR))
            // key = column position, data = index to corresponding data in extdtaData_
            // ASSERT: the server always returns the EXTDTA objects in ascending order
            {
                extdtaPositions_.put(i + 1, currentPosition++);
            }
        }
    }

    // prereq: the base data for the cursor has been processed for offsets and lengths
    private boolean isNonTrivialDataLob(int index) {
        long length = 0L;

        if (isNull_[index] ||
                (jdbcTypes_[index] != ClientTypes.BLOB &&
                jdbcTypes_[index] != ClientTypes.CLOB)) {
            return false;
        }

        int position = columnDataPosition_[index];

        // if the high-order bit is set, length is unknown -> set value to x'FF..FF'
        //if (((dataBuffer_[position]) & 0x80) == 0x80) {
        if ((dataBuffer_.getByte(dataBuffer_.readerIndex()) & 0x80) == 0x80) {
            length = -1;
        } else {

            byte[] lengthBytes = new byte[columnDataComputedLength_[index]];
            byte[] longBytes = new byte[8];

//            System.arraycopy(dataBuffer_, position, lengthBytes, 0, columnDataComputedLength_[index]);
            dataBuffer_.getBytes(position, lengthBytes, 0, columnDataComputedLength_[index]);

            // right-justify for BIG ENDIAN
            int j = 0;
            for (int i = 8 - columnDataComputedLength_[index]; i < 8; i++) {
                longBytes[i] = lengthBytes[j];
                j++;
            }
            length = getLong(longBytes, 0);
        }
        return (length != 0L) ? true : false;
    }
    
    private static final long getLong(byte[] buffer, int offset) {
        return (long) (((buffer[offset + 0] & 0xffL) << 56) +
                ((buffer[offset + 1] & 0xffL) << 48) +
                ((buffer[offset + 2] & 0xffL) << 40) +
                ((buffer[offset + 3] & 0xffL) << 32) +
                ((buffer[offset + 4] & 0xffL) << 24) +
                ((buffer[offset + 5] & 0xffL) << 16) +
                ((buffer[offset + 6] & 0xffL) << 8) +
                ((buffer[offset + 7] & 0xffL) << 0));
    }

    protected void clearLobData_() {
        extdtaData_.clear();
        extdtaPositions_.clear();
    }

    // SQLCARD : FDOCA EARLY ROW
    // SQL Communications Area Row Description
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLCAGRP; GROUP LID 0x54; ELEMENT TAKEN 0(all); REP FACTOR 1
    private NetSqlca[] parseSQLCARD(Typdef typdef) {
        return parseSQLCAGRP(typdef);
    }

    // SQLCAGRP : FDOCA EARLY GROUP
    // SQL Communcations Area Group Description
    //
    // FORMAT FOR SQLAM <= 6
    //   SQLCODE; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLSTATE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 5
    //   SQLERRPROC; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 8
    //   SQLCAXGRP; PROTOCOL TYPE N-GDA; ENVLID 0x52; Length Override 0
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLCODE; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLSTATE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 5
    //   SQLERRPROC; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 8
    //   SQLCAXGRP; PROTOCOL TYPE N-GDA; ENVLID 0x52; Length Override 0
    //   SQLDIAGGRP; PROTOCOL TYPE N-GDA; ENVLID 0x56; Length Override 0
    private NetSqlca[] parseSQLCAGRP(Typdef typdef) {

        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return null;
        }
        int sqlcode = readFdocaInt();
        byte[] sqlstate = readFdocaBytes(5);
        byte[] sqlerrproc = readFdocaBytes(8);
        NetSqlca netSqlca = new NetSqlca(/*netAgent_.netConnection_, */sqlcode, sqlstate, sqlerrproc);

        parseSQLCAXGRP(typdef, netSqlca);

        NetSqlca[] sqlCa = parseSQLDIAGGRP();

        NetSqlca[] ret_val;
        if (sqlCa != null) {
            ret_val = new NetSqlca[sqlCa.length + 1];
            System.arraycopy(sqlCa, 0, ret_val, 1, sqlCa.length);
        } else {
            ret_val = new NetSqlca[1];
        }
        ret_val[0] = netSqlca;
        
        return ret_val;
    }

    // SQLCAXGRP : EARLY FDOCA GROUP
    // SQL Communications Area Exceptions Group Description
    //
    // FORMAT FOR SQLAM <= 6
    //   SQLRDBNME; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 18
    //   SQLERRD1; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD2; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD3; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD4; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD5; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD6; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLWARN0; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN1; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN2; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN3; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN4; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN5; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN6; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN7; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN8; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN9; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARNA; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLERRMSG_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 70
    //   SQLERRMSG_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 70
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLERRD1; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD2; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD3; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD4; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD5; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLERRD6; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLWARN0; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN1; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN2; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN3; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN4; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN5; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN6; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN7; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN8; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARN9; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLWARNA; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    //   SQLRDBNAME; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
    //   SQLERRMSG_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 70
    //   SQLERRMSG_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 70
    private void parseSQLCAXGRP(Typdef typdef, NetSqlca netSqlca) {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            netSqlca.setContainsSqlcax(false);
            return;
        }


        //   SQLERRD1 to SQLERRD6; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
        int[] sqlerrd = new int[ NetSqlca.SQL_ERR_LENGTH ];
        for (int i = 0; i < sqlerrd.length; i++) {
            sqlerrd[i] = readFdocaInt();
        }

        //   SQLWARN0 to SQLWARNA; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
        byte[] sqlwarn = readFdocaBytes(11);

        // skip over the rdbnam for now
        // SQLRDBNAME; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
        parseVCS(typdef);

        //   SQLERRMSG_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 70
        //   SQLERRMSG_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 70
        int varcharLength = readFdocaTwoByteLength();  // mixed length
        byte[] sqlerrmc = null;
        int sqlerrmcCcsid = 0;
        if (varcharLength != 0) {                    // if mixed
            sqlerrmc = readFdocaBytes(varcharLength);      // read mixed bytes
            sqlerrmcCcsid = typdef.getCcsidMbc();
            skipFdocaBytes(2);                          // skip single length
        } else {
            varcharLength = readFdocaTwoByteLength();  // read single length
            sqlerrmc = readFdocaBytes(varcharLength);     // read single bytes
            sqlerrmcCcsid = typdef.getCcsidSbc();
        }

        netSqlca.setSqlerrd(sqlerrd);
        netSqlca.setSqlwarnBytes(sqlwarn);
        netSqlca.setSqlerrmcBytes(sqlerrmc);
    }

    // SQLDIAGGRP : FDOCA EARLY GROUP
    private NetSqlca[] parseSQLDIAGGRP() {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return null;
        }

        parseSQLDIAGSTT();
        NetSqlca[] sqlca = parseSQLDIAGCI();
        parseSQLDIAGCN();

        return sqlca;
    }

    // SQL Diagnostics Statement Group Description - Identity 0xD3
    // NULLDATA will be received for now
    private void parseSQLDIAGSTT() {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return;
        }

        // The server should send NULLDATA
        throw new IllegalStateException("SQLState.DRDA_COMMAND_NOT_IMPLEMENTED parseSQLDIAGSTT");
//        netAgent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(netAgent_, 
//                    new ClientMessageId(SQLState.DRDA_COMMAND_NOT_IMPLEMENTED),
//                    "parseSQLDIAGSTT"));
    }

    // SQL Diagnostics Condition Information Array - Identity 0xF5
    // SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    // SQLDCIROW; ROW LID 0xE5; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    private NetSqlca[] parseSQLDIAGCI()  {
        int num = readFdocaTwoByteLength(); // SQLNUMGRP - SQLNUMROW
        NetSqlca[] ret_val = null;
        if (num != 0) {
            ret_val = new NetSqlca[num];
        } 

        for (int i = 0; i < num; i++) {
            ret_val[i] = parseSQLDCROW();
        }
        return ret_val;
    }

    // SQL Diagnostics Connection Array - Identity 0xF6
    // NULLDATA will be received for now
    private void parseSQLDIAGCN() {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return;
        }
        
        // The server should send NULLDATA
        throw new UnsupportedOperationException("SQLState.DRDA_COMMAND_NOT_IMPLEMENTED parseSQLDIAGCN");
//        netAgent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(netAgent_, 
//                    new ClientMessageId(SQLState.DRDA_COMMAND_NOT_IMPLEMENTED),
//                    "parseSQLDIAGCN"));
    }

    // SQL Diagnostics Condition Group Description
    //
    // SQLDCCODE; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCSTATE; PROTOCOL TYPE FCS; ENVLID Ox30; Lengeh Override 5
    // SQLDCREASON; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCLINEN; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCROWN; PROTOCOL TYPE I8; ENVLID 0x16; Lengeh Override 8
    // SQLDCER01; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER02; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER03; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER04; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCPART; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCPPOP; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCMSGID; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 10
    // SQLDCMDE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 8
    // SQLDCPMOD; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 5
    // SQLDCRDB; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
    // SQLDCTOKS; PROTOCOL TYPE N-RLO; ENVLID 0xF7; Length Override 0
    // SQLDCMSG_m; PROTOCOL TYPE NVMC; ENVLID 0x3F; Length Override 32672
    // SQLDCMSG_S; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 32672
    // SQLDCCOLN_m; PROTOCOL TYPE NVCM ; ENVLID 0x3F; Length Override 255
    // SQLDCCOLN_s; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCCURN_m; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCCURN_s; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCPNAM_m; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCPNAM_s; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXGRP; PROTOCOL TYPE N-GDA; ENVLID 0xD3; Length Override 1
    private NetSqlca parseSQLDCGRP()  {
        
        int sqldcCode = readFdocaInt(); // SQLCODE
        String sqldcState = readFdocaString(5, 
                Typdef.targetTypdef.getCcsidSbcEncoding()); // SQLSTATE
        int sqldcReason = readFdocaInt();  // REASON_CODE

        skipFdocaBytes(12); // LINE_NUMBER + ROW_NUMBER

        NetSqlca sqlca = new NetSqlca(//netAgent_.netConnection_,
                    sqldcCode,
                    sqldcState,
                    (byte[]) null);

        skipFdocaBytes(49); // SQLDCER01-04 + SQLDCPART + SQLDCPPOP + SQLDCMSGID
                            // SQLDCMDE + SQLDCPMOD + RDBNAME
        parseSQLDCTOKS(); // MESSAGE_TOKENS

        String sqldcMsg = parseVCS(qrydscTypdef_); // MESSAGE_TEXT

        if (sqldcMsg != null) {
            sqlca.setSqlerrmcBytes(sqldcMsg.getBytes());
        }

        skipFdocaBytes(12);  // COLUMN_NAME + PARAMETER_NAME + EXTENDED_NAMES

        parseSQLDCXGRP(); // SQLDCXGRP
        return sqlca;
    }

    // SQL Diagnostics Condition Row - Identity 0xE5
    // SQLDCGRP; GROUP LID 0xD5; ELEMENT TAKEN 0(all); REP FACTOR 1
    private NetSqlca parseSQLDCROW() {
        return parseSQLDCGRP();
    }
    
    // SQL Diagnostics Condition Token Array - Identity 0xF7
    // NULLDATA will be received for now
    private void parseSQLDCTOKS() {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return;
        }

        // The server should send NULLDATA
        throw new UnsupportedOperationException("SQLState.DRDA_COMMAND_NOT_IMPLEMENTED parseSQLDCTOKS");
//        netAgent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(netAgent_, 
//                    new ClientMessageId(SQLState.DRDA_COMMAND_NOT_IMPLEMENTED),
//                    "parseSQLDCTOKS"));
    }

    // SQL Diagnostics Extended Names Group Description - Identity 0xD5
    // NULLDATA will be received for now
    private void parseSQLDCXGRP() {
        if (readFdocaOneByte() == CodePoint.NULLDATA) {
            return;
        }

        // The server should send NULLDATA
        throw new UnsupportedOperationException("SQLState.DRDA_COMMAND_NOT_IMPLEMENTED parseSQLDCXGRP");
//        netAgent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(netAgent_, 
//                    new ClientMessageId(SQLState.DRDA_COMMAND_NOT_IMPLEMENTED),
//                    "parseSQLDCXGRP"));
    }

    private String parseVCS(Typdef typdefInEffect) {
        return readFdocaString(readFdocaTwoByteLength(),
                typdefInEffect.getCcsidSbcEncoding());
    }

    // This is not used for column data.
    private String readFdocaString(int length, Charset encoding) {
        if (length == 0) {
            return null;
        }

        checkForSplitRowAndComplete(length);

        return dataBuffer_.readCharSequence(length, encoding).toString();
//        String s = new String(dataBuffer_, position_, length, encoding);
//        position_ += length;
//        return s;
    }

    void allocateColumnOffsetAndLengthArrays() {
        columnDataPosition_ = new int[columns_];
        columnDataComputedLength_ = new int[columns_];
        isNull_ = new boolean[columns_];
    }

    private byte[] findExtdtaData(int column) {
        byte[] data = null;

        // locate the EXTDTA bytes, if any
        Integer extdtaQueuePosition = extdtaPositions_.get(column);

        if (extdtaQueuePosition != null) {
            //  found, get the data
            data = extdtaData_.get(extdtaQueuePosition);
        }

        return data;
    }
    
    /**
     * Get locator for LOB of the designated column
     * <p>
     * Note that this method cannot be invoked on a LOB column that is NULL.
     *
     * @param column column number, starts at 1
     * @return locator value, <code>Lob.INVALID_LOCATOR</code> if LOB
     *         value was sent instead of locator
     */
    protected int locator(int column)
    {
        int locator = get_INTEGER(column);
        // If Lob value was sent instead of locator, the value will be
        // 0x8000, 0x8002, 0x8004, 0x8006, 0x8008. This is not a locator 
        // but the blob has been sent by value.
        // Zero is not a valid locator, it indicates a zero length value
        if ((locator == 0x8000) || (locator == 0x8002) || (locator == 0x8004) || 
                (locator == 0x8006) || (locator == 0x8008) ||(locator == 0)) {
            return -1; // Lob.INVALID_LOCATOR;
        } else {
            return locator;
        }
    }

//    public ClientBlob getBlobColumn_(
//            int column,
//            Agent agent,
//            boolean toBePublished) throws SQLException {
//
//        // Only inform the tracker if the Blob is published to the user.
//        if (toBePublished) {
//            if ( netResultSet_ != null ) { netResultSet_.markLOBAsPublished(column); }
//        }
//        // Check for locator
//        int locator = locator(column);
//        if (locator > 0) { // Create locator-based LOB object
//            return new ClientBlob(agent, locator);
//        }
//        
//        // The Blob value has been sent instead of locator 
//        int index = column - 1;
//        int dataOffset;
//        byte[] data;
//        ClientBlob blob = null;
//
//        // locate the EXTDTA bytes, if any
//        data = findExtdtaData(column);
//
//        if (data != null) {
//            // data found
//            // set data offset based on the presence of a null indicator
//            if (!nullable_[index]) {
//                dataOffset = 0;
//            } else {
//                dataOffset = 1;
//            }
//
//            blob = new ClientBlob(data, agent, dataOffset);
//        } else {
//            blob = new ClientBlob(new byte[0], agent, 0);
//        }
//
//        return blob;
//    }


//    public ClientClob getClobColumn_(
//            int column,
//            Agent agent,
//            boolean toBePublished) {
//
//        // Only inform the tracker if the Clob is published to the user.
//        if (toBePublished) {
//            if ( netResultSet_ != null ) { netResultSet_.markLOBAsPublished(column); }
//        }
//        // Check for locator
//        int locator = locator(column);
//        if (locator > 0) { // Create locator-based LOB object
//            return new ClientClob(agent, locator);
//        }
//        
//        // The Clob value has been sent instead of locator 
//        int index = column - 1;
//        int dataOffset;
//        byte[] data;
//        ClientClob clob = null;
//
//        // locate the EXTDTA bytes, if any
//        data = findExtdtaData(column);
//
//        if (data != null) {
//            // data found
//            // set data offset based on the presence of a null indicator
//            if (!nullable_[index]) {
//                dataOffset = 0;
//            } else {
//                dataOffset = 1;
//            }
//            clob = new ClientClob(agent, data, charset_[index], dataOffset);
//        } else {
//            // the locator is not valid, it is a zero-length LOB
//            clob = new ClientClob(agent, "");
//        }
//
//        return clob;
//    }

//    // this is really an event-callback from NetStatementReply.parseSQLDTARDarray()
    void initializeColumnInfoArrays(
            Typdef typdef,
            int columnCount) {

        qrydscTypdef_ = typdef;

        // Allocate  arrays to hold the descriptor information.
        setNumberOfColumns(columnCount);
        fdocaLength_ = new int[columnCount];
        isGraphic_ = new boolean[columnCount];
        typeToUseForComputingDataLength_ = new int[columnCount];
    }

    protected void getMoreData_() {
        // reset the dataBuffer_ before getting more data if cursor is foward-only.
        // getMoreData() is only called in Cursor.next() when current position is
        // equal to lastValidBytePosition_.
        // @AGG assume FORWARD_ONLY
//        if (netResultSet_.resultSetType_ == ResultSet.TYPE_FORWARD_ONLY) {
            resetDataBuffer();
//        }
        throw new UnsupportedOperationException("flowFetch");
        //netResultSet_.flowFetch();
    }

    public void nullDataForGC()       // memory leak fix
    {
        dataBuffer_ = null;
        columnDataPosition_ = null;
        qrydscTypdef_ = null;
        columnDataComputedLength_ = null;
        columnDataPositionCache_ = null;
        columnDataLengthCache_ = null;
        columnDataIsNullCache_ = null;
        jdbcTypes_ = null;
        nullable_ = null;
        charset_ = null;
        this.ccsid_ = null;
        isUpdateDeleteHoleCache_ = null;
        isNull_ = null;
        fdocaLength_ = null;
        charBuffer_ = null;
        typeToUseForComputingDataLength_ = null;
        isGraphic_ = null;

        if (extdtaPositions_ != null) {
            extdtaPositions_.clear();
        }
        extdtaPositions_ = null;

        if (extdtaData_ != null) {
            extdtaData_.clear();
        }
        extdtaData_ = null;
    }

    /**
     * Check if the data we want crosses a row split, and fetch more data
     * if necessary.
     *
     * @param length the length in bytes of the data needed
     * @param index the index of the column to be fetched, or -1 when not
     * fetching column data
     */
    private void checkForSplitRowAndComplete(int length, int index) {
        // For singleton select, the complete row always comes back, even if
        // multiple query blocks are required, so there is no need to drive a
        // flowFetch (continue query) request for singleton select.
        //while ((position_ + length) > lastValidBytePosition_) {
        while (dataBuffer_.readableBytes() > lastValidBytePosition_) {
            // Check for ENDQRYRM, throw SQLException if already received one.
            checkAndThrowReceivedEndqryrm();

            // Send CNTQRY to complete the row/rowset.
            int lastValidByteBeforeFetch = completeSplitRow(index);

            // If lastValidBytePosition_ has not changed, and an ENDQRYRM was
            // received, throw a SQLException for the ENDQRYRM.
            checkAndThrowReceivedEndqryrm(lastValidByteBeforeFetch);
        }
    }

    /**
     * Check if the data we want crosses a row split, and fetch more data
     * if necessary. This method is not for column data; use
     * {@link #checkForSplitRowAndComplete(int, int)} for that.
     *
     * @param length the length in bytes of the data needed
     */
    private void checkForSplitRowAndComplete(int length) {
        checkForSplitRowAndComplete(length, -1);
    }

    // It is possible for the driver to have received an QRYDTA(with incomplete row)+ENDQRYRM+SQLCARD.
    // This means some error has occurred on the server and the server is terminating the query.
    // Before sending a CNTQRY to retrieve the rest of the split row, check if an ENDQRYRM has already
    // been received.  If so, do not send CNTQRY because the cursor is already closed on the server.
    // Instead, throw a SQLException.  Since we did not receive a complete row, it is not safe to
    // allow the application to continue to access the ResultSet, so we close it.
    private void checkAndThrowReceivedEndqryrm() {
        // If we are in a split row, and before sending CNTQRY, check whether an ENDQRYRM
        // has been received.
        // TODO: check for ENDQRYRM before throwing exception
        throw new IllegalStateException("SQLState.NET_QUERY_PROCESSING_TERMINATED");
//        if (!netResultSet_.openOnServer_) {
//            SQLException SQLException = null;
//            int sqlcode = Utils.getSqlcodeFromSqlca(
//                netResultSet_.queryTerminatingSqlca_);
//
//            if (sqlcode < 0) {
//                SQLException = new SQLException(agent_.logWriter_, netResultSet_.queryTerminatingSqlca_);
//            } else {
//                SQLException = new SQLException(agent_.logWriter_, 
//                    new ClientMessageId(SQLState.NET_QUERY_PROCESSING_TERMINATED));
//            }
//            try {
//                netResultSet_.closeX(); // the auto commit logic is in closeX()
//            } catch (SQLException e) {
//                SQLException.setNextException(e);
//            }
//            throw SQLException;
//        }
    }

    private void checkAndThrowReceivedEndqryrm(int lastValidBytePositionBeforeFetch) {
        // if we have received more data in the dataBuffer_, just return.
        if (lastValidBytePosition_ > lastValidBytePositionBeforeFetch) {
            return;
        }
        checkAndThrowReceivedEndqryrm();
    }

    /**
     * Fetch more data for a row that has been split up.
     *
     * @param index the index of the column that was split, or -1 when not
     * fetching column data
     * @return the value of {@code lastValidBytePosition_} before more data
     * was fetched
     */
    private int completeSplitRow(int index) {
        int lastValidBytePositionBeforeFetch = 0;
        // @AGG assume NOT scrollable
//        if (netResultSet_ != null && netResultSet_.scrollable_) {
//            lastValidBytePositionBeforeFetch = lastValidBytePosition_;
//            netResultSet_.flowFetchToCompleteRowset();
//        } else {
            // Shift partial row to the beginning of the dataBuffer
            shiftPartialRowToBeginning();
            adjustColumnOffsetsForColumnsPreviouslyCalculated(index);
            resetCurrentRowPosition();
            lastValidBytePositionBeforeFetch = lastValidBytePosition_;
            throw new UnsupportedOperationException();
            // netResultSet_.flowFetch(); TODO @AGG implement flowFetch
//        }
        //return lastValidBytePositionBeforeFetch;
    }

    private int[] allocateColumnDataPositionArray(int row) {
        int[] columnDataPosition;
        if (columnDataPositionCache_.size() == row) {
            columnDataPosition = new int[columns_];
            columnDataPositionCache_.add(columnDataPosition);
        } else {
            columnDataPosition = columnDataPositionCache_.get(row);
        }
        return columnDataPosition;
    }

    private int[] allocateColumnDataComputedLengthArray(int row) {
        int[] columnDataComputedLength;
        if (columnDataLengthCache_.size() == row) {
            columnDataComputedLength = new int[columns_];
            columnDataLengthCache_.add(columnDataComputedLength);
        } else {
            columnDataComputedLength = columnDataLengthCache_.get(row);
        }
        return columnDataComputedLength;
    }

    private boolean[] allocateColumnDataIsNullArray(int row) {
        boolean[] columnDataIsNull;
        if (columnDataIsNullCache_.size() <= row) {
            columnDataIsNull = new boolean[columns_];
            columnDataIsNullCache_.add(columnDataIsNull);
        } else {
            columnDataIsNull = columnDataIsNullCache_.get(row);
        }
        return columnDataIsNull;
    }

    protected int getDecimalLength(int index) {
        return (((fdocaLength_[index] >> 8) & 0xff) + 2) / 2;
    }

    /**
     * Set the value of value of allRowsReceivedFromServer_.
     *
     * @param b a <code>boolean</code> value indicating whether all
     * rows are received from the server
     */
    public final void setAllRowsReceivedFromServer(boolean b) {
        if (b && qryclsimpEnabled_) {
            // @AGG this only sets a boolean on the ResultSet
            //netResultSet_.markClosedOnServer();
        }
        allRowsReceivedFromServer_ = b;
    }

    /**
     * Set a flag indicating whether QRYCLSIMP is enabled.
     *
     * @param flag true if QRYCLSIMP is enabled
     */
    final void setQryclsimpEnabled(boolean flag) {
        qryclsimpEnabled_ = flag;
    }

    /**
     * Check whether QRYCLSIMP is enabled on this cursor.
     *
     * @return true if QRYCLSIMP is enabled
     */
    final boolean getQryclsimpEnabled() {
        return qryclsimpEnabled_;
    }
    
    protected boolean calculateColumnOffsetsForRow() {
        int colNullIndicator = CodePoint.NULLDATA;
        int length;

        extdtaPositions_.clear();  // reset positions for this row

        // read the da null indicator
        if (readFdocaOneByte() == 0xff) {
            return false;
        }

        incrementRowsReadEvent();
        // Use the arrays defined on the Cursor for forward-only cursors.
        // can they ever be null
        if (columnDataPosition_ == null || columnDataComputedLength_ == null || isNull_ == null) {
            allocateColumnOffsetAndLengthArrays();
        }

        // Loop through the columns
        for (int index = 0; index < columns_; index++) {
            // If column is nullable, read the 1-byte null indicator.
            if (nullable_[index])
            // Need to pass the column index so all previously calculated offsets can be
            // readjusted if the query block splits on a column null indicator.

            // null indicators from FD:OCA data
            // 0 to 127: a data value will flow.
            // -1 to -128: no data value will flow.
            {
                colNullIndicator = readFdocaOneByte();
            }

            // If non-null column data
            if (!nullable_[index] || (colNullIndicator >= 0 && colNullIndicator <= 127)) {
                isNull_[index] = false;

                switch (typeToUseForComputingDataLength_[index]) {
                // for variable character string and variable byte string,
                // there are 2-byte of length in front of the data
                case Typdef.TWOBYTELENGTH:
                    columnDataPosition_[index] = dataBuffer_.readerIndex();
                    length = readFdocaTwoByteLength();
                    // skip length + the 2-byte length field
                    if (isGraphic_[index]) {
                        columnDataComputedLength_[index] = skipFdocaBytes(length * 2) + 2;
                    } else {
                        columnDataComputedLength_[index] = skipFdocaBytes(length) + 2;
                    }
                    break;

                    // for short variable character string and short variable byte string,
                    // there is a 1-byte length in front of the data
                case Typdef.ONEBYTELENGTH:
                    columnDataPosition_[index] = dataBuffer_.readerIndex();
                    length = readFdocaOneByte();
                    // skip length + the 1-byte length field
                    if (isGraphic_[index]) {
                        columnDataComputedLength_[index] = skipFdocaBytes(length * 2) + 1;
                    } else {
                        columnDataComputedLength_[index] = skipFdocaBytes(length) + 1;
                    }
                    break;

                    // For decimal columns, determine the precision, scale, and the representation
                case Typdef.DECIMALLENGTH:
                    columnDataPosition_[index] = dataBuffer_.readerIndex();
                    columnDataComputedLength_[index] = skipFdocaBytes(getDecimalLength(index));
                    break;

                case Typdef.LOBLENGTH:
                    columnDataPosition_[index] = dataBuffer_.readerIndex();
                    columnDataComputedLength_[index] = skipFdocaBytes(fdocaLength_[index] & 0x7fff);
                    break;

                default:
                    columnDataPosition_[index] = dataBuffer_.readerIndex();
                    if (isGraphic_[index]) {
                        columnDataComputedLength_[index] = skipFdocaBytes(fdocaLength_[index] * 2);
                    } else {
                        columnDataComputedLength_[index] = skipFdocaBytes(fdocaLength_[index]);
                    }
                    break;
                }
            } else if ((colNullIndicator & 0x80) == 0x80) {
                // Null data. Set the isNull indicator to true.
                isNull_[index] = true;
            }
        }

        if (!allRowsReceivedFromServer()) {
            calculateLobColumnPositionsForRow();
        }

        return true; // hardwired for now, this means the current row position is a valid position
    }    
}