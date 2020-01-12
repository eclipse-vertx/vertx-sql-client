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

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Objects;

import io.netty.buffer.ByteBuf;

public class DRDAConnectRequest extends DRDARequest {
    
    public DRDAConnectRequest(ByteBuf buffer) {
        super(buffer);
    }
    
    // The Access RDB (ACCRDB) command makes a named relational database (RDB)
    // available to a requester by creating an instance of an SQL application
    // manager.  The access RDB command then binds the created instance to the target
    // agent and to the RDB. The RDB remains available (accessed) until
    // the communications conversation is terminate.
    public void buildACCRDB(String rdbnam,
                     boolean readOnly,
                     byte[] crrtkn,
                     String typdef) {
        createCommand();

        markLengthBytes(CodePoint.ACCRDB);

        // the relational database name specifies the name of the rdb to
        // be accessed.  this can be different sizes depending on the level of
        // support.  the size will have ben previously checked so at this point just
        // write the data and pad with the correct number of bytes as needed.
        // this instance variable is always required.
        buildRDBNAM(rdbnam,true);

        // the rdb access manager class specifies an instance of the SQLAM
        // that accesses the RDB.  the sqlam manager class codepoint
        // is always used/required for this.  this instance variable
        // is always required.
        buildRDBACCCL();

        // product specific identifier specifies the product release level
        // of this driver.  see the hard coded value in the NetConfiguration class.
        // this instance variable is always required.
        buildPRDID();

        // product specific data.  this is an optional parameter which carries
        // product specific information.  although it is optional, it will be
        // sent to the server.  use the first byte to determine the number
        // of the prddta bytes to write to the buffer. note: this length
        // doesn't include itself so increment by it by 1 to get the actual
        // length of this data.
        buildPRDDTA();


        // the typdefnam parameter specifies the name of the data type to data representation
        // mappings used when this driver sends command data objects.
        buildTYPDEFNAM(typdef);

//        if (crrtkn == null) {
//            constructCrrtkn();
//        }

        Objects.requireNonNull(crrtkn);
        buildCRRTKN(crrtkn);

        // This specifies the single-byte, double-byte
        // and mixed-byte CCSIDs of the Scalar Data Arrays (SDAs) in the identified
        // data type to the data representation mapping definitions.  This can
        // contain 3 CCSIDs.  The driver will only send the ones which were set.
        buildTYPDEFOVR(true,
                CCSIDManager.TARGET_UNICODE_MGR,
                true,
                CCSIDManager.TARGET_UNICODE_MGR,
                true,
                CCSIDManager.TARGET_UNICODE_MGR);

        // RDB allow update is an optional parameter which indicates
        // whether the RDB allows the requester to perform update operations
        // in the RDB.  If update operations are not allowed, this connection
        // is limited to read-only access of the RDB resources.
        buildRDBALWUPD(readOnly);



        // the Statement Decimal Delimiter (STTDECDEL),
        // Statement String Delimiter (STTSTRDEL),
        // and Target Default Value Return (TRGDFTRT) are all optional
        // instance variables which will not be sent to the server.

        // the command and the dss are complete so make the call to notify
        // the request object.
        updateLengthBytes();
    }
    
    private byte[] getProductData() {
        ByteBuffer prddta_ = ByteBuffer.allocate(DRDAConstants.PRDDTA_MAXSIZE);

        for (int i = 0; i < DRDAConstants.PRDDTA_ACCT_SUFFIX_LEN_BYTE; i++) {
            prddta_.put(i, CCSIDManager.getCCSID().encode(" ").get());
        }

        // Start inserting data right after the length byte.
        prddta_.position(DRDAConstants.PRDDTA_LEN_BYTE + 1);

        prddta_.put(CCSIDManager.getCCSID().encode(DRDAConstants.PRDID));//, prddta_);

        prddta_.put(CCSIDManager.getCCSID().encode(DRDAConstants.PRDDTA_PLATFORM_ID));
//        success &= ccsidMgr.encode(
//                CharBuffer.wrap(DRDAConstants.PRDDTA_PLATFORM_ID),
//                prddta_, agent_);

        int prddtaLen = prddta_.position();

        String extnamTruncated = DRDAConstants.EXTNAM.substring(0, Math.min(DRDAConstants.EXTNAM.length(), DRDAConstants.PRDDTA_APPL_ID_FIXED_LEN));
        prddta_.put(CCSIDManager.getCCSID().encode(extnamTruncated));
//        success &= ccsidMgr.encode(
//                CharBuffer.wrap(extnam_, 0, extnamTruncateLength),
//                prddta_, agent_);

//        if (SanityManager.DEBUG) {
//            // The encode() calls above should all complete without overflow,
//            // since we control the contents of the strings. Verify this in
//            // sane mode so that we notice it if the strings change so that
//            // they go beyond the max size of PRDDTA.
//            SanityManager.ASSERT(success,
//                "PRDID, PRDDTA_PLATFORM_ID and EXTNAM exceeded PRDDTA_MAXSIZE");
//        }

        prddtaLen += DRDAConstants.PRDDTA_APPL_ID_FIXED_LEN;

        prddtaLen += DRDAConstants.PRDDTA_USER_ID_FIXED_LEN;

        // Mark that we have an empty suffix in PRDDTA_ACCT_SUFFIX_LEN_BYTE.
        prddta_.put(DRDAConstants.PRDDTA_ACCT_SUFFIX_LEN_BYTE, (byte) 0);
        prddtaLen++;

        // the length byte value does not include itself.
        prddta_.put(DRDAConstants.PRDDTA_LEN_BYTE, (byte) (prddtaLen - 1));
        
        return prddta_.array();
    }
    
