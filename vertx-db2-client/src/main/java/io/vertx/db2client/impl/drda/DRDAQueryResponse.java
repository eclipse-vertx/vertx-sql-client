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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;

public class DRDAQueryResponse extends DRDAConnectResponse {
    
//    protected boolean ensuredLengthForDecryption_ = false; // A layer lengths have already been ensured in decrypt method.
//    protected byte[] longBufferForDecryption_ = null;
//    protected int longPosForDecryption_ = 0;
//    protected ByteBuf longValueForDecryption_ = null;
//    protected int longCountForDecryption_ = 0;
    
    private ColumnMetaData outputColumnMetaData = null;
    private ColumnMetaData inputColumnMetaData = null;
    
    private Cursor cursor;
    
    private NetSqlca sqlca;
    
    private long queryInstanceId = 0;
    
    public DRDAQueryResponse(ByteBuf buffer, ConnectionMetaData metadata) {
        super(buffer, metadata);
    }
    
    public void readPrepareDescribeOutput() { // @AGG removed callback StatementCallbackInterface statement) {
        startSameIdChainParse();
        parsePRPSQLSTTreply(); // @AGG removed callback statement);
        endOfSameIdChainData();
    }
    
    public void readPrepareDescribeInputOutput() {
        readPrepareDescribeOutput();
        readDescribeInput();
        completePrepareDescribe();
    }
    
    private void completePrepareDescribe() {
        if (outputColumnMetaData == null) {
            return;
        }
        // TODO: @AGG move this stuff to non-DRDA class
//        parameters_ = expandObjectArray(parameters_, parameterMetaData_.columns_);
//        parameterSet_ = expandBooleanArray(parameterSet_, parameterMetaData_.columns_);
//        parameterRegistered_ = expandBooleanArray(parameterRegistered_, parameterMetaData_.columns_);
    }
    

    public void readDescribeInput(/*PreparedStatementCallbackInterface preparedStatement*/) {
        // @AGG no encryption
//        if (longBufferForDecryption_ != null) {
//            buffer_ = longBufferForDecryption_;
//            pos_ = longPosForDecryption_;
//            count_ = longCountForDecryption_;
//            if (longBufferForDecryption_ != null && count_ > longBufferForDecryption_.length) {
//                count_ = longBufferForDecryption_.length;
//            }
//            dssLength_ = 0;
//            longBufferForDecryption_ = null;
//        }

        startSameIdChainParse();
        parseDSCSQLSTTreply(/*preparedStatement, */false);
        endOfSameIdChainData();
    }
    
    // Parse the reply for the Describe SQL Statement Command.
    // This method handles the parsing of all command replies and reply data
    // for the dscsqlstt command.
    private void parseDSCSQLSTTreply(boolean isOutput) {
        int peekCP = parseTypdefsOrMgrlvlovrs();

        ColumnMetaData columnMetaData = new ColumnMetaData();
        if (isOutput)
            outputColumnMetaData = columnMetaData;
        else
            inputColumnMetaData = columnMetaData;
        
        if (peekCP == CodePoint.SQLDARD) {
            // SQLDARD here means we have some data to parse
            NetSqlca netSqlca = parseSQLDARD(columnMetaData, false);  // false means do not skip SQLDARD bytes
            NetSqlca.complete(netSqlca);
        } else if (peekCP == CodePoint.SQLCARD) {
            // SQLCARD here means there was no data to parse, just a return code
            NetSqlca netSqlca = parseSQLCARD(null);
            NetSqlca.complete(netSqlca);
        } else {
            throw new IllegalStateException("Parse describe error");
            //parseDescribeError(ps);
        }
    }
    
    @Deprecated // @AGG reads all data sync
    public void readOpenQuery() { // @AGG removed callback StatementCallbackInterface statement) {
        startSameIdChainParse();
        parseOPNQRYreply(); // @AGG removed callback statement);
        endOfSameIdChainData();
    }
    
    /**
     * Parse the reply for the Open Query Command. This method handles the
     * parsing of all command replies and reply data for the opnqry command.
     */
    public void readBeginOpenQuery() { // @AGG removed callback StatementCallbackInterface statementI) {
    	startSameIdChainParse();
        int peekCP = peekCodePoint();

        if (peekCP == CodePoint.OPNQRYRM) {
        	parseBeginOpenQuery();
//            parseOpenQuery(); // @AGG removed callback statementI);
//            peekCP = peekCodePoint();
//            if (peekCP == CodePoint.RDBUPDRM) {
//                parseRDBUPDRM();
//                peekCP = peekCodePoint();
//            }
        } else if (peekCP == CodePoint.RDBUPDRM) {
            parseRDBUPDRM();
            parseBeginOpenQuery();
//            parseOpenQuery(); // @AGG removed callback statementI);
//            peekCP = peekCodePoint();
        } else if (peekCP == CodePoint.OPNQFLRM) {
            parseOpenQueryFailure(); // @AGG removed callback statementI);
            //peekCP = peekCodePoint();
        } else {
            parseOpenQueryError(); // @AGG removed callback statementI);
            //peekCP = peekCodePoint();
        }

//        if (peekCP == CodePoint.PBSD) {
//            parsePBSD();
//        }
    }
    
    /**
     * Reads the following items:
     * - Open Query Complete (OPNQRYRM)
     * - Query Answer Set Description (QRYDSC)
     * Does NOT parse result data
     */
    private void parseBeginOpenQuery() {
        int peekCP = peekCodePoint();
        
        if (peekCP == CodePoint.OPNQRYRM) {
            parseOPNQRYRM(true);

            //NetSqlca sqlca = null;
            peekCP = peekCodePoint();
            if (peekCP != CodePoint.QRYDSC) {

                peekCP = parseTypdefsOrMgrlvlovrs();

                if (peekCP == CodePoint.SQLDARD) {
                    outputColumnMetaData = new ColumnMetaData();// ClientDriver.getFactory().newColumnMetaData(netAgent_.logWriter_);
                    NetSqlca netSqlca = parseSQLDARD(outputColumnMetaData, false);  // false means do not skip SQLDARD bytes

                    //For java stored procedure, we got the resultSetMetaData from server,
                    //Do we need to save the resultSetMetaData and propagate netSqlca?
                    //The following statement are doing the both, but it do more than
                    //we want. It also mark the completion of Prepare statement.
                    //
                    // this will override the same call made from parsePrepareDescribe
                    //  this will not work, this is not the DA for the stored proc params
                    //
                    // We may now receive a new SQLDARD (unrequested, a
                    // DRDA protocol extension) when a query is opened iff the
                    // underlying server's prepared statement has been recompiled
                    // since the client first received metadata when preparing the
                    // statement.
                    if (netSqlca != null) {
                        NetSqlca.complete(netSqlca);
                    }
                    peekCP = parseTypdefsOrMgrlvlovrs();
                }
                // check if the DARD is mutually exclusive with CARD, if so, then the following if should be an elese

                if (peekCP == CodePoint.SQLCARD) {
                    sqlca = parseSQLCARD(null);
                    peekCP = parseTypdefsOrMgrlvlovrs();
                }
            }
            parseQRYDSC(/*netResultSet.netCursor_*/);
        }
    }
    
    public void readFetch(Cursor c) {
        startSameIdChainParse();
        
        // Carry over state from previous request
        cursor = c;
        cursor.resetDataBuffer();
//        if (cursor != null)
//            throw new IllegalStateException("Invoking readFetch on a request that has already created a cursor");
//        cursor = new Cursor();
////        setOutputColumnMetaData(columnMetadata);
//        cursor.initializeColumnInfoArrays(Typdef.targetTypdef, columnMetadata.columns_);
//        
//        cursor.qrydscTypdef_.updateColumn(cursor, i, readFastUnsignedByte(), readFastUnsignedShort());
//        Typdef.targetTypdef.updateColumn(netCursor, columnIndex, protocolLid, protocolLength);
        
        parseCNTQRYreply(true); // true means we expect row data
        endOfSameIdChainData();
    }
    
    public void readCursorClose() {
        startSameIdChainParse();
        parseCLSQRYreply();
        endOfSameIdChainData();
    }
    
    public long readExecuteImmediate() {
        startSameIdChainParse();
        long updateCount = parseEXCSQLIMMreply();
        endOfSameIdChainData();
        return updateCount;
    }
    
    public long readExecute(/*PreparedStatementCallbackInterface preparedStatement*/) {
        startSameIdChainParse();
        long updateCount = parseEXCSQLSTTreply();//preparedStatement);
        endOfSameIdChainData();
        return updateCount;
    }
    
    // Parse the reply for the Continue Query Command.
    // This method handles the parsing of all command replies and reply data for the cntqry command.
    // If doCopyQrydta==false, then there is no data, and we're only parsing out the sqlca to get the row count.
    private void parseCNTQRYreply(/*ResultSetCallbackInterface resultSetI, */
                                  boolean doCopyQrydta) {
        boolean found = false;
        int peekCP = peekCodePoint();
        if (peekCP == CodePoint.RDBUPDRM) {
            found = true;
            parseRDBUPDRM();
            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.QRYDTA) {
            found = true;
            if (!doCopyQrydta) {
                throw new UnsupportedOperationException("Not implemented");
//                parseLengthAndMatchCodePoint(CodePoint.QRYDTA);
//                //we don't need to copy QRYDTA since there is no data
//                if (longValueForDecryption_ != null) {
//                    longValueForDecryption_ = null;
//                }
//                if (longBufferForDecryption_ != null) {
//                    longBufferForDecryption_ = null;
//                }
//
//                int ddmLength = getDdmLength();
//                ensureBLayerDataInBuffer(ddmLength);
//                ((ClientResultSet) resultSetI).expandRowsetSqlca();
//                NetSqlca sqlca = parseSQLCARDrow(/*((ClientResultSet) resultSetI).rowsetSqlca_*/);
//                int daNullIndicator = readFastByte();
//                adjustLengths(getDdmLength());
//                // define event interface and use the event method
//                // only get the rowCount_ if sqlca is not null and rowCount_ is unknown
//                if (sqlca != null && sqlca.containsSqlcax()) {
//                    ((ClientResultSet)resultSetI).setRowCountEvent(
//                        sqlca.getRowCount());
//                }
//
//                peekCP = peekCodePoint();
//                if (peekCP == CodePoint.SQLCARD) {
//                    NetSqlca netSqlca = parseSQLCARD(((ClientResultSet) resultSetI).rowsetSqlca_);
//                    resultSetI.completeSqlca(netSqlca);
//                }
//                if (peekCP == CodePoint.RDBUPDRM) {
//                    parseRDBUPDRM();
//                    peekCP = peekCodePoint();
//                }
//                if (peekCP == CodePoint.PBSD) {
//                    parsePBSD();
//                }
//                return;
            }
            do {
                parseQRYDTA();
                peekCP = peekCodePoint();
            } while (peekCP == CodePoint.QRYDTA);
        }

        if (peekCP == CodePoint.EXTDTA) {
            found = true;
            throw new UnsupportedOperationException("Not implemented");
//            do {
//                copyEXTDTA((NetCursor) ((ClientResultSet) resultSetI).cursor_);
//                if (longBufferForDecryption_ != null) {//encrypted EXTDTA
//                    buffer_ = longBufferForDecryption_;
//                    pos_ = longPosForDecryption_;
//                    if (longBufferForDecryption_ != null && count_ > longBufferForDecryption_.length) {
//                        count_ = longBufferForDecryption_.length;
//                    }
//                }
//
//                peekCP = peekCodePoint();
//            } while (peekCP == CodePoint.EXTDTA);
        }

        if (peekCP == CodePoint.SQLCARD) {
            found = true;
            throw new UnsupportedOperationException("Read SQLCARD on fetch");
//            ((ClientResultSet) resultSetI).expandRowsetSqlca();
//            NetSqlca netSqlca = parseSQLCARD(((ClientResultSet)resultSetI).rowsetSqlca_);
//            // for an atomic operation, the SQLCA contains the sqlcode for the first (statement
//            // terminating)error, the last warning, or zero.  all multi-row fetch operatons are
//            // atomic.  (the only operation that is not atomic is multi-row insert).
//            if (((ClientResultSet)resultSetI).sensitivity_ !=
//                ClientResultSet.sensitivity_sensitive_dynamic__) {
//
//                if (netSqlca != null && netSqlca.containsSqlcax() && netSqlca.getRowsetRowCount() == 0) {
//                    ((ClientResultSet)resultSetI).setRowCountEvent(
//                        netSqlca.getRowCount());
//                }
//            }
//            resultSetI.completeSqlca(netSqlca);
//            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.ENDQRYRM) {
            found = true;
            parseEndQuery();
            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.RDBUPDRM) {
            found = true;
            parseRDBUPDRM();
            peekCP = peekCodePoint();
        }

        if (!found) {
            parseFetchError();
        }

        if (peekCP == CodePoint.PBSD) {
            parsePBSD();
        }

        // TODO encryption
//        if (longBufferForDecryption_ != null) {
//            // Not a good idea to create a new buffer_
//            buffer_ = new byte[DEFAULT_BUFFER_SIZE];
//            longBufferForDecryption_ = null;
//        }
    }
    
