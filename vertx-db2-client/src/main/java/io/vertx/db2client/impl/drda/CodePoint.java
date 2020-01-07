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


public class CodePoint {
    // ---------------callable statement codepoints-------------------------------

    /** 
     * PKGSNLST
     */
    public static final int PKGSNLST = 0x2139;

    /** 
     * Output Expected
     */
    public static final int OUTEXP = 0x2111;

    /** 
     * Procedure Name
     */
    public static final int PRCNAM = 0x2138;

    /** 
     * Maximum Result Set Count.
     */
    public static final int MAXRSLCNT = 0x2140;

    /** 
     * Maximum Result Set Count No Limit.
     * Requester is capable of receiving all result sets in the response to EXCSQLSTT.
     */
    public static final int MAXRSLCNT_NOLIMIT = 0xffff;

    /** 
     * Result Set Flags
     */
    public static final int RSLSETFLG = 0x2142;

    /** 
     * RSLSETFLGs added in SQLAM 7 for requesting standard, extended, or light sqldas
     */
    public static final int RSLSETFLG_EXTENDED_SQLDA = 0x04;

    // --------------------code points for constant ddm data----------------------

    /** 
     * Indicates false state.  This 1-byte code point is used by some DDM parameters.
     */
    public static final byte FALSE = -16;  // was 0xf0

    /** 
     * Indicates true state.  This 1-byte code point is used by some DDM parameters.
     */
    public static final byte TRUE = -15;  // was 0xf1

    /** 
     * FDOCA NULL indicator constant.
     * Indicates data does not flow.
     */
    public static final int NULLDATA = 0xFF;

    /** 
     * Security check was successful.
     */
    public static final int SECCHKCD_00 = 0x00;

    /** 
     * SECMEC value not supported.
     */
    public static final int SECCHKCD_01 = 0x01;

    /** 
     * Local security service non-retryable error.
     */
    public static final int SECCHKCD_0A = 0x0A;

    /** 
     * SECTKN missing or invalid.
     */
    public static final int SECCHKCD_0B = 0x0B;

    /** 
     * Password expired.
     */
    public static final int SECCHKCD_0E = 0x0E;

    /** 
     * Password invalid.
     */
    public static final int SECCHKCD_0F = 0x0F;

    /** 
     * Password missing.
     */
    public static final int SECCHKCD_10 = 0x10;

    /** 
     * Userid missing.
     */
    public static final int SECCHKCD_12 = 0x12;

    /** 
     * Userid invalid.
     */
    public static final int SECCHKCD_13 = 0x13;

    /** 
     * Userid revoked.
     */
    public static final int SECCHKCD_14 = 0x14;

    /** 
     * New password invalid.
     */
    public static final int SECCHKCD_15 = 0x15;

    //-----------------------ddm enumerated values-------------------------------

    /** 
     * TYPSQLDA - Standard Output SQLDA
     */
    public static final int TYPSQLDA_STD_OUTPUT = 0;

    /** 
     * TYPSQLDA - Extended Output SQLDA
     */
    public static final int TYPSQLDA_X_OUTPUT = 4;

    /** 
     * TYPSQLDA - Extended Input SQLDA
     */
    public static final int TYPSQLDA_X_INPUT = 5;

    /** 
     * QRYCLSIMP - Target Server must implicitly close the cursor
     * upon SQLSTATE 02000.
     */
    public static final int QRYCLSIMP_YES = 0x01;

    // ----------------------ddm code points--------------------------------------

    /** 
     * Exchange Server Attributes.
     */
    final static int EXCSAT = 0x1041;


    /** 
     * Sync Point Control Request.
     */
    final static int SYNCCTL = 0x1055;

    /** 
     * Sync Point Resync Command.
     */
    final static int SYNCRSY = 0x1069;

    /** 
     * Access Security.
     */
    final static int ACCSEC = 0x106D;

    /** 
     * Security Check.
     */
    final static int SECCHK = 0x106E;

