package io.vertx.db2client.impl.drda;

import java.sql.Connection;

import io.netty.buffer.ByteBuf;

public class DRDAConnectResponse extends DRDAResponse {
    
    public DRDAConnectResponse(ByteBuf buffer, CCSIDManager ccsidManager) {
        super(buffer, ccsidManager);
    }
    
    public void readAccessSecurity(int securityMechanism) {
        startSameIdChainParse();
        parseACCSECreply(securityMechanism);
        endOfSameIdChainData();
//      agent_.checkForChainBreakingException_();
    }

    public void readExchangeServerAttributes() {
        startSameIdChainParse();
        parseEXCSATreply();
        endOfSameIdChainData();
//        agent_.checkForChainBreakingException_();
    }
    
    // NET only entry point
    public void readSecurityCheck() {
        startSameIdChainParse();
        parseSECCHKreply();
        endOfSameIdChainData();
    }
    
    public RDBAccessData readAccessDatabase() {
        startSameIdChainParse();
        RDBAccessData accessData = parseACCRDBreply();
        endOfSameIdChainData();
        return accessData;
//        agent_.checkForChainBreakingException_();
    }
    
    public void readLocalCommit() {
        startSameIdChainParse();
        parseRDBCMMreply();
        endOfSameIdChainData();
    }
    
    // Parse the reply for the RDB Commit Unit of Work Command.
    // This method handles the parsing of all command replies and reply data
    // for the rdbcmm command.
    private void parseRDBCMMreply(/*ConnectionCallbackInterface connection*/) {
        parseTypdefsOrMgrlvlovrs();

        parseENDUOWRM();
        int peekCP = parseTypdefsOrMgrlvlovrs();

        if (peekCP == CodePoint.SQLCARD) {
            NetSqlca netSqlca = parseSQLCARD(null);
//            connection.completeSqlca(netSqlca);
        } else {
            parseCommitError();
        }
    }
    
    private void parseCommitError() {
        int peekCP = peekCodePoint();
        switch (peekCP) {
        case CodePoint.ABNUOWRM:
            throw new IllegalStateException("Abnormal ending to UOW");
//            NetSqlca sqlca = parseAbnormalEndUow(null);
//            connection.completeSqlca(sqlca);
//            break;
        case CodePoint.CMDCHKRM:
            parseCMDCHKRM();
            break;
        case CodePoint.RDBNACRM:
            parseRDBNACRM();
            break;
        default:
            throwUnknownCodepoint(peekCP);
//            parseCommonError(peekCP);
            break;
        }
    }
    
    void parseCommonError(int peekCP) {
        throw new IllegalStateException("Common error: " + Integer.toHexString(peekCP));
//        switch (peekCP) {
//        case CodePoint.CMDNSPRM:
//            parseCMDNSPRM();
//            break;
//        case CodePoint.PRCCNVRM:
//            parsePRCCNVRM();
//            break;
//        case CodePoint.SYNTAXRM:
//            parseSYNTAXRM();
//            break;
//        case CodePoint.VALNSPRM:
//            parseVALNSPRM();
//            break;
//        default:
//            doObjnsprmSemantics(peekCP);
//        }
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
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);
        if (!rdbnamReceived)
            throwMissingRequiredCodepoint("RDBNAM", CodePoint.RDBNAM);

//        netAgent_.setSvrcod(svrcod);
        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED");
//        agent_.accumulateChainBreakingReadExceptionAndThrow(
//            new DisconnectException(agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                msgutil_.getTextMessage(MessageId.CONN_DRDA_RDBNACRM)));            
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
            }

        }
        popCollectionStack();
        if (!svrcodReceived)
            throwMissingRequiredCodepoint("SVRCOD", CodePoint.SVRCOD);

//        netAgent_.setSvrcod(svrcod);
        NetSqlca netSqlca = parseSQLCARD(null); 
//        netAgent_.netConnection_.completeSqlca(netSqlca);

        throw new IllegalStateException("SQLState.DRDA_CONNECTION_TERMINATED");