    private void parseFetchError() {

        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM:
            {
                //passing the ResultSetCallbackInterface implementation will
                //help in retrieving the the UnitOfWorkListener that needs to
                //be rolled back 
                throw new IllegalStateException("Abnormal UOW end");
//                NetSqlca sqlca = parseAbnormalEndUow(resultSetI);
//                resultSetI.completeSqlca(sqlca);
//                break;
            }
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.CMDNSPRM:
            parseCMDNSPRM();
            break;
        case CodePoint.QRYNOPRM:
            parseQRYNOPRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        default:
            parseCommonError(peekCP);
        }
    }
    
    // Query Not Opened Reply Message is issued if a CNTQRY or CLSQRY
    // command is issued for a query that is not open.  A previous
    // ENDQRYRM, ENDUOWRM, or ABNUOWRM reply message might have
    // terminated the command.
    // PROTOCOL architects the SQLSTATE value depending on SVRCOD
    // SVRCOD 4 -> SQLSTATE is 24501
    // SVRCOD 8 -> SQLSTATE of 58008 or 58009
    //
    // if SVRCOD is 4 then SQLSTATE 24501, SQLCODE -501
    // else SQLSTATE 58009, SQLCODE -30020
    //
    // Messages
    // SQLSTATE : 24501
    //     The identified cursor is not open.
    //     SQLCODE : -501
    //     The cursor specified in a FETCH or CLOSE statement is not open.
    //     The statement cannot be processed.
    // SQLSTATE : 58009
    //     Execution failed due to a distribution protocol error that caused deallocation of the conversation.
    //     SQLCODE : -30020
    //     Execution failed because of a Distributed Protocol
    //         Error that will affect the successful execution of subsequent
    //         commands and SQL statements: Reason Code <reason-code>.
    //     Some possible reason codes include:
    //     121C Indicates that the user is not authorized to perform the requested command.
    //     1232 The command could not be completed because of a permanent error.
    //         In most cases, the server will be in the process of an abend.
    //     220A The target server has received an invalid data description.
    //         If a user SQLDA is specified, ensure that the fields are
    //         initialized correctly. Also, ensure that the length does not exceed
    //         the maximum allowed length for the data type being used.
    //
    //     The command or statement cannot be processed.  The current
    //         transaction is rolled back and the application is disconnected
    //         from the remote database.
    //
    // Returned from Server:
    // SVRCOD - required  (4 - WARNING, 8 - ERROR)
    // RDBNAM - required
    // PKGNAMCSN - required
    //
    private void parseQRYNOPRM(/*ResultSetCallbackInterface resultSet*/) {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;
        boolean pkgnamcsnReceived = false;
        Object pkgnamcsn = null;

        parseLengthAndMatchCodePoint(CodePoint.QRYNOPRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_WARNING, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.PKGNAMCSN) {
                foundInPass = true;
                pkgnamcsnReceived = checkAndGetReceivedFlag(pkgnamcsnReceived);
                pkgnamcsn = parsePKGNAMCSN(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);
        if (!pkgnamcsnReceived)
            throwMissingRequiredCodepoint("PKGNAMCSN", CodePoint.PKGNAMCSN);

//        netAgent_.setSvrcod(svrcod);
        if (svrcod == CodePoint.SVRCOD_WARNING) {
//            netAgent_.accumulateReadException(new SqlException(netAgent_.logWriter_,
//                new ClientMessageId(SQLState.DRDA_CURSOR_NOT_OPEN)));
            throw new IllegalStateException("DRDA_CURSOR_NOT_OPEN");
        } else {
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                    SqlException.getMessageUtil().
//                    getTextMessage(MessageId.CONN_CURSOR_NOT_OPEN)));
            throw new IllegalStateException("DRDA_CURSOR_NOT_OPEN");
        }
    }
    
    // Parse the reply for the Execute SQL Statement Command.
    // This method handles the parsing of all command replies and reply data
    // for the excsqlstt command.
    // Also called by ClientCallableStatement.readExecuteCall()
    private long parseEXCSQLSTTreply(/*StatementCallbackInterface statementI*/) {
        // first handle the transaction component, which consists of one or more
        // reply messages indicating the transaction state.
        // These are ENDUOWRM, CMMRQSRM, or RDBUPDRM.  If RDBUPDRM is returned,
        // it may be followed by ENDUOWRM or CMMRQSRM
        int peekCP = peekCodePoint();
        if (peekCP == CodePoint.RDBUPDRM) {
            parseRDBUPDRM();
            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.ENDUOWRM) {
            parseENDUOWRM();//statementI.getConnectionCallbackInterface());
            peekCP = peekCodePoint();
        }

        // Check for a RSLSETRM, this is first rm of the result set summary component
        // which would be returned if a stored procedure was called which returned result sets.
        if (peekCP == CodePoint.RSLSETRM) {
            parseResultSetProcedure();
            peekCP = peekCodePoint();
            if (peekCP == CodePoint.RDBUPDRM) {
                parseRDBUPDRM();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.PBSD) {
                parsePBSD();
            }
            return 0;
        }

        // check for a possible TYPDEFNAM or TYPDEFOVR which may be present
        // before the SQLCARD or SQLDTARD.
        peekCP = parseTypdefsOrMgrlvlovrs();

        // an SQLCARD may be retunred if there was no output data, result sets or parameters,
        // or in the case of an error.
        long updateCount = 0;
        if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);

            //statementI.completeExecute(netSqlca);
            NetSqlca.complete(netSqlca);
            updateCount = netSqlca.getUpdateCount();
            peekCP = peekCodePoint();
        } else if (peekCP == CodePoint.SQLDTARD) {
            throw new UnsupportedOperationException("stored procedure");
//            // in the case of singleton select or if a stored procedure was called which had
//            // parameters but no result sets, an SQLSTARD may be returned
//            // keep the PreparedStatementCallbackInterface, since only preparedstatement and callablestatement
//            // has parameters or singleton select which translates to sqldtard.
//            NetSqldta netSqldta = null;
//            boolean useCachedSingletonRowData = false;
//            if (((ClientStatement)statementI).cachedSingletonRowData_ == null) {
//                netSqldta = new NetSqldta(netAgent_);
//            } else {
//                netSqldta = (NetSqldta)((ClientStatement) statementI).
//                    cachedSingletonRowData_;
//                netSqldta.resetDataBuffer();
//                netSqldta.extdtaData_.clear();
//                useCachedSingletonRowData = true;
//            }
//            NetSqlca netSqlca =
//                    parseSQLDTARD(netSqldta);
//
//            // there may be externalized LOB data which also gets returned.
//            peekCP = peekCodePoint();
//            while (peekCP == CodePoint.EXTDTA) {
//                copyEXTDTA(netSqldta);
//                peekCP = peekCodePoint();
//            }
//            statementI.completeExecuteCall(netSqlca, netSqldta);
        } else {
            // if here, then assume an error reply message was returned.
            parseExecuteError();
        }

        if (peekCP == CodePoint.PBSD) {
            parsePBSD();
            peekCP = peekCodePoint();
        }
        return updateCount;
    }
    
    private void parseResultSetProcedure(/*StatementCallbackInterface statementI*/) {
        // when a stored procedure is called which returns result sets,
        // the next thing to be returned after the optional transaction component
        // is the summary component.
        //
        // Parse the Result Set Summary Component which consists of a
        // Result Set Reply Message, SQLCARD or SQLDTARD, and an SQL Result Set
        // Reply data object.  Also check for possible TYPDEF overrides before the
        // OBJDSSs.
        // This method returns an ArrayList of generated sections which contain the
        // package and section information for the result sets which were opened on the
        // server.

        // the result set summary component consists of a result set reply message.
        List<Section> sectionAL = parseRSLSETRM();

        // following the RSLSETRM is an SQLCARD or an SQLDTARD.  check for a
        // TYPDEFNAM or TYPDEFOVR before looking for these objects.
        int peekCP = parseTypdefsOrMgrlvlovrs();

        // The SQLCARD and the SQLDTARD are mutually exclusive.
        // The SQLDTARD is returned if the stored procedure had parameters.
        // (Note: the SQLDTARD contains an sqlca also.  this is the sqlca for the
        // stored procedure call.
//        Cursor netSqldta = null;
        NetSqlca netSqlca = null;
        if (peekCP == CodePoint.SQLCARD) {
            netSqlca = parseSQLCARD(null);
        } else {
            // keep the PreparedStatementCallbackInterface, since only preparedstatement and callablestatement
            // has parameters or singleton select which translates to sqldtard.
            cursor = new Cursor(metadata);
            netSqlca = parseSQLDTARD();//netSqldta);
        }

        // check for a possible TYPDEFNAM or TYPDEFOVR
        // before the SQL Result Set Reply Data object
        peekCP = parseTypdefsOrMgrlvlovrs();

        int numberOfResultSets = parseSQLRSLRD(sectionAL);

        // The result set summary component parsed above indicated how many result sets were opened
        // by the stored pocedure call.  It contained section information for
        // each of these result sets.  Loop through the section array and
        // parse the result set component for each of the retunred result sets.
        if(true)
        throw new UnsupportedOperationException("Results for stored procedures not implemented");
//        NetResultSet[] resultSets = new NetResultSet[numberOfResultSets];
//        for (int i = 0; i < numberOfResultSets; i++) {
//            // parse the result set component of the stored procedure reply.
//            NetResultSet netResultSet = parseResultSetCursor(statementI, sectionAL.get(i));
//            resultSets[i] = netResultSet;
//        }

        // LOBs may have been returned for one of the stored procedure parameters so
        // check for any externalized data.
        peekCP = peekCodePoint();
        while (peekCP == CodePoint.EXTDTA) {
            copyEXTDTA();
            peekCP = peekCodePoint();
        }
//        statementI.completeExecuteCall(netSqlca, cursor, resultSets);
        completeExecute(netSqlca);
    }
    
    public void completeExecute(NetSqlca sqlca) {
        if (sqlca == null) {
            return;
        }

        int sqlcode = sqlca.getSqlCode();
        if (sqlcode < 0) {
            throw new IllegalStateException("" + sqlcode);
//            agent_.accumulateReadException(new SqlException(agent_.logWriter_, sqlca));
        } else {
            long updateCount_ = sqlca.getUpdateCount(); // TODO: @AGG return this?
            // sometime for call statement, protocol will return updateCount_, we will always set that to 0
            // sqlMode_ is not set for statements, only for prepared statements
//            if (sqlMode_ == isCall__) {
//                updateCount_ = -1L;
//            }
            // Sqlcode 466 indicates a call statement has issued and result sets returned.
            // This is a good place to set some state variable to indicate result sets are open
            // for call, so that when autocommit is true, commit will not be issued until the
            // result sets are closed.
            // Currently, commit is not issued even there is no result set.
            // do not externalize sqlcode +100
            if (sqlcode > 0 && sqlcode != 466 && sqlcode != 100) {
                System.out.println("WARN: sqlcode: " + sqlcode);
//                accumulateWarning(new SqlWarning(agent_.logWriter_, sqlca));
            }
        }
    }
    
    void copyEXTDTA() {
//        try {
            parseLengthAndMatchCodePoint(CodePoint.EXTDTA);
            byte[] data = null;
            // TODO @AGG encryption
//            if (longValueForDecryption_ == null) {
                //data = (getData(null)).toByteArray();
                data = getData().array();
//            } else {
//                data = longValueForDecryption_;
//                dssLength_ = 0;
//                longValueForDecryption_ = null;
//            }
            cursor.extdtaData_.add(data);
//        } catch (OutOfMemoryError e) {
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.NET_LOB_DATA_TOO_LARGE_FOR_JVM), e));
//        }
    }
    
    private int parseSQLRSLRD(List<Section> sections) {
        parseLengthAndMatchCodePoint(CodePoint.SQLRSLRD);
        return parseSQLRSLRDarray(sections);
    }
    
    // SQLRSLRD : FDOCA EARLY ARRAY
    // Data Array of a Result Set
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLRSROW; ROW LID 0x6F; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    //
    // SQL Result Set Reply Data (SQLRSLRD) is a byte string that specifies
    // information about result sets returned as reply data in the response to
    // an EXCSQLSTT command that invokes a stored procedure
    private int parseSQLRSLRDarray(List<Section> sections) {
        int numOfResultSets = parseSQLNUMROW();
        for (int i = 0; i < numOfResultSets; i++) {
            parseSQLRSROW(sections.get(i));
        }
        return numOfResultSets;
    }
    
    // SQLRSROW : FDOCA EARLY ROW
    // SQL Row Description for One Result Set Row
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLRSGRP; GROUP LID 0x5F; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void parseSQLRSROW(Section section) {
        parseSQLRSGRP(section);
    }
    
    // SQLRSGRP : EARLY FDOCA GROUP
    // SQL Result Set Group Description
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLRSLOCATOR; PROTOCOL TYPE RSL; ENVLID 0x14; Length Override 4
    //   SQLRSNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLRSNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLRSNUMROWS; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    private void parseSQLRSGRP(Section section) {

        int rsLocator = buffer.readInt();//readInt();
        String rsName = parseVCMorVCS();  // ignore length change bt SQLAM 6 and 7
        int rsNumRows = buffer.readInt();//readInt();
        // currently rsLocator and rsNumRows are not being used.