    /** 
     * Access RDB.
     */
    final static int ACCRDB = 0x2001;

    /** 
     * Close Query.
     */
    final static int CLSQRY = 0x2005;

    /** 
     * Continue Query.
     */
    final static int CNTQRY = 0x2006;


    /** 
     * Describe SQL Statement.
     */
    final static int DSCSQLSTT = 0x2008;


    /** 
     * Execute Immediate SQL Statement.
     */
    final static int EXCSQLIMM = 0x200A;

    /** 
     * Execute SQL Statement.
     */
    final static int EXCSQLSTT = 0x200B;

    /** 
     * Set SQL Environment.
     */
    final static int EXCSQLSET = 0x2014;

    /** 
     * Open Query.
     */
    final static int OPNQRY = 0x200C;

    /** 
     * Output override.
     */
    final static int OUTOVR = 0x2415;

    /** 
     * Prepare SQL Statement.
     */
    final static int PRPSQLSTT = 0x200D;

    /** 
     * RDB Commit Unit of Work.
     */
    final static int RDBCMM = 0x200E;

    /** 
     * RDB Rollback Unit of Work.
     */
    final static int RDBRLLBCK = 0x200F;


    /** 
     * Describe RDB Table.
     */
    final static int DSCRDBTBL = 0x2012;

    /** 
     * SQL Program Variable Data.
     */
    final static int SQLDTA = 0x2412;

    /** 
     * SQL Data Reply Data.
     */
    final static int SQLDTARD = 0x2413;

    /** 
     * SQL Statement.
     */
    final static int SQLSTT = 0x2414;


    /** 
     * Query Answer Set Description.
     */
    final static int QRYDSC = 0x241A;

    /** 
     * Query Answer Set Data.
     */
    final static int QRYDTA = 0x241B;

    /** 
     * SQL Statement Attributes.
     */
    final static int SQLATTR = 0x2450;

    /** Access Security Reply Data.
    Contains the security information from a target server's
    security manager.  This information is returned in response
    to an ACCSEC command.
     */
    public static final int ACCSECRD = 0x14AC;


    /** 
     * Agent codepoint constant.
     */
    public static final int AGENT = 0x1403;

    /** 
     * UNICODE Manager. Min. level 0.
     * Provides character encoding of the DDM objects and parameters
     */
    public static final int UNICODEMGR = 0x1C08;
    
    /** 
     * The codepoint for codepoint
     */
    public static final int CODPNT = 0x000C;

    /** 
     * CCSID for Double-Byte Characters codepoint constant.
     */
    public static final int CCSIDDBC = 0x119D;

    /** 
     * CCSID for Mixed-Byte Characters codepoint constant.
     */
    public static final int CCSIDMBC = 0x119E;


    /** 
     * CCSID for Single-Byte Characters codepoint constant.
     */
    public static final int CCSIDSBC = 0x119C;
    
    /** 
     * @AGG not sure what codepoint this is but getting it at the end of a ACCRDBRM.TYPEDEFOVR
     */
    public static final int CCSIDXML = 0x1913;

    /** 
     * Describes the communications manager that supports
     conversational protocols by using System Network
     Architecture Logical Unit 6.2 (SNA LU 6.2) local
     communications facilities.
     */
    public static final int CMNAPPC = 0x1444;

    /** 
     * TCP/IP Communication Manager codepoint constant.  Min. level 5.
     */
    public static final int CMNTCPIP = 0x1474;

    /** 
     * Correlation Token codepoint constant.
     */
    public static final int CRRTKN = 0x2135;

    /** 
     * Server Attributes Reply Data codepoint constant.
     */
    public static final int EXCSATRD = 0x1443;

    /** 
     * External Name codepoint constant.
     */
    public static final int EXTNAM = 0x115E;

    /** 
     * Fixed Row Query Protocol.
     */
    public static final int FIXROWPRC = 0x2418;

    /** 
     * Limited Block Query Protocol.
     */
    public static final int LMTBLKPRC = 0x2417;