//        agent_.accumulateChainBreakingReadExceptionAndThrow(
//            new DisconnectException(
//                agent_,
//                new ClientMessageId(SQLState.DRDA_CONNECTION_TERMINATED),
//                new SqlException(agent_.logWriter_, netSqlca),
//                msgutil_.getTextMessage(MessageId.CONN_DRDA_CMDCHKRM)));
    }
    
    // Parse the reply for the Access RDB Command.
    // This method handles the parsing of all command replies and reply data
    // for the accrdb command.
    private RDBAccessData parseACCRDBreply() {
        int peekCP = peekCodePoint();
        if (peekCP != CodePoint.ACCRDBRM) {
            throw new IllegalStateException("Expected state ACCRDBRM but got " + Integer.toHexString(peekCP));
        }

        RDBAccessData accessData = parseACCRDBRM();
        parseInitialPBSD();
        peekCP = peekCodePoint();
        if (peekCP == END_OF_SAME_ID_CHAIN) {
            return accessData;
        }

        parseTypdefsOrMgrlvlovrs();
        NetSqlca netSqlca = parseSQLCARD(null);
        NetSqlca.complete(netSqlca);
        return accessData;
    }
    

    
    /**
    * Parse the initial PBSD - PiggyBackedSessionData code point.
    * <p>
    * If sent by the server, it contains a PBSD_ISO code point followed by a
    * byte representing the JDBC isolation level, and a PBSD_SCHEMA code point
    * followed by the name of the current schema as an UTF-8 String.
    *
    * @throws org.apache.derby.client.am.DisconnectException
    */
   private void parseInitialPBSD() {
       if (peekCodePoint() != CodePoint.PBSD) {
           return;
       }
       parseLengthAndMatchCodePoint(CodePoint.PBSD);
       int peekCP = peekCodePoint();
       while (peekCP != END_OF_SAME_ID_CHAIN) {
           parseLengthAndMatchCodePoint(peekCP);
           switch (peekCP) {
               case CodePoint.PBSD_ISO:
                   int isolationLevel = readUnsignedByte();
                   if (isolationLevel != Connection.TRANSACTION_READ_COMMITTED)
                       throw new IllegalStateException("Database using unsupported transaction isolation level: " + isolationLevel);
//                   netAgent_.netConnection_.
//                       completeInitialPiggyBackIsolation(readUnsignedByte());
                   break;
               case CodePoint.PBSD_SCHEMA:
                   String pbSchema = readString(getDdmLength(), CCSIDManager.UTF8);
//                   netAgent_.netConnection_.
//                       completeInitialPiggyBackSchema
//                           (readString(getDdmLength(), Typdef.UTF8ENCODING));
                   break;
               default:
                   throw new IllegalStateException("Found unknown codepoint: " + Integer.toHexString(peekCP));
           }
           peekCP = peekCodePoint();
       }
   }
    
    // Access to RDB Completed (ACRDBRM) Reply Message specifies that an
    // instance of the SQL application manager has been created and is bound
    // to the specified relation database (RDB).
    //
    // Returned from Server:
    // SVRCOD - required  (0 - INFO, 4 - WARNING)
    // PRDID - required
    // TYPDEFNAM - required (MINLVL 4) (QTDSQLJVM)
    // TYPDEFOVR - required
    // RDBINTTKN - optional
    // CRRTKN - optional
    // USRID - optional
    // SRVLST - optional (MINLVL 5)
    private RDBAccessData parseACCRDBRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean prdidReceived = false;
        String prdid = null;
        boolean typdefnamReceived = false;
        boolean typdefovrReceived = false;
        boolean rdbinttknReceived = false;
        boolean crrtknReceived = false;
        byte[] crrtkn = null;
        boolean usridReceived = false;
        String usrid = null;

        parseLengthAndMatchCodePoint(CodePoint.ACCRDBRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                // severity code.  If the target SQLAM cannot support the typdefovr
                // parameter values specified for the double-byte and mixed-byte CCSIDs
                // on the corresponding ACCRDB command, then the severity code WARNING
                // is specified on the ACCRDBRM.
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_WARNING);
                peekCP = peekCodePoint();
            }

            // this is the product release level of the target RDB server.
            if (peekCP == CodePoint.PRDID) {
                foundInPass = true;
                prdidReceived = checkAndGetReceivedFlag(prdidReceived);
                prdid = parsePRDID(false); // false means do not skip the bytes
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.TYPDEFNAM) {
                // this is the name of the data type to the data representation mapping
                // definitions tha the target SQLAM uses when sending reply data objects.
                foundInPass = true;
                typdefnamReceived = checkAndGetReceivedFlag(typdefnamReceived);
                parseTYPDEFNAM();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.TYPDEFOVR) {
                // this is the single-byte, double-byte, and mixed-byte CCSIDs of the
                // scalar data arrays (SDA) in the identified data type to data representation
                // mapping definitions.
                foundInPass = true;
                typdefovrReceived = checkAndGetReceivedFlag(typdefovrReceived);
                parseTYPDEFOVR();
                peekCP = peekCodePoint();
            }
            
            if (peekCP == CodePoint.RDBINTTKN) {
                // @AGG added manually
                foundInPass = true;
                rdbinttknReceived = checkAndGetReceivedFlag(rdbinttknReceived);
                parseRDBINTTKN(false);
                peekCP = peekCodePoint();
            }


            if (peekCP == CodePoint.USRID) {
                // specifies the target defined user ID.  It is returned if the value of
                // TRGDFTRT is TRUE in ACCRDB.  Right now this driver always sets this
                // value to false so this should never get returned here.
                // if it is returned, it could be considered an error but for now
                // this driver will just skip the bytes.
                foundInPass = true;
                usridReceived = checkAndGetReceivedFlag(usridReceived);
                usrid = parseUSRID(true);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.CRRTKN) {
                // carries information to correlate with the work being done on bahalf
                // of an application at the source and at the target server.
                // defualt value is ''.
                // this parameter is only retunred if an only if the CRRTKN parameter
                // is not received on ACCRDB.  We will rely on server to send us this
                // in ACCRDBRM
                foundInPass = true;
                crrtknReceived = checkAndGetReceivedFlag(crrtknReceived);
                crrtkn = parseCRRTKN(false);
                peekCP = peekCodePoint();
            }


            if (!foundInPass) {
                throw new IllegalStateException("Found unknown codepoint: " + Integer.toHexString(peekCP));
                //doPrmnsprmSemantics(peekCP);
            }
        }
        popCollectionStack();
        // check for the required instance variables.
        if (!svrcodReceived)
            throw new IllegalStateException("Did not find codepoint SVRCOD in reply data");
        if (!prdidReceived)
            throw new IllegalStateException("Did not find codepoint PRDID in reply data");
        if (!typdefnamReceived)
            throw new IllegalStateException("Did not find codepoint TYPDEFNAM in reply data");
        if (!typdefovrReceived)
            throw new IllegalStateException("Did not find codepoint TYPDEFOVR in reply data");
