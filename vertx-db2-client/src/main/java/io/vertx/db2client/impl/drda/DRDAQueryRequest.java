package io.vertx.db2client.impl.drda;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import io.netty.buffer.ByteBuf;

public class DRDAQueryRequest extends DRDAConnectRequest {
    
    private static final String COLLECTIONNAME = "NULLID";
    
    public static final int defaultFetchSize = 64;
    
    private ArrayList<Integer> extdtaPositions_ = null;
    
    // promototed parameters hold parameters that are promotoed to a different
    // data type because they are too large to represent in PROTOCOL otherwise.
    // This currently only applies for promotion of (VAR)CHAR -> CLOB and (VAR)BINARY -> BLOB
    // The key for this structure is the parameter index.  Note that having this
    // collection does not eliminate the need for extdtaPositions_ because that
    // is still needed for non-promototed LOBs
    private final HashMap<Integer, Object> promototedParameters_ = new HashMap<>();
    
    public DRDAQueryRequest(ByteBuf buffer, CCSIDManager ccsidManager) {
        super(buffer, ccsidManager);
    }
    
    /**
     * @return True if the SQL is a query (i.e. SELECT) and false otherwise
     */
    public static boolean isQuery(String sql) {
        return sql.startsWith("SELECT") 
                || sql.startsWith("select")
                || sql.startsWith("WITH")
                || sql.startsWith("with");
    }
    
    // Write the message to preform a prepare into.
    // Once the SQL statement has been prepared, it is executed until the unit of work, in
    // which the PRPSQLSTT command was issued, ends.  An exception to this is if
    // Keep Dynamic is being used.
    //
    // preconditions:
    public void writePrepareDescribeOutput(String sql, String dbName, Section section) {
        // @AGG primary query method
        buildPRPSQLSTT(section,
                sql,
                dbName,
                true, //sendRtnsqlda
                true, //sendTypsqlda
                CodePoint.TYPSQLDA_X_OUTPUT);  //typsqlda

//        if (((NetStatement) materialStatement).statement_.cursorAttributesToSendOnPrepare_ != null) {
            //buildSQLATTRcommandData(((NetStatement) materialStatement).statement_.cursorAttributesToSendOnPrepare_);
        buildSQLATTRcommandData("FOR READ ONLY "); // @AGG assuming readonly (query)
//        }

        buildSQLSTTcommandData(sql);  // statement follows in sqlstt command data object
    }
    
    public void writeDescribeInput(Section section, String dbName) {
        int typsqlda = CodePoint.TYPSQLDA_X_INPUT;
        buildDSCSQLSTT(section, dbName, true, typsqlda);
    }
    
    // Build the Describe SQL Statement command.
    //
    // preconditions:
    //   the sqlam and/or prdid must support command and parameters passed to this method,
    //   method will not validate against the connection's level of support
    private void buildDSCSQLSTT(Section section, String dbName, boolean sendTypsqlda, int typsqlda) {
        createCommand();
        markLengthBytes(CodePoint.DSCSQLSTT);

        buildPKGNAMCSN(dbName, section);
        if (sendTypsqlda) {
            buildTYPSQLDA(typsqlda);
        }

        updateLengthBytes();
    }
    
    // Build the Execute SQL Statement (EXCSQLSTT) Command
    // to execute a non-cursor SQL statement previously bound into a named package
    // of a relational database (RDB).  The SQL statement can optionally include
    // references to input variables, output variables, or both.
    //
    // At SQLAM >= 7 we can get a DA back on this, are there times that we want to request it
    // If so, we probably want to pass a parameter indicating the sqldaLevel requested.
    //
    // preconditions:
    //   the sqlam and/or prdid must support command and parameters passed to this method,
    //   method will not validate against the connection's level of support
    // Here is the preferred codepoint ordering:
    // PKGNAMCSN
    // RDBCMTOK
    // OUTEXP
    // QRYBLKSZ
    // MAXBLKEXT
    // MAXRSLCNT
    // RSLSETFLG
    // QRYROWSET
    // RTNSQLDA
    // TYPSQLDA
    // NBRROW
    // ATMIND
    // PRCNAM
    // OUTOVROPT
    // RDBNAM
    private void buildEXCSQLSTT(Section section,
    					String dbName,
                        boolean sendOutexp,
                        boolean outexp,
                        boolean sendPrcnam,
                        String prcnam,
                        boolean sendQryblksz,
                        boolean sendMaxrslcnt,
                        int maxrslcnt,
                        boolean sendMaxblkext,
                        int maxblkext,
                        boolean sendRslsetflg,
                        int resultSetFlag,
                        boolean sendQryrowset,
                        int qryrowset) {
        createCommand();
        markLengthBytes(CodePoint.EXCSQLSTT);

        buildPKGNAMCSN(dbName, section);
        buildRDBCMTOK();
        if (sendOutexp) {
            buildOUTEXP(outexp);
        }
        if (sendQryblksz) {
            buildQRYBLKSZ();
        }
        if (sendQryrowset && sendMaxblkext) {
            buildMAXBLKEXT(maxblkext);
        }
        if (sendMaxrslcnt) {
            buildMAXRSLCNT(maxrslcnt);
        }
        if (sendRslsetflg) {
            buildRSLSETFLG(resultSetFlag);
        }
        if (sendQryrowset) {
            buildQRYROWSET(qryrowset);
        }
        if (sendPrcnam) {
            buildPRCNAM(prcnam);
        }

        updateLengthBytes();  // command is complete, update the length bytes
    }
    
    // The Procedure Name.
    // The default value of PRCNAM is the procedure name value contained
    // within the section identified by the pkgnamcsn parameter.  If that
    // value is null, then the prcnam parameter must be specified.
    // it has a max length of 255.
    // the prcnam is required on commands if the procedure name is
    // specified by a host variable.
    // the default value is the procedure name contained in the section
    // specified by the pkgnamcsn parameter on the EXCSQLSTT command.
    //
    // preconditions:
    //   sqlam must support this parameter for the command, method will not check.
    //   prcnam can not be null, SQLException will be thrown
    //   prcnam can not be 0 length or > 255 length, SQLException will be thrown.
    private void buildPRCNAM(String prcnam) {
        if (prcnam == null) {
        	throw new IllegalArgumentException("SQLState.NET_NULL_PROCEDURE_NAME");
        }

        int prcnamLength = prcnam.length();
        if ((prcnamLength == 0) || (prcnamLength > 255)) {
        	throw new IllegalArgumentException("SQLState.NET_PROCEDURE_NAME_LENGTH_OUT_OF_RANGE: "  + prcnamLength);
        }

        writeScalarString(CodePoint.PRCNAM, prcnam);
    }
    
    // Result Set Flags is a single byte where each bit it a boolean flag.
    // It specifies wheter the requester desires the server to return name,
    // label and comment information for the columns of result sets generated by the command.
    // The default is b'00000000'.
    // columnNamesRequired
    //    false means the requester does not desire column names returned.
    //    true means the requester desires column names returned.
    // columnLabelsRequired
    //    false means the requester does not desire column labels returned.
    //    true means the requester desires column labels returned.
    // columnCommentsRequired
    //    false means the requester does not desire column comments returned.
    //    true means the requester desired column comments returned.
    // cantProcessAnswerSetData
    //    false means that for each result set, the requester expects the command
    //    to return an FDOCA description of the answer set data and to possibly
    //    return answer set data.  the block containing the end of the description
    //    may be completed if room exists with answer set data.  additional blocks
    //    of answer set data may also be chained to the block containing the end of the
    //    FDOCA description.  up to the maximum number of extra blocks of answer set data
    //    per result set specified in the MAXBLKEXT parameter.
    //    true means that for each result set, the requester expects the command to return
    //    an FDOCA description of the answer set data but does not expect the command to
    //    return any answer set data.
    // at SQLAM 7, new flags are supported which can be used to return
    // standard, extended, and light sqlda
    //
    // preconditions:
    //    sqlam must support this parameter, method will not check.
    private void buildRSLSETFLG(int resultSetFlag) {
        writeScalar1Byte(CodePoint.RSLSETFLG, resultSetFlag);
    }
    