//        section.setCursorName(rsName);
    }
    
    private String parseVCMorVCS() {
        String stringToBeSet = null;

        int vcm_length = readUnsignedShort();
        if (vcm_length > 0) {
            stringToBeSet = readString(vcm_length, Typdef.targetTypdef.getCcsidMbcEncoding());
        }
        int vcs_length = readUnsignedShort();
        if (vcm_length > 0 && vcs_length > 0) {
            throw new IllegalStateException("NET_VCM_VCS_LENGTHS_INVALID");
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.NET_VCM_VCS_LENGTHS_INVALID)));
        } else if (vcs_length > 0) {
            stringToBeSet = readString(vcs_length, Typdef.targetTypdef.getCcsidSbcEncoding());
        }

        return stringToBeSet;
    }
    
    // SQL Data Reply Data consists of output data from the relational database (RDB)
    // processing of an SQL statement.  It also includes a description of the data.
    //
    // Returned from Server:
    //   FDODSC - required
    //   FDODTA - required
    private NetSqlca parseSQLDTARD() { //Cursor netSqldta) {
        boolean fdodscReceived = false;
        boolean fdodtaReceived = false;

        parseLengthAndMatchCodePoint(CodePoint.SQLDTARD);
        pushLengthOnCollectionStack();

        NetSqlca netSqlca = null;
        int peekCP = peekCodePoint();
        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.FDODSC) {
                foundInPass = true;
                fdodscReceived = checkAndGetReceivedFlag(fdodscReceived);
                parseFDODSC();//netSqldta);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.FDODTA) {
                foundInPass = true;
                fdodtaReceived = checkAndGetReceivedFlag(fdodtaReceived);
                netSqlca = parseFDODTA();//netSqldta);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }

        }
        popCollectionStack();
        if (!fdodscReceived)
            throwMissingRequiredCodepoint("FDODSC", CodePoint.FDODSC);
        if (!fdodtaReceived)
            throwMissingRequiredCodepoint("FDODTA", CodePoint.FDODTA);
        cursor.calculateColumnOffsetsForRow();
        return netSqlca;
    }
    
    private void parseFDODSC() {
        parseLengthAndMatchCodePoint(CodePoint.FDODSC);
        parseSQLDTARDarray(false); // false means don't just skip the bytes
    }
    
    private NetSqlca parseFDODTA() {
        parseLengthAndMatchCodePoint(CodePoint.FDODTA);
        int ddmLength = getDdmLength();
        ensureBLayerDataInBuffer(ddmLength);
        int start = buffer.readerIndex();
//        mark();
        NetSqlca netSqlca = parseSQLCARDrow(null);
//        int length = getFastSkipSQLCARDrowLength();
        int length = buffer.readerIndex() - start;
        adjustLengths(length);
        parseFastSQLDTARDdata();//netCursor);
        return netSqlca;
    }
    
    private void parseFastSQLDTARDdata() {
        parseSQLDTARDdata();
//        netCursor.dataBufferStream_ = getFastData(netCursor.dataBufferStream_);
//        netCursor.dataBuffer_ = netCursor.dataBufferStream_.toByteArray();
//        netCursor.lastValidBytePosition_ = netCursor.dataBuffer_.length;
    }

    
    // RDB Result Set Reply Message (RSLSETRM) indicates that an
    // EXCSQLSTT command invoked a stored procedure, that the execution
    // of the stored procedure generated one or more result sets, and
    // additional information aobut these result sets follows the SQLCARD and
    // SQLDTARD in the reply data of the response
    //
    // Returned from Server:
    //   SVRCOD - required  (0 INFO)
    //   PKGSNLST - required
    //   SRVDGN - optional
    private List<Section> parseRSLSETRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean pkgsnlstReceived = false;
        List<Section> pkgsnlst = null;

        parseLengthAndMatchCodePoint(CodePoint.RSLSETRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_INFO);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.PKGSNLST) {
                // contain repeatable PKGNAMCSN
                foundInPass = true;
                pkgsnlstReceived = checkAndGetReceivedFlag(pkgsnlstReceived);
                pkgsnlst = parsePKGSNLST();
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
//                doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!pkgsnlstReceived)
            throwMissingRequiredCodepoint("PKGSNLST", CodePoint.PKGSNLST);

//        netAgent_.setSvrcod(svrcod);

        return pkgsnlst;
    }
    
    // RDB Package Namce, Consistency Token, and Section Number List
    // specifies a list of fully qualified names of specific sections
    // within one or more packages.
    private List<Section> parsePKGSNLST() {
        ArrayList<Section> pkgsnlst = new ArrayList<Section>();

        parseLengthAndMatchCodePoint(CodePoint.PKGSNLST);
        pushLengthOnCollectionStack();
        while (peekCodePoint() != END_OF_COLLECTION) {
            pkgsnlst.add(parsePKGNAMCSN(false));
        }
        popCollectionStack();
        return pkgsnlst;
    }
    
    // RDB Package name, consistency token, and section number
    // specifies the fully qualified name of a relational
    // database package, its consistency token, and a specific
    // section within a package.
    //
    // Only called for generated secctions from a callable statement.
    //
    Section parsePKGNAMCSN(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.PKGNAMCSN);
        if (skip) {
            skipBytes();
            return null;
        }

        // Still need to populate the logical members in case of an "set current packageset"
        String rdbnam = null;
        String rdbcolid = null;
        String pkgid = null;
        byte[] pkgcnstkn = null;

        int pkgsn = 0;
        byte[] pkgnamcsnBytes = null;
        int pkgnamcsnLength = 0;

        int ddmLength = getDdmLength();
        int offset = 0;

        ensureBLayerDataInBuffer(ddmLength);

        int maxDDMlength;
//        if (netAgent_.netConnection_.databaseMetaData_.serverSupportLongRDBNAM()) {
            maxDDMlength = 781 - DRDAConstants.PKG_IDENTIFIER_MAX_LEN + DRDAConstants.RDBNAM_MAX_LEN;
//        } else {
//            maxDDMlength = 781;
//        }

        if (ddmLength == 64) {
            // read all the bytes except the section number into the byte[] for caching
            pkgnamcsnLength = ddmLength - 2;
            //pkgnamcsnBytes = readBytes (pkgnamcsnLength);
            pkgnamcsnBytes = new byte[pkgnamcsnLength];
            // readFast() does a read without moving the read head.
            offset = peekFastBytes(pkgnamcsnBytes, offset, pkgnamcsnLength);

            // populate the logical members
            rdbnam = readFastString(18);   // RDB name
            rdbcolid = readFastString(18); // RDB Collection ID
            pkgid = readFastString(18);    // RDB Package ID
            pkgcnstkn = readFastBytes(8);  // Package Consistency Token
        } else if ((ddmLength >= 71) && (ddmLength <= maxDDMlength)) {
            // this is the new SCLDTA format.

            // new up a byte[] to cache all the bytes except the 2-byte section number
            pkgnamcsnBytes = new byte[ddmLength - 2];

            // get rdbnam
            int scldtaLen = peekFastLength();
            int maxRDBlength = DRDAConstants.RDBNAM_MAX_LEN;
//                    ((netAgent_.netConnection_.databaseMetaData_.serverSupportLongRDBNAM())? 
//                            NetConfiguration.RDBNAM_MAX_LEN 
//                            : NetConfiguration.PKG_IDENTIFIER_MAX_LEN);
            if (scldtaLen < DRDAConstants.PKG_IDENTIFIER_FIXED_LEN || scldtaLen > maxRDBlength) {
                throw new IllegalStateException("NET_SQLCDTA_INVALID_FOR_RDBNAM " + scldtaLen);
//                agent_.accumulateChainBreakingReadExceptionAndThrow(
//                    new DisconnectException(agent_,
//                        new ClientMessageId(
//                            SQLState.NET_SQLCDTA_INVALID_FOR_RDBNAM),
//                    scldtaLen));
//                return null;
            }
            // read 2+scldtaLen number of bytes from the reply buffer into the pkgnamcsnBytes
            //offset = readBytes (pkgnamcsnBytes, offset, 2+scldtaLen);
            offset = peekFastBytes(pkgnamcsnBytes, offset, 2 + scldtaLen);
            skipFastBytes(2);
            rdbnam = readFastString(scldtaLen);

            // get rdbcolid
            scldtaLen = peekFastLength();
            if (scldtaLen < 18 || scldtaLen > 255) {
                throw new IllegalStateException("NET_SQLCDTA_INVALID_FOR_RDBCOLID " + scldtaLen);
//                agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                    new ClientMessageId(SQLState.NET_SQLCDTA_INVALID_FOR_RDBCOLID),
//                    scldtaLen));
//                return null;
            }
            // read 2+scldtaLen number of bytes from the reply buffer into the pkgnamcsnBytes
            offset = peekFastBytes(pkgnamcsnBytes, offset, 2 + scldtaLen);
            skipFastBytes(2);
            rdbcolid = readFastString(scldtaLen);

            // get pkgid
            scldtaLen = peekFastLength();
            if (scldtaLen < 18 || scldtaLen > 255) {
                throw new IllegalStateException("NET_SQLCDTA_INVALID_FOR_PKGID " + scldtaLen);
//                agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                    new ClientMessageId(SQLState.NET_SQLCDTA_INVALID_FOR_PKGID),
//                    scldtaLen));
//                return null; // To make compiler happy.
            }
            // read 2+scldtaLen number of bytes from the reply buffer into the pkgnamcsnBytes
            offset = peekFastBytes(pkgnamcsnBytes, offset, 2 + scldtaLen);
            skipFastBytes(2);
            pkgid = readFastString(scldtaLen);

            // get consistency token
            offset = peekFastBytes(pkgnamcsnBytes, offset, 8);
            pkgcnstkn = readFastBytes(8);

        } else {
            throw new IllegalStateException("NET_PGNAMCSN_INVALID_AT_SQLAM " + ddmLength);
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.NET_PGNAMCSN_INVALID_AT_SQLAM),
//                ddmLength, netAgent_.targetSqlam_));
//            return null;  // To make compiler happy.
        }

        pkgsn = readFastUnsignedShort();  // Package Section Number.
        adjustLengths(ddmLength);
