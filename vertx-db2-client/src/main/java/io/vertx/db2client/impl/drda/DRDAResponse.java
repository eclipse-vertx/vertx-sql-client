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

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import io.netty.buffer.ByteBuf;

public abstract class DRDAResponse {
    
    final ByteBuf buffer;
    final DatabaseMetaData metadata;

    Deque<Integer> ddmCollectionLenStack = new ArrayDeque<>(4);
    int ddmScalarLen_ = 0; // a value of -1 -> streamed ddm -> length unknown

    protected int dssLength_;
    boolean dssIsContinued_;
    private boolean dssIsChainedWithSameID_;
    private int dssCorrelationID_ = 1;

    protected int peekedLength_ = 0;
    int peekedCodePoint_ = END_OF_COLLECTION; // saves the peeked codept
    int peekedNumOfExtendedLenBytes_ = 0;

    final static int END_OF_COLLECTION = -1;
    final static int END_OF_SAME_ID_CHAIN = -2;
    final static int END_OF_BUFFER = -3;

    public DRDAResponse(ByteBuf buffer, DatabaseMetaData metadata) {
        this.buffer = buffer;
        this.metadata = metadata;
    }
    
    protected final void startSameIdChainParse() {
        // TODO: remove this method once everything is ported
        readDssHeader();
    }
    
    protected final void endOfSameIdChainData() {
        if (!ddmCollectionLenStack.isEmpty()) {
            throw new IllegalStateException("SQLState.NET_COLLECTION_STACK_NOT_EMPTY");
//            agent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(agent_, 
//                new ClientMessageId(SQLState.NET_COLLECTION_STACK_NOT_EMPTY)));
        }
        if (this.dssLength_ != 0) {
            throw new IllegalStateException("SQLState.NET_DSS_NOT_ZERO " + dssLength_);
//            agent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(agent_, 
//                new ClientMessageId(SQLState.NET_DSS_NOT_ZERO)));
        }
        if (dssIsChainedWithSameID_ == true) {
            throw new IllegalStateException("SQLState.NET_DSS_CHAINED_WITH_SAME_ID");
//            agent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(agent_, 
//                new ClientMessageId(SQLState.NET_DSS_CHAINED_WITH_SAME_ID)));
        }
    }
    
    // The End Unit of Work Condition (ENDUOWRM) Reply Mesage specifies
    // that the unit of work has ended as a result of the last command.
    //
    // Returned from Server:
    //   SVRCOD - required  (4 WARNING)
    //   UOWDSP - required
    //   RDBNAM - optional
    void parseENDUOWRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean uowdspReceived = false;
        int uowdsp = 0;
        boolean rdbnamReceived = false;
        String rdbnam = null;

        parseLengthAndMatchCodePoint(CodePoint.ENDUOWRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_WARNING, CodePoint.SVRCOD_WARNING);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.UOWDSP) {
                foundInPass = true;
                uowdspReceived = checkAndGetReceivedFlag(uowdspReceived);
                uowdsp = parseUOWDSP();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.RDBNAM) {
                foundInPass = true;
                rdbnamReceived = checkAndGetReceivedFlag(rdbnamReceived);
                rdbnam = parseRDBNAM(true);
                peekCP = peekCodePoint();
            }
            
            if (peekCP == CodePoint.RLSCONV) {
              foundInPass = true;
              parseRLSCONV();
              peekCP = peekCodePoint();
            }


            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
            }
        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!uowdspReceived)
            throwMissingRequiredCodepoint("UOWDSP", CodePoint.UOWDSP);