    // Maximum Result Set Count specifies a limit on the number of result sets
    // the requester is capable of receiving as reply data in response to an ECSQLSTT
    // command that invokes a stored procedure.  If the stored procedure generates
    // more than MAXRSLCNT result sets, then the target system returns at most, the first
    // MAXRSLCNT of these result sets.  The stored procedure defines the order
    // in which the target system returns result sets.
    // this is s two byte signed binary number.
    // it has a min value of 0 which indicates the requester is not capable
    // of receiving result sets as reply data in response to the command.
    // a special value, -1 (CodePoint.MAXRSLCNT_NOLIMIT = 0xffff), indicates the
    // requester is capable of receiving all result sets in response the EXCSQLSTT.
    //
    // preconditions:
    //   sqlam must support this parameter for the command, method will not check.
    //   the value must be in correct range (-1 to 32767), method will not check.
    private void buildMAXRSLCNT(int maxResultSetCount) {
        if (maxResultSetCount == 0) {
            return;
        }
        writeScalar2Bytes(CodePoint.MAXRSLCNT, maxResultSetCount);
    }
    
    // Output Expected indicates wheterh the requester expects the target
    // SQLAM to return output with an SQLDTARD reply data object
    // as a result of the execution of the referenced SQL statement.
    // this is a single byte.
    // there are two possible enumerated values:
    // 0x'F1' (CodePoint.TRUE) - for true indicating the requester expects output
    // 0x'F0' (CodePoint.FALSE) - for false indicating the requeser does not expect output
    // 0x'F0' is the default.
    //
    // preconditions:
    //   sqlam must support this parameter on the command, method will not check.
    private void buildOUTEXP(boolean outputExpected) {
        if (outputExpected) {
            writeScalar1Byte(CodePoint.OUTEXP, CodePoint.TRUE);
        }
    }
    
    public void writeOpenQuery(Section section,
            String dbName,
            int fetchSize,
            int resultSetType,
            int numInputColumns,
            ColumnMetaData parameterMetaData,
            Object[] inputs) {
        boolean sendQryrowset = checkSendQryrowset(fetchSize, resultSetType);
        fetchSize = checkFetchsize(fetchSize, resultSetType);
        // think about if there is a way we can call build ddm just passing ddm parameters and not passing the material ps object
        // maybe not, if sometimes we need to set the caches hanging off the ps object during the ddm build
        // maybe we can extricate conditionals in the build ddm logic outside

        buildOPNQRY(section,
                dbName,
                sendQryrowset,
                fetchSize);


        // may be able to merge this with firstContinueQuery_ and push above conditional to common
//        ((NetStatement) materialPreparedStatement).qryrowsetSentOnOpnqry_ = sendQryrowset;

        if (numInputColumns > 0) {
            if ((extdtaPositions_ != null) && (!extdtaPositions_.isEmpty())) {
                extdtaPositions_.clear();  // reset extdta column position markers
            }
            // is this the best place for this
            // EXCSQSTT needs this too

            // think about having this method return a boolean to
            // indicate the extdta should be built
            boolean overrideExists = buildSQLDTAcommandData(numInputColumns,
                    parameterMetaData,
                    inputs);

            // can we eleminate the chain argument needed for lobs
            // do we chain after Extdta's on open, verify this
            buildEXTDTA(parameterMetaData,
                    inputs,
                    false);  //chained, do we chain after Extdta's on open
        }
    }
    
    // preconditions:
    private void buildEXTDTA(ColumnMetaData parameterMetaData,
                             Object[] inputRow,
                             boolean chained) {
//        try
//        {
            // build the EXTDTA data, if necessary
            if (extdtaPositions_ == null) {
                return;
            }
            throw new UnsupportedOperationException("EXTDTA");
//                boolean chainFlag, chainedWithSameCorrelator;
//
//                for (int i = 0; i < extdtaPositions_.size(); i++) {
//                    int index = extdtaPositions_.get(i);
//
//                    // is this the last EXTDTA to be built?
//                    if (i != extdtaPositions_.size() - 1) { // no
//                        chainFlag = true;
//                        chainedWithSameCorrelator = true;
//                    } else { // yes
//                        chainFlag = chained;
//                        chainedWithSameCorrelator = false;
//                    }
//
//                    // do we have to write a null byte?
//                    boolean writeNullByte = false;
//                    if (parameterMetaData.nullable_[index]) {
//                        writeNullByte = true;
//                    }
//                    // Use the type of the input parameter rather than the input
//                    // column if possible.
//                    int parameterType = parameterMetaData.clientParamtertype_[index];
//                    if (parameterType == 0) {
//                        parameterType = parameterMetaData.types_[index];
//                    }
//
//                    // the follow types are possible due to promotion to BLOB
//                    if (parameterType == ClientTypes.BLOB
//                            || parameterType == ClientTypes.BINARY
//                            || parameterType == ClientTypes.VARBINARY
//                            || parameterType == ClientTypes.LONGVARBINARY) {
//                        ClientBlob o = (ClientBlob) retrievePromotedParameterIfExists(index);
//
//                        Blob b = (o == null) ? (Blob) inputRow[index] : o;
//                        boolean isExternalBlob = !(b instanceof ClientBlob);
//                        if (isExternalBlob) {
//                            try {
//                                writeScalarStream(chainFlag,
//                                        chainedWithSameCorrelator,
//                                        CodePoint.EXTDTA,
//                                        (int) b.length(),
//                                        b.getBinaryStream(),
//                                        writeNullByte,
//                                        index + 1);
//                            } catch (SQLException e) {
//                                throw new SqlException(netAgent_.logWriter_, 
//                                    new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH),
//                                    e);
//                            }
//                        } else if ( ( (ClientBlob) b).isBinaryStream()) {
//                            
//                            if( ( (ClientBlob) b).willBeLayerBStreamed() ){
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    ((ClientBlob)b).getBinaryStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }else{
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    (int)((ClientBlob)b).length(),
//                                    ((ClientBlob)b).getBinaryStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }
//                        } else { // must be a binary string
//                            // note: a possible optimization is to use writeScalarLobBytes
//                            //       when the input is small
//                            //   use this: if (b.length () < DssConstants.MAX_DSS_LEN - 6 - 4)
//                            //               writeScalarLobBytes (...)
//                            // Yes, this would avoid having to new up a java.io.ByteArrayInputStream
//                            writeScalarStream(chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    (int) ((ClientBlob) b).length(),
//                                    ((ClientBlob) b).getBinaryStream(),
//                                    writeNullByte,
//                                    index + 1);
//                        }
//                    }
//                    // the follow types are possible due to promotion to CLOB
//                    else if (
//                            parameterType == ClientTypes.CLOB
//                            || parameterType == ClientTypes.CHAR
//                            || parameterType == ClientTypes.VARCHAR
//                            || parameterType == ClientTypes.LONGVARCHAR) {
//                        ClientClob o = (ClientClob)retrievePromotedParameterIfExists(index);
//
//                        Clob c = (o == null) ? (Clob) inputRow[index] : o;
//                        boolean isExternalClob = !(c instanceof ClientClob);
//
//                        if (isExternalClob) {
//                            try {
//                                writeScalarStream(chainFlag,
//                                        chainedWithSameCorrelator,
//                                        CodePoint.EXTDTA,
//                                        (int) c.length(),
//                                        c.getCharacterStream(),
//                                        writeNullByte,
//                                        index + 1);
//                            } catch (SQLException e) {
//                                throw new SqlException(netAgent_.logWriter_, 
//                                    new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH),
//                                    e);
//                            }
//                        } else if ( ( (ClientClob) c).isCharacterStream()) {
//                            
//                            if( ( (ClientClob) c).willBeLayerBStreamed() ) {
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    ((ClientClob)c).getCharacterStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }else{
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    (int)((ClientClob)c).length(),
//                                    ((ClientClob)c).getCharacterStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }
//                            
//                        } else if (((ClientClob) c).isAsciiStream()) {
//                            
//                            if( ( (ClientClob) c).willBeLayerBStreamed() ){
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    ((ClientClob)c).getAsciiStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }else { 
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    (int)((ClientClob)c).length(),
//                                    ((ClientClob)c).getAsciiStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }
//                                
//                        } else if (((ClientClob) c).isUnicodeStream()) {
//                            
//                            if( ( (ClientClob) c).willBeLayerBStreamed() ){
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    ((ClientClob)c).getUnicodeStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }else{
//                                writeScalarStream(
//                                    chainFlag,
//                                    chainedWithSameCorrelator,
//                                    CodePoint.EXTDTA,
//                                    (int)((ClientClob)c).length(),
//                                    ((ClientClob)c).getUnicodeStream(),
//                                    writeNullByte,
//                                    index + 1);
//                            }
//                        } else { // must be a String
//                            // note: a possible optimization is to use writeScalarLobBytes
//                            //       when the input is small.
//                            //   use this: if (c.length () < DssConstants.MAX_DSS_LEN - 6 - 4)
//                            //               writeScalarLobBytes (...)
//                            writeScalarStream(
//                                chainFlag,
//                                chainedWithSameCorrelator,
//                                CodePoint.EXTDTA,
//                                (int)((ClientClob)c).getUTF8Length(),
//                                new ByteArrayInputStream(
//                                    ((ClientClob) c).getUtf8String()),
//                                writeNullByte,
//                                index + 1);
//                        }
//                    }
//                }
//        }
//        catch ( SQLException se )
//        {
//            throw new SqlException(se);
//        }
    }
    