//        // this is a server generated section
//        // the -1 is set for holdability and it is not used for generated sections
//        Section section = new Section(pkgid, pkgsn, null, -1, true);
//        section.setPKGNAMCBytes(pkgnamcsnBytes);
//        return section;
        throw new UnsupportedOperationException("Server generated sections not implemented");
    }

    
    private void parseExecuteError() {
        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM:
            {
                throw new IllegalStateException("Abnormal end UOW");
//                //passing the StatementCallbackInterface implementation will
//                //help in retrieving the the UnitOfWorkListener that needs to
//                //be rolled back
//                NetSqlca sqlca = parseAbnormalEndUow(statementI);
//                statementI.completeSqlca(sqlca);
//                break;
            }
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.DTAMCHRM:
            parseDTAMCHRM();
            break;
        case CodePoint.OBJNSPRM:
            parseOBJNSPRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        case CodePoint.SQLERRRM:
            {
                NetSqlca sqlca = parseSqlErrorCondition();
                NetSqlca.complete(sqlca);
                //statementI.completeSqlca(sqlca);
                break;
            }
        default:
            parseCommonError(peekCP);
            break;
        }
    }
    
    // Parse the reply for the Close Query Command.
    // This method handles the parsing of all command replies and reply data
    // for the clsqry command.
    private void parseCLSQRYreply() {
        int peekCP = parseTypdefsOrMgrlvlovrs();

        if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);  //@f48553sxg - null means rowsetSqlca_ is null
            // Set the cursor state if null SQLCA or sqlcode is equal to 0.
            if (netSqlca != null) {
                int sqlcode = netSqlca.getSqlCode();
                if (sqlcode == 100 || sqlcode == 20237) {
                    cursor.setAllRowsReceivedFromServer(true);
                } else {
                    NetSqlca.complete(netSqlca);
                }
                
            }
        } else {
        	throwUnknownCodepoint(peekCP);
//            parseCloseError(resultSet);
        }
    }
    
    /**
     * Reads the bytes for the current QRYDTA into the cursor's buffer
     * @return
     */
    public boolean readOpenQueryData() {
        int peekCP = peekCodePoint();
        if (peekCP == CodePoint.QRYDTA) {
            parseQRYDTA(/*NetResultSet*/);
            return true;
        }
        return false;
    }
    
    public boolean isQueryComplete() {
        if (cursor.allRowsReceivedFromServer())
            return true;
        int peekCP = peekCodePoint();
        boolean isQueryComplete = (peekCP == CodePoint.SQLCARD || peekCP == CodePoint.ENDQRYRM);
        if (isQueryComplete)
            cursor.setAllRowsReceivedFromServer(true);
        return isQueryComplete;
    }
    
    public void readEndOpenQuery() {
        int peekCP = peekCodePoint();
        if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);
            NetSqlca.complete(netSqlca);
            peekCP = peekCodePoint();
        }
        
        if (peekCP == CodePoint.ENDQRYRM) {
            parseEndQuery(/*netResultSet*/);
        }
        
        completeOpenQuery(sqlca);
        
        endOfSameIdChainData();
    }
    
    // Parse the reply for the Execute Immediate SQL Statement Command.
    // This method handles the parsing of all command replies and reply data
    // for the excsqlimm command.
    private long parseEXCSQLIMMreply() {
        int peekCP = parseTypdefsOrMgrlvlovrs();

        if (peekCP == CodePoint.RDBUPDRM) {
            parseRDBUPDRM();
            peekCP = parseTypdefsOrMgrlvlovrs();
        }

        long updateCount = 0;
        switch (peekCP) {
            case CodePoint.ENDUOWRM:
                parseENDUOWRM();
                parseTypdefsOrMgrlvlovrs();
            case CodePoint.SQLCARD:
                NetSqlca netSqlca = parseSQLCARD(null);
                if (netSqlca != null) {
                    NetSqlca.complete(netSqlca, 100); // sqlcode=100 means no rows updated
                    if (netSqlca.getSqlCode() >= 0) {
                        updateCount = netSqlca.getUpdateCount();
                    }
                }
                break;
            default:
                parseExecuteImmediateError();
                break;
        }

        peekCP = peekCodePoint();
        if (peekCP == CodePoint.PBSD) {
            parsePBSD();
        }
        return updateCount;
    }
    
    private void parseExecuteImmediateError() {
        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM:
            {
                throw new IllegalStateException("Abnormal ending to UOW");
//                //passing the StatementCallbackInterface implementation will
//                //help in retrieving the the UnitOfWorkListener that needs to
//                //be rolled back
//                NetSqlca sqlca = parseAbnormalEndUow();
//                statement.completeSqlca(sqlca);
//                break;
            }
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.DTAMCHRM:
            parseDTAMCHRM();
            break;
        case CodePoint.OBJNSPRM:
            parseOBJNSPRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        case CodePoint.SQLERRRM:
            {
                NetSqlca sqlca = parseSqlErrorCondition();
                NetSqlca.complete(sqlca);
                break;
            }
        default:
            throwUnknownCodepoint(peekCP);
            break;
        }
    }
    
    /**
     * Parse the reply for the Open Query Command. This method handles the
     * parsing of all command replies and reply data for the opnqry command.
     * will be replaced by parseOPNQRYreply (see parseOPNQRYreplyProto)
     * @param statementI statement to invoke callbacks on
     */
    private void parseOPNQRYreply() { // @AGG removed callback StatementCallbackInterface statementI) {
        int peekCP = peekCodePoint();

        if (peekCP == CodePoint.OPNQRYRM) {
            parseOpenQuery(); // @AGG removed callback statementI);
            peekCP = peekCodePoint();
            if (peekCP == CodePoint.RDBUPDRM) {
                parseRDBUPDRM();
                peekCP = peekCodePoint();
            }
        } else if (peekCP == CodePoint.RDBUPDRM) {
            parseRDBUPDRM();
            parseOpenQuery(); // @AGG removed callback statementI);
            peekCP = peekCodePoint();
        } else if (peekCP == CodePoint.OPNQFLRM) {
            parseOpenQueryFailure(); // @AGG removed callback statementI);
            //peekCP = peekCodePoint();
        } else {
            parseOpenQueryError(); // @AGG removed callback statementI);
            //peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.PBSD) {
            parsePBSD();
        }
    }
    
    private void parseOpenQueryError() {
        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM:
            {
                //passing the StatementCallbackInterface implementation will
                //help in retrieving the the UnitOfWorkListener that needs to
                //be rolled back
                NetSqlca sqlca = parseAbnormalEndUow();
                NetSqlca.complete(sqlca);
//                statementI.completeSqlca(sqlca);
                break;
            }
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.DTAMCHRM:
            parseDTAMCHRM();
            break;
        case CodePoint.OBJNSPRM:
            parseOBJNSPRM();
            break;
        case CodePoint.QRYPOPRM:
            parseQRYPOPRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        default:
            parseCommonError(peekCP);
        }
    }
    
    /**
     * Perform necessary actions for parsing of a ABNUOWRM message.
     *
     * @param connection an implementation of the ConnectionCallbackInterface
     *
     * @return an NetSqlca object obtained from parsing the ABNUOWRM
     */
    private NetSqlca parseAbnormalEndUow() {
        parseABNUOWRM();
        if (peekCodePoint() != CodePoint.SQLCARD) {
            parseTypdefsOrMgrlvlovrs();
        }

        NetSqlca netSqlca = parseSQLCARD(null);
        
//        if(ExceptionUtil.getSeverityFromIdentifier(netSqlca.getSqlState()) > 
//            ExceptionSeverity.STATEMENT_SEVERITY || uwl == null)
//            connection.completeAbnormalUnitOfWork();
//        else
//            connection.completeAbnormalUnitOfWork(uwl);
        
        return netSqlca;
    }
    
    // Query Previously Opened Reply Message is issued when an
    // OPNQRY command is issued for a query that is already open.
    // A previous OPNQRY command might have opened the query
    // which may not be closed.
    // PROTOCOL Architects an SQLSTATE of 58008 or 58009.
    //
    // Messages
    // SQLSTATE : 58009
    //     Execution failed due to a distribution protocol error that caused deallocation of the conversation.
    //     SQLCODE : -30020
    //     Execution failed because of a Distributed Protocol
    //         Error that will affect the successful execution of subsequent
    //         commands and SQL statements: Reason Code <reason-code>.
    //      Some possible reason codes include:
    //      121C Indicates that the user is not authorized to perform the requested command.
    //      1232 The command could not be completed because of a permanent error.
    //          In most cases, the server will be in the process of an abend.
    //      220A The target server has received an invalid data description.
    //          If a user SQLDA is specified, ensure that the fields are
    //          initialized correctly. Also, ensure that the length does not
    //          exceed the maximum allowed length for the data type being used.
    //
    //      The command or statement cannot be processed.  The current
    //      transaction is rolled back and the application is disconnected
    //      from the remote database.
    //
    //
    // Returned from Server:
    // SVRCOD - required  (8 - ERROR)
    // RDBNAM - required
    // PKGNAMCSN - required
    // SRVDGN - optional
    //
    private void parseQRYPOPRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;
        boolean pkgnamcsnReceived = false;
        Object pkgnamcsn = null;

        parseLengthAndMatchCodePoint(CodePoint.QRYPOPRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            if (peekCP == CodePoint.PKGNAMCSN) {
                foundInPass = true;
                pkgnamcsnReceived = checkAndGetReceivedFlag(pkgnamcsnReceived);
                pkgnamcsn = parsePKGNAMCSN(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
          throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
          throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);
        if (!pkgnamcsnReceived)
          throwMissingRequiredCodepoint("PKGNAMCSN", CodePoint.PKGNAMCSN);

//        netAgent_.setSvrcod(svrcod); // @AGG removed
        throw new IllegalStateException("DRDA_CONNECTION_TERMINATED CONN_DRDA_QRYOPEN");
//        agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//            new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//            MessageUtil.getCompleteMessage(MessageId.CONN_DRDA_QRYOPEN,
//                SqlException.CLIENT_MESSAGE_RESOURCE_NAME,
//                (Object [])null)));
    }
    
    /**
     * Parse a PBSD - PiggyBackedSessionData code point. Can contain one or
     * both of, a PBSD_ISO code point followed by a byte representing the jdbc
     * isolation level, and a PBSD_SCHEMA code point followed by the name of the
     * current schema as an UTF-8 String.
     */
    void parsePBSD() {
        parseLengthAndMatchCodePoint(CodePoint.PBSD);
        int peekCP = peekCodePoint();
        while (peekCP != END_OF_SAME_ID_CHAIN) {
            parseLengthAndMatchCodePoint(peekCP);
            switch (peekCP) {
            case CodePoint.PBSD_ISO:
                throw new UnsupportedOperationException("PBSD_ISO not implemented");
//                netAgent_.netConnection_.
//                    completePiggyBackIsolation(readUnsignedByte());
            case CodePoint.PBSD_SCHEMA:
                throw new UnsupportedOperationException("PBSD_ISO not implemented");
//                netAgent_.netConnection_.
//                    completePiggyBackSchema
//                    (readString(getDdmLength(), Typdef.UTF8ENCODING));
            default:
                //parseCommonError(peekCP);
                throwUnknownCodepoint(peekCP);
            }
            peekCP = peekCodePoint();
        }
    }
    
    // RDB Update Reply Message indicates that a DDM command resulted
    // in an update at the target relational database.  If a command
    // generated multiple reply messages including an RDBUPDRM, then
    // the RDBUPDRM must be the first reply message for the command.
    // For each target server, the RDBUPDRM  must be returned the first
    // time an update is made to the target RDB within a unit of work.
    // The target server may optionally return the RDBUPDRM after subsequent
    // updates within the UOW.  If multiple target RDBs are involved with
    // the current UOW and updates are made with any of them, then the RDBUPDRM
    // must be returned in response to the first update at each of them.
    protected void parseRDBUPDRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;

        parseLengthAndMatchCodePoint(CodePoint.RDBUPDRM);
        pushLengthOnCollectionStack();

        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_INFO);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
                //doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);
        //checkRequiredObjects(svrcodReceived, rdbnamReceived);

        // call an event to indicate the server has been updated
        // @AGG unused netAgent_.setSvrcod(svrcod);

    }
    
    @Deprecated // @AGG splitting this out
    private void parseOpenQuery() { // @AGG removed callback StatementCallbackInterface statementI) {
        //NetResultSet netResultSet = parseOPNQRYRM(statementI, true);
        parseOPNQRYRM(true);

        NetSqlca sqlca = null;
        int peekCP = peekCodePoint();
        if (peekCP != CodePoint.QRYDSC) {

            peekCP = parseTypdefsOrMgrlvlovrs();

            if (peekCP == CodePoint.SQLDARD) {
                outputColumnMetaData = new ColumnMetaData();// ClientDriver.getFactory().newColumnMetaData(netAgent_.logWriter_);
                NetSqlca netSqlca = parseSQLDARD(outputColumnMetaData, false);  // false means do not skip SQLDARD bytes

                //For java stored procedure, we got the resultSetMetaData from server,
                //Do we need to save the resultSetMetaData and propagate netSqlca?
                //The following statement are doing the both, but it do more than
                //we want. It also mark the completion of Prepare statement.
                //
                // this will override the same call made from parsePrepareDescribe
                //  this will not work, this is not the DA for the stored proc params
                //
                // We may now receive a new SQLDARD (unrequested, a
                // DRDA protocol extension) when a query is opened iff the
                // underlying server's prepared statement has been recompiled
                // since the client first received metadata when preparing the
                // statement.
                NetSqlca.complete(netSqlca);
                peekCP = parseTypdefsOrMgrlvlovrs();
            }
            // check if the DARD is mutually exclusive with CARD, if so, then the following if should be an elese

            if (peekCP == CodePoint.SQLCARD) {
                sqlca = parseSQLCARD(null);
                peekCP = parseTypdefsOrMgrlvlovrs();
            }
        }
        parseQRYDSC(/*netResultSet.netCursor_*/);

        peekCP = peekCodePoint();
        while (peekCP == CodePoint.QRYDTA) {
            parseQRYDTA(/*NetResultSet*/);
            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);
            NetSqlca.complete(netSqlca);
            peekCP = peekCodePoint();
        }

        if (peekCP == CodePoint.ENDQRYRM) {
            parseEndQuery(/*netResultSet*/);
        }

        completeOpenQuery(sqlca);
    }
    
    private void parseOpenQueryFailure() {
      parseOPNQFLRM();
      parseTypdefsOrMgrlvlovrs();
      NetSqlca netSqlca = parseSQLCARD(null);
      NetSqlca.complete(netSqlca);
    }
    
    // Open Query Failure (OPNQFLRM) Reply Message indicates that the
    // OPNQRY command failed to open the query.  The reason that the
    // target relational database was unable to open the query is reported in an
    // SQLCARD reply data object.
    // Whenever an OPNQFLRM is returned, an SQLCARD object must also be returned
    // following the OPNQFLRM.
    //
    // Returned from Server:
    //   SVRCOD - required (8 - ERROR)
    //   RDBNAM - required
    //   SRVDGN - optional
    private void parseOPNQFLRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;

        parseLengthAndMatchCodePoint(CodePoint.OPNQFLRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                // skip the rdbnam since it doesn't tell us anything new.
                // there is no way to return it to the application anyway.
                // not having to convert this to a string is a time saver also.
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
          throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
          throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);

        // @AGG removed netAgent_.setSvrcod(svrcod);

        // get SQLSTATE from SQLCARD...
    }
    
    public void completeOpenQuery(NetSqlca sqlca/*, ClientResultSet resultSet*/) {
        NetSqlca.complete(sqlca);
//        resultSet_ = resultSet;
//        // For NET, resultSet_ == null when open query fails and receives OPNQFLRM.
//        // Then, in NetStatementReply.parseOpenQueryFailure(), completeOpenQuery() is
//        // invoked with resultSet explicitly set to null.
//        if (resultSet == null) {
//            return;
//        }
//        resultSet.resultSetMetaData_ = resultSetMetaData_;
//        resultSet.resultSetMetaData_.resultSetConcurrency_ = resultSet.resultSetConcurrency_;
//        // Create tracker for LOB locator columns.
//        resultSet.createLOBColumnTracker();
//
//        // only cache the Cursor object for a PreparedStatement and if a Cursor object is
//        // not already cached.
//        if (cachedCursor_ == null && isPreparedStatement_) {
//            cachedCursor_ = resultSet_.cursor_;
//        }

        // The following two assignments should have already happened via prepareEvent(),
        // but are included here for safety for the time being.
        if (sqlca != null && sqlca.getSqlCode() < 0) {
            return;
        }
//        resultSet.cursor_.rowsRead_ = fetchedRowBase;

//        // Set fetchSize_ to the default(64) if not set by the user if the resultset is scrollable.
//        // This fetchSize_ is used to check for a complete rowset when rowsets are parsed.
//        // For scrollable cursors when the fetchSize_ is not set, (fetchSize_ == 0), a default
//        // fetchSize of 64 is sent on behalf of the application, so we need to update the fetchSize_
//        // here to 64.
//        if (fetchSize_ == 0 &&
//             (resultSet_.resultSetType_ == ResultSet.TYPE_SCROLL_INSENSITIVE ||
//              resultSet_.resultSetType_ == ResultSet.TYPE_SCROLL_SENSITIVE)) {
//            fetchSize = DRDAQueryRequest.defaultFetchSize;
//        }
        // @AGG customize fetchSize here if needed
        // @AGG assuming always TYPE_FORWARD_ONLY 
    }
    
    void parseEndQuery(/*ResultSetCallbackInterface resultSetI*/) {
        parseENDQRYRM(/*resultSetI*/);
        parseTypdefsOrMgrlvlovrs();
        NetSqlca netSqlca = parseSQLCARD(null);
        cursor.setAllRowsReceivedFromServer(true);
        
        int peekCP = peekCodePoint();
		if (peekCP == CodePoint.RDBUPDRM) {
			parseRDBUPDRM();
		}
    }
    
    // Also called by NetResultSetReply subclass.
    // The End of Query Reply Message indicates that the query process has
    // terminated in such a manner that the query or result set is now closed.
    // It cannot be resumed with the CNTQRY command or closed with the CLSQRY command.
    // The ENDQRYRM is always followed by an SQLCARD.
    private void parseENDQRYRM(/*ResultSetCallbackInterface resultSetI*/) {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;

        parseLengthAndMatchCodePoint(CodePoint.ENDQRYRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_WARNING, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
//                doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if(!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
//        checkRequiredObjects(svrcodReceived);

//        netAgent_.setSvrcod(svrcod); @AGG removed

    }
    
    void parseQRYDTA(/*NetResultSet netResultSet*/) {
        parseLengthAndMatchCodePoint(CodePoint.QRYDTA);
        // @AGG assume no encryption
//        if (longValueForDecryption_ == null) {
            int ddmLength = getDdmLength();
            ensureBLayerDataInBuffer(ddmLength);
//        }
        parseSQLDTARDdata(/*netResultSet.netCursor_*/);
     // @AGG assume no encryption
//        if (longValueForDecryption_ == null) {
            adjustLengths(getDdmLength());
//        } else {
//            longValueForDecryption_ = null;
//        }
//        if (longBufferForDecryption_ != null) {
//            buffer_ = longBufferForDecryption_;
//            pos_ = longPosForDecryption_;
//            if (count_ > longBufferForDecryption_.length) {
//                count_ = longBufferForDecryption_.length;
//            } else if (longCountForDecryption_ != 0) {
//                count_ = longCountForDecryption_;
//                longCountForDecryption_ = 0;
//            }
//            dssLength_ = 0;
//            longBufferForDecryption_ = null;
//        }
    }
    
    public Cursor getCursor() {
        if (cursor == null)
            throw new IllegalStateException("Cursor has not been created yet");
        return cursor;
    }
    
    public long getQueryInstanceId() {
        return queryInstanceId;
    }
    
    public ColumnMetaData getOutputColumnMetaData() {
        if (outputColumnMetaData == null)
            throw new IllegalStateException("ColumnMetaData has not been created yet");
        return outputColumnMetaData;
    }
    
    public void setOutputColumnMetaData(ColumnMetaData md) {
        Objects.requireNonNull(md);
        this.outputColumnMetaData = md;
    }
    
    public ColumnMetaData getInputColumnMetaData() {
        if (inputColumnMetaData == null)
            throw new IllegalStateException("ColumnMetaData has not been created yet");
        return inputColumnMetaData;
    }
    
    private void parseSQLDTARDdata(/*NetCursor netCursor*/) {
//        if (longValueForDecryption_ == null) {
//            netCursor.dataBufferStream_ = getData(/*netCursor.dataBufferStream_*/);
//            netCursor.dataBuffer_ = netCursor.dataBufferStream_.toByteArray();
        if (cursor == null)
            cursor = new Cursor(metadata);
        cursor.dataBuffer_ = getData();
//        } else {
//            int size = netCursor.dataBufferStream_.size();
//            if (size == 0) {
//                netCursor.dataBuffer_ = longValueForDecryption_;
//                //longValue_ = null;
//            } else {
//                byte[] newArray = new byte[size + longValueForDecryption_.length];
//                System.arraycopy(netCursor.dataBuffer_, 0, newArray, 0, size);
//                System.arraycopy(longValueForDecryption_, 0, newArray, size, longValueForDecryption_.length);
//                netCursor.dataBuffer_ = newArray;
//                //longValue_ = null;
//            }
//        }

//        netCursor.lastValidBytePosition_ = netCursor.dataBuffer_.length;
        cursor.lastValidBytePosition_ = cursor.dataBuffer_.capacity();
    }
    
    // This will be the new and improved getData that handles all QRYDTA/EXTDTA
    // Returns the stream so that the caller can cache it
    final ByteBuf getData(/*ByteArrayOutputStream existingBuffer*/) {
        boolean readHeader;
        int copySize;
        ByteBuf baos = null;
//        ByteArrayOutputStream baos;

//        // note: an empty baos can yield an allocated and empty byte[]
//        if (existingBuffer != null) {
//            baos = existingBuffer;
//        } else {
//            if (ddmScalarLen_ != -1) {
//                // allocate a stream based on a known amount of data
//                baos = new ByteArrayOutputStream(ddmScalarLen_);
//            } else {
//                // allocate a stream to hold an unknown amount of data
//                baos = new ByteArrayOutputStream();
//                //isLengthAndNullabilityUnknown = true;
//            }
//        }

        // set the amount to read for the first segment
        copySize = dssLength_; // note: has already been adjusted for headers

        do {
            // determine if a continuation header needs to be read after the data
            if (dssIsContinued_) {
                readHeader = true;
            } else {
                readHeader = false;
            }

            // read the segment
            ensureALayerDataInBuffer(copySize);
            adjustLengths(copySize);
            baos = buffer.readRetainedSlice(copySize);
//            baos.write(buffer_, pos_, copySize);
//            pos_ += copySize;

            // read the continuation header, if necessary
            if (readHeader) {
                readDSSContinuationHeader();
            }

            copySize = dssLength_;
        } while (readHeader == true);
        

        return baos;
    }
    
    private void parseQRYDSC() {
        parseLengthAndMatchCodePoint(CodePoint.QRYDSC);
        parseSQLDTARDarray(false); // false means don't just skip the bytes
    }
    
    private void parseSQLDTARDarray(boolean skipBytes) {
        if (skipBytes) {
            skipBytes();
        }
        int previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_START;
        int previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_START;
        int mddProtocolType = 0;
        int columnCount = 0;
        Typdef.targetTypdef.clearMddOverrides();
        // netAgent_.targetTypdef_.clearMddOverrides();

        int ddmLength = getDdmLength();
        ensureBLayerDataInBuffer(ddmLength);

        while (ddmLength > 0) {

            int tripletLength = readFastUnsignedByte();
            int tripletType = readFastUnsignedByte();
            int tripletId = readFastUnsignedByte();

            switch (tripletType) {

            case FdocaConstants.MDD_TRIPLET_TYPE:
                if ((tripletLength != FdocaConstants.MDD_TRIPLET_SIZE) ||
                        (tripletId != FdocaConstants.NULL_LID)) {
                    descriptorErrorDetected();
                }
                checkPreviousSQLDTARDtriplet(previousTripletType,
                        FdocaConstants.SQLDTARD_TRIPLET_TYPE_MDD,
                        previousTripletId,
                        FdocaConstants.SQLDTARD_TRIPLET_ID_0);
                previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_MDD;
                previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_0;

                // read in remaining MDD bytes
                int mddClass = readFastUnsignedByte();
                int mddType = readFastUnsignedByte();
                int mddRefType = readFastUnsignedByte();
                mddProtocolType = readFastUnsignedByte();
                break;

            case FdocaConstants.NGDA_TRIPLET_TYPE: // rename to NGDA_TRIPLET_CODEPOINT
                if (tripletId != FdocaConstants.SQLDTAGRP_LID) {
                    descriptorErrorDetected();
                }
                checkPreviousSQLDTARDtriplet(previousTripletType,
                        FdocaConstants.SQLDTARD_TRIPLET_TYPE_GDA,
                        previousTripletId,
                        FdocaConstants.SQLDTARD_TRIPLET_ID_D0);
                previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_GDA;
                previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_0;

                // add a quick check to see if the table is altered (columns are added or deleted)
                // before reusing the cached cursor.  note: this check does not catch the case
                // where the number of columns stay the same, but the column type or length changes,
                // i.e. from integer to char.
                int columns = peekTotalColumnCount(tripletLength);
                // peek ahead to get the total number of columns.
                outputColumnMetaData.setColumnCount(columns);
                cursor.initializeColumnInfoArrays(Typdef.targetTypdef, columns);
                columnCount += parseSQLDTAGRPdataLabelsAndUpdateColumn(/*cursor, */columnCount, tripletLength);
                break;


            case FdocaConstants.RLO_TRIPLET_TYPE:  // rename to RLO_TRIPLET_CODEPOINT

                switch (tripletId) {
                case FdocaConstants.SQLCADTA_LID:
                    if (tripletLength != FdocaConstants.SQLCADTA_RLO_SIZE) {
                        descriptorErrorDetected(); // DSCERRCD_06
                    }
                    checkPreviousSQLDTARDtriplet(previousTripletType,
                            FdocaConstants.SQLDTARD_TRIPLET_TYPE_RLO,
                            previousTripletId,
                            FdocaConstants.SQLDTARD_TRIPLET_ID_E0);
                    previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_RLO;
                    previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_E0;
                    checkFastRLO(FdocaConstants.RLO_SQLCADTA);
                    break;

                case FdocaConstants.SQLDTARD_LID:
                    if (tripletLength != FdocaConstants.SQLDTARD_RLO_SIZE) {
                        descriptorErrorDetected(); // DSCERRCD_06
                    }
                    checkPreviousSQLDTARDtriplet(previousTripletType,
                            FdocaConstants.SQLDTARD_TRIPLET_TYPE_RLO,
                            previousTripletId,
                            FdocaConstants.SQLDTARD_TRIPLET_ID_F0);
                    previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_RLO;
                    previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_F0;
                    checkFastRLO(FdocaConstants.RLO_SQLDTARD);
                    break;
                default:
                    descriptorErrorDetected(); // DSCERRCD_07
                    break;
                }
                break;

            case FdocaConstants.CPT_TRIPLET_TYPE:  // rename to CPT_TRIPLET_CODEPOINT
                if (tripletId != FdocaConstants.NULL_LID) {
                    descriptorErrorDetected();
                }
                checkPreviousSQLDTARDtriplet(previousTripletType,
                        FdocaConstants.SQLDTARD_TRIPLET_TYPE_CPT,
                        previousTripletId,
                        FdocaConstants.SQLDTARD_TRIPLET_ID_0);
                previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_CPT;
                previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_0;

                columnCount += parseSQLDTAGRPdataLabelsAndUpdateColumn(/*cursor, */columnCount, tripletLength);
                break;


            case FdocaConstants.SDA_TRIPLET_TYPE:  // rename to SDA_TRIPLET_CODEPOINT
                if (tripletLength != FdocaConstants.SDA_TRIPLET_SIZE) {
                    descriptorErrorDetected();  // DSCERRCD_06
                }
                checkPreviousSQLDTARDtriplet(previousTripletType,
                        FdocaConstants.SQLDTARD_TRIPLET_TYPE_SDA,
                        previousTripletId,
                        FdocaConstants.SQLDTARD_TRIPLET_ID_SDA);
                previousTripletType = FdocaConstants.SQLDTARD_TRIPLET_TYPE_SDA;
                previousTripletId = FdocaConstants.SQLDTARD_TRIPLET_ID_SDA;
                Typdef.targetTypdef.setMddOverride(mddProtocolType, // mdd protocol type
                        tripletId, // fdocaTripletLid
                        readFastUnsignedByte(), // fdocaFieldType
                        readFastInt(), // ccsid
                        readFastUnsignedByte(), // characterSize
                        readFastUnsignedByte(), // mode
                        readFastShort());
                break;

            default:
                descriptorErrorDetected();  //DSCERRCD_01
                break;
            }

            ddmLength -= tripletLength;
        }

        adjustLengths(getDdmLength());

        // Allocate a char buffer after all of the descriptors have been parsed out.
//        cursor.allocateCharBuffer();

        checkPreviousSQLDTARDtriplet(previousTripletType,
                FdocaConstants.SQLDTARD_TRIPLET_TYPE_END,
                previousTripletId,
                FdocaConstants.SQLDTARD_TRIPLET_ID_END);
    }
    
    private void checkFastRLO(int[][] rlo) {
        for (int i = 0; i < rlo.length; i++) {
            int lid = readFastUnsignedByte();
            if (lid != rlo[i][FdocaConstants.RLO_GROUP_LID]) {
                descriptorErrorDetected(); // DSCERRCD_42
            }
            int elementTaken = readFastUnsignedByte();
            if (elementTaken != rlo[i][FdocaConstants.RLO_ELEMENT_TAKEN]) {
                descriptorErrorDetected();  // DSCERRCD_07
            }
            int repFactor = readFastUnsignedByte();
            if (repFactor != rlo[i][FdocaConstants.RLO_REP_FACTOR]) {
                descriptorErrorDetected();  // DSCERRCD_07
            }
        }
    }
    
    private int parseSQLDTAGRPdataLabelsAndUpdateColumn(/*NetCursor cursor, */int columnIndex, int tripletLength) {
        int numColumns = (tripletLength - 3) / 3;
        for (int i = columnIndex; i < columnIndex + numColumns; i++) {
            cursor.qrydscTypdef_.updateColumn(cursor, metadata, i, readFastUnsignedByte(), readFastUnsignedShort());
        }
        return numColumns;
    }
    
    protected final int peekTotalColumnCount(int tripletLength) {
        int columnCount = 0;
        //int offset = 0;
        int tripletType = FdocaConstants.CPT_TRIPLET_TYPE;
        while (tripletType == FdocaConstants.CPT_TRIPLET_TYPE) {
            columnCount += ((tripletLength - 3) / 3);
            // Peek ahead for the next triplet's tripletLength and tripletType.
            // The number of bytes to skip before the next tripletType is tripletLength - 3.
            ensureBLayerDataInBuffer(tripletLength - 3);
            buffer.markReaderIndex();
            buffer.skipBytes(tripletLength - 3);
            //offset += (tripletLength - 3);
            tripletLength = buffer.readByte();
//            tripletLength = (buffer_[pos_ + offset++] & 0xff);
            tripletType = buffer.readByte();
//            tripletType = (buffer_[pos_ + offset++] & 0xff);
            // Skip the 1-byte tripletId.
            //offset++;
            buffer.resetReaderIndex();
        }
        return columnCount;
    }
    
    private void checkPreviousSQLDTARDtriplet(int previousTripletType, int tripletType, int previousTripletId,
            int tripletId) {
        if (FdocaConstants.SQLDTARD_TRIPLET_TYPES[previousTripletType][tripletType] == false) {
            descriptorErrorDetected(); // DSCERRCD_02 move error identity into array
        }
        if (FdocaConstants.SQLDTARD_TRIPLET_IDS[previousTripletId][tripletId] == false) {
            descriptorErrorDetected(); // DSCERRCD_02 move error identity into array
        }
    }
    
    // Possible errors to detect include:
    // DSCERRCD_01 - FDOCA triplet is not used in PROTOCOL descriptors or type code is invalid
    // DSCERRCD_02 - FDOCA triplet sequence error
    // DSCERRCD_03 - An array description is required and this is not one
    //               (too many or too few RLO triplets)
    // DSCERRCD_04 - A row description is required and this is not one
    //               (too many or too few RLO triplets)
    // DSCERRCD_05 - Late Environmental Descriptor just received not supported
    // DSCERRCD_06 - Malformed triplet, required parameter is missing
    // DSCERRCD_07 - Parameter value is not acceptable
    // DSCERRCD_11 - MDD present is not recognized as an SQL descriptor
    // DSCERRCD_12 - MDD class is not recognized as a valid SQL class
    // DSCERRCD_13 - MDD type not recognized as a valid SQL type
    // DSCERRCD_21 - Representation is incompatible with SQL type (in prior MDD)
    // DSCERRCD_22 - CCSID is not supported
    // DSCERRCD_32 - GDA references a local identifier which is not an SDA or GDA
    // DSCERRCD_33 - GDA length override exceeds limits
    // DSCERRCD_34 - GDA precision exceeds limits
    // DSCERRCD_35 - GDA scale greater than precision or scale negative
    // DSCERRCD_36 - GDA length override missing or incompatible with data type
    // DSCERRCD_41 - RLO references a LID which is not an RLO or GDA.
    // DSCERRCD_42 - RLO fails to reference a required GDA or RLO.
    private void descriptorErrorDetected() {
        throw new IllegalStateException("MessageId.CONN_DRDA_INVALIDFDOCA");
//        agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//            new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//            MessageUtil.getCompleteMessage(MessageId.CONN_DRDA_INVALIDFDOCA,
//                SqlException.CLIENT_MESSAGE_RESOURCE_NAME,
//                (Object [])null)));
    }
    
    /**
     * Open Query Complete Reply Message indicates to the requester
     * that an OPNQRY or EXCSQLSTT command completed normally and that
     * the query process has been initiated.  It also indicates the
     * type of query protocol and cursor used for the query.
     * <p>
     * When an EXCSQLSTT contains an SQL statement that invokes a
     * stored procedure, and the procedure completes, an OPNQRYRM is
     * returned for each answer set.
     *
     * @param statementI statement callback interface
     * @param isOPNQRYreply If true, parse a reply to an OPNQRY
     * command. Otherwise, parse a reply to an EXCSQLSTT command.
     * @return a <code>NetResultSet</code> value
     */
    //private NetResultSet parseOPNQRYRM(boolean isOPNQRYreply) {
    private void parseOPNQRYRM(boolean isOPNQRYreply) {
        // these need to be initialized to the correct default values.
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean svrcodReceived = false;
        int qryprctyp = 0;
        boolean qryprctypReceived = false;
        int sqlcsrhld = 0xF0;    // 0xF0 is false (default), 0xF1 is true.
        boolean sqlcsrhldReceived = false;
        int qryattscr = 0xF0;   // 0xF0 is false (default), 0xF1 is true.
        boolean qryattscrReceived = false;
        int qryattsns = CodePoint.QRYUNK;
        boolean qryattsnsReceived = false;
        int qryattupd = CodePoint.QRYUNK;
        boolean qryattupdReceived = false;
        long qryinsid = 0;
        boolean qryinsidReceived = false;


        int qryattset = 0xF0;    // 0xF0 is false (default), 0xF1 is true.
        boolean qryattsetReceived = false;

        parseLengthAndMatchCodePoint(CodePoint.OPNQRYRM);
        //pushLengthOnCollectionStack();
        int ddmLength = getDdmLength();
        ensureBLayerDataInBuffer(ddmLength);
        int peekCP = peekCodePoint();
        int length = 0;

        //while (peekCP != Reply.END_OF_COLLECTION) {
        while (ddmLength > 0) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                length = peekedLength_;
                svrcod = parseFastSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_SESDMG);
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYPRCTYP) {
                foundInPass = true;
                qryprctypReceived = checkAndGetReceivedFlag(qryprctypReceived);
                length = peekedLength_;
                qryprctyp = parseFastQRYPRCTYP();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SQLCSRHLD) {
                // Indicates whether the requester specified the HOLD option.
                // When specified, the cursor is not closed upon execution of a commit operation.
                foundInPass = true;
                sqlcsrhldReceived = checkAndGetReceivedFlag(sqlcsrhldReceived);
                length = peekedLength_;
                sqlcsrhld = parseFastSQLCSRHLD();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYATTSCR) {
                foundInPass = true;
                qryattscrReceived = checkAndGetReceivedFlag(qryattscrReceived);
                length = peekedLength_;
                qryattscr = parseFastQRYATTSCR();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYATTSNS) {
                foundInPass = true;
                qryattsnsReceived = checkAndGetReceivedFlag(qryattsnsReceived);
                length = peekedLength_;
                qryattsns = parseFastQRYATTSNS();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYATTUPD) {
                foundInPass = true;
                qryattupdReceived = checkAndGetReceivedFlag(qryattupdReceived);
                length = peekedLength_;
                qryattupd = parseFastQRYATTUPD();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYINSID) {
                foundInPass = true;
                qryinsidReceived = checkAndGetReceivedFlag(qryinsidReceived);
                length = peekedLength_;
                qryinsid = parseFastQRYINSID();
                queryInstanceId = qryinsid;
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.QRYATTSET) {
                foundInPass = true;
                qryattsetReceived = checkAndGetReceivedFlag(qryattsetReceived);
                length = peekedLength_;
                qryattset = parseFastQRYATTSET();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }
            
            if (peekCP == CodePoint.QRYATTISOL) {
                // @AGG added
                foundInPass = true;
                length = peekedLength_;
                parseFastQRYATTISOL();
                ddmLength = adjustDdmLength(ddmLength, length);
                peekCP = peekCodePoint();
            }


            if (!foundInPass) {
                //doPrmnsprmSemantics(peekCP);
                throwUnknownCodepoint(peekCP);
            }

        }
        //checkRequiredObjects(svrcodReceived, qryprctypReceived, qryinsidReceived);
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!qryprctypReceived)
            throwMissingRequiredCodepoint("QRYPRCTYP", CodePoint.QRYPRCTYP);
        if (!qryinsidReceived)
            throwMissingRequiredCodepoint("QRYINSID", CodePoint.QRYINSID);

        // @AGG not implemented netAgent_.setSvrcod(svrcod);

        // hack for now until event methods are used below