    // Construct the correlation token.
    // The crrtkn has the following format.
    //
    // <Almost IP address>.<local port number><current time in millis>
    // |                   | |               ||                  |
    // +----+--------------+ +-----+---------++---------+--------+
    //      |                      |                |
    //    8 bytes               4 bytes         6 bytes
    // Total lengtho of 19 bytes.
    //
    // 1 char for each 1/2 byte in the IP address.
    // If the first character of the <IP address> or <port number>
    // starts with '0' thru '9', it will be mapped to 'G' thru 'P'.
    // Reason for mapping the IP address is in order to use the crrtkn as the LUWID when using SNA in a hop site.
    public byte[] getCorrelationToken(int port) {
        byte[] crrtkn_ = new byte[19];

        byte[] localAddressBytes;
        try {
            localAddressBytes = Inet4Address.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        // IP addresses are returned in a 4 byte array.
        // Obtain the character representation of each half byte.
        for (int i = 0, j = 0; i < 4; i++, j += 2) {

            // since a byte is signed in java, convert any negative
            // numbers to positive before shifting.
            int num = localAddressBytes[i] < 0 ? localAddressBytes[i] + 256 : localAddressBytes[i];
            int halfByte = (num >> 4) & 0x0f;

            // map 0 to G
            // The first digit of the IP address is is replaced by
            // the characters 'G' thro 'P'(in order to use the crrtkn as the LUWID when using
            // SNA in a hop site). For example, 0 is mapped to G, 1 is mapped H,etc.
            if (i == 0) {
                crrtkn_[j] = CCSIDManager.getCCSID().encode("" + (char) (halfByte + 'G')).get(); 
                        //ccsidManager.getCCSID().numToSnaRequiredCrrtknChar_[halfByte];
            } else {
                crrtkn_[j] = CCSIDManager.getCCSID().encode("" + halfByte).get();
                        //netAgent_.getCurrentCcsidManager().numToCharRepresentation_[halfByte];
            }

            halfByte = (num) & 0x0f;
            crrtkn_[j + 1] = CCSIDManager.getCCSID().encode("" + halfByte).get();
            //netAgent_.getCurrentCcsidManager().numToCharRepresentation_[halfByte];
        }

        // fill the '.' in between the IP address and the port number
        crrtkn_[8] = CCSIDManager.getCCSID().encode(".").get();

        // Port numbers have values which fit in 2 unsigned bytes.
        // Java returns port numbers in an int so the value is not negative.
        // Get the character representation by converting the
        // 4 low order half bytes to the character representation.
        int num = port;
        //int num = netAgent_.socket_.getLocalPort();

        int halfByte = (num >> 12) & 0x0f;
        crrtkn_[9] = CCSIDManager.getCCSID().encode("" + halfByte).get(); 
                //netAgent_.getCurrentCcsidManager().numToSnaRequiredCrrtknChar_[halfByte];
        halfByte = (num >> 8) & 0x0f;
        crrtkn_[10] = CCSIDManager.getCCSID().encode("" + halfByte).get();
                //netAgent_.getCurrentCcsidManager().numToCharRepresentation_[halfByte];
        halfByte = (num >> 4) & 0x0f;
        crrtkn_[11] = CCSIDManager.getCCSID().encode("" + halfByte).get();
                //netAgent_.getCurrentCcsidManager().numToCharRepresentation_[halfByte];
        halfByte = (num) & 0x0f;
        crrtkn_[12] = CCSIDManager.getCCSID().encode("" + halfByte).get();
                //netAgent_.getCurrentCcsidManager().numToCharRepresentation_[halfByte];

        // The final part of CRRTKN is a 6 byte binary number that makes the
        // crrtkn unique, which is usually the time stamp/process id.
        // If the new time stamp is the
        // same as one of the already created ones, then recreate the time stamp.
        long time = System.currentTimeMillis();

        for (int i = 0; i < 6; i++) {
            // store 6 bytes of 8 byte time into crrtkn
            crrtkn_[i + 13] = (byte) (time >>> (40 - (i * 8)));
        }
        return crrtkn_;
    }
    
    public void buildSECCHK(int secmec, String rdbnam, String user, String password, byte[] sectkn, byte[] sectkn2){
        createCommand();
        markLengthBytes(CodePoint.SECCHK);

        // always send the negotiated security mechanism for the connection.
        buildSECMEC(secmec);

        // the rdbnam will be built and sent. different sqlam levels support
        // different lengths. at this point the length has been checked against
        // the maximum allowable length. so write the bytes and padd up to the
        // minimum length if needed.
        buildRDBNAM(rdbnam, false);
        if (user != null) {
            buildUSRID(user);
        }
        if (password != null) {
            buildPASSWORD(password);
        }
        if (sectkn != null) {
            buildSECTKN(sectkn);
        }
        if (sectkn2 != null) {
            buildSECTKN(sectkn2);
        }
        updateLengthBytes();

    }
    
    public void buildACCSEC(int secmec, String rdbnam, byte[] sectkn) throws SQLException {
        createCommand();

        // place the llcp for the ACCSEC in the buffer. save the length bytes for
        // later update
        markLengthBytes(CodePoint.ACCSEC);

        // the security mechanism is a required instance variable. it will
        // always be sent.
        buildSECMEC(secmec);

        // the rdbnam will be built and sent. different sqlam levels support
        // different lengths. at this point the length has been checked against
        // the maximum allowable length. so write the bytes and padd up to the
        // minimum length if needed. We want to defer sending the rdbnam if an
        // EBCDIC conversion is not possible.
        buildRDBNAM(rdbnam, true);

        if (sectkn != null) {
            buildSECTKN(sectkn);
        }

        // the accsec command is complete so notify the the request object to
        // update the ddm length and the dss header length.
        updateLengthBytes();
    }
    
    /** 
     * RDB Commit Unit of Work (RDBCMM) Command commits all work performed
     for the current unit of work.
    
     The Relational Database Name (RDBNAM) is an optional parameter
     which will not be sent by this command to reduce size, building,
     and parsing.
     * */
    public void buildRDBCMM() {
        createCommand();
        writeLengthCodePoint(0x04, CodePoint.RDBCMM);
    }
    
    private void buildRDBALWUPD(boolean readOnly) {
        // TODO: @AGG collapse
        if (readOnly) {
            writeScalar1Byte(CodePoint.RDBALWUPD, CodePoint.FALSE);
        }
    }
    
    private void buildTYPDEFOVR(boolean sendCcsidSbc, int ccsidSbc, boolean sendCcsidDbc, int ccsidDbc,
            boolean sendCcsidMbc, int ccsidMbc) {
        // TODO: @AGG collapse
        markLengthBytes(CodePoint.TYPDEFOVR);
        // write the single-byte ccsid used by this driver.
        if (sendCcsidSbc) {
            writeScalar2Bytes(CodePoint.CCSIDSBC, ccsidSbc);
        }

        // write the double-byte ccsid used by this driver.
        if (sendCcsidDbc) {
            writeScalar2Bytes(CodePoint.CCSIDDBC, ccsidDbc);
        }

        // write the mixed-byte ccsid used by this driver
        if (sendCcsidMbc) {
            writeScalar2Bytes(CodePoint.CCSIDMBC, ccsidMbc);
        }

        updateLengthBytes();

    }
    
    private void buildCRRTKN(byte[] crrtkn) {
        // TODO: @AGG collapse
        writeScalarBytes(CodePoint.CRRTKN, crrtkn);
    }
    
    private void buildTYPDEFNAM(String typdefnam) {
        // TODO: @AGG collapse
        writeScalarString(CodePoint.TYPDEFNAM, typdefnam);
    }
    
    private void buildPRDDTA() {
        // TODO: @AGG collapse
        byte[] prddta = getProductData();
        int prddtaLength = (prddta[DRDAConstants.PRDDTA_LEN_BYTE] & 0xff) + 1;
        writeScalarBytes(CodePoint.PRDDTA, prddta, 0, prddtaLength);
    }
    
    private void buildPRDID() {
        // TODO: @AGG collapse this
        writeScalarString(CodePoint.PRDID, DRDAConstants.PRDID);  // product id is hard-coded to DNC01000 for dnc 1.0.
    }
    
    private void buildRDBACCCL() {
        // TODO: @AGG collapse this method after porting
        writeScalar2Bytes(CodePoint.RDBACCCL, CodePoint.SQLAM);
    }
    
    private void buildUSRID(String usrid) {
        writeScalarString(CodePoint.USRID, usrid,0,DRDAConstants.USRID_MAXSIZE,
                SQLState.NET_USERID_TOO_LONG);
    }
    
    private void buildPASSWORD(String password) {
        int passwordLength = password.length();
        if ((passwordLength == 0) ) {
            throw new IllegalArgumentException("Password size must be > 0");
        }
        writeScalarString(CodePoint.PASSWORD, password, 0, DRDAConstants.PASSWORD_MAXSIZE,
                SQLState.NET_PASSWORD_TOO_LONG);
    }
    
    private void buildSECTKN(byte[] sectkn) {
        if (sectkn.length > 32763) {
            throw new IllegalArgumentException("SQLState.NET_SECTKN_TOO_LONG");
        }
        writeScalarBytes(CodePoint.SECTKN, sectkn);
    }
    
    /**
     * 
     * Relational Database Name specifies the name of a relational database
     * of the server.
     * if length of RDB name &lt;= 18 characters, there is not change to the format
     * of the RDB name.  The length of the RDBNAM remains fixed at 18 which includes
     * any right bland padding if necessary.
     * if length of the RDB name is &gt; 18 characters, the length of the RDB name is
     * identical to the length of the RDB name.  No right blank padding is required.
     * @param rdbnam  name of the database.
     * @param dontSendOnConversionError omit sending the RDBNAM if there is an
     * exception converting to EBCDIC.  This will be used by ACCSEC to defer
     * sending the RDBNAM to SECCHK if it can't be converted.
     *
     */
    private void buildRDBNAM(String rdbnam, boolean dontSendOnConversionError) {
        //DERBY-4805(Increase the length of the RDBNAM field in the 
        // DRDA implementation)
        //The new RDBNAM length in 10.11 is 1024bytes(it used to be 254 bytes).
        //But if a 10.11 or higher client talks to a 10.10 or under server with
        // a RDBNAM > 254 bytes, it will result in a protocol exception
        // because those servers do not support RDBNAM greater than 254 bytes.
        // This behavior will logged in the jira.
        //One way to fix this would have been to check the server version
        // before hand but we do not have that information when the client is
        // first trying to establish connection to the server by sending the
        // connect request along with the RDBNAM.
        int maxRDBlength = 1024;
        writeScalarString(CodePoint.RDBNAM, rdbnam,
                18, //minimum RDBNAM length in bytes
                maxRDBlength,  
                SQLState.NET_DBNAME_TOO_LONG);
                
    }
    
    // Precondition: valid secmec is assumed.
    private void buildSECMEC(int secmec) {
//        writeScalar2Bytes(CodePoint.SECMEC, secmec);
        ensureLength(6);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x06);
        buffer.writeShort(CodePoint.SECMEC);
        buffer.writeShort(secmec);
    }
    