    // Build the SQL Program Variable Data Command Data Object.
    // This object contains the input data to an SQL statement
    // that an RDB is executing.
    //
    // preconditions:
    private boolean buildSQLDTAcommandData(int numInputColumns,
                                   ColumnMetaData parameterMetaData,
                                   Object[] inputRow) {
        createEncryptedCommandData();

        int loc = buffer.writerIndex();

        markLengthBytes(CodePoint.SQLDTA);

        int[][] protocolTypesAndLengths = allocateLidAndLengthsArray(parameterMetaData);

        Hashtable protocolTypeToOverrideLidMapping = null;
        ArrayList mddOverrideArray = null;
        protocolTypeToOverrideLidMapping =
                computeProtocolTypesAndLengths(inputRow, parameterMetaData, protocolTypesAndLengths,
                        protocolTypeToOverrideLidMapping);

        boolean overrideExists = false;

        buildFDODSC(numInputColumns,
                protocolTypesAndLengths,
                overrideExists,
                protocolTypeToOverrideLidMapping,
                mddOverrideArray);

        buildFDODTA(numInputColumns,
                protocolTypesAndLengths,
                inputRow);

        updateLengthBytes(); // for sqldta
        // TODO @AGG encryption
//        if (netAgent_.netConnection_.getSecurityMechanism() ==
//                NetConfiguration.SECMEC_EUSRIDDTA ||
//                netAgent_.netConnection_.getSecurityMechanism() ==
//                NetConfiguration.SECMEC_EUSRPWDDTA) {
//            encryptDataStream(loc);
//        }

        return overrideExists;
    }
    
    private void buildFDODTA(int numVars, int[][] protocolTypesAndLengths, Object[] inputs) {
//        try {

            Object o = null;

            markLengthBytes(CodePoint.FDODTA);
            buffer.writeByte(FdocaConstants.NULL_LID); // write the 1-byte row indicator

            // write data for each input column
            for (int i = 0; i < numVars; i++) {
                if (inputs[i] == null) {
                    if ((protocolTypesAndLengths[i][0] % 2) == 1) {
                        buffer.writeByte(FdocaConstants.NULL_DATA);
                    } else {
                        // bug check
                    }
                } else {
                    if ((protocolTypesAndLengths[i][0] % 2) == 1) {
                        buffer.writeByte(FdocaConstants.INDICATOR_NULLABLE);
                    }

                    switch (protocolTypesAndLengths[i][0] | 0x01) { // mask out null indicator
                    case DRDAConstants.DRDA_TYPE_NVARMIX:
                    case DRDAConstants.DRDA_TYPE_NLONGMIX:
                        // What to do for server that don't understand 1208 (UTF-8)
                        // check for a promototed type, and use that instead if it exists
                        o = retrievePromotedParameterIfExists(i);
                        if (o == null) {
                            writeSingleorMixedCcsidLDString((String) inputs[i],
                                    Typdef.typdef.getCcsidMbcEncoding());
                        } else { // use the promototed object instead
                            throw new UnsupportedOperationException("CLOB");
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientClob) o,
//                                    protocolTypesAndLengths, i);
                        }
                        break;

                    case DRDAConstants.DRDA_TYPE_NVARCHAR:
                    case DRDAConstants.DRDA_TYPE_NLONG:
                        o = retrievePromotedParameterIfExists(i);
                        if (o == null) {

                        } else { // use the promototed object instead
                            throw new UnsupportedOperationException("CLOB");
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientClob) o,
//                                    protocolTypesAndLengths, i);

                        }
                        break;

                    case DRDAConstants.DRDA_TYPE_NBOOLEAN:
                        buffer.writeBoolean(((Boolean) inputs[i]).booleanValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NINTEGER:
                        buffer.writeInt(((Integer) inputs[i]).intValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NSMALL:
                        buffer.writeShort(((Short) inputs[i]).shortValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NFLOAT4:
                        buffer.writeFloat(((Float) inputs[i]).floatValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NFLOAT8:
                        buffer.writeDouble(((Double) inputs[i]).doubleValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NDECIMAL:
                        writeBigDecimal((BigDecimal) inputs[i], (protocolTypesAndLengths[i][1] >> 8) & 0xff, // described
                                                                                                             // precision
                                                                                                             // not
                                                                                                             // actual
                                protocolTypesAndLengths[i][1] & 0xff); // described scale, not actual
                        break;
                    case DRDAConstants.DRDA_TYPE_NDATE:
                        throw new UnsupportedOperationException("DRDA_TYPE_NDATE");
//                        // The value may be a Date if it comes from one of the
//                        // methods that don't specify the calendar, or a
//                        // DateTimeValue if it comes from a method that does
//                        // specify the calendar. Convert to DateTimeValue if
//                        // needed.
//                        DateTimeValue dateVal = (inputs[i] instanceof Date) ? new DateTimeValue((Date) inputs[i])
//                                : (DateTimeValue) inputs[i];
//                        writeDate(dateVal);
//                        break;
                    case DRDAConstants.DRDA_TYPE_NTIME:
                        throw new UnsupportedOperationException("DRDA_TYPE_NTIME");
//                        // The value may be a Time if it comes from one of the
//                        // methods that don't specify the calendar, or a
//                        // DateTimeValue if it comes from a method that does
//                        // specify the calendar. Convert to DateTimeValue if
//                        // needed.
//                        DateTimeValue timeVal = (inputs[i] instanceof Time) ? new DateTimeValue((Time) inputs[i])
//                                : (DateTimeValue) inputs[i];
//                        writeTime(timeVal);
//                        break;
                    case DRDAConstants.DRDA_TYPE_NTIMESTAMP:
                        throw new UnsupportedOperationException("DRDA_TYPE_NTIMESTAMP");
//                        // The value may be a Timestamp if it comes from one of
//                        // the methods that don't specify the calendar, or a
//                        // DateTimeValue if it comes from a method that does
//                        // specify the calendar. Convert to DateTimeValue if
//                        // needed.
//                        DateTimeValue tsVal = (inputs[i] instanceof Timestamp)
//                                ? new DateTimeValue((Timestamp) inputs[i])
//                                : (DateTimeValue) inputs[i];
//                        writeTimestamp(tsVal);
//                        break;
                    case DRDAConstants.DRDA_TYPE_NINTEGER8:
                        buffer.writeLong(((Long) inputs[i]).longValue());
                        break;
                    case DRDAConstants.DRDA_TYPE_NVARBYTE:
                    case DRDAConstants.DRDA_TYPE_NLONGVARBYTE:
                        o = retrievePromotedParameterIfExists(i);
                        if (o == null) {
                            writeLDBytes((byte[]) inputs[i]);
                        } else { // use the promototed object instead
                            throw new UnsupportedOperationException("CLOB");
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientClob) o,
//                                    protocolTypesAndLengths, i);
                        }
                        break;
                    case DRDAConstants.DRDA_TYPE_NUDT:
                        throw new UnsupportedOperationException("DRDA_TYPE_NUDT");
//                        writeUDT(inputs[i]);
//                        break;
                    case DRDAConstants.DRDA_TYPE_NLOBCSBCS:
                    case DRDAConstants.DRDA_TYPE_NLOBCDBCS:
                        throw new UnsupportedOperationException("DRDA_TYPE_NLOBCDBCS");
//                        // check for a promoted Clob
//                        o = retrievePromotedParameterIfExists(i);
//                        if (o == null) {
//                            try {
//
//                                Clob c = (Clob) inputs[i];
//
//                                if (c instanceof ClientClob && ((ClientClob) c).willBeLayerBStreamed()) {
//
//                                    setFDODTALobLengthUnknown(i);
//                                } else {
//                                    long dataLength = c.length();
//                                    setFDODTALobLength(protocolTypesAndLengths, i, dataLength);
//
//                                }
//
//                            } catch (SQLException e) {
//                                throw new SqlException(netAgent_.logWriter_,
//                                        new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH), e);
//                            }
//                        } else {
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientClob) o,
//                                    protocolTypesAndLengths, i);
//                        }
//
//                        break;
                    case DRDAConstants.DRDA_TYPE_NLOBBYTES:
                        throw new UnsupportedOperationException("DRDA_TYPE_NLOBBYTES");
//                        // check for a promoted Clob
//                        o = retrievePromotedParameterIfExists(i);
//                        if (o == null) {
//                            try {
//
//                                Blob b = (Blob) inputs[i];
//
//                                if (b instanceof ClientBlob && ((ClientBlob) b).willBeLayerBStreamed()) {
//
//                                    setFDODTALobLengthUnknown(i);
//                                } else {
//                                    long dataLength = b.length();
//                                    setFDODTALobLength(protocolTypesAndLengths, i, dataLength);
//
//                                }
//
//                            } catch (SQLException e) {
//                                throw new SqlException(netAgent_.logWriter_,
//                                        new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH), e);
//                            }
//                        } else { // use promoted Blob
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientBlob) o,
//                                    protocolTypesAndLengths, i);
//                        }
//                        break;
                    case DRDAConstants.DRDA_TYPE_NLOBCMIXED:
                        throw new UnsupportedOperationException("DRDA_TYPE_NLOBCMIXED");
//                        // check for a promoted Clob
//                        o = retrievePromotedParameterIfExists(i);
//                        if (o == null) {
//
//                            final ClientClob c = (ClientClob) inputs[i];
//
//                            if (c.isString()) {
//                                setFDODTALobLength(protocolTypesAndLengths, i, c.getUTF8Length());
//
//                            } else if (!c.willBeLayerBStreamed()) {
//                                // must be a Unicode stream
//                                setFDODTALobLength(protocolTypesAndLengths, i, c.length());
//
//                            } else {
//                                setFDODTALobLengthUnknown(i);
//
//                            }
//
//                        } else { // use promoted Clob
//                            setFDODTALob(netAgent_.netConnection_.getSecurityMechanism(), (ClientClob) o,
//                                    protocolTypesAndLengths, i);
//                        }
//
//                        break;
                    case DRDAConstants.DRDA_TYPE_NLOBLOC:
                        throw new UnsupportedOperationException("DRDA_TYPE_NLOBLOC");
//                        // The FD:OCA data or the FDODTA contains the locator
//                        // value corresponding to the LOB. write the integer
//                        // value representing the locator here.
//                        writeIntFdocaData(((ClientBlob) inputs[i]).getLocator());
//                        break;
                    case DRDAConstants.DRDA_TYPE_NCLOBLOC:
                        throw new UnsupportedOperationException("DRDA_TYPE_NCLOBLOC");
//                        // The FD:OCA data or the FDODTA contains the locator
//                        // value corresponding to the LOB. write the integer
//                        // value representing the locator here.
//                        writeIntFdocaData(((ClientClob) inputs[i]).getLocator());
//                        break;
                    default:
                        throw new IllegalStateException("SQLState.NET_UNRECOGNIZED_JDBC_TYPE " + protocolTypesAndLengths[i][0] + 
                                numVars + i);
                    }
                }
            }
            updateLengthBytes(); // for fdodta
//        } catch (SQLException se) {
//            throw new SqlException(se);
//        }
    }
    