//        ClientStatement statement = (ClientStatement) statementI;

        // if there is a cached Cursor object, then use the cached cursor object.
//        NetResultSet rs = null;
//        if (statement.cachedCursor_ != null) {
//            statement.cachedCursor_.resetDataBuffer();
//            ((NetCursor) statement.cachedCursor_).extdtaData_.clear();
//            try {
//                rs = (NetResultSet) ClientDriver.getFactory().newNetResultSet
//                        (netAgent_,
//                        (NetStatement) statement.getMaterialStatement(),
//                        statement.cachedCursor_,
//                        qryprctyp, //protocolType, CodePoint.FIXROWPRC | 
//                                   //              CodePoint.LMTBLKPRC
//                        sqlcsrhld, //holdOption, 0xF0 for false (default) | 0xF1 for true.
//                        qryattscr, //scrollOption, 0xF0 for false (default) | 0xF1 for true.
//                        qryattsns, //sensitivity, CodePoint.QRYUNK | 
//                                   //             CodePoint.QRYINS |
//                                   //             CodePoint.QRYSNSSTC
//                        qryattset,
//                        qryinsid, //instanceIdentifier, 0 (if not returned, check default) or number
//                        calculateResultSetType(qryattscr, qryattsns, statement.resultSetType_),
//                        calculateResultSetConcurrency(qryattupd, statement.resultSetConcurrency_),
//                        calculateResultSetHoldability(sqlcsrhld));
//            } catch(SqlException sqle) {
//                throw new DisconnectException(netAgent_,sqle);
//            }
//        } else {
//            try {
//                rs = (NetResultSet)ClientDriver.getFactory().newNetResultSet
//                        (netAgent_,
//                        (NetStatement) statement.getMaterialStatement(),
//                        new NetCursor(netAgent_, qryprctyp),
//                        qryprctyp, //protocolType, CodePoint.FIXROWPRC | 
//                                   //              CodePoint.LMTBLKPRC
//                        sqlcsrhld, //holdOption, 0xF0 for false (default) | 0xF1 for true.
//                        qryattscr, //scrollOption, 0xF0 for false (default) | 0xF1 for true.
//                        qryattsns, //sensitivity, CodePoint.QRYUNK | CodePoint.QRYINS
//                        qryattset,
//                        qryinsid, //instanceIdentifier, 0 (if not returned, check default) or number
//                        calculateResultSetType(qryattscr, qryattsns, statement.resultSetType_),
//                        calculateResultSetConcurrency(qryattupd, statement.resultSetConcurrency_),
//                        calculateResultSetHoldability(sqlcsrhld));
//            } catch(SqlException sqle) {
//               throw new DisconnectException(netAgent_,sqle);
//            }
//        }
        // @AGG returning a cursor instead of a ResultSet
        cursor = new Cursor(metadata);

        // QRYCLSIMP only applies to OPNQRY, not EXCSQLSTT