    // build the Exchange Server Attributes Command.
    // This command sends the following information to the server.
    // - this driver's server class name
    // - this driver's level of each of the manager's it supports
    // - this driver's product release level
    // - this driver's external name
    // - this driver's server name
    public void buildEXCSAT(String externalName,
                     int targetAgent,
                     int targetSqlam,
                     int targetRdb,
                     int targetSecmgr,
                     int targetCmntcpip,
                     int targetCmnappc,
                     int targetXamgr,
                     int targetSyncptmgr,
                     int targetRsyncmgr,
                     int targetUnicodemgr) throws SQLException {
        createCommand();

        // begin excsat collection by placing the 4 byte llcp in the buffer.
        // the length of this command will be computed later and "filled in"
        // with the call to request.updateLengthBytes().
        markLengthBytes(CodePoint.EXCSAT);

        // place the external name for the client into the buffer.
        // the external name was previously calculated before the call to this method.
        buildEXTNAM(externalName);

        // place the server name for the client into the buffer.
        buildSRVNAM(getHostname());

        // place the server release level for the client into the buffer.
        // this is a hard coded value for the driver.
        buildSRVRLSLV();

        // the managers supported by this driver and their levels will
        // be sent to the server.  the variables which store these values
        // were initialized during object constrcution to the highest values
        // supported by the driver.

        // for the case of the manager levels object, there is no
        // need to have the length of the ddm object dynamically calculated
        // because this method knows exactly how many will be sent and can set
        // this now.
        // each manager level class and level are 4 bytes long and
        // right now 5 are being sent for a total of 20 bytes or 0x14 bytes.
        // writeScalarHeader will be called to insert the llcp.
        buildMGRLVLLS(targetAgent,
                targetSqlam,
                targetRdb,
                targetSecmgr,
                targetXamgr,
                targetSyncptmgr,
                targetRsyncmgr,
                targetUnicodemgr);


        // place the server class name into the buffer.
        // this value is hard coded for the driver.
        buildSRVCLSNM();

        // the excsat command is complete so the updateLengthBytes method
        // is called to dynamically compute the length for this command and insert
        // it into the buffer
        updateLengthBytes();
    }
    