    // Build the FDOCA Data Descriptor Scalar whose value is a FDOCA
    // Descriptor or a segment of an FDOCA Descriptor.
    //
    // preconditions:
    private void buildFDODSC(int numColumns,
                             int[][] protocolTypesAndLengths,
                             boolean overrideExists,
                             Hashtable overrideMap,
                             ArrayList overrideArray) {
        markLengthBytes(CodePoint.FDODSC);
        buildSQLDTA(numColumns, protocolTypesAndLengths, overrideExists, overrideMap, overrideArray);
        updateLengthBytes();
    }
    
    // Build the FDOCA SQLDTA Late Row Descriptor.
    //
    // preconditions:
    private void buildSQLDTA(int numColumns,
                               int[][] lidAndLengthOverrides,
                               boolean overrideExists,
                               Hashtable overrideMap,
                               ArrayList overrideArray) {
        // mdd overrides need to be built first if any before the descriptors are built.
        if (overrideExists) {
            buildMddOverrides(overrideArray);
            buffer.writeBytes(FdocaConstants.MDD_SQLDTAGRP_TOSEND);
        }

        buildSQLDTAGRP(numColumns, lidAndLengthOverrides, overrideExists, overrideMap);

        if (overrideExists) {
            buffer.writeBytes(FdocaConstants.MDD_SQLDTA_TOSEND);
        }
        buffer.writeBytes(FdocaConstants.SQLDTA_RLO_TOSEND);
    }
    
    private void buildMddOverrides(ArrayList sdaOverrides) {
        byte[] mddBytes;
        for (int i = 0; i < sdaOverrides.size(); i++) {
            mddBytes = (byte[]) (sdaOverrides.get(i));
            buffer.writeBytes(mddBytes);
        }
    }
    
    // Build the FDOCA SQLDTAGRP Late Group Descriptor.
    // preconditions:
    private void buildSQLDTAGRP(int numVars,
                                  int[][] lidAndLengthOverrides,
                                  boolean mddRequired,
                                  Hashtable overrideMap) {
        int n = 0;
        int offset = 0;

        n = calculateColumnsInSQLDTAGRPtriplet(numVars);
        buildTripletHeader(((3 * n) + 3),
                FdocaConstants.NGDA_TRIPLET_TYPE,
                FdocaConstants.SQLDTAGRP_LID);

        do {
            writeLidAndLengths(lidAndLengthOverrides, n, offset, mddRequired, overrideMap);
            numVars -= n;
            if (numVars == 0) {
                break;
            }

            offset += n;
            n = calculateColumnsInSQLDTAGRPtriplet(numVars);
            buildTripletHeader(((3 * n) + 3),
                    FdocaConstants.CPT_TRIPLET_TYPE,
                    0x00);
        } while (true);
    }
    
    private int calculateColumnsInSQLDTAGRPtriplet(int numVars) {
        if (numVars > FdocaConstants.MAX_VARS_IN_NGDA) //rename to MAX_VARS_IN_SQLDTAGRP_TRIPLET
        {
            return FdocaConstants.MAX_VARS_IN_NGDA;
        }
        return numVars;
    }
    
    private int[][] allocateLidAndLengthsArray(ColumnMetaData parameterMetaData) {
        int numVars = parameterMetaData.columns_;
        int[][] lidAndLengths = parameterMetaData.protocolTypesCache_;
        if ((lidAndLengths) == null || (lidAndLengths.length != numVars)) {
            lidAndLengths = new int[numVars][2];
            parameterMetaData.protocolTypesCache_ = lidAndLengths;
        }
        return lidAndLengths;
    }
    