//        final boolean qryclsimp =
//            isOPNQRYreply &&
//            (rs.resultSetType_ == ResultSet.TYPE_FORWARD_ONLY) &&
//            netAgent_.netConnection_.serverSupportsQryclsimp();
//        rs.netCursor_.setQryclsimpEnabled(qryclsimp);
//        return rs;
    }
    
    private int parseFastSQLCSRHLD() {
        matchCodePoint(CodePoint.SQLCSRHLD);
        int sqlcsrhld = readFastUnsignedByte();
        // 0xF0 is false (default), 0xF1 is true  // use constants in if
        if ((sqlcsrhld != 0xF0) && (sqlcsrhld != 0xF1)) {
            doValnsprmSemantics(CodePoint.SQLCSRHLD, sqlcsrhld);
        }
        return sqlcsrhld;
    }

    private int parseFastQRYATTSCR() {
        matchCodePoint(CodePoint.QRYATTSCR);
        int qryattscr = readFastUnsignedByte();  // use constants in if
        if ((qryattscr != 0xF0) && (qryattscr != 0xF1)) {
            doValnsprmSemantics(CodePoint.QRYATTSCR, qryattscr);
        }
        return qryattscr;
    }

    private int parseFastQRYATTSET() {
        matchCodePoint(CodePoint.QRYATTSET);
        int qryattset = readFastUnsignedByte();  // use constants in if
        if ((qryattset != 0xF0) && (qryattset != 0xF1)) {
            doValnsprmSemantics(CodePoint.QRYATTSET, qryattset);
        }
        return qryattset;
    }
    
    private int parseFastQRYATTISOL() {
        // @AGG added
        matchCodePoint(CodePoint.QRYATTISOL);
        return readUnsignedShort();
    }

    private int parseFastQRYATTSNS() {
        matchCodePoint(CodePoint.QRYATTSNS);
        int qryattsns = readFastUnsignedByte();
        switch (qryattsns) {
        case CodePoint.QRYUNK:
        case CodePoint.QRYSNSSTC:
        case CodePoint.QRYINS:
            break;
        default:
            doValnsprmSemantics(CodePoint.QRYATTSNS, qryattsns);
            break;
        }
        return qryattsns;
    }

    private int parseFastQRYATTUPD() {
        matchCodePoint(CodePoint.QRYATTUPD);
        int qryattupd = readFastUnsignedByte();
        switch (qryattupd) {
        case CodePoint.QRYUNK:
        case CodePoint.QRYRDO:
        case CodePoint.QRYUPD:
            break;
        default:
            doValnsprmSemantics(CodePoint.QRYATTUPD, qryattupd);
            break;
        }
        return qryattupd;
    }


    private long parseFastQRYINSID() {
        matchCodePoint(CodePoint.QRYINSID);
        return readFastLong();
    }
    
    private int parseFastQRYPRCTYP() {
        matchCodePoint(CodePoint.QRYPRCTYP);
        int qryprctyp = readFastUnsignedShort();
        if ((qryprctyp != CodePoint.FIXROWPRC) && (qryprctyp != CodePoint.LMTBLKPRC)) {
            doValnsprmSemantics(CodePoint.QRYPRCTYP, qryprctyp);
        }
        return qryprctyp;
    }
    
    int parseFastSVRCOD(int minSvrcod, int maxSvrcod)  {
        matchCodePoint(CodePoint.SVRCOD);

        int svrcod = readFastUnsignedShort();
        if ((svrcod != CodePoint.SVRCOD_INFO) &&
                (svrcod != CodePoint.SVRCOD_WARNING) &&
                (svrcod != CodePoint.SVRCOD_ERROR) &&
                (svrcod != CodePoint.SVRCOD_SEVERE) &&
                (svrcod != CodePoint.SVRCOD_ACCDMG) &&
                (svrcod != CodePoint.SVRCOD_PRMDMG) &&
                (svrcod != CodePoint.SVRCOD_SESDMG)) {
            doValnsprmSemantics(CodePoint.SVRCOD, svrcod);
        }

        if (svrcod < minSvrcod || svrcod > maxSvrcod) {
            doValnsprmSemantics(CodePoint.SVRCOD, svrcod);
        }

        return svrcod;
    }
    
    // This method is only used to match the codePoint for those class instance variables
    // that are embedded in other reply messages.
    final protected void matchCodePoint(int expectedCodePoint) {
        int actualCodePoint = 0;
        actualCodePoint = peekedCodePoint_;
        buffer.skipBytes(4);
        //pos_ += 4;
        if (actualCodePoint != expectedCodePoint) {
//            agent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(agent_, 
//                    new ClientMessageId(SQLState.NET_NOT_EXPECTED_CODEPOINT), 
//                    actualCodePoint, expectedCodePoint));
            throw new IllegalStateException("SQLState.NET_NOT_EXPECTED_CODEPOINT");
        }
    }
    
    private void parsePRPSQLSTTreply() { // @AGG removed callback StatementCallbackInterface statement) {
        int peekCP = parseTypdefsOrMgrlvlovrs();

        outputColumnMetaData = new ColumnMetaData();
        if (peekCP == CodePoint.SQLDARD) {
            // the sqlcagrp is most likely null for insert/update/deletes.  if it is null, then we can
            // peek ahead for the column number which most likely will be 0.  if it is 0, then we will
            // not new up a ColumnMetaData, and we can skip the rest of the bytes in sqldard.
            // if sqlcargrp is not null, (most likely for select's) then we will not peek ahead for the
            // column number since it will never be 0 in a select case.
            //ColumnMetaData columnMetaData = null;
            NetSqlca netSqlca = null;
            boolean nullSqlca = peekForNullSqlcagrp();
            if (nullSqlca && peekNumOfColumns() == 0) {
                netSqlca = parseSQLDARD(outputColumnMetaData, true); // true means to skip the rest of SQLDARD bytes
            } else {
                //columnMetaData = ClientDriver.getFactory().newColumnMetaData(netAgent_.logWriter_);
            	// @AGG Moved this up so we get non-null col metadata for SQLDARD AND SQLCARD scenarios
                //outputColumnMetaData = new ColumnMetaData();
                netSqlca = parseSQLDARD(outputColumnMetaData, false); // false means do not skip SQLDARD bytes.
            }

            // @AGG not implemented, checks for error codes on the sqlca
            NetSqlca.complete(netSqlca);
//            statement.completePrepareDescribeOutput(columnMetaData,
//                    netSqlca);
        } else if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);
            NetSqlca.complete(netSqlca);
        } else {
            //throw new IllegalStateException("Unable to prepare statement");
            parsePrepareError();
        }

    }
    
    private void parsePrepareError() { // @AGG removed callback StatementCallbackInterface statement) {
        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM: {
            // passing the StatementCallbackInterface implementation will
            // help in retrieving the the UnitOfWorkListener that needs to
            // be rolled back
            //NetSqlca sqlca = parseAbnormalEndUow(); // @AGG removed callback statement);
            //statement.completeSqlca(sqlca);
            //break;
            throw new UnsupportedOperationException("Abnormal end UOW handling not implemented");
        }
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.DTAMCHRM:
            parseDTAMCHRM();
            break;
        case CodePoint.OBJNSPRM:
            parseOBJNSPRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        case CodePoint.SQLERRRM: {
            NetSqlca sqlca = parseSqlErrorCondition();
            NetSqlca.complete(sqlca);
            break;
        }
        default:
        	throwUnknownCodepoint(peekCP);
            // parseCommonError(peekCP);
        }
    }
    
    NetSqlca parseSqlErrorCondition() {
        parseSQLERRRM();
        parseTypdefsOrMgrlvlovrs();
        NetSqlca netSqlca = parseSQLCARD(null);
        return netSqlca;
    }
    
    // SQL Error Condition Reply Message indicates that an SQL error
    // has occurred.  It may be sent even though no reply message
    // precedes the SQLCARD object that is the normal
    // response to a command when an exception occurs.
    // The SQLERRM is also used when a BNDSQLSTT command is terminated
    // by an INTRRDBRQS command.
    // This reply message must precede an SQLCARD object.
    // The SQLSTATE is returned in the SQLCARD.
    //
    // Returned from Server:
    // SVRCOD - required  (8 - ERROR)
    // RDBNAM - optional
    // SRVDGN - optional
    //
    // Also called by NetResultSetReply and NetStatementReply
    private void parseSQLERRRM() {
    	// TODO @AGG we let this information flow up into the Sqlca so the
    	// user is presented with as much information as possible on errors
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;
        String serverDiagnostics = null;

        parseLengthAndMatchCodePoint(CodePoint.SQLERRRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            
            if (peekCP == CodePoint.SRVDGN) {
            	foundInPass = true;
            	serverDiagnostics = parseSRVDGN();
            	peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
//                doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
//        checkRequiredObjects(svrcodReceived);

        // move into a method
        // @AGG unused netAgent_.setSvrcod(svrcod);
    }
    
    // RDB Not Accessed Reply Message indicates that the access relational
    // database command (ACCRDB) was not issued prior to a command
    // requesting the RDB Services.
    // PROTOCOL Architects an SQLSTATE of 58008 or 58009.
    //
    // Messages
    // SQLSTATE : 58009
    //     Execution failed due to a distribution protocol error that caused deallocation of the conversation.
    //     SQLCODE : -30020
    //     Execution failed because of a Distributed Protocol
    //         Error that will affect the successful execution of subsequent
    //         commands and SQL statements: Reason Code <reason-code>.
    //      Some possible reason codes include:
    //      121C Indicates that the user is not authorized to perform the requested command.
    //      1232 The command could not be completed because of a permanent error.
    //          In most cases, the server will be in the process of an abend.
    //      220A The target server has received an invalid data description.
    //          If a user SQLDA is specified, ensure that the fields are
    //          initialized correctly. Also, ensure that the length does not
    //          exceed the maximum allowed length for the data type being used.
    //
    //      The command or statement cannot be processed.  The current
    //      transaction is rolled back and the application is disconnected
    //      from the remote database.
    //
    //
    // Returned from Server:
    // SVRCOD - required  (8 - ERROR)
    // RDBNAM - required
    //
    // Called by all the NET*Reply classes.
    void parseRDBNACRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;

        parseLengthAndMatchCodePoint(CodePoint.RDBNACRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
//                doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);
//        checkRequiredObjects(svrcodReceived, rdbnamReceived);

        // @AGG unused netAgent_.setSvrcod(svrcod);
        
//        agent_.accumulateChainBreakingReadExceptionAndThrow(
//            new DisconnectException(agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                msgutil_.getTextMessage(MessageId.CONN_DRDA_RDBNACRM)));
        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED");
    }
    
    // Object Not Supported Reply Message indicates that the target
    // server does not recognize or support the object
    // specified as data in an OBJDSS for the command associated
    // with the object.
    // The OBJNSPRM is also returned if an object is found in a
    // valid collection in an OBJDSS (such as RECAL collection)
    // that that is not valid for that collection.
    // PROTOCOL Architects an SQLSTATE of 58015.
    //
    // Messages
    // SQLSTATE : 58015
    //     The DDM object is not supported.
    //     SQLCODE : -30071
    //      <object-identifier> Object is not supported.
    //     The current transaction is rolled back and the application
    //     is disconnected from the remote database. The command
    //     cannot be processed.
    //
    //
    // Returned from Server:
    // SVRCOD - required  (8 - ERROR, 16 - SEVERE)
    // CODPNT - required
    // RECCNT - optional (MINVAL 0)  (will not be returned - should be ignored)
    // RDBNAM - optional (MINLVL 3)
    //
    // Also called by NetPackageReply and NetStatementReply
    void parseOBJNSPRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;
        boolean codpntReceived = false;
        int codpnt = 0;

        parseLengthAndMatchCodePoint(CodePoint.OBJNSPRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_SEVERE);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.CODPNT) {
                foundInPass = true;
                codpntReceived = checkAndGetReceivedFlag(codpntReceived);
                codpnt = parseCODPNT();
                peekCP = peekCodePoint();
            }

            // skip the RECCNT

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);;
//                doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!codpntReceived)
            throwMissingRequiredCodepoint("CODPNT", CodePoint.CODPNT);