    private void buildSRVCLSNM() throws SQLException {
        // Server class name is hard-coded to QDERBY/JVM for dnc.
        writeScalarString(CodePoint.SRVCLSNM, "QDB2/JVM");
    }
    
    private void buildMGRLVLLS(int agent, int sqlam, int rdb, int secmgr, int xamgr, int syncptmgr, int rsyncmgr,
            int unicodemgr) throws SQLException {
        markLengthBytes(CodePoint.MGRLVLLS);

        // place the managers and their levels in the buffer
        buffer.writeShort(CodePoint.AGENT);
        buffer.writeShort(agent);
        buffer.writeShort(CodePoint.SQLAM);
        buffer.writeShort(sqlam);
        buffer.writeShort(CodePoint.UNICODEMGR);
        buffer.writeShort(unicodemgr);
        buffer.writeShort(CodePoint.RDB);
        buffer.writeShort(rdb);
        buffer.writeShort(CodePoint.SECMGR);
        buffer.writeShort(secmgr);
        buffer.writeShort(CodePoint.CMNTCPIP);// @AGG added
        buffer.writeShort(0x08);

        // @AGG removed
        // if (netAgent_.netConnection_.isXAConnection()) {
        // if (xamgr != NetConfiguration.MGRLVL_NA) {
        // writeCodePoint4Bytes(CodePoint.XAMGR, xamgr);
        // }
        // if (syncptmgr != NetConfiguration.MGRLVL_NA) {
        // writeCodePoint4Bytes(CodePoint.SYNCPTMGR, syncptmgr);
        // }
        // if (rsyncmgr != NetConfiguration.MGRLVL_NA) {
        // writeCodePoint4Bytes(CodePoint.RSYNCMGR, rsyncmgr);
        // }
        // }
        updateLengthBytes();
    }
    