    /** 
     * Maximum Number of Extra Blocks.
     */
    public static final int MAXBLKEXT = 0x2141;

    /** 
     * Manager Level List codepoint constant.
     */
    public static final int MGRLVLLS = 0x1404;

    /** 
     * Password
     */
    public static final int PASSWORD = 0x11A1;

    /** 
     * Conversational Protocol Error Code
     */
    public static final int PRCCNVCD = 0x113F;

    /** 
     * Product Specific Identifier codepoint constant.
     */
    public static final int PRDID = 0x112E;

    /** 
     * Product Specific Data
     */
    public static final int PRDDTA = 0x2104;

    /** 
     * Query Attribute for Scrollability.
     */
    public static final int QRYATTSCR = 0x2149;

    /** 
     * Query Attribute for Rowset
     */
    public static final int QRYATTSET = 0x214A;

    /** 
     * Query Attribute for Sensitivity.
     */
    public static final int QRYATTSNS = 0x2157;

    /** 
     * Query Attribute for Updatability.
     */
    public static final int QRYATTUPD = 0x2150;
    
    /**
     * 
     */
    public static final int QRYATTISOL = 9312;

    /** 
     * Query Close Implicit
     */
    public static final int QRYCLSIMP = 0x215D;

    /** 
     * Query Scroll Orientation.
     */
    public static final int QRYSCRORN = 0x2152;

    /** 
     * Query Scroll Relative Orientation.
     */
    public static final int QRYSCRREL = 1;

    /** 
     * Query Scroll Absolute Orientation.
     */
    public static final int QRYSCRABS = 2;

    /** 
     * Query Scroll After Orientation.
     */
    public static final int QRYSCRAFT = 3;

    /** 
     * Query Scroll Before Orientation.
     */
    public static final int QRYSCRBEF = 4;

    /** 
     * Query Instance Identifier
     */
    public static final int QRYINSID = 0x215B;

    /** 
     * Query Insensitive to Changes
     */
    public static final int QRYINS = 1;

    /** 
     * Sensitive static
     */
    public static final int QRYSNSSTC = 0x2;

    /** 
     * Query Attributes is Unknown or Undefined
     */
    public static final int QRYUNK = 0;

    /** 
     * Query Row Number.
     */
    public static final int QRYROWNBR = 0x213D;

    /** 
     * Query Block Reset.
     */
    public static final int QRYBLKRST = 0x2154;

    /** 
     * Query Returns Data.
     */
    public static final int QRYRTNDTA = 0x2155;

    /** 
     * Query Block Size
     */
    public static final int QRYBLKSZ = 0x2114;

    /** 
     * Query Protocol Type
     */
    public static final int QRYPRCTYP = 0x2102;

    /** 
     * Query Rowset Size.
     */
    public static final int QRYROWSET = 0x2156;

    /** 
     * Cursor is Read-only.
     */
    public static final int QRYRDO = 0x1;

    /** 
     * Cursor Allows Read, Delete, and Update Operations.
     */
    public static final int QRYUPD = 0x4;

    /** 
     * Relational Database codepoint constant.  Min. level 3.
     */
    public static final int RDB = 0x240F;

    /** 
     * RDB Access Manager Class.
     */
    public static final int RDBACCCL = 0x210F;

    /** 
     * RDB Allow Updates
     */
    public static final int RDBALWUPD = 0x211A;

    /** 
     * Relational Database Name codepoint constant.
     */
    public static final int RDBNAM = 0x2110;


    /** 
     * Resynchronization Manager.  Min levl 5.
     It is a manager object of DDM that performs
     resynchronization for in-doubt units of work after
     a sync point operation failure.
     */
    public static final int RSYNCMGR = 0x14C1;

    /** 
     * Retuan SQL Descriptor Area
     */
    public static final int RTNSQLDA = 0x2116;


    /** 
     * Type of SQL Descriptor Area
     */
    public static final int TYPSQLDA = 0x2146;