//        checkRequiredObjects(svrcodReceived, codpntReceived);

        // @AGG don't think this is used netAgent_.setSvrcod(svrcod);
        
        //doObjnsprmSemantics(codpnt);
        throw new IllegalStateException("SQLState.DRDA_DDM_OBJECT_NOT_SUPPORTED");
    }
    
    // The Code Point Data specifies a scalar value that is an architected code point.
    private int parseCODPNT() {
        parseLengthAndMatchCodePoint(CodePoint.CODPNT);
        return readUnsignedShort();
    }
    
    void parseDTAMCHRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;

        parseLengthAndMatchCodePoint(CodePoint.DTAMCHRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
                //doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);
        //checkRequiredObjects(svrcodReceived, rdbnamReceived);

        // @AGG not used netAgent_.setSvrcod(svrcod);
        
        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED");
        //doDtamchrmSemantics();
    }

    
    // Command Check Reply Message indicates that the requested
    // command encountered an unarchitected and implementation-specific
    // condition for which there is no architected message.  If the severity
    // code value is ERROR or greater, the command has failed.  The
    // message can be accompanied by other messages that help to identify
    // the specific condition.
    // The CMDCHKRM should not be used as a general catch-all in place of
    // product-defined messages when using product extensions to DDM.
    // PROTOCOL architects the SQLSTATE value depending on SVRCOD
    // SVRCOD 0 -> SQLSTATE is not returned
    // SVRCOD 8 -> SQLSTATE of 58008 or 58009
    // SVRCOD 16,32,64,128 -> SQLSTATE of 58009
    //
    // Messages
    //   SQLSTATE : 58009
    //     Execution failed due to a distribution protocol error that caused deallocation of the conversation.
    //     SQLCODE : -30020
    //     Execution failed because of a Distributed Protocol
    //       Error that will affect the successful execution of subsequent
    //       commands and SQL statements: Reason Code <reason-code>.
    //     Some possible reason codes include:
    //       121C Indicates that the user is not authorized to perform the requested command.
    //       1232 The command could not be completed because of a permanent error.
    //         In most cases, the server will be in the process of an abend.
    //       220A The target server has received an invalid data description.
    //         If a user SQLDA is specified, ensure that the fields are
    //         initialized correctly. Also, ensure that the length does not
    //         exceed the maximum allowed length for the data type being used.
    //
    // The command or statement cannot be processed.  The current
    // transaction is rolled back and the application is disconnected
    //  from the remote database.
    //
    //
    // Returned from Server:
    //   SVRCOD - required  (0 - INFO, 4 - WARNING, 8 - ERROR, 16 - SEVERE,
    //                       32 - ACCDMG, 64 - PRMDMG, 128 - SESDMG))
    //   RDBNAM - optional (MINLVL 3)
    //   RECCNT - optional (MINVAL 0, MINLVL 3)
    //
    // Called by all the Reply classesCMDCHKRM
    void parseCMDCHKRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;
        String rdbnam = null;
        parseLengthAndMatchCodePoint(CodePoint.CMDCHKRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_SESDMG);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            // skip over the RECCNT since it can't be found in the DDM book.

            if (peekCP == 0x115C) {
                foundInPass = true;
                parseLengthAndMatchCodePoint(0x115C);
                skipBytes();
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
                //doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
//        checkRequiredObjects(svrcodReceived);

        //netAgent_.setSvrcod(svrcod); @AGG not setting svrcod, doesn't seem to be needed
        NetSqlca netSqlca = parseSQLCARD(null); 
        NetSqlca.complete(netSqlca);

//        agent_.accumulateChainBreakingReadExceptionAndThrow(
//            new DisconnectException(
//                agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                new SqlException(agent_.logWriter_, netSqlca),
//                msgutil_.getTextMessage(MessageId.CONN_DRDA_CMDCHKRM)));
        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED");
    }
    
    // Abnormal End Unit of Work Condition Reply Message indicates
    // that the current unit of work ended abnormally because
    // of some action at the target server.  This can be caused by a
    // deadlock resolution, operator intervention, or some similar
    // situation that caused the relational database to rollback
    // the current unit of work.  This reply message is returned only
    // if an SQLAM issues the command.  Whenever an ABNUOWRM is returned
    // in response to a command, an SQLCARD object must also be returned
    // following the ABNUOWRM.  The SQLSTATE is returned in the SQLCARD.
    //
    // Returned from Server:
    //   SVRCOD - required (8 - ERROR)
    //   RDBNAM - required
    //
    // Called by all the NET*Reply classes.
    private void parseABNUOWRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean rdbnamReceived = false;

        parseLengthAndMatchCodePoint(CodePoint.ABNUOWRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_ERROR, CodePoint.SVRCOD_ERROR);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                // skip the rbbnam since it doesn't tell us anything new.
                // there is no way to return it to the application anyway.
                // not having to convert this to a string is a time saver also.
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                String rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }
        }

        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);

        // the abnuowrm has been received, do whatever state changes are necessary
        //netAgent_.setSvrcod(svrcod);
    }
    
    // Relational Database Name specifies the name of a relational
    // database of the server.  A server can have more than one RDB.
    String parseRDBNAM(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.RDBNAM);
        if (skip) {
            skipBytes();
            return null;
        }
        return readString();
    };
    
//    /**
//     * Perform necessary actions for parsing of a ABNUOWRM message.
//     *
//     * @param s an implementation of the StatementCallbackInterface
//     *
//     * @return an NetSqlca object obtained from parsing the ABNUOWRM
//     *
//     */
//    NetSqlca parseAbnormalEndUow(StatementCallbackInterface s) {
//        return parseAbnormalEndUow(s.getConnectionCallbackInterface(),s);
//    }
//    
//    /**
//     * Perform necessary actions for parsing of a ABNUOWRM message.
//     *
//     * @param r an implementation of the ResultsetCallbackInterface
//     *
//     * @return an NetSqlca object obtained from parsing the ABNUOWRM
//     *
//     */
//    NetSqlca parseAbnormalEndUow(ResultSetCallbackInterface r) {
//        return parseAbnormalEndUow(r.getConnectionCallbackInterface(),r);
//    }
//    /**
//     * Perform necessary actions for parsing of a ABNUOWRM message.
//     *
//     * @param connection an implementation of the ConnectionCallbackInterface
//     *
//     * @return an NetSqlca object obtained from parsing the ABNUOWRM
//     *
//     */
//    private NetSqlca parseAbnormalEndUow(
//            ConnectionCallbackInterface connection,
//            UnitOfWorkListener uwl) {
//
//        parseABNUOWRM();
//        if (peekCodePoint() != CodePoint.SQLCARD) {
//            parseTypdefsOrMgrlvlovrs();
//        }
//
//        NetSqlca netSqlca = parseSQLCARD(null);
//        
////        if(ExceptionUtil.getSeverityFromIdentifier(netSqlca.getSqlState()) > 
////            ExceptionSeverity.STATEMENT_SEVERITY || uwl == null)
//            connection.completeAbnormalUnitOfWork();
////        else
////            connection.completeAbnormalUnitOfWork(uwl);
//        
//        return netSqlca;
//    }
    
    private NetSqlca parseSQLDARD(ColumnMetaData columnMetaData, boolean skipBytes) {
        parseLengthAndMatchCodePoint(CodePoint.SQLDARD);
        return parseSQLDARDarray(columnMetaData, skipBytes);
    }
    
    // SQLDARD : FDOCA EARLY ARRAY
    // SQL Descriptor Area Row Description with SQL Communications Area
    //
    // FORMAT FOR SQLAM <= 6
    //   SQLCARD; ROW LID 0x64; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLDAROW; ROW LID 0x60; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLCARD; ROW LID 0x64; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLDHROW; ROW LID 0xE0; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    //   SQLDAROW; ROW LID 0x60; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    private NetSqlca parseSQLDARDarray(ColumnMetaData columnMetaData, boolean skipBytes) {
        int ddmLength = 0;
//        if (!ensuredLengthForDecryption_ && longValueForDecryption_ == null) {  //if ensuredLength = true, means we already ensured length in decryptData, so don't need to do it again
            ddmLength = getDdmLength();
            ensureBLayerDataInBuffer(ddmLength);
//        }
//        if (longValueForDecryption_ != null) {
//            buffer = longValueForDecryption_;
//            pos_ = 0;
//            count_ = longValueForDecryption_.length;
//        }
            // @AGG assuming no encryption


        NetSqlca netSqlca = null;
        if (skipBytes) {
            int startPos = buffer.readerIndex();
//            mark();
            netSqlca = parseSQLCARDrow(null);
            skipFastBytes(ddmLength - (buffer.readerIndex() - startPos));//getFastSkipSQLCARDrowLength());
            adjustLengths(getDdmLength());
            return netSqlca;
        } else {
            netSqlca = parseSQLCARDrow(null);
        }

        parseSQLDHROW(columnMetaData);
        
//        buffer.skipBytes(6); // @AGG manually added 6 byte skip
        
        int numColumns = parseSQLNUMROW();
        columnMetaData.setColumnCount(numColumns);
//        if (columns > columnMetaData.columns_)  // this will only be true when columnMetaData.columns_ = 0 under deferred prepare
//        // under deferred prepare the CMD arrays are not allocated until now, no guesses were made
//        {
//            columnMetaData.initializeCache(columns);
//        } else // column count was guessed, don't bother reallocating arrays, just truncate their lengths
//        {
//            columnMetaData.columns_ = columns;
//        }

        // is this correct for 0 SQLNUMROW
        // does rest of code expect a null ColumnMetaData object
        // or does rest of code expect an non null object
        // with columns_ set to 0

        for (int i = 0; i < columnMetaData.columns_; i++) {
            parseSQLDAROW(columnMetaData, i);
        }

        // @AGG assuming no encryption
//        if (longValueForDecryption_ == null) {
            adjustLengths(getDdmLength());