//        checkRequiredObjects(svrcodReceived,
//                prdidReceived,
//                typdefnamReceived,
//                typdefovrReceived);

//        rdbAccessed(svrcod,
//                prdid,
//                crrtknReceived,
//                crrtkn);
        return new RDBAccessData(svrcod, prdid, crrtknReceived, crrtkn);
    }
    
    public static class RDBAccessData {
        public final int svrcod;
        public final String prdid;
        public final byte[] correlationToken;
        public RDBAccessData(int svrcod, String prdid, boolean crrtknReceived, byte[] crrtkn) {
            this.svrcod = svrcod;
            this.prdid = prdid;
            correlationToken = crrtknReceived ? crrtkn : null;
        }
    }
    
    private byte[] parseRDBINTTKN(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.RDBINTTKN);
        if (skip) {
            skipBytes();
            return null;
        }
        return readBytes();
    }
    
    // Correlation Token specifies a token that is conveyed between source
    // and target servers for correlating the processing between servers.
    private byte[] parseCRRTKN(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.CRRTKN);
        if (skip) {
            skipBytes();
            return null;
        }
        return readBytes();
    }
    
    // The User Id specifies an end-user name.
    private String parseUSRID(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.USRID);
        if (skip) {
            skipBytes();
            return null;
        }
        return readString();
    };
    
    
    // Product specific Identifier specifies the product release level
    // of a DDM server.
    private String parsePRDID(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.PRDID);
        if (skip) {
            skipBytes();
            return null;
        } else {
            return readString();
        }
    }
    
    // Parse the reply for the Security Check Command.
    // This method handles the parsing of all command replies and reply data
    // for the secchk command.
    private void parseSECCHKreply() {
        if (peekCodePoint() != CodePoint.SECCHKRM) {
            throw new IllegalStateException("Expected CodePoint.SECCHKRM");
        }

        parseSECCHKRM();
        if (peekCodePoint() == CodePoint.SECTKN) {
            // rpydta used only if the security mechanism returns
            // a security token that must be sent back to the source system.
            // this is only used for DCSSEC.  In the case of DCESEC,
            // the sectkn must be returned as reply data if DCE is using
            // mutual authentication.
            // Need to double check what to map this to.  This is probably
            // incorrect but consider it a conversation protocol error
            // 0x03 - OBJDSS sent when not allowed.
            //parseSECTKN (true);
            parseSECTKN(false);
        }
    }
    
    // The Security Check (SECCHKRM) Reply Message indicates the acceptability
    // of the security information.
    // this method returns the security check code. it is up to the caller to check
    // the value of this return code and take the appropriate action.
    //
    // Returned from Server:
    // SVRCOD - required  (0 - INFO, 8 - ERROR, 16 -SEVERE)
    // SECCHKCD - required
    // SECTKN - optional, ignorable
    // SVCERRNO - optional
    private void parseSECCHKRM() {
        boolean svrcodReceived = false;
        int svrcod = CodePoint.SVRCOD_INFO;
        boolean secchkcdReceived = false;
        int secchkcd = CodePoint.SECCHKCD_00;
        boolean sectknReceived = false;
        byte[] sectkn = null;

        parseLengthAndMatchCodePoint(CodePoint.SECCHKRM);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();
        
        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SVRCOD) {
                // severity code.  it's value is dictated by the SECCHKCD.
                // right now it will not be checked that it is the correct value
                // for the SECCHKCD.  maybe this will be done in the future.
                foundInPass = true;
                svrcodReceived = checkAndGetReceivedFlag(svrcodReceived);
                svrcod = parseSVRCOD(CodePoint.SVRCOD_INFO, CodePoint.SVRCOD_SEVERE);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SECCHKCD) {
                // security check code. this specifies the state of the security information.
                // there is a relationship between this value and the SVRCOD value.
                // right now this driver will not check these values against each other.
                foundInPass = true;
                secchkcdReceived = checkAndGetReceivedFlag(secchkcdReceived);
                secchkcd = parseSECCHKCD();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SECTKN) {
                // security token.
                // used when mutual authentication of the source and target servers
                // is requested.  The architecture lists this as an instance variable
                // and also says that the SECTKN flows as reply data to the secchk cmd and
                // it must flow after the secchkrm message.  Right now this driver doesn't
                // support ay mutual authentication so it will be ignored (it is listed
                // as an ignorable instance variable in the ddm manual).
                foundInPass = true;
                sectknReceived = checkAndGetReceivedFlag(sectknReceived);
                sectkn = parseSECTKN(true);
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throw new IllegalStateException("Found unexpected codepoint: " + peekCP);
            }

        }
        popCollectionStack();
        // check for the required instance variables.
        if (!svrcodReceived)
            throw new IllegalStateException("Did not receive SVRCOD codepoint");
        if (!secchkcdReceived)
            throw new IllegalStateException("Did not receive SECCHKCD codepoint");