    /** 
     * Security Check Code codepoint constant.
     */
    public static final int SECCHKCD = 0x11A4;

    /** 
     * Security Mechanism codepoint constant.
     */
    public static final int SECMEC = 0x11A2;

    /** 
     * Security Manager codepoint constant.
     */
    public static final int SECMGR = 0x1440;

    /** 
     * Security Token codepoint constant.
     */
    public static final int SECTKN = 0x11DC;

    /** 
     * SQL Application Manager codepoint constant.  Min. level 3.
     */
    public static final int SQLAM = 0x2407;

    /** 
     * SQL Communication Area Reply Data codepoint constant.
     */
    public static final int SQLCARD = 0x2408;

    /** 
     * SQL Result Set Column Information Reply Data.
     */
    public static final int SQLCINRD = 0x240B;

    /** 
     * Hold Cursor Position
     */
    public static final int SQLCSRHLD = 0x211F;

    /** 
     * SQL Result Set Reply Data.
     */
    public static final int SQLRSLRD = 0x240E;

    /** 
     * SQLDA Reply Data codepoint constant.
     * See DRDA Arch Vol 3 pg. 859
     */
    public static final int SQLDARD = 0x2411;

    /** 
     * Server Class Name codepoint constant.
     */
    public static final int SRVCLSNM = 0x1147;


    /** 
     * Server Name codepoint constant.
     */
    public static final int SRVNAM = 0x116D;

    /** 
     * Server Product Release Level codepoint constant.
     */
    public static final int SRVRLSLV = 0x115A;

    /**
     *  Severity Code codepoint constant.
     */
    public static final int SVRCOD = 0x1149;

    /** 
     * Sync Point Manager.  Min. level 4.
     It is a manager object of DDM that coordinates resource
     recovery of the units of work associated with recoverable
     resources in multiple DDM servers.
     */
    public static final int SYNCPTMGR = 0x14C0;

    /** 
     * Syntax Error code
     */
    public static final int SYNERRCD = 0x114A;

    /** 
     * Data Type Definition Name codepoint constant.
     */
    public static final int TYPDEFNAM = 0x002F;

    /** 
     * TYPDEF Overrides codepoint constant.
     */
    public static final int TYPDEFOVR = 0x0035;

    /** 
     * Unit of Work Disposition codepoint constant.
     */
    public static final int UOWDSP = 0x2115;

    /** 
     * Unit of Work Disposition.  Committed Enumerated Value.
     */
    public static final int UOWDSP_COMMIT = 0x01;

    /** 
     * Unit of Work Dispostion. Rolled Back Enumerated Value.
     */
    public static final int UOWDSP_ROLLBACK = 0x02;

    /** 
     * Usrid codepoint constant.
     */
    public static final int USRID = 0x11A0;

    /** Rdb Package Name, Consistency Token, and Section
      Number codepoint constant.
      */
    public static final int PKGNAMCSN = 0x2113;

    /** 
     * RDB Package Section Number
     */
    public static final int PKGSN = 0x210C;

    /** 
     * XA Manager
     */
    public static final int XAMGR = 0x1C01;

    //-----------------------DDM reply codepoints---------------------------------

    /** 
     * Command Check codepoint constant.
     */
    public static final int CMDCHKRM = 0x1254;

    /** 
     * Command Not Supported codepoint constant.
     */
    public static final int CMDNSPRM = 0x1250;

    /** 
     * Abnormal End of Unit of Work Condition codepoint constant.
     */
    public static final int ABNUOWRM = 0x220D;

    /** 
     * Access to RDB Completed.
     Specifies that an instance of the SQL application manager
     has been created and is bound to the specified RDB.
     */
    public static final int ACCRDBRM = 0x2201;


    final static int MGRLVLRM = 0x1210;

    /** 
     * End Unit of Work Condition codepoint constant.
     */
    public static final int ENDUOWRM = 0x220C;

    /** 
     * Object Not Supported codepoint constant.
     */
    public static final int OBJNSPRM = 0x1253;