//        } else {
//            dssLength_ = 0;
//            longValueForDecryption_ = null;
//        }


        return netSqlca;
    }
    
    // SQLDAROW : FDOCA EARLY ROW
    // SQL Data Area Row Description
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLDAGRP; GROUP LID 0x50; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void parseSQLDAROW(ColumnMetaData columnMetaData, int columnNumber) {
        parseSQLDAGRP(columnMetaData, columnNumber);
    }
    
    // SQLDAGRP : EARLY FDOCA GROUP
    // SQL Data Area Group Description
    //
    // FORMAT FOR SQLAM <= 6
    //   SQLPRECISION; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLSCALE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLLENGTH; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    //   SQLTYPE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLCCSID; PROTOCOL TYPE FB; ENVLID 0x26; Length Override 2
    //   SQLNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 30
    //   SQLNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 30
    //   SQLLABEL_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 30
    //   SQLLABEL_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 30
    //   SQLCOMMENTS_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 254
    //   SQLCOMMENTS_m; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 254
    //
    // FORMAT FOR SQLAM == 6
    //   SQLPRECISION; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLSCALE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLLENGTH; PROTOCOL TYPE I8; ENVLID 0x16; Length Override 8
    //   SQLTYPE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLCCSID; PROTOCOL TYPE FB; ENVLID 0x26; Length Override 2
    //   SQLNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 30
    //   SQLNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 30
    //   SQLLABEL_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 30
    //   SQLLABEL_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 30
    //   SQLCOMMENTS_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 254
    //   SQLCOMMENTS_m; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 254
    //   SQLUDTGRP; PROTOCOL TYPE N-GDA; ENVLID 0x51; Length Override 0
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLPRECISION; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLSCALE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLLENGTH; PROTOCOL TYPE I8; ENVLID 0x16; Length Override 8
    //   SQLTYPE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLCCSID; PROTOCOL TYPE FB; ENVLID 0x26; Length Override 2
    //   SQLDOPTGRP; PROTOCOL TYPE N-GDA; ENVLID 0xD2; Length Override 0
    private void parseSQLDAGRP(ColumnMetaData columnMetaData, int columnNumber) {
        long columnLength = 0;

        // 2-byte precision
        int precision = readFastShort();

        // 2-byte scale
        int scale = readFastShort();

        // 8 byte sqllength
        columnLength = readFastLong();
        
        // create a set method after sqlType and ccsid is read
        // possibly have it set the nullable
        int sqlType = readFastShort();

        // 2-byte sqlccsid
        // (NOTE: SQLCCSID is always flown as BIG ENDIAN, not as data!)
        // The C-Common Client also does the following:
        // 1. Determine which type of code page is to be used for this variable:
        // 2. Map the CCSID to the correct codepage:
        // 3. "Split" the CCSID to see whether it is for SBCS or MBCS:
        int ccsid = readFastUnsignedShort();

        columnMetaData.sqlPrecision_[columnNumber] = precision;
        columnMetaData.sqlScale_[columnNumber] = scale;
        columnMetaData.sqlLength_[columnNumber] = columnLength;
        columnMetaData.sqlType_[columnNumber] = sqlType;
        // Set the nullables
        columnMetaData.nullable_[columnNumber] = (sqlType | 0x01) == sqlType; //Utils.isSqlTypeNullable(sqlType);
        columnMetaData.sqlCcsid_[columnNumber] = ccsid;
        columnMetaData.types_[columnNumber] =
            ClientTypes.mapDB2TypeToDriverType(
                true, sqlType, columnLength, ccsid);
            // true means isDescribed

        parseSQLDOPTGRP(columnMetaData, columnNumber);
    }
    
    // SQLDOPTGRP : EARLY FDOCA GROUP
    // SQL Descriptor Optional Group Description
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLUNNAMED; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLLABEL_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLLABEL_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLCOMMENTS_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLCOMMENTS_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLUDTGRP; PROTOCOL TYPE N-GDA; ENVLID 0x5B; Length Override 0
    //   SQLDXGRP; PROTOCOL TYPE N-GDA; ENVLID 0xD4; Length Override 0
    private void parseSQLDOPTGRP(ColumnMetaData columnMetaData, int columnNumber) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }

        //   SQLUNNAMED; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // has value of 0 or 1. Value of 1 indicates name is generated by the DB.
        // Value of 0 otherwise
        short sqlunnamed = readFastShort(); // 0 or 1

        //   SQLNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String name = parseFastVCMorVCS(); // * or id?

        //   SQLLABEL_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLLABEL_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String label = parseFastVCMorVCS(); // id?

        //   SQLCOMMENTS_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLCOMMENTS_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String colComments = parseFastVCMorVCS();

        if (columnMetaData.sqlName_ == null) {
            columnMetaData.sqlName_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlLabel_ == null) {
            columnMetaData.sqlLabel_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlUnnamed_ == null) {
            columnMetaData.sqlUnnamed_ = new short[columnMetaData.columns_];
        }
        if (columnMetaData.sqlComment_ == null) {
            columnMetaData.sqlComment_ = new String[columnMetaData.columns_];
        }
        columnMetaData.sqlName_[columnNumber] = name;
        columnMetaData.sqlLabel_[columnNumber] = label;
        columnMetaData.sqlUnnamed_[columnNumber] = sqlunnamed;
        columnMetaData.sqlComment_[columnNumber] = colComments;

        // possibly move all the assignments into a single method on the columnMetaData object

        parseSQLUDTGRP(columnMetaData, columnNumber);
        parseSQLDXGRP(columnMetaData, columnNumber);
    }
    
    // SQLUDTGRP : EARLY FDOCA GROUP
    // SQL User-Defined Data Group Description
    //
    // For an explanation of the format, see the header comment on
    // DRDAConnThread.writeSQLUDTGRP().
    //
    private void parseSQLUDTGRP(ColumnMetaData columnMetaData, int columnNumber)
    {
        int jdbcType = columnMetaData.types_[columnNumber];

        if (!(jdbcType == ClientTypes.JAVA_OBJECT)) { // || 
                // !netAgent_.netConnection_.serverSupportsUDTs()) { // @AGG assume supported
            if (readFastUnsignedByte() == CodePoint.NULLDATA) { 
                return; 
            }
        }
        else
        {
            String typeName = parseFastVCMorVCS();
            String className = parseFastVCMorVCS();

            columnMetaData.sqlUDTname_[columnNumber] = typeName;
            columnMetaData.sqlUDTclassName_[columnNumber] = className;
        }
    }
    
    // SQLDXGRP : EARLY FDOCA GROUP
    // SQL Descriptor Extended Group Description
    //
    // FORMAT FOR SQLAM >=7
    //   SQLXKEYMEM; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLXUPDATEABLE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLXGENERATED; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLXPARMMODE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLXRDBNAM; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
    //   SQLXCORNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLXCORNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLXBASENAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLXBASENAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLXSCHEMA_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLXSCHEMA_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    //   SQLXNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLXNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    private void parseSQLDXGRP(ColumnMetaData columnMetaData, int column) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        
        //buffer.skipBytes(8); // @AGG manually skip 8 bytes

        //   SQLXKEYMEM; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        short sqlxkeymem = readFastShort(); // primary key == 1

        //   SQLXUPDATEABLE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        short sqlxupdateable = readFastShort(); // 0 or 1

        //   SQLXGENERATED; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        short sqlxgenerated = readFastShort(); // 0

        //   SQLXPARMMODE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        short sqlxparmmode = readFastShort(); // 0

        //   SQLXRDBNAM; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
        String sqlxrdbnam = parseFastVCS(); // db name

        //   SQLXCORNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLXCORNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String sqlxcorname = parseFastVCMorVCS(); // null or USERS?

        //   SQLXBASENAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLXBASENAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String sqlxbasename = parseFastVCMorVCS(); // USERS

        //   SQLXSCHEMA_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLXSCHEMA_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String sqlxschema = parseFastVCMorVCS(); // username

        //   SQLXNAME_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLXNAME_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String sqlxname = parseFastVCMorVCS(); // ID
        
        // @AGG manually skip 1 unknown byte
        if (!metadata.isZos()) {
          if (readUnsignedByte() != CodePoint.NULLDATA)
              throw new IllegalStateException("@AGG expecting 0xFF here");
        }

        if (columnMetaData.sqlxKeymem_ == null) {
            columnMetaData.sqlxKeymem_ = new short[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxGenerated_ == null) {
            columnMetaData.sqlxGenerated_ = new short[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxParmmode_ == null) {
            columnMetaData.sqlxParmmode_ = new short[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxCorname_ == null) {
            columnMetaData.sqlxCorname_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxName_ == null) {
            columnMetaData.sqlxName_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxBasename_ == null) {
            columnMetaData.sqlxBasename_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxUpdatable_ == null) {
            columnMetaData.sqlxUpdatable_ = new int[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxSchema_ == null) {
            columnMetaData.sqlxSchema_ = new String[columnMetaData.columns_];
        }
        if (columnMetaData.sqlxRdbnam_ == null) {
            columnMetaData.sqlxRdbnam_ = new String[columnMetaData.columns_];
        }

        columnMetaData.sqlxKeymem_[column] = sqlxkeymem;
        columnMetaData.sqlxGenerated_[column] = sqlxgenerated;
        columnMetaData.sqlxParmmode_[column] = sqlxparmmode;
        columnMetaData.sqlxCorname_[column] = sqlxcorname;
        columnMetaData.sqlxName_[column] = sqlxname;
        columnMetaData.sqlxBasename_[column] = sqlxbasename;
        columnMetaData.sqlxUpdatable_[column] = sqlxupdateable;
        columnMetaData.sqlxSchema_[column] = (sqlxschema == null) ? columnMetaData.sqldSchema_ : sqlxschema;
        columnMetaData.sqlxRdbnam_[column] = (sqlxrdbnam == null) ? columnMetaData.sqldRdbnam_ : sqlxrdbnam;
    }
    
    // SQLDHROW : FDOCA EARLY ROW
    // SQL Descriptor Header Row Description
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLDHGRP;  GROUP LID 0xD0; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void parseSQLDHROW(ColumnMetaData columnMetaData) {
        parseSQLDHGRP(columnMetaData);
    }
    
    // SQLDHGRP : EARLY FDOCA GROUP
    // SQL Descriptor Header Group Description
    // See DRDA V3 Vol 1 pg. 288
    //
    // FORMAT FOR SQLAM >= 7
    //   SQLDHOLD; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDRETURN; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDSCROLL; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDSENSITIVE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDFCODE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDKEYTYPE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    //   SQLDRDBNAM; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
    //   SQLDSCHEMA_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
    //   SQLDSCHEMA_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    private void parseSQLDHGRP(ColumnMetaData columnMetaData) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        
        //   SQLDHOLD; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // value of 0 or 1. Value of 1 indicates WITH HOLD. Otherwise 0
        short sqldhold = readFastShort();

        //   SQLDRETURN; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // value of 0, 1, or 2. Value of 1 indicates WITH RETURN CLIENT.
        // value of 2 indicates WITH RETURN CALLER. Otherwise 0
        short sqldreturn = readFastShort();

        //   SQLDSCROLL; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // Value of 1 indicates stmt related to a cursor with SCROLL clause. Otherwise 0
        short sqldscroll = readFastShort();

        //   SQLDSENSITIVE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // 0 statement not related to a cursor and SQLDSCROLL field has value of 0
        // 1 statement related to cursor that is SENSITIVE DYNAMIC
        // 2 statement related to cursor that is SENSITIVE STATIC
        // 3 statement related to cursor that is INSENSITIVE
        short sqldsensitive = readFastShort();

        //   SQLDFCODE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // Type of SQL statement
        short sqldfcode = readFastShort();

        //   SQLDKEYTYPE; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
        // 0 key type when descriptor is not describing the column of a query; for example, a describe input
        // 1 The select list includes all the columns of the primary key of
        // 2 The table referenced by the query does not have a primary key but the select list includes a set of columns that are
        short sqldkeytype = readFastShort();

        //   SQLDRDBNAM; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
        String sqldrdbnam = parseFastVCS();

        //   SQLDSCHEMA_m; PROTOCOL TYPE VCM; ENVLID 0x3E; Length Override 255
        //   SQLDSCHEMA_s; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
        String sqldschema = parseFastVCMorVCS();

        columnMetaData.sqldHold_ = sqldhold;
        columnMetaData.sqldReturn_ = sqldreturn;
        columnMetaData.sqldScroll_ = sqldscroll;
        columnMetaData.sqldSensitive_ = sqldsensitive;
        columnMetaData.sqldFcode_ = sqldfcode;
        columnMetaData.sqldKeytype_ = sqldkeytype;
        columnMetaData.sqldRdbnam_ = sqldrdbnam;
        columnMetaData.sqldSchema_ = sqldschema;
    }
    
//    final int getFastSkipSQLCARDrowLength() {
//        return pos_ - popMark();
//    }

    
    protected final boolean peekForNullSqlcagrp() {
        // skip the 4-byte LLCP and any extended length bytes
        int offset = (4 + peekedNumOfExtendedLenBytes_);
        int nullInd = buffer.getByte(buffer.readerIndex() + offset);
        //int nullInd = buffer_[pos_ + offset] & 0xff;
        return (nullInd == CodePoint.NULLDATA);
    }
    
    protected final int peekNumOfColumns() {
        // skip the 4-byte LLCP and any extended length bytes + 1-byte null sqlcagrp null indicator
        int offset = (4 + peekedNumOfExtendedLenBytes_ + 1);

        offset = skipSQLDHROW(offset);

        //return SignedBinary.getShort(buffer_, pos_ + offset);
        if (metadata.isZos())
          return buffer.getShort(buffer.readerIndex() + offset);
        else
          return buffer.getShortLE(buffer.readerIndex() + offset);
    }
    
    private int skipSQLDHROW(int offset) {
        buffer.markReaderIndex();
        buffer.readerIndex(offset);
        //int sqldhrowgrpNullInd = buffer_[pos_ + offset++] & 0xff;
        int sqldhrowgrpNullInd = buffer.readByte();
        if (sqldhrowgrpNullInd == CodePoint.NULLDATA) {
            return offset;
        }

        offset += 12;

        // skip sqldrdbnam
//        int stringLength = ((buffer_[pos_ + offset++] & 0xff) << 8) +
//                ((buffer_[pos_ + offset++] & 0xff) << 0);
        int stringLength = buffer.readShort();
        offset += stringLength;

        // skip sqldschema
//        stringLength = ((buffer_[pos_ + offset++] & 0xff) << 8) +
//                ((buffer_[pos_ + offset++] & 0xff) << 0);
        stringLength = buffer.readShort();
        offset += stringLength;

//        stringLength = ((buffer_[pos_ + offset++] & 0xff) << 8) +
//                ((buffer_[pos_ + offset++] & 0xff) << 0);
        stringLength = buffer.readShort();
        offset += stringLength;

        buffer.resetReaderIndex();

        return offset;
    }
}