//        netAgent_.setSvrcod(svrcod);
//        if (uowdsp == CodePoint.UOWDSP_COMMIT) {
//            connection.completeLocalCommit();
//        } else {
//            connection.completeLocalRollback();
//        }
        if (uowdsp == CodePoint.UOWDSP_COMMIT) {
            //System.out.println("@AGG commit completed normally");
        }
    }
    
    private int parseRLSCONV() {
      parseLengthAndMatchCodePoint(CodePoint.RLSCONV);
      int i = readUnsignedByte();
      if (i != 0xF0 && // NO_TERM
          i != 0xF1 && // TERM
          i != 0xF2) { // REUSE
        throw new IllegalStateException("Unknown value for RLSCONV: " + Integer.toHexString(i));
      }
      return i;
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
    }
    
    // Unit of Work Disposition Scalar Object specifies the disposition of the
    // last unit of work.
    private int parseUOWDSP() {
        parseLengthAndMatchCodePoint(CodePoint.UOWDSP);
        int uowdsp = readUnsignedByte();
        if ((uowdsp != CodePoint.UOWDSP_COMMIT) && (uowdsp != CodePoint.UOWDSP_ROLLBACK)) {
            doValnsprmSemantics(CodePoint.UOWDSP, uowdsp);
        }
        return uowdsp;
    }
    
    NetSqlca parseSQLCARD(NetSqlca[] rowsetSqlca) {
        parseLengthAndMatchCodePoint(CodePoint.SQLCARD);
        int ddmLength = getDdmLength();
        ensureBLayerDataInBuffer(ddmLength);
        NetSqlca netSqlca = parseSQLCARDrow(rowsetSqlca);
        adjustLengths(getDdmLength());
        return netSqlca;
    }
    
    // SQLCAGRP : FDOCA EARLY GROUP
    // SQL Communcations Area Group Description
    // See DRDA V3 Vol 1 pg. 280
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
    NetSqlca parseSQLCAGRP(NetSqlca[] rowsetSqlca) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return null;
        }

        int sqlcode = readFastInt();
        byte[] sqlstate = readFastBytes(5);
        byte[] sqlerrproc = readFastBytes(8);
        NetSqlca netSqlca = null;

        netSqlca = new NetSqlca(sqlcode, sqlstate, sqlerrproc);
        parseSQLCAXGRP(netSqlca);

        if (DRDAConstants.TARGET_SQL_AM >= DRDAConstants.MGRLVL_7) {
            netSqlca.setRowsetRowCount(parseSQLDIAGGRP(rowsetSqlca));
        }

        return netSqlca;
    }
    
    // SQLDIAGGRP : FDOCA EARLY GROUP
    // See DRDA V3 Vol 1 pg. 283
    // SQL Diagnostics Group Description - Identity 0xD1
    // Nullable Group
    // SQLDIAGSTT; PROTOCOL TYPE N-GDA; ENVLID 0xD3; Length Override 0
    // SQLDIAGCN;  DRFA TYPE N-RLO; ENVLID 0xF6; Length Override 0
    // SQLDIAGCI;  PROTOCOL TYPE N-RLO; ENVLID 0xF5; Length Override 0
    private long parseSQLDIAGGRP(NetSqlca[] rowsetSqlca) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return 0;
        }

        long row_count = parseSQLDIAGSTT(rowsetSqlca);
        parseSQLDIAGCI(rowsetSqlca);
        parseSQLDIAGCN();

        return row_count;
    }
    
    // SQL Diagnostics Connection Array - Identity 0xF6
    // SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    // SQLCNROW;  ROW LID 0xE6; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    private void parseSQLDIAGCN() {
        if (readUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        int num = parseFastSQLNUMROW();
        for (int i = 0; i < num; i++) {
            parseSQLCNROW();
        }
    }
    
    // SQL Diagnostics Connection Row - Identity 0xE6
    // SQLCNGRP; GROUP LID 0xD6; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void parseSQLCNROW() {
        parseSQLCNGRP();
    }
    
    // SQL Diagnostics Connection Group Description - Identity 0xD6
    // Nullable
    //
    // SQLCNSTATE; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLCNSTATUS; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLCNATYPE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLCNETYPE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLCNPRDID; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 8
    // SQLCNRDB; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
    // SQLCNCLASS; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    // SQLCNAUTHID; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
    private void parseSQLCNGRP() {
        skipBytes(18);
        String sqlcnRDB = parseFastVCS();    // RDBNAM
        String sqlcnClass = parseFastVCS();  // CLASS_NAME
        String sqlcnAuthid = parseFastVCS(); // AUTHID
    }
    
    // SQLNUMROW : FDOCA EARLY ROW
    // SQL Number of Elements Row Description
    //
    // FORMAT FOR SQLAM LEVELS
    //   SQLNUMGRP; GROUP LID 0x58; ELEMENT TAKEN 0(all); REP FACTOR 1
    int parseSQLNUMROW() {
        return parseSQLNUMGRP();
    }

    int parseFastSQLNUMROW() {
        return parseFastSQLNUMGRP();
    }

    // SQLNUMGRP : FDOCA EARLY GROUP
    // SQL Number of Elements Group Description
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLNUM; PROTOCOL TYPE I2; ENVLID 0x04; Length Override 2
    private int parseSQLNUMGRP() {
        return readShort();
    }

    private int parseFastSQLNUMGRP() {
        return readFastShort();
    }
    
    // SQL Diagnostics Statement Group Description - Identity 0xD3
    // Nullable Group
    // SQLDSFCOD; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSCOST; PROTOCOL TYPE I4; ENVLID 0X02; Length Override 4
    // SQLDSLROW; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSNPM; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSNRS; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSRNS; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSDCOD; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDSROWC; PROTOCOL TYPE FD; ENVLID 0x0E; Length Override 31
    // SQLDSNROW; PROTOCOL TYPE FD; ENVLID 0x0E; Length Override 31
    // SQLDSROWCS; PROTOCOL TYPE FD; ENVLID 0x0E; Length Override 31
    // SQLDSACON; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSACRH; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSACRS; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSACSL; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSACSE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSACTY; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSCERR; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    // SQLDSMORE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
    private long parseSQLDIAGSTT(NetSqlca[] rowsetSqlca) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return 0;
        }
        int sqldsFcod = readFastInt(); // FUNCTION_CODE
        int sqldsCost = readFastInt(); // COST_ESTIMATE
        int sqldsLrow = readFastInt(); // LAST_ROW

        skipFastBytes(16);

        long sqldsRowc = readFastLong(); // ROW_COUNT
        //System.out.println("@AGG row count: " + sqldsRowc);

        skipFastBytes(24);

        return sqldsRowc;
    }
    
    // SQL Diagnostics Condition Information Array - Identity 0xF5
    // SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    // SQLDCIROW; ROW LID 0xE5; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    private void parseSQLDIAGCI(NetSqlca[] rowsetSqlca) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        int num = parseFastSQLNUMROW();
        if (num == 0) {
            resetRowsetSqlca(rowsetSqlca, 0);
        }

        // lastRow is the row number for the last row that had a non-null SQLCA.
        int lastRow = 1;
        for (int i = 0; i < num; i++) {
            lastRow = parseSQLDCROW(rowsetSqlca, lastRow);
        }
        resetRowsetSqlca(rowsetSqlca, lastRow + 1);
    }
    
    private void resetRowsetSqlca(NetSqlca[] rowsetSqlca, int row) {
        // rowsetSqlca can be null.
        int count = ((rowsetSqlca == null) ? 0 : rowsetSqlca.length);
        for (int i = row; i < count; i++) {
            rowsetSqlca[i] = null;
        }
    }
    
    // SQL Diagnostics Condition Row - Identity 0xE5
    // SQLDCGRP; GROUP LID 0xD5; ELEMENT TAKEN 0(all); REP FACTOR 1
    private int parseSQLDCROW(NetSqlca[] rowsetSqlca, int lastRow) {
        return parseSQLDCGRP(rowsetSqlca, lastRow);
    }
    
    // SQL Diagnostics Condition Group Description
    //
    // SQLDCCODE; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCSTATE; PROTOCOL TYPE FCS; ENVLID Ox30; Lengeh Override 5
    // SQLDCREASON; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCLINEN; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCROWN; PROTOCOL TYPE FD; ENVLID 0x0E; Lengeh Override 31
    // SQLDCER01; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER02; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER03; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCER04; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCPART; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCPPOP; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
    // SQLDCMSGID; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 10
    // SQLDCMDE; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 8
    // SQLDCPMOD; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 5
    // SQLDCRDB; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 255
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
    private int parseSQLDCGRP(NetSqlca[] rowsetSqlca, int lastRow) {
        int sqldcCode = readFastInt(); // SQLCODE
        String sqldcState = readFastString(5, CCSIDConstants.UTF8); // SQLSTATE
        int sqldcReason = readFastInt();  // REASON_CODE
        int sqldcLinen = readFastInt(); // LINE_NUMBER
        int sqldcRown = (int) readFastLong(); // ROW_NUMBER

        // save +20237 in the 0th entry of the rowsetSqlca's.
        // this info is going to be used when a subsequent fetch prior is issued, and if already
        // received a +20237 then we've gone beyond the first row and there is no need to
        // flow another fetch to the server.
        if (sqldcCode == 20237) {
            rowsetSqlca[0] = new NetSqlca(sqldcCode,
                    sqldcState,
                    null);
        } else {
            if (rowsetSqlca[sqldcRown] != null) {
                rowsetSqlca[sqldcRown].resetRowsetSqlca(sqldcCode,
                        sqldcState);
            } else {
                rowsetSqlca[sqldcRown] = new NetSqlca(sqldcCode,
                        sqldcState,
                        null);
            }
        }

        // reset all entries between lastRow and sqldcRown to null
        for (int i = lastRow + 1; i < sqldcRown; i++) {
            rowsetSqlca[i] = null;
        }

        skipFastBytes(47);
        String sqldcRdb = parseFastVCS(); // RDBNAM
        // skip the tokens for now, since we already have the complete message.
        parseSQLDCTOKS(); // MESSAGE_TOKENS
        String sqldcMsg = parseFastNVCMorNVCS(); // MESSAGE_TEXT

        // skip the following for now.
        skipFastNVCMorNVCS();  // COLUMN_NAME
        skipFastNVCMorNVCS();  // PARAMETER_NAME
        skipFastNVCMorNVCS();  // EXTENDED_NAMES

        parseSQLDCXGRP(); // SQLDCXGRP
        return sqldcRown;
    }
    
    // Severity Code is an indicator of the severity of a condition
    // detected during the execution of a command.
    int parseSVRCOD(int minSvrcod, int maxSvrcod) {
        parseLengthAndMatchCodePoint(CodePoint.SVRCOD);

        int svrcod = readUnsignedShort();
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
    
    // Also called by NetStatementReply
    void doValnsprmSemantics(int codePoint, int value) {
        doValnsprmSemantics(codePoint, Integer.toString(value));
    }

    private void doValnsprmSemantics(int codePoint, String value) {

        // special case the FDODTA codepoint not to disconnect.
        if (codePoint == CodePoint.FDODTA) {
            throw new IllegalStateException("SQLState.DRDA_DDM_PARAMVAL_NOT_SUPPORTED codePoint=" + Integer.toHexString(codePoint));
//            agent_.accumulateReadException(new SqlException(agent_.logWriter_,
//                new ClientMessageId(SQLState.DRDA_DDM_PARAMVAL_NOT_SUPPORTED),
//                Integer.toHexString(codePoint)));
        }

        if (codePoint == CodePoint.CCSIDSBC ||
                codePoint == CodePoint.CCSIDDBC ||
                codePoint == CodePoint.CCSIDMBC) {
            // the server didn't like one of the ccsids.
            // the message should reflect the error in question.  right now these values
            // will be hard coded but this won't be correct if our driver starts sending
            // other values to the server.  In order to pick up the correct values,
            // a little reorganization may need to take place so that this code (or
            // whatever code sets the message) has access to the correct values.
            int cpValue = 0;
            switch (codePoint) {
            case CodePoint.CCSIDSBC:
                cpValue = Typdef.typdef.getCcsidSbc();
                break;
            case CodePoint.CCSIDDBC:
                cpValue = Typdef.typdef.getCcsidDbc();
                break;
            case CodePoint.CCSIDMBC:
                cpValue = Typdef.typdef.getCcsidSbc();
                break;
            default:
                // should never be in this default case...
                break;
            }
            throw new IllegalStateException("SQLState.DRDA_NO_AVAIL_CODEPAGE_CONVERSION");
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.DRDA_NO_AVAIL_CODEPAGE_CONVERSION),
//                cpValue, value));
//            return;
        }
        // the problem isn't with one of the ccsid values so...

        throw new IllegalStateException("SQLState.DRDA_DDM_PARAMVAL_NOT_SUPPORTED");
        // Returning more information would
        // require rearranging this code a little.
//        agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//            new ClientMessageId(SQLState.DRDA_DDM_PARAMVAL_NOT_SUPPORTED),
//            Integer.toHexString(codePoint)));
    }
    
    // SQL Diagnostics Extended Names Group Description - Identity 0xD5
    // Nullable
    //
    // SQLDCXRDB_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 1024
    // SQLDCXSCH_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXNAM_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXTBLN_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXRDB_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 1024
    // SQLDCXSCH_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXNAM_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXTBLN_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    //
    // SQLDCXCRDB_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 1024
    // SQLDCXCSCH_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXCNAM_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXCRDB_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 1024
    // SQLDCXCSCH_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXCNAM_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    //
    // SQLDCXRRDB_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 1024
    // SQLDCXRSCH_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXRNAM_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXRRDB_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 1024
    // SQLDCXRSCH_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXRNAM_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    //
    // SQLDCXTRDB_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 1024
    // SQLDCXTSCH_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXTNAM_m ; PROTOCOL TYPE NVCM; ENVLID 0x3F; Length Override 255
    // SQLDCXTRDB_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 1024
    // SQLDCXTSCH_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    // SQLDCXTNAM_s ; PROTOCOL TYPE NVCS; ENVLID 0x33; Length Override 255
    private void parseSQLDCXGRP() {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        skipFastNVCMorNVCS();  // OBJECT_RDBNAM
        skipFastNVCMorNVCS();  // OBJECT_SCHEMA
        skipFastNVCMorNVCS();  // SPECIFIC_NAME
        skipFastNVCMorNVCS();  // TABLE_NAME
        String sqldcxCrdb = parseFastVCS();        // CONSTRAINT_RDBNAM
        skipFastNVCMorNVCS();  // CONSTRAINT_SCHEMA
        skipFastNVCMorNVCS();  // CONSTRAINT_NAME
        parseFastVCS();        // ROUTINE_RDBNAM
        skipFastNVCMorNVCS();  // ROUTINE_SCHEMA
        skipFastNVCMorNVCS();  // ROUTINE_NAME
        parseFastVCS();        // TRIGGER_RDBNAM
        skipFastNVCMorNVCS();  // TRIGGER_SCHEMA
        skipFastNVCMorNVCS();  // TRIGGER_NAME
    }
    
    private String parseFastNVCMorNVCS() {
        String stringToBeSet = null;
        if (readFastUnsignedByte() != CodePoint.NULLDATA) {
            int vcm_length = readFastUnsignedShort();
            if (vcm_length > 0) {
                stringToBeSet = readFastString(vcm_length, Typdef.targetTypdef.getCcsidMbcEncoding());
            }
            if (readFastUnsignedByte() != CodePoint.NULLDATA) {
                throw new IllegalStateException("SQLState.NET_NVCM_NVCS_BOTH_NON_NULL");
//                agent_.accumulateChainBreakingReadExceptionAndThrow(
//                    new DisconnectException(agent_,
//                        new ClientMessageId(
//                            SQLState.NET_NVCM_NVCS_BOTH_NON_NULL)));
            }
        } else {
            if (readFastUnsignedByte() != CodePoint.NULLDATA) {
                int vcs_length = readFastUnsignedShort();
                if (vcs_length > 0) {
                    stringToBeSet = readFastString(vcs_length, Typdef.targetTypdef.getCcsidSbcEncoding());
                }
            }
        }
        return stringToBeSet;
    }
    
    // SQL Diagnostics Condition Token Array - Identity 0xF7
    // SQLNUMROW; ROW LID 0x68; ELEMENT TAKEN 0(all); REP FACTOR 1
    // SQLTOKROW; ROW LID 0xE7; ELEMENT TAKEN 0(all); REP FACTOR 0(all)
    private void parseSQLDCTOKS() {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            return;
        }
        int num = parseFastSQLNUMROW();
        for (int i = 0; i < num; i++) {
            parseSQLTOKROW();
        }
    }
    
    // SQL Diagnostics Token Row - Identity 0xE7
    // SQLTOKGRP; GROUP LID 0xD7; ELEMENT TAKEN 0(all); REP FACTOR 1
    private void parseSQLTOKROW() {
        parseSQLTOKGRP();
    }

    // check on SQLTOKGRP format
    private void parseSQLTOKGRP() {
        skipFastNVCMorNVCS();
    }
    
    private void skipFastNVCMorNVCS() {
        if (readFastUnsignedByte() != CodePoint.NULLDATA) {
            int vcm_length = readFastUnsignedShort();
            if (vcm_length > 0)
            {
                skipFastBytes(vcm_length);
            }
            if (readFastUnsignedByte() != CodePoint.NULLDATA) {
                throw new IllegalStateException("SQLState.NET_NVCM_NVCS_BOTH_NON_NULL");
//                agent_.accumulateChainBreakingReadExceptionAndThrow(
//                    new DisconnectException(agent_,
//                        new ClientMessageId(
//                            SQLState.NET_NVCM_NVCS_BOTH_NON_NULL)));
            }
        } else {
            if (readFastUnsignedByte() != CodePoint.NULLDATA) {
                int vcs_length = readFastUnsignedShort();
                if (vcs_length > 0)
                {
                    skipFastBytes(vcs_length);
                }
            }
        }
    }
    
    // SQLCAXGRP : EARLY FDOCA GROUP
    // SQL Communications Area Exceptions Group Description
    // See DRDA V3 Vol 1 pg. 281
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
    private void parseSQLCAXGRP(NetSqlca netSqlca) {
        if (readFastUnsignedByte() == CodePoint.NULLDATA) {
            netSqlca.setContainsSqlcax(false);
            return;
        }
        
//        if (DRDAConstants.TARGET_SQL_AM < DRDAConstants.MGRLVL_7) {
//            // skip over the rdbnam for now
//            //   SQLRDBNME; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 18
//            skipFastBytes(18);
//        }
        //   SQLERRD1 to SQLERRD6; PROTOCOL TYPE I4; ENVLID 0x02; Length Override 4
        int[] sqlerrd = new int[ NetSqlca.SQL_ERR_LENGTH ];
        readFastIntArray(sqlerrd);

        //   SQLWARN0 to SQLWARNA; PROTOCOL TYPE FCS; ENVLID 0x30; Length Override 1
        byte[] sqlwarn = readFastBytes(11);

        if (DRDAConstants.TARGET_SQL_AM >= DRDAConstants.MGRLVL_7) {
            // skip over the rdbnam for now
            // SQLRDBNAME; PROTOCOL TYPE VCS; ENVLID 0x32; Length Override 1024
            String rdbname = parseFastVCS();
        }


        int sqlerrmcCcsid;
        byte[] sqlerrmc = readFastLDBytes();
        if (sqlerrmc != null) {
            sqlerrmcCcsid = Typdef.targetTypdef.getCcsidMbc();
            skipFastBytes(2);
        } else {
            sqlerrmc = readFastLDBytes();
            sqlerrmcCcsid = Typdef.targetTypdef.getCcsidSbc();
        }
        
        netSqlca.setSqlerrd(sqlerrd);
        netSqlca.setSqlwarnBytes(sqlwarn);
        netSqlca.setSqlerrmcBytes(sqlerrmc); // sqlerrmc may be null
    }
    
    // this is duplicated in parseColumnMetaData, but different
    // DAGroup under NETColumnMetaData requires a lot more stuffs including
    // precsion, scale and other stuffs
    String parseFastVCMorVCS() {
        String stringToBeSet = null;

        int vcm_length = readFastUnsignedShort();
        if (vcm_length > 0) {
            stringToBeSet = readFastString(vcm_length, Typdef.targetTypdef.getCcsidMbcEncoding());
        }
        int vcs_length = readFastUnsignedShort();
        if (vcm_length > 0 && vcs_length > 0) {
            throw new IllegalStateException("SQLState.NET_VCM_VCS_LENGTHS_INVALID: VCM and VCS lengths are mutually exclusive " +
        "but both were set: vcsLen=" + vcs_length + "  vcmLen=" + vcm_length);
//            agent_.accumulateChainBreakingReadExceptionAndThrow(new DisconnectException(agent_,
//                new ClientMessageId(SQLState.NET_VCM_VCS_LENGTHS_INVALID)));
        } else if (vcs_length > 0) {
            stringToBeSet = readFastString(vcs_length, Typdef.targetTypdef.getCcsidSbcEncoding());
        }

        return stringToBeSet;
    }
    
    // SQLCARD : FDOCA EARLY ROW
    // SQL Communications Area Row Description
    //
    // FORMAT FOR ALL SQLAM LEVELS
    //   SQLCAGRP; GROUP LID 0x54; ELEMENT TAKEN 0(all); REP FACTOR 1
    NetSqlca parseSQLCARDrow(NetSqlca[] rowsetSqlca) {
        return parseSQLCAGRP(rowsetSqlca);
    }

    int parseTypdefsOrMgrlvlovrs() {
        boolean targetTypedefCloned = false;
        while (true) {
            int peekCP = peekCodePoint();
            if (peekCP == CodePoint.TYPDEFNAM) {
                if (!targetTypedefCloned) {
                    //netAgent_.targetTypdef_ = (Typdef) netAgent_.targetTypdef_.clone(); @AGG not sure if used?
                    targetTypedefCloned = true;
                }
                parseTYPDEFNAM();
            } else if (peekCP == CodePoint.TYPDEFOVR) {
                if (!targetTypedefCloned) {
                    //netAgent_.targetTypdef_ = (Typdef) netAgent_.targetTypdef_.clone(); @AGG not sure if used?
                    targetTypedefCloned = true;
                }
                parseTYPDEFOVR();
            } else {
                return peekCP;
            }
        }
    }
    
    void parseTYPDEFNAM() {
        parseLengthAndMatchCodePoint(CodePoint.TYPDEFNAM);
        String typedef = readString();
        Typdef.targetTypdef.setTypdefnam(typedef);
    }
    
    void parseTYPDEFOVR() {
        parseLengthAndMatchCodePoint(CodePoint.TYPDEFOVR);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.CCSIDSBC) {
                foundInPass = true;
                Typdef.targetTypdef.setCcsidSbc(parseCCSIDSBC());
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.CCSIDDBC) {
                foundInPass = true;
                Typdef.targetTypdef.setCcsidDbc(parseCCSIDDBC());
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.CCSIDMBC) {
                foundInPass = true;
                Typdef.targetTypdef.setCcsidMbc(parseCCSIDMBC());
                peekCP = peekCodePoint();
            }
            
            // @AGG added this block
            if (peekCP == CodePoint.CCSIDXML) {
                parseLengthAndMatchCodePoint(CodePoint.CCSIDXML);
                readUnsignedShort();
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throwUnknownCodepoint(peekCP);
                //doPrmnsprmSemantics(peekCP);
            }

        }
        popCollectionStack();
    }
    
    static void throwUnknownCodepoint(int codepoint) {
        throw new IllegalStateException("Found unknown codepoint: 0x" + Integer.toHexString(codepoint));
    }
    
    static void throwMissingRequiredCodepoint(String codepointStr, int expectedCodepoint) {
        throw new IllegalStateException("Did not find required " + codepointStr + " (" + Integer.toHexString(expectedCodepoint) + ") codepoint");
    }
    
    // CCSID for Single-Byte Characters specifies a coded character
    // set identifier for single-byte characters.
    private int parseCCSIDSBC() {
        parseLengthAndMatchCodePoint(CodePoint.CCSIDSBC);
        return readUnsignedShort();
    }

    // CCSID for Mixed-Byte Characters specifies a coded character
    // set identifier for mixed-byte characters.
    private int parseCCSIDMBC() {
        parseLengthAndMatchCodePoint(CodePoint.CCSIDMBC);
        return readUnsignedShort();
    }

    // CCSID for Double-Byte Characters specifies a coded character
    // set identifier for double-byte characters.
    private int parseCCSIDDBC() {
        parseLengthAndMatchCodePoint(CodePoint.CCSIDDBC);
        return readUnsignedShort();
    }
    
    private void readDssHeader() {
        int correlationID;
        int nextCorrelationID;
        ensureALayerDataInBuffer(6);

        // read out the dss length
        dssLength_ = buffer.readUnsignedShort();

        // Remember the old dss length for decryption only.
        int oldDssLength = dssLength_;

        // check for the continuation bit and update length as needed.
        if ((dssLength_ & 0x8000) == 0x8000) {
            dssLength_ = 32767;
            dssIsContinued_ = true;
        } else {
            dssIsContinued_ = false;
        }

        if (dssLength_ < 6) {
            throw new IllegalStateException("DSS header length must be at least 6 bytes but was: " + dssLength_);
            // doSyntaxrmSemantics(CodePoint.SYNERRCD_DSS_LESS_THAN_6);
        }

        // If the GDS id is not valid, or
        // if the reply is not an RPYDSS nor
        // a OBJDSS, then throw an exception.
        byte magic = buffer.readByte();
        if (magic != (byte) 0xd0) {
            throw new IllegalStateException(String.format("Magic bit needs to be 0xD0 but was %02x", magic));
            // doSyntaxrmSemantics(CodePoint.SYNERRCD_CBYTE_NOT_D0);
        }

        int gdsFormatter = buffer.readByte() & 0xFF;
        if (((gdsFormatter & 0x02) != 0x02) && ((gdsFormatter & 0x03) != 0x03) && ((gdsFormatter & 0x04) != 0x04)) {
            throw new IllegalStateException("CodePoint.SYNERRCD_FBYTE_NOT_SUPPORTED");
            // doSyntaxrmSemantics(CodePoint.SYNERRCD_FBYTE_NOT_SUPPORTED);
        }

        // Determine if the current DSS is chained with the
        // next DSS, with the same or different request ID.
        if ((gdsFormatter & 0x40) == 0x40) { // on indicates structure chained to next structure
            if ((gdsFormatter & 0x10) == 0x10) {
                dssIsChainedWithSameID_ = true;
                nextCorrelationID = dssCorrelationID_;
            } else {
                dssIsChainedWithSameID_ = false;
                nextCorrelationID = dssCorrelationID_ + 1;
            }
        } else {
            // chaining bit not b'1', make sure DSSFMT bit3 not b'1'
            if ((gdsFormatter & 0x10) == 0x10) { // Next DSS can not have same correlator
                throw new IllegalStateException("CodePoint.SYNERRCD_CHAIN_OFF_SAME_NEXT_CORRELATOR");
                // doSyntaxrmSemantics(CodePoint.SYNERRCD_CHAIN_OFF_SAME_NEXT_CORRELATOR);
            }

            // chaining bit not b'1', make sure no error continuation
            if ((gdsFormatter & 0x20) == 0x20) { // must be 'do not continue on error'
                throw new IllegalStateException("CodePoint.SYNERRCD_CHAIN_OFF_ERROR_CONTINUE");
                // doSyntaxrmSemantics(CodePoint.SYNERRCD_CHAIN_OFF_ERROR_CONTINUE);
            }

            dssIsChainedWithSameID_ = false;
            nextCorrelationID = 1;
        }

        correlationID = buffer.readShort();

        // corrid must be the one expected or a -1 which gets returned in some error
        // cases.
        if ((correlationID != dssCorrelationID_) && (correlationID != 0xFFFF)) {
            // doSyntaxrmSemantics(CodePoint.SYNERRCD_INVALID_CORRELATOR);
            throw new IllegalStateException(
                    "Invalid correlator ID. Got " + correlationID + " expected " + dssCorrelationID_);
        } else {
            dssCorrelationID_ = nextCorrelationID;
        }
        dssLength_ -= 6;
        // if ((gdsFormatter & 0x04) == 0x04) {
        // decryptData(gdsFormatter, oldDssLength); //we have to decrypt data here
        // because
        // }
        // we need the decrypted codepoint. If
        // Data is very long > 32767, we have to
        // get all the data first because decrypt
        // piece by piece doesn't work.
    }
    
    // reads a DSS continuation header
    // prereq: pos_ is positioned on the first byte of the two-byte header
    // post:   dssIsContinued_ is set to true if the continuation bit is on, false otherwise
    //         dssLength_ is set to DssConstants.MAX_DSS_LEN - 2 (don't count the header for the next read)
    // helper method for getEXTDTAData
    void readDSSContinuationHeader() {
        ensureALayerDataInBuffer(2);

        dssLength_ = buffer.readUnsignedShort();
//                ((buffer_[pos_++] & 0xFF) << 8) +
//                ((buffer_[pos_++] & 0xFF) << 0);

        if ((dssLength_ & 0x8000) == 0x8000) {
            dssLength_ = DssConstants.MAX_DSS_LENGTH;
            dssIsContinued_ = true;
        } else {
            dssIsContinued_ = false;
        }
        // it is a syntax error if the dss continuation header length
        // is less than or equal to two
        if (dssLength_ <= 2) {
            doSyntaxrmSemantics(CodePoint.SYNERRCD_DSS_CONT_LESS_OR_EQUAL_2);
        }

        dssLength_ -= 2;  // avoid consuming the DSS cont header
    }
    
    final String readString() {
        int len = ddmScalarLen_;
        ensureBLayerDataInBuffer(len);
        adjustLengths(len);
        String result = buffer.readCharSequence(len, metadata.getCCSID()).toString();
//        String result = currentCCSID.decode(buffer); 
//                netAgent_.getCurrentCcsidManager()
//                            .convertToJavaString(buffer_, pos_, len);
//        pos_ += len;
        return result;
    }
    
    final String readString(int length, Charset encoding) {
        ensureBLayerDataInBuffer(length);
        adjustLengths(length);
        String s = buffer.readCharSequence(length, encoding).toString();
        //String s = new String(buffer.array(), pos_, length, encoding);
        //pos_ += length;
        return s;
    }
    
    final short readShort() {
        // should we be checking dss lengths and ddmScalarLengths here
        ensureBLayerDataInBuffer(2);
        adjustLengths(2);
//        short s = SignedBinary.getShort(buffer_, pos_);

        //pos_ += 2;
        if (metadata.isZos())
          return buffer.readShort();
        else
          return buffer.readShortLE();
    }

    boolean checkAndGetReceivedFlag(boolean receivedFlag){
        if (receivedFlag) {
            // this method will throw a disconnect exception if
            // the received flag is already true;
            doSyntaxrmSemantics(CodePoint.SYNERRCD_DUP_OBJ_PRESENT);
        }
        return true;
    }
    
    final void doSyntaxrmSemantics(int syntaxErrorCode) {
        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED CONN_DRDA_DATASTREAM_SYNTAX_ERROR " + syntaxErrorCode);
//        DisconnectException e = new DisconnectException(agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                SqlException.getMessageUtil().getTextMessage(
//                    MessageId.CONN_DRDA_DATASTREAM_SYNTAX_ERROR,
//                    syntaxErrorCode));
            
        // if we are communicating to an older server, we may get a SYNTAXRM on
        // ACCSEC (missing codepoint RDBNAM) if we were unable to convert to
        // EBCDIC.  In that case we should chain 
        // the original conversion exception, so it is clear to the user what
        // the problem was.
//        if (netAgent_.exceptionConvertingRdbnam != null) {
//            e.setNextException(netAgent_.exceptionConvertingRdbnam);
//            netAgent_.exceptionConvertingRdbnam = null;
//        }
//        agent_.accumulateChainBreakingReadExceptionAndThrow(e);
    }
    
    // Read "length" number of bytes from the buffer into the byte array b starting from offset
    // "offset".  The current offset in the buffer does not change.
    protected final int peekFastBytes(byte[] b, int offset, int length) {
        for (int i = 0; i < length; i++) {
            b[offset + i] = buffer.getByte(buffer.readerIndex() + i);
//            b[offset + i] = buffer_[pos_ + i];
        }
        return offset + length;
    }
    
    protected final int peekFastLength() {
        return buffer.getUnsignedShort(buffer.readerIndex());
//        return (((buffer_[pos_] & 0xff) << 8) +
//                ((buffer_[pos_ + 1] & 0xff) << 0));
    }

    protected final int peekCodePoint() {
        if (!ddmCollectionLenStack.isEmpty()) {
            if (ddmCollectionLenStack.peek() == 0) {
                return END_OF_COLLECTION;
            } else if (ddmCollectionLenStack.peek() < 4) {
                // error
                throw new IllegalStateException("Invalid ddm collection length: " + ddmCollectionLenStack.peek());
            }
        }

        // if there is no more data in the current dss, and the dss is not
        // continued, indicate the end of the same Id chain or read the next dss header.
        if ((dssLength_ == 0) && (!dssIsContinued_)) {
            if (!dssIsChainedWithSameID_) {
                return END_OF_SAME_ID_CHAIN;
            }
            if (!buffer.isReadable()) {
              return END_OF_BUFFER;
            }
            readDssHeader();
        }

        // if (longBufferForDecryption_ == null) //we don't need to do this if it's data
        // stream encryption
        // {
        // ensureBLayerDataInBuffer(4);
        // }
        peekedLength_ = buffer.getUnsignedShort(buffer.readerIndex()); //buffer.readShort();// ((buffer_[pos_] & 0xff) << 8) + ((buffer_[pos_ + 1] & 0xff) << 0);
        peekedCodePoint_ = buffer.getUnsignedShort(buffer.readerIndex() + 2); //buffer.readShort(); // ((buffer_[pos_ + 2] & 0xff) << 8) + ((buffer_[pos_ + 3] & 0xff) << 0);

        // check for extended length
        if ((peekedLength_ & 0x8000) == 0x8000) {
            peekExtendedLength();
        } else {
            peekedNumOfExtendedLenBytes_ = 0;
        }
        return peekedCodePoint_;
    }
    
    protected final void pushLengthOnCollectionStack() {
        ddmCollectionLenStack.push(ddmScalarLen_);
        ddmScalarLen_ = 0;
//        System.out.println("@AGG pushed length: " + ddmCollectionLenStack);
    }
    
    protected final void parseLengthAndMatchCodePoint(int expectedCodePoint) {
        int actualCodePoint = 0;
        if (peekedCodePoint_ == END_OF_COLLECTION) {
            actualCodePoint = readLengthAndCodePoint();
        } else {
            actualCodePoint = peekedCodePoint_;
            //pos_ += (4 + peekedNumOfExtendedLenBytes_);
            buffer.readerIndex(buffer.readerIndex() + (4 + peekedNumOfExtendedLenBytes_));
            ddmScalarLen_ = peekedLength_;
            if (peekedNumOfExtendedLenBytes_ == 0 && ddmScalarLen_ != -1) {
                adjustLengths(4);
            } else {
                adjustCollectionAndDssLengths(4 + peekedNumOfExtendedLenBytes_);
            }
            peekedLength_ = 0;
            peekedCodePoint_ = END_OF_COLLECTION;
            peekedNumOfExtendedLenBytes_ = 0;
        }

        if (actualCodePoint != expectedCodePoint) {
            throw new IllegalStateException("Expected code point " + Integer.toHexString(expectedCodePoint)
                    + " but was " + Integer.toHexString(actualCodePoint));
        }
    }

    private int readLengthAndCodePoint() {
        if (!ddmCollectionLenStack.isEmpty()) {
            if (ddmCollectionLenStack.peek() == 0) {
                return END_OF_COLLECTION;
            } else if (ddmCollectionLenStack.peek() < 4) {
                // error
                throw new IllegalStateException("Invalid ddm collection length: " + ddmCollectionLenStack.peek());
            }
        }

        // if there is no more data in the current dss, and the dss is not
        // continued, indicate the end of the same Id chain or read the next dss header.
        if ((dssLength_ == 0) && (!dssIsContinued_)) {
            if (!dssIsChainedWithSameID_) {
                return END_OF_SAME_ID_CHAIN;
            }
            readDssHeader();
        }

        ensureBLayerDataInBuffer(4);
        ddmScalarLen_ = buffer.readUnsignedShort();
//                ((buffer_[pos_++] & 0xff) << 8) +
//                ((buffer_[pos_++] & 0xff) << 0);
        int codePoint = buffer.readUnsignedShort();
//                ((buffer_[pos_++] & 0xff) << 8) +
//                ((buffer_[pos_++] & 0xff) << 0);
        adjustLengths(4);

        // check for extended length
        if ((ddmScalarLen_ & 0x8000) == 0x8000) {
            readExtendedLength();
        }
        return codePoint;
    }

    private void readExtendedLength() {
        int numberOfExtendedLenBytes = (ddmScalarLen_ - 0x8000); // fix scroll problem was - 4
        int adjustSize = 0;
        switch (numberOfExtendedLenBytes) {
        case 4:
            ensureBLayerDataInBuffer(4);
            ddmScalarLen_ = (int) buffer.readUnsignedInt();
//                    ((buffer_[pos_++] & 0xff) << 24) +
//                    ((buffer_[pos_++] & 0xff) << 16) +
//                    ((buffer_[pos_++] & 0xff) << 8) +
//                    ((buffer_[pos_++] & 0xff) << 0);
            adjustSize = 4;
            break;
        case 0:
            ddmScalarLen_ = -1;
            adjustSize = 0;
            break;
        default:
            throw new IllegalStateException("CodePoint.SYNERRCD_INCORRECT_EXTENDED_LEN");
            //doSyntaxrmSemantics(CodePoint.SYNERRCD_INCORRECT_EXTENDED_LEN);
        }

        adjustCollectionAndDssLengths(adjustSize);
    }

    private void adjustCollectionAndDssLengths(int length) {
        // adjust the lengths here.  this is a special case since the
        // extended length bytes do not include their own length.
        Deque<Integer> original = ddmCollectionLenStack;
        ddmCollectionLenStack = new ArrayDeque<>(original.size());
        while (!original.isEmpty()) {
            ddmCollectionLenStack.add(original.pop() - length);
        }
        dssLength_ -= length;
        if (dssLength_ < 0)
          throw new IllegalStateException("DSS length has gone negative: " + dssLength_);
//        System.out.println("@AGG reduced len by " + length + " stack is now: " + ddmCollectionLenStack);
    }
    
    protected final void adjustLengths(int length) {
        ddmScalarLen_ -= length;
        if (ddmScalarLen_ < 0)
          throw new IllegalStateException("DDM scalar length has gone negative: " + ddmScalarLen_);
        adjustCollectionAndDssLengths(length);
    }

    protected int adjustDdmLength(int ddmLength, int length) {
        ddmLength -= length;
        if (ddmLength == 0) {
            adjustLengths(getDdmLength());
        }
        return ddmLength;
    }
    
    final int getDdmLength() {
        return ddmScalarLen_;
    }

    private void peekExtendedLength() {
        peekedNumOfExtendedLenBytes_ = (peekedLength_ - 0x8004);
        switch (peekedNumOfExtendedLenBytes_) {
        case 4:
            // L L C P Extended Length
            // -->2-bytes<-- --->4-bytes<---
            // We are only peeking the length here, the actual pos_ is still before LLCP. We ensured
            // 4-bytes in peedCodePoint() for the LLCP, and we need to ensure 4-bytes(of LLCP) + the
            // extended length bytes here.
            // if (longBufferForDecryption_ == null) //we ddon't need to do this if it's
            // data stream encryption
            // {
            // ensureBLayerDataInBuffer(4 + 4);
            // }
            // The ddmScalarLen_ we peek here does not include the LLCP and the extended
            // length bytes
            // themselves. So we will add those back to the ddmScalarLen_ so it can be
            // adjusted
            // correctly in parseLengthAndMatchCodePoint(). (since the adjustLengths()
            // method will
            // subtract the length from ddmScalarLen_)
            peekedLength_ = (int) buffer.getUnsignedInt(buffer.readerIndex() + 4);
            // ((buffer_[pos_ + 4] & 0xff) << 24) +
            // ((buffer_[pos_ + 5] & 0xff) << 16) +
            // ((buffer_[pos_ + 6] & 0xff) << 8) +
            // ((buffer_[pos_ + 7] & 0xff) << 0);
            break;
        case 0:
            peekedLength_ = -1; // this ddm is streamed, so set -1 -> length unknown
            break;
        default:
            throw new IllegalStateException("CodePoint.SYNERRCD_INCORRECT_EXTENDED_LEN");
            // doSyntaxrmSemantics(CodePoint.SYNERRCD_INCORRECT_EXTENDED_LEN);
        }
    }
    
    final int[] readUnsignedShortList() {
        int len = ddmScalarLen_;
        ensureBLayerDataInBuffer(len);
        adjustLengths(len);

        int count = len / 2;
        int[] list = new int[count];

        for (int i = 0; i < count; i++) {
            list[i] = buffer.readUnsignedShort();
//            list[i] = ((buffer_[pos_++] & 0xff) << 8) +
//                    ((buffer_[pos_++] & 0xff) << 0);
        }

        return list;
    }
    
    final int readUnsignedByte() {
        ensureBLayerDataInBuffer(1);
        adjustLengths(1);
        return buffer.readUnsignedByte();
//        return (buffer_[pos_++] & 0xff);
    }

    final byte readByte() {
        ensureBLayerDataInBuffer(1);
        adjustLengths(1);
        return buffer.readByte();
//        return (byte) (buffer_[pos_++] & 0xff);
    }

    
    final byte[] readBytes(int length) {
        ensureBLayerDataInBuffer(length);
        adjustLengths(length);

        byte[] b = new byte[length];
        //System.arraycopy(buffer, pos_, b, 0, length);
        buffer.readBytes(b, 0, length);
        //pos_ += length;
        return b;
    }

    final byte[] readBytes() {
        int len = ddmScalarLen_;
        ensureBLayerDataInBuffer(len);
        adjustLengths(len);

        byte[] b = new byte[len];
        buffer.readBytes(b, 0, len);
//        System.arraycopy(buffer, pos_, b, 0, len);
        //pos_ += len;
        return b;
    }
    
    final int readUnsignedShort() {
        // should we be checking dss lengths and ddmScalarLengths here
        // if yes, i am not sure this is the correct place if we should be checking
        ensureBLayerDataInBuffer(2);
        adjustLengths(2);
        return buffer.readUnsignedShort();
//        return ((buffer_[pos_++] & 0xff) << 8) +
//                ((buffer_[pos_++] & 0xff) << 0);
    }
    
    // this is duplicated in parseColumnMetaData, but different
    // DAGroup under NETColumnMetaData requires a lot more stuffs including
    // precsion, scale and other stuffs
    String parseFastVCS() {
        // doublecheck what readString() does if the length is 0
        return readFastString(readFastUnsignedShort(), Typdef.targetTypdef.getCcsidSbcEncoding());
    }

    final void skipBytes(int length) {
        ensureBLayerDataInBuffer(length);
        adjustLengths(length);
        buffer.skipBytes(length);
        //pos_ += length;
    }

    final void skipBytes() {
        int len = ddmScalarLen_;
        ensureBLayerDataInBuffer(len);
        adjustLengths(len);
        buffer.skipBytes(len);
        //pos_ += len;
    }
    
    final void skipFastBytes(int length) {
        buffer.skipBytes(length);
        //pos_ += length;
    }
    
    protected final void popCollectionStack() {
        // TODO: remove this after done porting
        ddmCollectionLenStack.pop();
    }
    
    final String readFastString(int length, Charset encoding) {
//        String s = new String(buffer_, pos_, length, encoding);
        //pos_ += length;
        return buffer.readCharSequence(length, encoding).toString();
    }
    
    final void readFastIntArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
          if (metadata.isZos())
            array[i] = buffer.readInt();
          else
            array[i] = buffer.readIntLE(); //SignedBinary.getInt(buffer_, pos_);
            //pos_ += 4;
        }
    }
    
    final int readFastUnsignedByte() {
        //pos_++;
        return buffer.readUnsignedByte();
//        return (buffer_[pos_++] & 0xff);
    }

    final short readFastShort() {
        //pos_ += 2;
      if (metadata.isZos())
        return buffer.readShort();
      else
        return buffer.readShortLE();
//        short s = SignedBinary.getShort(buffer_, pos_);
//        return s;
    }
    
    final long readFastLong() {
//        long l = SignedBinary.getLong(buffer_, pos_);
        //pos_ += 8;
        if (metadata.isZos())
          return buffer.readLong();
        else
          return buffer.readLongLE();
    }

    final int readFastUnsignedShort() {
        //pos_ += 2;
      return buffer.readUnsignedShort();
//        return ((buffer_[pos_++] & 0xff) << 8) +
//                ((buffer_[pos_++] & 0xff) << 0);
    }

    final int readFastInt() {
//        int i = SignedBinary.getInt(buffer_, pos_);
        //pos_ += 4;
        if (metadata.isZos())
          return buffer.readInt();
        else
          return buffer.readIntLE();
    }

    final String readFastString(int length) {
        String result = buffer.readCharSequence(length, metadata.getCCSID()).toString();
//                            .convertToJavaString(buffer_, pos_, length);
        //pos_ += length;
        return result;
    }

    final byte[] readFastBytes(int length) {
        byte[] b = new byte[length];
        //System.arraycopy(buffer, pos_, b, 0, length);
        buffer.readBytes(b, 0, length);
        //pos_ += length;
        return b;
    }
    
    final byte[] readFastLDBytes() {
        int len = buffer.readShort();
//        int len = ((buffer_[pos_++] & 0xff) << 8) + ((buffer_[pos_++] & 0xff) << 0);
        if (len == 0) {
            return null;
        }

        byte[] b = new byte[len];
        //System.arraycopy(buffer, pos_, b, 0, len);
        buffer.readBytes(b, 0, len);
        //pos_ += len;
        return b;
    }
    
    void ensureBLayerDataInBuffer(int desiredDataSize) {
        // TODO: remove this after done porting
        ensureALayerDataInBuffer(desiredDataSize);
    }

    // Make sure a certain amount of Layer A data is in the buffer.
    // The data will be in the buffer after this method is called.
    void ensureALayerDataInBuffer(int desiredDataSize) {
        if (buffer.readableBytes() < desiredDataSize) {
            throw new IllegalStateException(
                    "Needed to have " + desiredDataSize + " in buffer but only had " + buffer.readableBytes() + 
                    ". In JDBC we would normally block here but need to find a non-blocking solution");
        }
    }

}