    // Consider refacctor so that this does not even have to look
    // at the actual object data, and only uses tags from the meta data
    // only have to call this once, rather than calling this for every input row
    // Comment: I don't think that it is possible to send decimal values without looking at the data for 
    // precision and scale (Kathey Marsden 10/11)
    // backburner: after refactoring this, later on, think about replacing case statements with table lookups
    private Hashtable computeProtocolTypesAndLengths(
            Object[] inputRow,
            ColumnMetaData parameterMetaData,
            int[][] lidAndLengths,
            Hashtable overrideMap) {

//        try
//        {
            int numVars = parameterMetaData.columns_;
            String s = null;
//            if (!promototedParameters_.isEmpty()) {
//                promototedParameters_.clear();
//            }

            for (int i = 0; i < numVars; i++) {

                int jdbcType;
                // Send the input type unless it is not available.
                // (e.g an output parameter)
                jdbcType = parameterMetaData.clientParamtertype_[i];
                if (jdbcType == 0) {
                    jdbcType = parameterMetaData.types_[i];
                }

                // jdbc semantics - This should happen outside of the build methods
                // if describe input is not supported, we require the user to at least
                // call setNull() and provide the type information.  Otherwise, we won't
                // be able to guess the right PROTOCOL type to send to the server, and an
                // exception is thrown.

                if (jdbcType == 0) {
                    throw new IllegalStateException("SQLState.NET_INVALID_JDBC_TYPE_FOR_PARAM " + i);
                }

                switch (jdbcType) {
                case Types.CHAR:
                case Types.VARCHAR:
                    // lid: PROTOCOL_TYPE_NVARMIX, length override: 32767 (max)
                    // dataFormat: String
                    // this won't work if 1208 is not supported
                    s = (String) inputRow[i];
                    // assumes UTF-8 characters at most 3 bytes long
                    // Flow the String as a VARCHAR
                    if (s == null || s.length() <= 32767 / 3) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NVARMIX;
                        lidAndLengths[i][1] = 32767;
                    } else {
                        throw new UnsupportedOperationException("CLOB");
//                        // Flow the data as CLOB data if the data too large to for LONGVARCHAR
//                        byte[] ba = s.getBytes(Typdef.UTF8ENCODING);
//                        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
//                        ClientClob c = new ClientClob(netAgent_, bais, Typdef.UTF8ENCODING, ba.length);
//                        // inputRow[i] = c;
//                        // Place the new Lob in the promototedParameter_ collection for
//                        // NetStatementRequest use
//                        promototedParameters_.put(i, c);
//
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCMIXED;
//
//                        if( c.willBeLayerBStreamed() ){
//
//                            //Correspond to totalLength 0 as default length for unknown
//                            lidAndLengths[i][1] = 0x8002;
//
//                        }else {
//                            lidAndLengths[i][1] = buildPlaceholderLength(c.length());
//
//                        }
                    }
                    break;
                case Types.INTEGER:
                    // lid: PROTOCOL_TYPE_NINTEGER, length override: 4
                    // dataFormat: Integer
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NINTEGER;
                    lidAndLengths[i][1] = 4;
                    break;
                case Types.BIT:
                case Types.BOOLEAN:
                    if (true) // @AGG assume server supports boolean transport
//                    if ( netAgent_.netConnection_.databaseMetaData_.
//                            serverSupportsBooleanParameterTransport() )
                    {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NBOOLEAN;
                        lidAndLengths[i][1] = 1;
                    }
                    else
                    {
                        // If the server doesn't support BOOLEAN parameters,
                        // send the parameter as a SMALLINT instead.
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NSMALL;
                        lidAndLengths[i][1] = 2;
                        if (inputRow[i] instanceof Boolean) {
                            Boolean bool = (Boolean) inputRow[i];
                            inputRow[i] = Short.valueOf(
                                    bool.booleanValue() ? (short) 1 : 0);
                        }
                    }
                    break;
                case Types.SMALLINT:
                case Types.TINYINT:
                    // lid: PROTOCOL_TYPE_NSMALL,  length override: 2
                    // dataFormat: Short
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NSMALL;
                    lidAndLengths[i][1] = 2;
                    break;
                case Types.REAL:
                    // lid: PROTOCOL_TYPE_NFLOAT4, length override: 4
                    // dataFormat: Float
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NFLOAT4;
                    lidAndLengths[i][1] = 4;
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                    // lid: PROTOCOL_TYPE_NFLOAT8, length override: 8
                    // dataFormat: Double
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NFLOAT8;
                    lidAndLengths[i][1] = 8;
                    break;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // lid: PROTOCOL_TYPE_NDECIMAL
                    // dataFormat: java.math.BigDecimal
                    // if null - guess with precision 1, scale 0
                    // if not null - get scale from data and calculate maximum precision.
                    // DERBY-2073. Get scale and precision from data so we don't lose fractional digits.
                    BigDecimal bigDecimal = (BigDecimal) inputRow[i];
                    int scale;
                    int precision;
                    
                    if (bigDecimal == null)
                    {
                        scale = 0;
                        precision = 1;
                    }
                    else
                    {
                        // adjust scale if it is negative. Packed Decimal cannot handle 
                        // negative scale. We don't want to change the original 
                        // object so make a new one.
                        if (bigDecimal.scale() < 0) 
                        {
                            bigDecimal =  bigDecimal.setScale(0);
                            inputRow[i] = bigDecimal;
                        }                        
                        scale = bigDecimal.scale();
                        precision = Decimal.computeBigDecimalPrecision(bigDecimal);
                    }                    
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NDECIMAL;
                    lidAndLengths[i][1] = (precision << 8) + // use precision above
                        (scale << 0);
                    
                    break;
                case Types.DATE:
                    // for input, output, and inout parameters
                    // lid: PROTOCOL_TYPE_NDATE, length override: 8
                    // dataFormat: java.sql.Date
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NDATE;
                    lidAndLengths[i][1] = 10;
                    break;
                case Types.TIME:
                    // for input, output, and inout parameters
                    // lid: PROTOCOL_TYPE_NTIME, length override: 8
                    // dataFormat: java.sql.Time
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NTIME;
                    lidAndLengths[i][1] = 8;
                    break;
                case Types.TIMESTAMP:
                    throw new UnsupportedOperationException("Types.TIMESTAMP");
//                    // for input, output, and inout parameters
//                    // lid: PROTOCOL_TYPE_NTIMESTAMP, length overrid: 26 or 29
//                    // dataFormat: java.sql.Timestamp
//                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NTIMESTAMP;
//                    lidAndLengths[i][1] = DateTime.getTimestampLength( netAgent_.netConnection_.serverSupportsTimestampNanoseconds() );
//                    break;
                case Types.BIGINT:
                    // if SQLAM < 6 this should be mapped to decimal (19,0) in common layer
                    // if SQLAM >=6, lid: PROTOCOL_TYPE_NINTEGER8, length override: 8
                    // dataFormat: Long
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NINTEGER8;
                    lidAndLengths[i][1] = 8;
                    break;
                case Types.LONGVARCHAR:
                    // Is this the right thing to do  // should this be 32700
                    s = (String) inputRow[i];
                    if (s == null || s.length() <= 32767 / 3) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLONGMIX;
                        lidAndLengths[i][1] = 32767;
                    } else {
                        throw new UnsupportedOperationException("CLOB");
//                        // Flow the data as CLOB data if the data too large to for LONGVARCHAR
//                        byte[] ba = s.getBytes(Typdef.UTF8ENCODING);
//                        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
//                        ClientClob c = new ClientClob(
//                            netAgent_, bais, Typdef.UTF8ENCODING, ba.length);
//
//                        // inputRow[i] = c;
//                        // Place the new Lob in the promototedParameter_ collection for
//                        // NetStatementRequest use
//                        promototedParameters_.put(i, c);
//
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCMIXED;
//                        lidAndLengths[i][1] = buildPlaceholderLength(c.length());
                    }
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                    byte[] ba = (byte[]) inputRow[i];
                    if (ba == null) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NVARBYTE;
                        lidAndLengths[i][1] = 32767;
                    } else if (ba.length <= 32767) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NVARBYTE;
                        lidAndLengths[i][1] = 32767;
                    } else {
                        throw new UnsupportedOperationException("BLOB");
//                        // Promote to a BLOB. Only reach this path in the absence of describe information.
//                        ClientBlob b = new ClientBlob(ba, netAgent_, 0);
//
//                        // inputRow[i] = b;
//                        // Place the new Lob in the promototedParameter_ collection for
//                        // NetStatementRequest use
//                        promototedParameters_.put(i, b);
//
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBBYTES;
//                        lidAndLengths[i][1] = buildPlaceholderLength(ba.length);
                    }
                    break;
                case Types.LONGVARBINARY:
                    ba = (byte[]) inputRow[i];
                    if (ba == null) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLONGVARBYTE;
                        lidAndLengths[i][1] = 32767;
                    } else if (ba.length <= 32767) {
                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLONGVARBYTE;
                        lidAndLengths[i][1] = 32767;
                    } else {
                        throw new UnsupportedOperationException("BLOB");
//                        // Promote to a BLOB. Only reach this path in the absensce of describe information.
//                        ClientBlob b = new ClientBlob(ba, netAgent_, 0);
//
//                        // inputRow[i] = b;
//                        // Place the new Lob in the promototedParameter_ collection for
//                        // NetStatementRequest use
//                        promototedParameters_.put(i, b);
//
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBBYTES;
//                        lidAndLengths[i][1] = buildPlaceholderLength(ba.length);
                    }
                    break;
                case Types.JAVA_OBJECT:
                    lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NUDT;
                    lidAndLengths[i][1] = 32767;
                    break;
                case Types.BLOB:
                    throw new UnsupportedOperationException("BLOB");