    /** 
     * Conversational Protocol Error
     */
    public static final int PRCCNVRM = 0x1245;

    /** 
     * Query not open codepoint constant.
     */
    public static final int QRYNOPRM = 0x2202;

    /** 
     * Query previously opened codepoint
     */
    public static final int QRYPOPRM = 0x220F;

    /** 
     * RDB Currently Accessed Codepoint
     */
    public static final int RDBACCRM = 0x2207;

    /** 
     * RDB Commit Allowed codepoint
     */
    public static final int RDBCMTOK = 0x2105;
    
    public static final int RDBINTTKN = 0x2103;

    /** 
     * Security Check.
     Indicates the acceptability of the security information.
     */
    public static final int SECCHKRM = 0x1219;

    /** 
     * RDB Access Failed Reply Message codepoint
     */
    public static final int RDBAFLRM = 0x221A;

    /** 
     * Not Authorized To RDB reply message codepoint
     */
    public static final int RDBATHRM = 0x22CB;

    /** 
     * RDB Not Accessed codepoint constant.
     */
    public static final int RDBNACRM = 0x2204;

    /** 
     * RDB not found codepoint
     */
    public static final int RDBNFNRM = 0x2211;

    /** 
     * RDB Update Reply Message codepoint constant.
     */
    public static final int RDBUPDRM = 0x2218;

    /** 
     * Data Stream Syntax Error
     */
    public static final int SYNTAXRM = 0x124C;

    /** 
     * Parameter Value Not Supported codepoint constant.
     */
    public static final int VALNSPRM = 0x1252;

    /** 
     * SQL Error Condition codepoint constant.
     */
    public static final int SQLERRRM = 0x2213;

    /**
     *  Open Query Complete.
     *  See: DRDA Vol. 3: DDM Architecture pg. 566
     */
    final static int OPNQRYRM = 0x2205;

    /** 
     * End of Query.
     */
    final static int ENDQRYRM = 0x220B;

    /** 
     * Data Descriptor Mismatch.
     */
    final static int DTAMCHRM = 0x220E;

    /** 
     * Open Query Failure.
     */
    final static int OPNQFLRM = 0x2212;

    /** 
     * RDB Result Set Reply Message.
     */
    final static int RSLSETRM = 0x2219;

    //----------------------------fdoca code points-------------------------------

    public static final int RTNEXTDTA = 0x2148;
    public static final int RTNEXTALL = 0x02;

    /** 
     * Externalized FD:OCA Data codepoint constant.
     */
    public static final int EXTDTA = 0x146C;

    /** 
     * FDOCA data descriptor
     */
    public static final int FDODSC = 0x0010;

    /** 
     * FDOCA data
     */
    public static final int FDODTA = 0x147A;

    /** 
     * --- Product-specific 0xC000-0xFFFF ---
     * Piggy-backed session data (product-specific)
     */
    public static final int PBSD = 0xC000;

    /** 
     * Isolation level as a byte (product-specific)
     */
    public static final int PBSD_ISO = 0xC001;

    /** 
     * Current schema as UTF8 String (product-specific)
     */
    public static final int PBSD_SCHEMA = 0xC002;

    //--------------------------ddm error code points---------------------------------
    /** 
     * Syntax Error Code.  DSS header length less than 6.
     */
    public static int SYNERRCD_DSS_LESS_THAN_6 = 0x01;

    /** 
     * Syntax Error Code.  DSS header length does not match the number of
     * bytes of data found.
     */
    public static int SYNERRCD_DSS_LENGTH_BYTE_NUMBER_MISMATCH = 0x02;

    /** 
     * Syntax Error Code.  DSS header C-byte not D0.
     */
    public static int SYNERRCD_CBYTE_NOT_D0 = 0x03;

    /** 
     * Syntax Error Code.  DSS header f-bytes either not recognized or not supported.
     */
    public static int SYNERRCD_FBYTE_NOT_SUPPORTED = 0x04;