//        checkRequiredObjects(svrcodReceived, secchkcdReceived);

//        netConnection.securityCheckComplete(svrcod, secchkcd);
        if (secchkcd != CodePoint.SECCHKCD_00) {
            throw new IllegalArgumentException("Authentication failed");
        }
    }
    
    private void parseACCSECreply(int securityMechanism) {
        int peekCP = peekCodePoint();
        if (peekCP != CodePoint.ACCSECRD) {
            throw new IllegalStateException(String.format("Expecting ACCSECRD codepoint (0x14AC) but got %04x", peekCP));
            //parseAccessSecurityError(netConnection);
        }
        parseACCSECRD(securityMechanism);

//        peekCP = peekCodePoint();
//        if (SanityManager.DEBUG) {
//            if (peekCP != Reply.END_OF_SAME_ID_CHAIN) {
//                SanityManager.THROWASSERT("expected END_OF_SAME_ID_CHAIN");
//            }
//        }
    }
    
    // The Access Security Reply Data (ACSECRD) Collection Object contains
    // the security information from a target server's security manager.
    // this method returns the security check code received from the server
    // (if the server does not return a security check code, this method
    // will return 0).  it is up to the caller to check
    // the value of this return code and take the appropriate action.
    //
    // Returned from Server:
    // SECMEC - required
    // SECTKN - optional (MINLVL 6)
    // SECCHKCD - optional
    private void parseACCSECRD(int securityMechanism) {
        boolean secmecReceived = false;
        int[] secmecList = null;
        boolean sectknReceived = false;
        byte[] sectkn = null;
        boolean secchkcdReceived = false;
        int secchkcd = 0;

        parseLengthAndMatchCodePoint(CodePoint.ACCSECRD);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();

        while (peekCP != END_OF_COLLECTION) {

            boolean foundInPass = false;

            if (peekCP == CodePoint.SECMEC) {
                // security mechanism.
                // this value must either reflect the value sent in the ACCSEC command
                // if the target server supports it; or the values the target server
                // does support when it does not support or accept the value
                // requested by the source server.
                // the secmecs returned are treated as a list and stored in
                // targetSecmec_List.
                // if the target server supports the source's secmec, it
                // will be saved in the variable targetSecmec_ (NOTE: so
                // after calling this method, if targetSecmec_'s value is zero,
                // then the target did NOT support the source secmec.  any alternate
                // secmecs would be contained in targetSecmec_List).
                foundInPass = true;
                secmecReceived = checkAndGetReceivedFlag(secmecReceived);
                secmecList = parseSECMEC();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SECTKN) {
                // security token
                foundInPass = true;
                sectknReceived = checkAndGetReceivedFlag(sectknReceived);
                sectkn = parseSECTKN(false);
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SECCHKCD) {
                // security check code.
                // included if and only if an error is detected when processing
                // the ACCSEC command.  this has an implied severity code
                // of ERROR.
                foundInPass = true;
                secchkcdReceived = checkAndGetReceivedFlag(secchkcdReceived);
                secchkcd = parseSECCHKCD();
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throw new IllegalStateException("Found invalid codepoint: " + Integer.toHexString(peekCP));
                //doPrmnsprmSemantics(peekCP);
            }
        }
        popCollectionStack();
        if (!secmecReceived)
            throw new IllegalStateException("Did not receive SECMEC flag in response");
//        checkRequiredObjects(secmecReceived);

        // TODO: port this method to validate secMec received is same as requested one
        setAccessSecurityData(secchkcd,
                securityMechanism,
                secmecList,
                sectknReceived,
                sectkn);
        
        /* Switch to UTF-8 or EBCDIC managers depending on what's supported */
//        if (netConnection.serverSupportsUtf8Ccsid()) {
//            netConnection.netAgent_.switchToUtf8CcsidMgr();
//        } else {
//            netConnection.netAgent_.switchToEbcdicMgr();
//        }
        // ccsidManager.setCCSID(CCSIDManager.UTF8); // TODO @AGG should be switching to UTF8 here?
    }
    
    // secmecList is always required and will not be null.
    // secchkcd has an implied severity of error.
    // it will be returned if an error is detected.
    // if no errors and security mechanism requires a sectkn, then
    void setAccessSecurityData(int secchkcd,
                               int desiredSecmec,
                               int[] secmecList,
                               boolean sectknReceived,
                               byte[] sectkn) {
        // @AGG this method was originally on NetConnection
        
        // - if the secchkcd is not 0, then map to an exception.
        if (secchkcd != CodePoint.SECCHKCD_00) {
            // the implied severity code is error
            throw new IllegalStateException("Got nonzero SECCHKCD codepoint: " + secchkcd);
//            netAgent_.setSvrcod(CodePoint.SVRCOD_ERROR);
//            agent_.accumulateReadException(mapSecchkcd(secchkcd));
        } else {
            // - verify that the secmec parameter reflects the value sent
            // in the ACCSEC command.
            // should we check for null list
            if ((secmecList.length == 1) && (secmecList[0] == desiredSecmec)) {
                // the security mechanism returned from the server matches
                // the mechanism requested by the client.
//                targetSecmec_ = secmecList[0];

                if ((desiredSecmec == DRDAConstants.SECMEC_USRENCPWD) ||
                        (desiredSecmec == DRDAConstants.SECMEC_EUSRIDPWD) ||
                        (desiredSecmec == DRDAConstants.SECMEC_USRSSBPWD) ||
                        (desiredSecmec == DRDAConstants.SECMEC_EUSRIDDTA) ||
                        (desiredSecmec == DRDAConstants.SECMEC_EUSRPWDDTA)) {

                    // a security token is required for USRENCPWD, or EUSRIDPWD.
                    if (!sectknReceived) {
                        throw new IllegalStateException("SQLState.NET_SECTKN_NOT_RETURNED");
//                        agent_.accumulateChainBreakingReadExceptionAndThrow(
//                            new DisconnectException(agent_, 
//                                new ClientMessageId(SQLState.NET_SECTKN_NOT_RETURNED)));
                    } else {
                        throw new UnsupportedOperationException();
//                        if (desiredSecmec == NetConfiguration.SECMEC_USRSSBPWD)
//                            targetSeed_ = sectkn;
//                        else
//                            targetPublicKey_ = sectkn;
//                        if (encryptionManager_ != null) {
//                            encryptionManager_.resetSecurityKeys();
//                        }
                    }
                }
            } else {
                // accumulate an SqlException and don't disconnect yet
                // if a SECCHK was chained after this it would receive a secchk code
                // indicating the security mechanism wasn't supported and that would be a
                // chain breaking exception.  if no SECCHK is chained this exception
                // will be surfaced by endReadChain
                // agent_.accumulateChainBreakingReadExceptionAndThrow (
                //   new DisconnectException (agent_,"secmec not supported ","0000", -999));
                throw new IllegalStateException("SQLState.NET_SECKTKN_NOT_RETURNED");
//                agent_.accumulateReadException(new SqlException(agent_.logWriter_, 
//                    new ClientMessageId(SQLState.NET_SECKTKN_NOT_RETURNED)));
            }
        }
    }
    
    // The Security Check Code String codifies the security information
    // and condition for the SECCHKRM.
    private int parseSECCHKCD() {
        parseLengthAndMatchCodePoint(CodePoint.SECCHKCD);
        int secchkcd = readUnsignedByte();
        if ((secchkcd < CodePoint.SECCHKCD_00) || (secchkcd > CodePoint.SECCHKCD_15)) {
            doValnsprmSemantics(CodePoint.SECCHKCD, secchkcd);
        }
        // @AGG remove this after fixing PeekCP() ? 
        //adjustLengths(1); // @AGG added this after some debugging
        return secchkcd;
    }
    
    // The Security Token Byte String is information provided and used
    // by the various security mechanisms.
    private byte[] parseSECTKN(boolean skip) {
        parseLengthAndMatchCodePoint(CodePoint.SECTKN);
        if (skip) {
            skipBytes();
            return null;
        }
        return readBytes();
    }
    
    // Security Mechanims.
    private int[] parseSECMEC() {
        parseLengthAndMatchCodePoint(CodePoint.SECMEC);
        return readUnsignedShortList();
    }
    
    // Parse the reply for the Exchange Server Attributes Command.
    // This method handles the parsing of all command replies and reply data
    // for the excsat command.
    private void parseEXCSATreply() {
        if (peekCodePoint() != CodePoint.EXCSATRD) {
            parseExchangeServerAttributesError();
            return;
        }
        parseEXCSATRD();
    }
    
    private void parseExchangeServerAttributesError() {
        int peekCP = peekCodePoint();
        throw new IllegalStateException(String.format("Invalid codepoint: %02x", peekCP));
        // switch (peekCP) {
        // case CodePoint.CMDCHKRM:
        // parseCMDCHKRM();
        // break;
        // case CodePoint.MGRLVLRM:
        // parseMGRLVLRM();
        // break;
        // default:
        // parseCommonError(peekCP);
        // }
    }
    
    // The Server Attributes Reply Data (EXCSATRD) returns the following
    // information in response to an EXCSAT command:
    // - the target server's class name
    // - the target server's support level for each class of manager
    //   the source requests
    // - the target server's product release level
    // - the target server's external name
    // - the target server's name
    //
    // Returned from Server:
    // EXTNAM - optional
    // MGRLVLLS - optional
    // SRVCLSNM - optional
    // SRVNAM - optional
    // SRVRLSLV - optional
    private void parseEXCSATRD() {
        boolean extnamReceived = false;
        boolean mgrlvllsReceived = false;
        boolean srvclsnmReceived = false;
        String srvclsnm = null;
        boolean srvnamReceived = false;
        boolean srvrlslvReceived = false;
        String srvrlslv = null;

        parseLengthAndMatchCodePoint(CodePoint.EXCSATRD);
        pushLengthOnCollectionStack();
        int peekCP = peekCodePoint();
        // EXTNAM = 71
        // MGRLVL = 28
        // SRVCLS = 19
        // SRVNAM = 9
        // SRVREL = 12
        // total = 139
        while (peekCP != END_OF_COLLECTION) {
            
            boolean foundInPass = false;

            if (peekCP == CodePoint.EXTNAM) {
                // External Name is the name of the job, task, or process
                // on a system for which a DDM server is active.  For a target
                // DDM server, the external name is the name of the job the system creates
                // or activates to run the DDM server.
                // No semantic meaning is assigned to external names in DDM.
                // External names are transmitted to aid in problem determination.
                foundInPass = true;
                extnamReceived = checkAndGetReceivedFlag(extnamReceived);
                parseLengthAndMatchCodePoint(CodePoint.EXTNAM);
                String extnam = readString();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.MGRLVLLS) {
                // Manager-Level List
                // specifies a list of code points and support levels for the
                // classes of managers a server supports
                foundInPass = true;
                mgrlvllsReceived = checkAndGetReceivedFlag(mgrlvllsReceived);
                parseMGRLVLLS();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SRVCLSNM) {
                // Server Class Name
                // specifies the name of a class of ddm servers.
                foundInPass = true;
                srvclsnmReceived = checkAndGetReceivedFlag(srvclsnmReceived);
                parseLengthAndMatchCodePoint(CodePoint.SRVCLSNM);
                srvclsnm = readString(); // parseSRVCLSNM();
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SRVNAM) {
                // Server Name
                // no semantic meaning is assigned to server names in DDM,
                // it is recommended (by the DDM manual) that the server's
                // physical or logical location identifier be used as a server name.
                // server names are transmitted for problem determination purposes.
                // this driver will save this name and in the future may use it
                // for logging errors.
                foundInPass = true;
                srvnamReceived = checkAndGetReceivedFlag(srvnamReceived);
                parseLengthAndMatchCodePoint(CodePoint.SRVNAM);
                readString();
                //parseSRVNAM(); // not used yet
                peekCP = peekCodePoint();
            }

            if (peekCP == CodePoint.SRVRLSLV) {
                // Server Product Release Level
                // specifies the procuct release level of a ddm server.
                // the contents are unarchitected.
                // this driver will save this information and in the future may
                // use it for logging purposes.
                foundInPass = true;
                srvrlslvReceived = checkAndGetReceivedFlag(srvrlslvReceived);
                parseLengthAndMatchCodePoint(CodePoint.SRVRLSLV);
                srvrlslv = readString(); // parseSRVRLSLV();
                peekCP = peekCodePoint();
            }

            if (!foundInPass) {
                throw new IllegalStateException(String.format("Did not find a codepoint in this pass: %02x", peekCP));
                //doPrmnsprmSemantics(peekCP);
            }

        }
        
        ddmCollectionLenStack.pop();
        // according the the DDM book, all these instance variables are optional
        //netConnection.setServerAttributeData(srvclsnm, srvrlslv);
    }
    
    // Manager-Level List.
    // Specifies a list of code points and support levels for the
    // classes of managers a server supports.
    // The target server must not provide information for any target
    // managers unless the source explicitly requests it.
    // For each manager class, if the target server's support level
    // is greater than or equal to the source server's level, then the source
    // server's level is returned for that class if the target server can operate
    // at the source's level; otherwise a level 0 is returned.  If the target
    // server's support level is less than the source server's level, the
    // target server's level is returned for that class.  If the target server
    // does not recognize the code point of a manager class or does not support
    // that class, it returns a level of 0.  The target server then waits
    // for the next command or for the source server to terminate communications.
    // When the source server receives EXCSATRD, it must compare each of the entries
    // in the mgrlvlls parameter it received to the corresponding entries in the mgrlvlls
    // parameter it sent.  If any level mismatches, the source server must decide
    // whether it can use or adjust to the lower level of target support for that manager
    // class.  There are no architectural criteria for making this decision.
    // The source server can terminate communications or continue at the target
    // servers level of support.  It can also attempt to use whatever
    // commands its user requests while receiving eror reply messages for real
    // functional mismatches.
    // The manager levels the source server specifies or the target server
    // returns must be compatible with the manager-level dependencies of the specified
    // manangers.  Incompatible manager levels cannot be specified.
    // After this method successfully returns, the targetXXXX values (where XXXX
    // represents a manager name.  example targetAgent) contain the negotiated
    // manager levels for this particular connection.
    private void parseMGRLVLLS() {
        parseLengthAndMatchCodePoint(CodePoint.MGRLVLLS);

        // each manager class and level is 4 bytes long.
        // get the length of the mgrlvls bytes, make sure it contains
        // the correct number of bytes for a mgrlvlls object, and calculate
        // the number of manager's returned from the server.
        int managerListLength = getDdmLength();
        if ((managerListLength == 0) || ((managerListLength % 4) != 0)) {
            doSyntaxrmSemantics(CodePoint.SYNERRCD_OBJ_LEN_NOT_ALLOWED);
        }
        int managerCount = managerListLength / 4;
        
        // the managerCount should be equal to the same number of
        // managers sent on the excsat.

        System.out.println("Database server attributes:");
        // read each of the manager levels returned from the server.
        for (int i = 0; i < managerCount; i++) {

            // first two byte are the manager's codepoint, next two bytes are the level.
            int managerCodePoint = readUnsignedShort(); //buffer.readUnsignedShort(); //parseCODPNTDR();
            int managerLevel = readUnsignedShort(); //buffer.readUnsignedShort(); // parseMGRLVLN();
            
            // TODO @AGG: decide which manager levels we should support
            // check each manager to make sure levels are within proper limits
            // for this driver.  Also make sure unexpected managers are not returned.
            switch (managerCodePoint) {
                case CodePoint.AGENT:
                    System.out.println("  AGENT=" + managerLevel);
                    break;
                case CodePoint.SQLAM:
                    System.out.println("  SQLAM=" + managerLevel);
                    break;
                case CodePoint.UNICODEMGR:
                    System.out.println("  UNICODEMGR=" + managerLevel);
                    break;
                case CodePoint.RDB:
                    System.out.println("  RDB=" + managerLevel);
                    break;
                case CodePoint.SECMGR:
                    System.out.println("  SECMGR=" + managerLevel);
                    break;
                case CodePoint.CMNTCPIP:
                    System.out.println("  CMNTCPIP=" + managerLevel);
                    break;
                default:
                    System.out.println("  WARN: Unknown manager codepoint: 0x" + Integer.toHexString(managerCodePoint));
            }
        }
    }

}