//                    Blob b = (Blob) inputRow[i];
//                    if (b == null) {
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBBYTES;
//                        lidAndLengths[i][1] =
//                                buildPlaceholderLength(parameterMetaData.sqlLength_[i]);
//                    } else if (b instanceof ClientBlob &&
//                               ((ClientBlob)b).isLocator()) {
//                        //we are sending locators.
//                        //Here the LID local identifier in the FDODSC
//                        //FD:OCA descriptor should be initialized as
//                        //to contain a BLOB locator.
//                        lidAndLengths[i][0] = 
//                                    DRDAConstants.DRDA_TYPE_NLOBLOC;
//                        lidAndLengths[i][1] = 4;
//                    } else {
//                        lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBBYTES;
//                        try {
//                            
//                            if( b instanceof ClientBlob &&
//                                ( (ClientBlob) b).willBeLayerBStreamed() ){
//                                
//                                //Correspond to totalLength 0 as default length for unknown
//                                lidAndLengths[i][1] = 0x8002;
//                                    
//                            }else {
//                                lidAndLengths[i][1] = buildPlaceholderLength(b.length());
//                                
//                            }
//                        } catch (SQLException e) {
//                            throw new SqlException(netAgent_.logWriter_, 
//                                new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH), e);
//                        }
//                    }
//                    break;
                case Types.CLOB:
                    {
                        throw new UnsupportedOperationException("CLOB");
//                        // use columnMeta.singleMixedByteOrDouble_ to decide protocolType
//                        Clob c = (Clob) inputRow[i];
//                        boolean isExternalClob = !(c instanceof ClientClob);
//                        long lobLength = 0;
//                        
//                        boolean doesLayerBStreaming = false;
//                        
//                        if (c == null) {
//                            lobLength = parameterMetaData.sqlLength_[i];
//                        } else if (c instanceof ClientClob &&
//                                   ((ClientClob)c).isLocator()) {
//                            //The inputRow contains an Integer meaning that
//                            //we are sending locators.
//                            //Here the LID local identifier in the FDODSC
//                            //FD:OCA descriptor should be initialized as
//                            //to contain a CLOB locator.
//                            lidAndLengths[i][0] = 
//                                    DRDAConstants.DRDA_TYPE_NCLOBLOC;
//                            lidAndLengths[i][1] = 4;
//                        } else if (isExternalClob) {
//                            try {
//                                lobLength = c.length();
//                            } catch (SQLException e) {
//                                throw new SqlException(netAgent_.logWriter_, 
//                                    new ClientMessageId(SQLState.NET_ERROR_GETTING_BLOB_LENGTH),
//                                    e);
//                            }
//                        } else {
//                            if( ( (ClientClob) c ).willBeLayerBStreamed() ){
//                                doesLayerBStreaming = true;
//                                
//                            }else{
//                                lobLength = ((ClientClob) c).length();
//                                
//                            }
//                            
//                        }
//                        
//                        if (c == null) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCMIXED;
//                            lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                        } else if (isExternalClob) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCDBCS;
//                            lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                        } else if (((ClientClob) c).isCharacterStream()) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCDBCS;
//                            
//                            if( doesLayerBStreaming ) {
//                                
//                                //Correspond to totalLength 0 as default length for unknown
//                                lidAndLengths[i][1] = 0x8002;
//                                
//                            }else {
//                                lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                            }
//                            
//                        } else if (((ClientClob) c).isUnicodeStream()) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCMIXED;
//                            
//                            if( doesLayerBStreaming ) {
//                                
//                                //Correspond to totalLength 0 as default length for unknown
//                                lidAndLengths[i][1] = 0x8002;
//                                
//                            }else {
//                                lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                            }
//                            
//                        } else if (((ClientClob) c).isAsciiStream()) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCSBCS;
//
//                            if( doesLayerBStreaming ) {
//
//                                //Correspond to totalLength 0 as default length for unknown
//                                lidAndLengths[i][1] = 0x8002;
//                                
//                            }else {
//                                lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                            }
//                            
//                        } else if (((ClientClob) c).isString()) {
//                            lidAndLengths[i][0] = DRDAConstants.DRDA_TYPE_NLOBCMIXED;
//                            
//                            if( doesLayerBStreaming ) {
//                                
//                                //Correspond to totalLength 0 as default length for unknown
//                                lidAndLengths[i][1] = 0x8002;
//                                
//                            }else {
//                                lidAndLengths[i][1] = buildPlaceholderLength(lobLength);
//                            }
//                            
//                        }
                    }
//                    break;
                default :
                    throw new IllegalArgumentException("SQLState.UNRECOGNIZED_JAVA_SQL_TYPE: " + jdbcType);
                }

                if (!parameterMetaData.nullable_[i]) {
                    lidAndLengths[i][0]--;
                }
            }
            return overrideMap;