    /** 
     * Syntax Error Code.  Object length not allowed.
     */
    public static int SYNERRCD_OBJ_LEN_NOT_ALLOWED = 0x0B;

    /** 
     * Syntax Error Code.  Required object not found.
     */
    public static int SYNERRCD_REQ_OBJ_NOT_FOUND = 0x0E;

    /** 
     * Syntax Error Code.  Duplicate object present.
     */
    public static int SYNERRCD_DUP_OBJ_PRESENT = 0x12;

    /** 
     * Syntax Error Code.  Invalid request correlator specified.
     */
    public static int SYNERRCD_INVALID_CORRELATOR = 0x13;

    /** 
     * Syntax Error Code.  Incorrect large object extended length field.
     */
    public static int SYNERRCD_INCORRECT_EXTENDED_LEN = 0x0C;

    /** 
     * Syntax Error Code.  DSS continuation less than or equal to two.
     */
    public static int SYNERRCD_DSS_CONT_LESS_OR_EQUAL_2 = 0x16;

    /** 
     * Syntax Error Code.  DSS chaining bit not b'1', but DSSFMT bit3 set to b'1'.
     */
    public static int SYNERRCD_CHAIN_OFF_SAME_NEXT_CORRELATOR = 0x18;

    /** 
     * Syntax Error Code.  DSS chaining bit not b'1', but error continuation requested.
     */
    public static int SYNERRCD_CHAIN_OFF_ERROR_CONTINUE = 0x1A;

    /** 
     * Information Only Severity Code.
     */
    public static int SVRCOD_INFO = 0;

    /** 
     * Warning Severity Code.
     */
    public static int SVRCOD_WARNING = 4;

    /** 
     * Error Severity Code.
     */
    public static int SVRCOD_ERROR = 8;

    /** 
     * Severe Error Severity Code.
     */
    public static int SVRCOD_SEVERE = 16;

    /** 
     * Access Damage Severity Code.
     */
    public static int SVRCOD_ACCDMG = 32;

    /** 
     * Permanent Damage Severity Code.
     */
    public static int SVRCOD_PRMDMG = 64;

    /** 
     * Session Damage Severity Code.
     */
    public static int SVRCOD_SESDMG = 128;


    //--------------------------XA code points---------------------------

    /** 
     * SYNC Point Control Reply
     */
    public static final int SYNCCRD = 0x1248;

    /** 
     * XA Return Value
     */
    public static final int XARETVAL = 0x1904;

    /** 
     * XA Timeout Value;
     */
    public static final int TIMEOUT = 0x1907;

    /** 
     * new unit of work for XA
     */
    public static final int SYNCTYPE_NEW_UOW = 0x09;

    /** 
     * End unit of work (Sync type).
     */
    public static final int SYNCTYPE_END_UOW = 0x0B;

    /** 
     * Prepare to commit (Sync type).
     */
    public static final int SYNCTYPE_PREPARE = 0x01;

    /** 
     * commit sync type
     */
    public static final int SYNCTYPE_COMMITTED = 0x03;

    /** 
     * request to forget sync type
     */
    public static final int SYNCTYPE_REQ_FORGET = 0x06;

    /**
     * rollback sync type
     */
    public static final int SYNCTYPE_ROLLBACK = 0x04;

    /**
     * recover sync type
     */
    public static final int SYNCTYPE_INDOUBT = 0x0C;

    /** 
     * SYNC Type Codepoint
     */
    public static final int SYNCTYPE = 0x1187;

    /** 
     * XId Codepoint
     */
    public static final int XID = 0x1801;

    /** 
     * XA Flag Codepoint
     */
    public static final int XAFLAGS = 0x1903;

    /** 
     * XA Flags
     */
    public static final int TMLOCAL = 0x10000000;

    /** 
     * Prepared and hueristic complete list
     */
    public static final int PRPHRCLST = 0x1905;

    /** 
     * XID count
     */
    public static final int XIDCNT = 0x1906;


    // hide the default constructor
    private CodePoint() {
    }
}