    // The External Name is the name of the job, task, or process on a
    // system for which a DDM server is active.
    private void buildEXTNAM(String extnam) throws SQLException {
        final int MAX_SIZE = 255;
        int extnamTruncateLength = Math.min(extnam.length(),MAX_SIZE);

        // Writing the truncated string as to preserve previous behavior
        writeScalarString(CodePoint.EXTNAM, extnam.substring(0, extnamTruncateLength), 141, // @AGG changed min byte len 0->141
                MAX_SIZE, "NET_EXTNAM_TOO_LONG");
    }
    
    // Server Name is the name of the DDM server.
    private void buildSRVNAM(String srvnam) throws SQLException {
        final int MAX_SIZE = 255;
        int srvnamTruncateLength = Math.min(srvnam.length(),MAX_SIZE);
        
        // Writing the truncated string as to preserve previous behavior
        writeScalarString(CodePoint.SRVNAM,srvnam.substring(0, srvnamTruncateLength),
                0, MAX_SIZE, "SQLState.NET_SRVNAM_TOO_LONG");
    }
    
    // Server Product Release Level String specifies the product
    // release level of a DDM server.
    private void buildSRVRLSLV() {
        writeScalarString(CodePoint.SRVRLSLV, DRDAConstants.PRDID);
    }

}