//        }
//        catch ( SQLException se )
//        {
//            throw new SqlException(se);
//        }
    }
    
    // returns the a promototedParameter object for index or null if it does not exist
    private Object retrievePromotedParameterIfExists(int index) {
        if (promototedParameters_.isEmpty()) {
            return null;
        }
        return promototedParameters_.get(index);
    }
    
    // helper method to buildFDODTA to build the actual data length
    private void setFDODTALobLength(int[][] protocolTypesAndLengths, int i, long dataLength) {
        if (protocolTypesAndLengths[i][1] == 0x8002) {
            buffer.writeShort((short) dataLength);
        } else if (protocolTypesAndLengths[i][1] == 0x8004) {
            buffer.writeInt((int) dataLength);  // 4 bytes to encode the length
        } else if (protocolTypesAndLengths[i][1] == 0x8006) {
            writeLong6Bytes(dataLength); // 6 bytes to encode the length
        } else if (protocolTypesAndLengths[i][1] == 0x8008) {
            buffer.writeLong(dataLength); // 8 bytes to encode the length
        }

        if (dataLength != 0) {
            if (extdtaPositions_ == null) {
                extdtaPositions_ = new ArrayList<Integer>();
            }
            extdtaPositions_.add(i);
        }
    }
    
    private void setFDODTALobLengthUnknown(int i) {
        short v = 1;
        buffer.writeShort( v <<= 15 );
        if (extdtaPositions_ == null) {
            extdtaPositions_ = new ArrayList<Integer>();
        }
        
        extdtaPositions_.add(i);
    }

    
    // Write the message to open a bound or prepared query without input parameters.
    // Check this-> For open query without input parameters
    public void writeOpenQuery(Section section,
                               String dbName,
                               int fetchSize,
                               int resultSetType) {
        boolean sendQryrowset = checkSendQryrowset(fetchSize, resultSetType);
        fetchSize = checkFetchsize(fetchSize, resultSetType);

        // think about if there is a way we can call build ddm just passing ddm parameters and not passing the material ps object
        // maybe not, if sometimes we need to set the caches hanging off the ps object during the ddm build
        // maybe we can extricate conditionals in the build ddm logic outside
        buildOPNQRY(section, dbName, sendQryrowset, fetchSize);


        // may be able to merge this with firstContinueQuery_ and push above conditional to common
        // @AGG don't think we need this
//        ((NetStatement) materialStatement).qryrowsetSentOnOpnqry_ = sendQryrowset; // net-specific event
    }
    
    // Write the message to execute  prepared sql statement.
    //
    // preconditions:
    public void writeExecute(Section section,
    						 String dbName,
                             ColumnMetaData parameterMetaData,
                             Object[] inputs,
                             int numInputColumns,
                             boolean outputExpected,
                             boolean chained)
    {
        buildEXCSQLSTT(section,
        		dbName,
                true, // sendOutexp
                outputExpected, // outexp
                false, // sendPrcnam
                null, // prcnam
                false, // sendQryblksz
                false, // sendMaxrslcnt,
                0, // maxrslcnt,
                false, // sendMaxblkext
                0, // maxblkext
                false, // sendRslsetflg
                0, // resultSetFlag
                false, // sendQryrowset
                0);               // qryrowset

        if (numInputColumns > 0) {
            if ((extdtaPositions_ != null) && (!extdtaPositions_.isEmpty())) {
                extdtaPositions_.clear();  // reset extdta column position markers
            }

            boolean overrideExists = buildSQLDTAcommandData(numInputColumns,
                    parameterMetaData,
                    inputs);

            // can we eleminate the chain argument needed for lobs
            buildEXTDTA(parameterMetaData, inputs, chained);
        }
    }
    
    // Write the message to perform an execute immediate.
    // The SQL statement sent as command data cannot contain references
    // to either input variables or output variables.
    //
    // preconditions:
    public void writeExecuteImmediate(String sql, Section section, String dbName) {
        buildEXCSQLIMM(section,
                dbName,
                false, //sendQryinsid
                0);                        //qryinsid
        buildSQLSTTcommandData(sql); // statement follows in sqlstt command data object
    }
    
    // Build the Execute Immediate SQL Statement Command to
    // execute a non-cursor SQL statement sent as command data.
    //
    // precondtions:
    private void buildEXCSQLIMM(Section section, String dbName, boolean sendQryinsid, long qryinsid) {
        createCommand();
        markLengthBytes(CodePoint.EXCSQLIMM);

        buildPKGNAMCSN(dbName, section);
        buildRDBCMTOK();
        if (sendQryinsid) {
            buildQRYINSID(qryinsid);
        }

        updateLengthBytes();
    }
    
    // RDB Commit Allowed specifies whether an RDB should allow the processing of any
    // commit or rollback operations that occure during execution of a statement.
    // True allow the processing of commits and rollbacks
    private void buildRDBCMTOK() {
        writeScalar1Byte(CodePoint.RDBCMTOK, CodePoint.TRUE);
    }
    
    public void buildCLSQRY(Section section, String dbName, long queryInstanceIdentifier) {
        createCommand();
        markLengthBytes(CodePoint.CLSQRY);
        buildPKGNAMCSN(dbName, section);
        buildQRYINSID(queryInstanceIdentifier);
        updateLengthBytes();
    }
    
    void buildQRYINSID(long qryinsid) {
        markLengthBytes(CodePoint.QRYINSID);
        buffer.writeLong(qryinsid);
        updateLengthBytes();
    }
    
    //----------------------helper methods----------------------------------------
    // These methods are "private protected", which is not a recognized java privilege,
    // but means that these methods are private to this class and to subclasses,
    // and should not be used as package-wide friendly methods.

    // Build the Open Query Command to open a query to a relational database.
    // At SQLAM >= 7 we can request the return of a DA, are there
    // scenarios where this should currently be done (it is not supported now)
    //
    // preconditions:
    //   the sqlam and/or prdid must support command and parameters passed to this method,
    //   method will not validate against the connection's level of support
    //
    private void buildOPNQRY(Section section,
            String dbName,
                     boolean sendQueryRowSet,
                     int fetchSize) {
        createCommand();
        markLengthBytes(CodePoint.OPNQRY);

        buildPKGNAMCSN(dbName, section);
        buildQRYBLKSZ();  // specify a hard coded query block size

        if (sendQueryRowSet) {
            buildMAXBLKEXT(-1);
            buildQRYROWSET(fetchSize);
        }

        // Tell the server to close forward-only result sets
        // implicitly when they are exhausted. The server will ignore
        // this parameter if the result set is scrollable.
        // @AGG assume server supports this
//        if (netAgent_.netConnection_.serverSupportsQryclsimp()) {
            buildQRYCLSIMP();
//        }

        updateLengthBytes();  // opnqry is complete
    }
    
    /**
     * Build QRYCLSIMP (Query Close Implicit). Query Close Implicit
     * controls whether the target server implicitly closes a
     * non-scrollable query upon end of data (SQLSTATE 02000).
     */
    private void buildQRYCLSIMP() {
        writeScalar1Byte(CodePoint.QRYCLSIMP, CodePoint.QRYCLSIMP_YES);
    }
    
    // Maximum Number of Extra Blocks specifies a limit on the number of extra
    // blocks of answer set data per result set that the requester is capable of
    // receiveing.
    // this value must be able to be contained in a two byte signed number.
    // there is a minimum value of 0.
    // a zero indicates that the requester is not capable of receiving extra
    // query blocks of answer set data.
    // there is a SPCVAL of -1.
    // a value of -1 indicates that the requester is capable of receiving
    // the entire result set.
    //
    // preconditions:
    //   sqlam must support this parameter on the command, method will not check.
    void buildMAXBLKEXT(int maxNumOfExtraBlocks) {
        if (maxNumOfExtraBlocks != 0) {
            writeScalar2Bytes(CodePoint.MAXBLKEXT, maxNumOfExtraBlocks);
        }
    }
    
    void buildQRYROWSET(int fetchSize) {
        writeScalar4Bytes(CodePoint.QRYROWSET, fetchSize);
    }
    
    // Query Block Size specifies the query block size for the reply
    // data objects and the reply messages being returned from this command.
    // this is a 4 byte unsigned binary number.
    // the sqlam 6 min value is 512 and max value is 32767.
    // this value was increased in later sqlam levels.
    // until the code is ready to support larger query block sizes,
    // it will always use DssConstants.MAX_DSS_LEN which is 32767.
    //
    // preconditions:
    //   sqlam must support this parameter for the command, method will not check.
    void buildQRYBLKSZ() {
        writeScalar4Bytes(CodePoint.QRYBLKSZ, DssConstants.MAX_DSS_LENGTH);
    }
    
    private int checkFetchsize(int fetchSize, int resultSetType) {
        // if fetchSize is not set for scrollable cursors, set it to the default fetchSize
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY && fetchSize == 0) {
            fetchSize = defaultFetchSize;
        }
        return fetchSize;
    }
    
    private boolean checkSendQryrowset(int fetchSize, int resultSetType) {
        // if the cursor is forward_only, ignore the fetchSize and let the server return
        // as many rows as fit in the query block.
        // if the cursor is scrollable, send qryrowset if it is supported by the server
        boolean sendQryrowset = false;
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            sendQryrowset = true;
        }
        return sendQryrowset;
    }
    
    protected void buildSQLSTTcommandData(String sql) {
        createEncryptedCommandData();
//        int loc = buffer.position();
        markLengthBytes(CodePoint.SQLSTT);
        buildSQLSTT(sql);
        updateLengthBytes();
        // @AGG encryption
//        if (netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRIDDTA ||
//                netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRPWDDTA) {
//            encryptDataStream(loc);
//        }
    }
    
    protected void buildSQLATTRcommandData(String sql) {
        createEncryptedCommandData();
//        int loc = buffer.position();
        markLengthBytes(CodePoint.SQLATTR);
        buildSQLSTT(sql);
        updateLengthBytes();
        // TODO @AGG encryption
//        if (netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRIDDTA ||
//                netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRPWDDTA) {
//            encryptDataStream(loc);
//        }

    }
    
    private void buildNOCMorNOCS(String string) {
        if (string == null) {
            buffer.writeShort(0xffff);
//            write2Bytes(0xffff);
        } else {
            if (Typdef.typdef.isCcsidMbcSet()) {
            byte[] sqlBytes = string.getBytes(Typdef.typdef.getCcsidMbcEncoding());
            buffer.writeByte(0x00);
//                write1Byte(0x00);
            buffer.writeInt(sqlBytes.length);
//                write4Bytes(sqlBytes.length);
            buffer.writeBytes(sqlBytes);
//                writeBytes(sqlBytes, sqlBytes.length);
            buffer.writeByte(0xff);
//                write1Byte(0xff);
            } else {
                byte[] sqlBytes = string.getBytes(Typdef.typdef.getCcsidSbcEncoding());
                buffer.writeByte(0xff);
                buffer.writeByte(0x00);
//                write1Byte(0xff);
//                write1Byte(0x00);
                buffer.writeInt(sqlBytes.length);
//                write4Bytes(sqlBytes.length);
                buffer.writeBytes(sqlBytes);
//                writeBytes(sqlBytes, sqlBytes.length);
//            }
            }
        }
    }

    // SQLSTTGRP : FDOCA EARLY GROUP
    // SQL Statement Group Description
    //
    // FORMAT FOR SQLAM <= 6
    //   SQLSTATEMENT_m; PROTOCOL TYPE LVCM; ENVLID 0x40; Length Override 32767
    //   SQLSTATEMENT_s; PROTOCOL TYPE LVCS; ENVLID 0x34; Length Override 32767
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLSTATEMENT_m; PROTOCOL TYPE NOCM; ENVLID 0xCF; Length Override 4
    //   SQLSTATEMENT_s; PROTOCOL TYPE NOCS; ENVLID 0xCB; Length Override 4
    private void buildSQLSTTGRP(String string) {
        buildNOCMorNOCS(string);
    }

    // SQLSTT : FDOCA EARLY ROW
    // SQL Statement Row Description
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLSTTGRP; GROUP LID 0x5C; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void buildSQLSTT(String string) {
        buildSQLSTTGRP(string);
    }
    
    // Build the Prepare SQL Statement Command to dynamically binds an
    // SQL statement to a section in an existing relational database (RDB) package.
    //
    // preconditions:
    //   the sqlam and/or prdid must support command and parameters passed to this method,
    //   method will not validate against the connection's level of support
    private void buildPRPSQLSTT(Section section,
                        String sql,
                        String dbName,
                        boolean sendRtnsqlda,
                        boolean sendTypsqlda,
                        int typsqlda) {
        createCommand();
        markLengthBytes(CodePoint.PRPSQLSTT);

        buildPKGNAMCSN(dbName, section);
        if (sendRtnsqlda) {
            buildRTNSQLDA();
        }
        if (sendTypsqlda) {
            buildTYPSQLDA(typsqlda);
        }

        updateLengthBytes();
    }
    
    // Type of SQL Descriptor Area.
    // This is a single byte signed number that specifies the type of sqlda to
    // return for the command.
    // below sqlam 7 there were two possible enumerated values for this parameter.
    // 0 (CodePoint.TYPSQLDA_STD_OUTPUT)- the default, indicates return the output sqlda.
    // 1 (CodePoint.TYPSQLDA_STD_INPUT) - indicates return the input sqlda.
    // the typsqlda was enhanced at sqlam 7 to support extened describe.
    // at sqlam 7 the following enumerated values are supported.
    // 0 (CodePoint.TYPSQLDA_STD_OUTPUT) - the default, standard output sqlda.
    // 1 (CodePoint.TYPSQLDA_STD_INPUT) - standard input sqlda.
    // 2 (CodePoint.TYPSQLDA_LIGHT_OUTPUT) - light output sqlda.
    // 3 (CodePoint.TYPSQLDA_LIGHT_INPUT) - light input sqlda.
    // 4 (CodePoint.TYPSQLDA_X_OUTPUT) - extended output sqlda.
    // 5 (CodePoint.TYPSQLDA_X_INPUT) - extended input sqlda.
    //
    // preconditions:
    //   sqlam or prdid must support this, method will not check.
    //   valid enumerated type must be passed to method, method will not check.
    private void buildTYPSQLDA(int typeSqlda) {
        // possibly inspect typeSqlda value and verify against sqlam level
        if (typeSqlda != CodePoint.TYPSQLDA_STD_OUTPUT) {
            writeScalar1Byte(CodePoint.TYPSQLDA, typeSqlda);
        }
    }
    
    // Return SQL Descriptor Area controls whether to return
    // an SQL descriptor area that applies to the SQL statement this command
    // identifies.  The target SQLAM obtains the SQL descriptor area by performing
    // an SQL DESCRIBE function on the statement after the statement has been
    // prepared.
    // The value TRUE, X'F1' (CodePoint.TRUE), indicates an SQLDA is returned
    // The value FALSE, X'F0' (CodePoint.FALSE), default, indicates an SQLDA is not returned.
    //
    // preconditions:
    //   sqlam must support this parameter for the command, method will not check.
    private void buildRTNSQLDA() {
        writeScalar1Byte(CodePoint.RTNSQLDA, CodePoint.TRUE);
    }
    
    // this specifies the fully qualified package name,
    // consistency token, and section number within the package being used
    // to execute the SQL.  If the connection supports reusing the previous
    // package information and this information is the same except for the section
    // number then only the section number needs to be sent to the server.
    void buildPKGNAMCSN(String dbName, Section section) {
//        if (!canCommandUseDefaultPKGNAMCSN()) { // Always true in derby code
            markLengthBytes(CodePoint.PKGNAMCSN);
            // If PKGNAMCBytes is already available, copy the bytes to the request buffer directly.
            if (section.getPKGNAMCBytes() != null) {
                writeStoredPKGNAMCBytes(section);
            } else {
                // Mark the beginning of PKGNAMCSN bytes.
                markForCachingPKGNAMCSN();
                buildCommonPKGNAMinfo(dbName, section);
                writeScalarPaddedBytes(
                        CCSIDManager.UTF8.encode("SYSLVL01").array(),
                        DRDAConstants.PKGCNSTKN_FIXED_LEN,
                        DRDAConstants.NON_CHAR_DDM_DATA_PAD_BYTE);
                // store the PKGNAMCbytes
                storePKGNAMCBytes(section);
            }
            buffer.writeShort(section.getSectionNumber());
            //write2Bytes(section.getSectionNumber());
            updateLengthBytes();
//        } else {
//            writeScalar2Bytes(CodePoint.PKGSN, section.getSectionNumber());
//        }
    }
    
    private void storePKGNAMCBytes(Section section) {
        // Get the locaton where we started writing PKGNAMCSN
        int startPos = popMarkForCachingPKGNAMCSN();
        byte[] b = new byte[buffer.writerIndex() - startPos];
        buffer.getBytes(startPos, b);
//        buffer.position(startPos);
//        buffer.get(b);
        section.setPKGNAMCBytes(b);
    }
    
    private void writeStoredPKGNAMCBytes(Section section) {
        buffer.writeBytes(section.getPKGNAMCBytes());
//        writeBytes(section.getPKGNAMCBytes());
    }
    
    // mark an offest into the buffer by placing the current offset value on
    // a stack.
    private final void mark() {
        markStack.push(buffer.writerIndex());
//        markStack_[top_++] = buffer.position();
    }

    // remove and return the top offset value from mark stack.
    private final int popMark() {
        return markStack.pop();
//        return markStack_[--top_];
    }

    protected final void markForCachingPKGNAMCSN() {
        mark();
    }

    protected final int popMarkForCachingPKGNAMCSN() {
        return popMark();
    }
    
    private void buildCommonPKGNAMinfo(String dbName, Section section) {
        String collectionToFlow = COLLECTIONNAME;
        // the scalar data length field may or may not be required.  it depends
        // on the level of support and length of the data.
        // check the lengths of the RDBNAM, RDBCOLID, and PKGID.
        // Determine if the lengths require an SCLDTALEN object.
        // Note: if an SQLDTALEN is required for ONE of them,
        // it is needed for ALL of them.  This is why this check is
        // up front.
        // the SQLAM level dictates the maximum size for
        // RDB Collection Identifier (RDBCOLID)
        // Relational Database Name (RDBNAM)
        // RDB Package Identifier (PKGID)
        int maxIdentifierLength = DRDAConstants.PKG_IDENTIFIER_MAX_LEN;
//        CcsidManager ccsidMgr = netAgent_.getCurrentCcsidManager();

        byte[] dbnameBytes = ccsidManager.getCCSID().encode(dbName).array(); 
//                ccsidMgr.convertFromJavaString(
//                netAgent_.netConnection_.databaseName_, netAgent_);

        byte[] collectionToFlowBytes = ccsidManager.getCCSID().encode(collectionToFlow).array(); 
//        ccsidMgr.convertFromJavaString(
//                collectionToFlow, netAgent_);

        byte[] pkgNameBytes = ccsidManager.getCCSID().encode(section.getPackageName()).array(); 
//                ccsidMgr.convertFromJavaString(
//                section.getPackageName(), netAgent_);
        
        //Maximum RDBNAM length would depend on the server level. 
        // Server under 10.11 support only 255 bytes but server
        // at 10.11 and higher support higher limit of 1024
        boolean scldtalenRequired =
                checkPKGNAMlengths(dbName, //netAgent_.netConnection_.databaseName_,
                dbnameBytes.length,
                DRDAConstants.RDBNAM_MAX_LEN,
                DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
//                (netAgent_.netConnection_.databaseMetaData_.serverSupportLongRDBNAM())? 
//                        DRDAConstants.RDBNAM_MAX_LEN 
//                        : DRDAConstants.PKG_IDENTIFIER_MAX_LEN,  
//                        DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);

        if (!scldtalenRequired) {
            scldtalenRequired = checkPKGNAMlengths(collectionToFlow,
                    collectionToFlowBytes.length,
                    maxIdentifierLength,
                    DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
        }

        if (!scldtalenRequired) {
            scldtalenRequired = checkPKGNAMlengths(section.getPackageName(),
                    pkgNameBytes.length,
                    maxIdentifierLength,
                    DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
        }

        // the format is different depending on if an SCLDTALEN is required.
        if (!scldtalenRequired) {
            byte padByte = ccsidManager.getCCSID().encode(" ").get();
            writeScalarPaddedBytes(dbnameBytes,
                    DRDAConstants.PKG_IDENTIFIER_FIXED_LEN, padByte);
            writeScalarPaddedBytes(collectionToFlowBytes,
                    DRDAConstants.PKG_IDENTIFIER_FIXED_LEN, padByte);
            writeScalarPaddedBytes(pkgNameBytes,
                    DRDAConstants.PKG_IDENTIFIER_FIXED_LEN, padByte);
        } else {
            buildSCLDTA(dbnameBytes, DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
            buildSCLDTA(collectionToFlowBytes, DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
            buildSCLDTA(pkgNameBytes, DRDAConstants.PKG_IDENTIFIER_FIXED_LEN);
        }
    }
    
    private void buildSCLDTA(byte[] identifier, int minimumLength) {
        int length = Math.max(minimumLength, identifier.length);
        buffer.writeShort(length);
        //write2Bytes(length);
        //byte padByte = netAgent_.getCurrentCcsidManager().space_;
        byte padByte = ccsidManager.getCCSID().encode(" ").get();
        writeScalarPaddedBytes(identifier, length, padByte);
    }
    
    // throws an exception if lengths exceed the maximum.
    // returns a boolean indicating if SLCDTALEN is required.
    private boolean checkPKGNAMlengths(String identifier,
                                       int length,
                                       int maxIdentifierLength,
                                       int lengthRequiringScldta) {
        if (length > maxIdentifierLength) {
            throw new IllegalArgumentException("SQLState.LANG_IDENTIFIER_TOO_LONG " + length);
//            throw new SqlException(netAgent_.logWriter_,
//                new ClientMessageId(SQLState.LANG_IDENTIFIER_TOO_LONG),
//                identifier, maxIdentifierLength);
        }

        return (length > lengthRequiringScldta);
    }

}
