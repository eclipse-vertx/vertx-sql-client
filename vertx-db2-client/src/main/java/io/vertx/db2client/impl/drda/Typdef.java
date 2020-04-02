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
import java.sql.Types;

//
//General Notes
//Descriptors are overriden using two distinct mechanisms
//- new TYPDEFNAM and TYPDEFOVR specifications which override environmental
//specifications originally established at connect time.
//- MDD/SDA pairs providing specific field level overrides for user data
//not conforming to the TYPDEFNAM and TYPDEFOVR specifications currently
//in effect.  Grouping triplets then refer to the new SDAs to specify
//the actual representation of the user data.
//- There are two types of early descriptor triplets, the T and the M triplets.
//    Early
//    ---------------------------
//    Environmental   Grp Row Arr
//    TTTTTTTTTTTTT   MMMMMMMMMMM
//  - The T triplets are established by the TYPDEFNAM and TYPDEFOVR values.
//    These can be overridden for any command or reply by specifying
//    a new value for TYPDEFNAM and TYPDEFOVR.
//  - The M triplets are established by the MGRLVL parameter on EXCSAT.
//    They define PROTOCOL information units such as the SQLCA.
//    These grouping and structuring triplets cannot be overriden
//    Any change would mean a change in what information was exchanged
//    rather than just how that information would be represented.
//- There are two types of late descriptor triplets, the O and U triplets.
//    Late
//    ---------------------------
//    Environmental   Grp Row Arr
//    OOOOOOOOOOOOO   UUUUUUUUUUU
//  - The O triplets provide specific overrides.
//  - The U triplets define actual user data, sometimes in combination
//    with PROTOCOL information units.  The U triplets reference O triplets
//    and both T triplets and M triplets (which in turn reference T triplets).
//- Rules for assigning LIDs to O triplets
//  - select LID within range of 1 to 255.
//  - select LID which doesn't interfere with references to early triplets
//    or O triplets.
//
//requirements
//- if this object handles overrides, they are only in effect for one
//command or the reply to one command.  Make sure that the correct
//"in effect" overrides are used especially when MDD overrides are present.
//- raise error if no CCSIDMBC or CCSIDDBC has been specified for mixed or
//double byte data.  Return SQLSTATE 57017 with 0 as source CCSID token.
//Possible errors:
//- check for valid lid at SQLAM level.
//  - if the lid is greater than the max
//    supported lid then the descriptor is invalid for the supported SQLAM level.
//Error Checking and Reporting Notes taken from PROTOCOL manual.
//- If the receiver of an FDODSC finds it in error, the error must
//  be reported with a DDM message DSCINVRM.  If the descriptor
//  passes PROTOCOL validity checks, but the data does not match the
//  descriptors, the mismatch must be reported with a DDM message DTAMCHRM.
//  so descriptor must be correct and valid and then the data must match
//  the descriptor.
//- Possible General Errors
//  - 01 FD:OCA Triplet not used in PROTOCOL descriptors or Type code invalid.
//  - 02 Triplet Sequence Error: the two possible sequences are:
//       1.) GDA,(CPT,)RLO<,RLO><== normal case with no overrrides
//       2.) MDD,SDA,(MDD,SDA,)MDD,GDA,(CPT,)\
//           MDD,RLO<,MDD,RLO>
//           where () indicates an optional repeating group and <> indicates
//           a field allowed only when arrays are expected.
//  - 03 An array description is required, and this one does not
//       describe an array (probably too many or too few RLO triplets).
//  - 04 A row description is required, and this one does not describe a row
//       probably too many or too few RLO triplets.
//  - 05 Late Environmental Descriptor just received not supported (probably
//       due to non-support of requested overrides).
//  - 06 Malformed triplet; missing required parameter.
//  - 07 Parameter value not acceptable.
//- Possible MDD Errors
//  - 11 MDD present is not recognized as PROTOCOL Descriptor
//  - 12 MDD class not recognized as valid PROTOCOL class.
//  - 13 MDD type not recognized as valid PROTOCOL type.
//- Possible SDA Errors
//  - 21 Representation incompatible with PROTOCOL type (in prior MDD).
//  - 22 CCSID not supported
//- Possible GDA/CPT Errors
//  - 32 GDA references a LID that is not an SDA or GDA.
//  - 33 GDA length override exceeds limits.
//  - 34 GDA precision exceeds limits.
//  - 35 GDA scale > precision or scale negative
//  - 36 GDA length override missing or incompatible with protocol type.
//- Possible RLO Errors
//  - 41 RLO references a LID that is not an RLO or GDA
//  - 42 RLO fails to reference a required GDA or RLO (for example, QRYDSC
//       must include a reference to SQLCAGRP).
//
//Nullable SQL and PROTOCOL types are all odd numbers and nullable type is
//one number higher than the related non-nullable type
//
//earlyTTriplets
//late0Triplets
public class Typdef implements Cloneable {
    
    // @AGG making this static instead of on an Agent class
    public static final Typdef typdef = new Typdef(1208, "QTDSQLASC", 1200, 1208);
    public static final Typdef targetTypdef = new Typdef();
    public static final Typdef originalTargetTypdef_ = targetTypdef;
    
    // double byte character set
    private static final short CCSIDDBC = 1;

    // multi-byte character set
    private static final short CCSIDMBC = 2;

    // single byte character set
    private static final short CCSIDSBC = 3;

    // No CCSID
    private static final short NOCCSID = 0;

    // fixed length
    public static final short FIXEDLENGTH = 0;

    // 2-byte variable length
    public static final short TWOBYTELENGTH = 1;

    // 1-byte variable length
    public static final short ONEBYTELENGTH = 2;

    // decimal length
    public static final short DECIMALLENGTH = 3;

    // lob length
    public static final short LOBLENGTH = 4;
    
    private static final int OVERRIDE_TABLE_SIZE = 0xff;

    private static final int[] fdocaTypeToRepresentationMap_ = {
        /* 0x00 */ 0,
        /* 0x01 */ Cursor.BYTES, /* FDOCA_TYPE_FIXEDBYTES */
        /* 0x02 */ Cursor.VARIABLE_STRING, /* FDOCA_TYPE_VARBYTES */
        /* 0x03 */ Cursor.NULL_TERMINATED_BYTES, /* FDOCA_TYPE_NTBYTES */
        /* 0x04 */ 0,
        /* 0x05 */ 0,
        /* 0x06 */ 0,
        /* 0x07 */ Cursor.VARIABLE_SHORT_STRING, /* FDOCA_TYPE_PSCLBYTE */
        /* 0x08 */ 0,
        /* 0x09 */ 0,
        /* 0x0A */ 0,
        /* 0x0B */ 0,
        /* 0x0C */ 0,
        /* 0x0D */ 0,
        /* 0x0E */ 0,
        /* 0x0F */ 0,
        /* 0x10 */ Cursor.STRING, /* FDOCA_TYPE_FIXEDCHAR */
        /* 0x11 */ Cursor.VARIABLE_STRING, /* FDOCA_TYPE_VARCHAR */
        /* 0x12 */ 0,
        /* 0x13 */ 0,
        /* 0x14 */ Cursor.NULL_TERMINATED_STRING, /* FDOCA_TYPE_NTCHAR */
        /* 0x15 */ 0,
        /* 0x16 */ 0,
        /* 0x17 */ 0,
        /* 0x18 */ 0,
        /* 0x19 */ Cursor.VARIABLE_SHORT_STRING, /* FDOCA_TYPE_PSCLCHAR */
        /* 0x1A */ 0,
        /* 0x1B */ 0,
        /* 0x1C */ 0,
        /* 0x1D */ 0,
        /* 0x1E */ 0,
        /* 0x1F */ 0,
        /* 0x20 */ 0,
        /* 0x21 */ 0,
        /* 0x22 */ 0,
        /* 0x23 */ 1, //SignedBinary.BIG_ENDIAN, /* FDOCA_TYPE_INTEGER_BE */
        /* 0x24 */ 2, //SignedBinary.LITTLE_ENDIAN, /* FDOCA_TYPE_INTEGER_LE */
        /* 0x25 */ 0,
        /* 0x26 */ 0,
        /* 0x27 */ 0,
        /* 0x28 */ 0,
        /* 0x29 */ 0,
        /* 0x2A */ 0,
        /* 0x2B */ 0,
        /* 0x2C */ 0,
        /* 0x2D */ 0,
        /* 0x2E */ 0,
        /* 0x2F */ 0,
        /* 0x30 */ 0x30, // Decimal.PACKED_DECIMAL, /* FDOCA_TYPE_DECIMAL */
        /* 0x31 */ 0,
        /* 0x32 */ 0,
        /* 0x33 */ 0,
        /* 0x34 */ 0,
        /* 0x35 */ 0,
        /* 0x36 */ 0,
        /* 0x37 */ 0,
        /* 0x38 */ 0,
        /* 0x39 */ 0,
        /* 0x3A */ 0,
        /* 0x3B */ 0,
        /* 0x3C */ 0,
        /* 0x3D */ 0,
        /* 0x3E */ 0,
        /* 0x3F */ 0,
        /* 0x40 */ 0,
        /* 0x41 */ 0,
        /* 0x42 */ 0,
        /* 0x43 */ 0,
        /* 0x44 */ 0,
        /* 0x45 */ 0,
        /* 0x46 */ 0,
        /* 0x47 */ 0,
        /* 0x48 */ 0x48, // FloatingPoint.IEEE_754_FLOATING_POINT, /* FDOCA_TYPE_FLOAT_IEEE */
        /* 0x49 */ 0,
        /* 0x4A */ 0,
        /* 0x4B */ 0,
        /* 0x4C */ 0,
        /* 0x4D */ 0,
        /* 0x4E */ 0,
        /* 0x4F */ 0,
        /* 0x50 */ 0, /* FDOCA_TYPE_LOBBYTES */   // is 0 correct
        /* 0x51 */ 0, /* FDOCA_TYPE_LOBCHAR */    // is 0 correct
        /* 0x52 */ 0,
        /* 0x53 */ 0,
        /* 0x54 */ 0,
        /* 0x55 */ 0,
        /* 0x56 */ 0,
        /* 0x57 */ 0,
        /* 0x58 */ 0,
        /* 0x59 */ 0,
        /* 0x5A */ 0,
        /* 0x5B */ 0,
        /* 0x5C */ 0,
        /* 0x5D */ 0,
        /* 0x5E */ 0,
        /* 0x5F */ 0
    };

    //
    // FIXME: Instead of magic numbers, the first arg in each of these
    // constructor calls should be the corresponding constant from
    // DRDAConstants.
    //
    private static final FdocaSimpleDataArray[] environmentTables_ = {
        /* 0x00 Empties */
        null,
        /* 0x01 Empties */
        null,
        /* 0x02 4-byte int */
        new FdocaSimpleDataArray(0x02,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x03 null 4-byte int */
        new FdocaSimpleDataArray(0x03,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x04 2-byte int */
        new FdocaSimpleDataArray(0x04,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x05 null 2-byte int */
        new FdocaSimpleDataArray(0x05,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x06 1-byte int */
        new FdocaSimpleDataArray(0x06,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x07 null 1-byte int */
        new FdocaSimpleDataArray(0x07,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x08 16-byte bin float */
        new FdocaSimpleDataArray(0x08,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x09 null 16-byte bin float */
        new FdocaSimpleDataArray(0x09,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x0A 8-byte bin float */
        new FdocaSimpleDataArray(0x0A,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x0B null 8-byte bin float */
        new FdocaSimpleDataArray(0x0B,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x0C 4-byte bin float */
        new FdocaSimpleDataArray(0x0C,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x0D null 4-byte bin float */
        new FdocaSimpleDataArray(0x0D,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x0E Fixed Decimal */
        new FdocaSimpleDataArray(0x0E,  NOCCSID, 0, DECIMALLENGTH),
        /* 0x0F null Fixed Decimal */
        new FdocaSimpleDataArray(0x0F,  NOCCSID, 0, DECIMALLENGTH),
        /* 0x10 empties */
        null,
        /* 0x11 empties */
        null,
        /* 0x12 Numeric */
        null,
        /* 0x13 null Numeric */
        null,
        /* 0x14 Empties */
        null,
        /* 0x15 Empties */
        null,
        /* 0x16 Big int */
        new FdocaSimpleDataArray(0x16,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x17 null Big int */
        new FdocaSimpleDataArray(0x17,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x18 Large Obj Bytes Loc */
        new FdocaSimpleDataArray(0x18,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x19 null Large Obj Bytes Loc */
        new FdocaSimpleDataArray(0x19,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x1A Empties */
        null,
        /* 0x1B null Large Obj Char Loc */
        new FdocaSimpleDataArray(0x1B,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x1C Large Obj Char DBCS Loc */
        new FdocaSimpleDataArray(0x1C,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x1D null Large Obj Char DBCS Loc */
        new FdocaSimpleDataArray(0x1D,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x1E Row Identifier */
        new FdocaSimpleDataArray(0x1E,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x1F null Row Identifier */
        new FdocaSimpleDataArray(0x1F,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x20 Date */
        new FdocaSimpleDataArray(0x20,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x21 null Date */
        new FdocaSimpleDataArray(0x21,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x22 Time */
        new FdocaSimpleDataArray(0x22,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x23 null Time */
        new FdocaSimpleDataArray(0x23,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x24 Timestamp */
        new FdocaSimpleDataArray(0x24,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x25 null Timestamp */
        new FdocaSimpleDataArray(0x25,  CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x26 Fixed bytes */
        new FdocaSimpleDataArray(0x26, NOCCSID, 0, FIXEDLENGTH),
        /* 0x27 null Fixed bytes */
        new FdocaSimpleDataArray(0x27, NOCCSID, 0, FIXEDLENGTH),
        /* 0x28 Variable bytes */
        new FdocaSimpleDataArray(0x28,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x29 null Variable bytes */
        new FdocaSimpleDataArray(0x29,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x2A Long var bytes */
        new FdocaSimpleDataArray(0x2A,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x2B null Long var bytes */
        new FdocaSimpleDataArray(0x2B,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x2C Nullterm bytes */
        new FdocaSimpleDataArray(0x2C,  NOCCSID, 0, FIXEDLENGTH),
        /* 0x2D null Nullterm bytes */
        new FdocaSimpleDataArray(0x2D,   NOCCSID, 0, FIXEDLENGTH),
        /* 0x2E Nullterm SBCS */
        new FdocaSimpleDataArray(0x2E,   CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x2F null Nullterm SBCS */
        new FdocaSimpleDataArray(0x2F,   CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x30 Fix char SBCS */
        new FdocaSimpleDataArray(0x30, CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x31 null Fix char SBCS */
        new FdocaSimpleDataArray(0x31, CCSIDSBC, 1, FIXEDLENGTH),
        /* 0x32 Var char SBCS */
        new FdocaSimpleDataArray(0x32,  CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x33 null Var char SBCS */
        new FdocaSimpleDataArray(0x33,  CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x34 Long var SBCS */
        new FdocaSimpleDataArray(0x34,  CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x35 null Long var SBCS */
        new FdocaSimpleDataArray(0x35,  CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x36 Fix char DBCS */
        new FdocaSimpleDataArray(0x36,  CCSIDDBC, 2, FIXEDLENGTH),
        /* 0x37 null Fix char DBCS */
        new FdocaSimpleDataArray(0x37,  CCSIDDBC, 2, FIXEDLENGTH),
        /* 0x38 Var char DBCS */
        new FdocaSimpleDataArray(0x38,  CCSIDDBC, 2, TWOBYTELENGTH),
        /* 0x39 null Var char DBCS */
        new FdocaSimpleDataArray(0x39,  CCSIDDBC, 2, TWOBYTELENGTH),
        /* 0x3A Long var DBCS */
        new FdocaSimpleDataArray(0x3A,  CCSIDDBC, 2, TWOBYTELENGTH),
        /* 0x3B null Long var DBCS */
        new FdocaSimpleDataArray(0x3B,  CCSIDDBC, 2, TWOBYTELENGTH),
        /* 0x3C Fix char MBCS */
        new FdocaSimpleDataArray(0x3C,  CCSIDMBC, 1, FIXEDLENGTH),
        /* 0x3D null Fix char MBCS */
        new FdocaSimpleDataArray(0x3D,  CCSIDMBC, 1, FIXEDLENGTH),
        /* 0x3E Var char MBCS */
        new FdocaSimpleDataArray(0x3E,  CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x3F null Var char MBCS */
        new FdocaSimpleDataArray(0x3F,  CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x40 Long var MBCS */
        new FdocaSimpleDataArray(0x40,  CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x41 null Long var MBCS */
        new FdocaSimpleDataArray(0x41,  CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x42 Nullterm MBCS */
        new FdocaSimpleDataArray(0x42,  CCSIDMBC, 1, FIXEDLENGTH),
        /* 0x43 null Nullterm MBCS */
        new FdocaSimpleDataArray(0x43,  CCSIDMBC, 1, FIXEDLENGTH),
        /* 0x44 L String bytes */
        new FdocaSimpleDataArray(0x44,  NOCCSID, 0, ONEBYTELENGTH),
        /* 0x45 null L String bytes */
        new FdocaSimpleDataArray(0x45,  NOCCSID, 0, ONEBYTELENGTH),
        /* 0x46 L String SBCS */
        new FdocaSimpleDataArray(0x46,  CCSIDSBC, 1, ONEBYTELENGTH),
        /* 0x47 null L String SBCS */
        new FdocaSimpleDataArray(0x47,  CCSIDSBC, 1, ONEBYTELENGTH),
        /* 0x48 L String MBCS */
        new FdocaSimpleDataArray(0x48,  CCSIDMBC, 1, ONEBYTELENGTH),
        /* 0x49 null L String MBCS */
        new FdocaSimpleDataArray(0x49,  CCSIDMBC, 1, ONEBYTELENGTH),
        /* 0x4A Empties */
        null,
        /* 0x4B Empties */
        null,
        /* 0x4C  SBCS */
        new FdocaSimpleDataArray(0x4C,   CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x4D null  SBCS */
        new FdocaSimpleDataArray(0x4D,   CCSIDSBC, 1, TWOBYTELENGTH),
        /* 0x4E  MBCS */
        new FdocaSimpleDataArray(0x4E,   CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x4F null  MBCS */
        new FdocaSimpleDataArray(0x4F,   CCSIDMBC, 1, TWOBYTELENGTH),
        /* 0x50 UDT */
        new FdocaSimpleDataArray(0x50,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x51 null UDT */
        new FdocaSimpleDataArray(0x51,  NOCCSID, 0, TWOBYTELENGTH),
        /* 0x52 Empties */
        null,
        /* 0x53 Empties */
        null,
        /* 0x54 Empties */
        null,
        /* 0x55 Empties */
        null,
        /* 0x56 Empties */
        null,
        /* 0x57 Empties */
        null,
        /* 0x58 Empties */
        null,
        /* 0x59 Empties */
        null,
        /* 0x5A Empties */
        null,
        /* 0x5B Empties */
        null,
        /* 0x5C Empties */
        null,
        /* 0x5D Empties */
        null,
        /* 0x5E Empties */
        null,
        /* 0x5F Empties */
        null,
        /* 0x60 Empties */
        null,
        /* 0x61 Empties */
        null,
        /* 0x62 Empties */
        null,
        /* 0x63 Empties */
        null,
        /* 0x64 Empties */
        null,
        /* 0x65 Empties */
        null,
        /* 0x66 Empties */
        null,
        /* 0x67 Empties */
        null,
        /* 0x68 Empties */
        null,
        /* 0x69 Empties */
        null,
        /* 0x6A Empties */
        null,
        /* 0x6B Empties */
        null,
        /* 0x6C Empties */
        null,
        /* 0x6D Empties */
        null,
        /* 0x6E Empties */
        null,
        /* 0x6F Empties */
        null,
        /* 0x70 Empties */
        null,
        /* 0x71 Empties */
        null,
        /* 0x72 Empties */
        null,
        /* 0x73 Empties */
        null,
        /* 0x74 Empties */
        null,
        /* 0x75 Empties */
        null,
        /* 0x76 Empties */
        null,
        /* 0x77 Empties */
        null,
        /* 0x78 Empties */
        null,
        /* 0x79 Empties */
        null,
        /* 0x7A Empties */
        null,
        /* 0x7B Empties */
        null,
        /* 0x7C Empties */
        null,
        /* 0x7D Empties */
        null,
        /* 0x7E Empties */
        null,
        /* 0x7F Empties */
        null,
        /* 0x80 Empties */
        null,
        /* 0x81 Empties */
        null,
        /* 0x82 Empties */
        null,
        /* 0x83 Empties */
        null,
        /* 0x84 Empties */
        null,
        /* 0x85 Empties */
        null,
        /* 0x86 Empties */
        null,
        /* 0x87 Empties */
        null,
        /* 0x88 Empties */
        null,
        /* 0x89 Empties */
        null,
        /* 0x8A Empties */
        null,
        /* 0x8B Empties */
        null,
        /* 0x8C Empties */
        null,
        /* 0x8D Empties */
        null,
        /* 0x8E Empties */
        null,
        /* 0x8F Empties */
        null,
        /* 0x90 Empties */
        null,
        /* 0x91 Empties */
        null,
        /* 0x92 Empties */
        null,
        /* 0x93 Empties */
        null,
        /* 0x94 Empties */
        null,
        /* 0x95 Empties */
        null,
        /* 0x96 Empties */
        null,
        /* 0x97 Empties */
        null,
        /* 0x98 Empties */
        null,
        /* 0x99 Empties */
        null,
        /* 0x9A Empties */
        null,
        /* 0x9B Empties */
        null,
        /* 0x9C Empties */
        null,
        /* 0x9D Empties */
        null,
        /* 0x9E Empties */
        null,
        /* 0x9F Empties */
        null,
        /* 0xA0 Empties */
        null,
        /* 0xA1 Empties */
        null,
        /* 0xA2 Empties */
        null,
        /* 0xA3 Empties */
        null,
        /* 0xA4 Empties */
        null,
        /* 0xA5 Empties */
        null,
        /* 0xA6 Empties */
        null,
        /* 0xA7 Empties */
        null,
        /* 0xA8 Empties */
        null,
        /* 0xA9 Empties */
        null,
        /* 0xAA Empties */
        null,
        /* 0xAB Empties */
        null,
        /* 0xAC Empties */
        null,
        /* 0xAD Empties */
        null,
        /* 0xAE Empties */
        null,
        /* 0xAF Empties */
        null,
        /* 0xB0 Empties */
        null,
        /* 0xB1 Empties */
        null,
        /* 0xB2 Empties */
        null,
        /* 0xB3 Empties */
        null,
        /* 0xB4 Empties */
        null,
        /* 0xB5 Empties */
        null,
        /* 0xB6 Empties */
        null,
        /* 0xB7 Empties */
        null,
        /* 0xB8 Empties */
        null,
        /* 0xB9 Empties */
        null,
        /* 0xBA Empties */
        null,
        /* 0xBB Empties */
        null,
        /* 0xBC Empties */
        null,
        /* 0xBD Empties */
        null,
        /* 0xBE Boolean */
        new FdocaSimpleDataArray(0xBE,  NOCCSID, 0, FIXEDLENGTH),
        /* 0xBF null Boolean */
        new FdocaSimpleDataArray(0xBF,  NOCCSID, 0, FIXEDLENGTH),
        /* 0xC0 Empties */
        null,
        /* 0xC1 Empties */
        null,
        /* 0xC2 Empties */
        null,
        /* 0xC3 Empties */
        null,
        /* 0xC4 Empties */
        null,
        /* 0xC5 Empties */
        null,
        /* 0xC6 Empties */
        null,
        /* 0xC7 Empties */
        null,
        /* 0xC8 Large object bytes */
        new FdocaSimpleDataArray(0xC8,  NOCCSID, 0, LOBLENGTH),
        /* 0xC9 null Large object bytes */
        new FdocaSimpleDataArray(0xC9,  NOCCSID, 0, LOBLENGTH),
        /* 0xCA Large object char SBCS */
        new FdocaSimpleDataArray(0xCA,  CCSIDSBC, 1, LOBLENGTH),
        /* 0xCB null Large object char SBCS */
        new FdocaSimpleDataArray(0xCB,  CCSIDSBC, 1, LOBLENGTH),
        /* 0xCC Large object char DBCS */
        new FdocaSimpleDataArray(0xCC,  CCSIDDBC, 2, LOBLENGTH),
        /* 0xCD null Large object char DBCS */
        new FdocaSimpleDataArray(0xCD,  CCSIDDBC, 2, LOBLENGTH),
        /* 0xCE Large object char MBCS */
        new FdocaSimpleDataArray(0xCE,  CCSIDMBC, 1, LOBLENGTH),
        /* 0xCF null Large object char MBCS */
        new FdocaSimpleDataArray(0xCF,  CCSIDMBC, 1, LOBLENGTH),
    };

    private static final int[] protocolToJdbcTypes_ = {
        0x00, // 0x00 Empties
        0x00, // 0x01 Empties
        Types.INTEGER, // 0x02 4-byte int
        Types.INTEGER, // 0x03 null 4-byte int
        Types.SMALLINT, // 0x04 2-byte int
        Types.SMALLINT, // 0x05 null 2-byte int
        Types.TINYINT, // 0x06 1-byte int
        Types.TINYINT, // 0x07 null 1-byte int
        Types.DECIMAL, // 0x08 16-byte bin float
        Types.DECIMAL, // 0x09 null 16-byte bin float
        Types.DOUBLE, // 0x0A 8-byte bin float
        Types.DOUBLE, // 0x0B null 8-byte bin float
        Types.REAL, // 0x0C 4-byte bin float
        Types.REAL, // 0x0D null 4-byte bin float
        Types.DECIMAL, // 0x0E Fixed Decimal
        Types.DECIMAL, // 0x0F null Fixed Decimal
        Types.DECIMAL, // 0x10 Zone Decimal
        Types.DECIMAL, // 0x11 null Zone Decimal
        Types.DECIMAL, // 0x12 Numeric
        Types.DECIMAL, // 0x13 null Numeric
        0x00, // 0x14 Empties
        0x00, // 0x15 Empties
        Types.BIGINT, // 0x16 Big int
        Types.BIGINT, // 0x17 null Big int
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        Types.ROWID, // 0x1E Row ID
        Types.ROWID, // 0x1F null Row ID
        Types.DATE, // 0x20 Date
        Types.DATE, // 0x21 null Date
        Types.TIME, // 0x22 Time
        Types.TIME, // 0x23 null Time
        Types.TIMESTAMP, // 0x24 Timestamp
        Types.TIMESTAMP, // 0x25 null Timestamp
        Types.BINARY, // 0x26 Fixed bytes
        Types.BINARY, // 0x27 null Fixed bytes
        Types.VARBINARY, // 0x28 Variable bytes
        Types.VARBINARY, // 0x29 null Variable bytes
        Types.LONGVARBINARY, // 0x2A Long var bytes
        Types.LONGVARBINARY, // 0x2B null Long var bytes
        Types.VARBINARY, // 0x2C Nullterm bytes
        Types.VARBINARY, // 0x2D null Nullterm bytes
        Types.CHAR, // 0x2E Nullterm SBCS
        Types.CHAR, // 0x2F null Nullterm SBCS
        Types.CHAR, // 0x30 Fix char SBCS
        Types.CHAR, // 0x31 null Fix char SBCS
        Types.VARCHAR, // 0x32 Var char SBCS
        Types.VARCHAR, // 0x33 null Var char SBCS
        Types.LONGVARCHAR, // 0x34 Long var SBCS
        Types.LONGVARCHAR, // 0x35 null Long var SBCS
        0x00, // 0x36 Empties
        0x00, // 0x37 Empties
        0x00, // 0x38 Empties
        0x00, // 0x39 Empties
        0x00, // 0x3A Empties
        0x00, // 0x3B Empties
        // The following will have to be changed later for situations where
        Types.CHAR, // 0x3C Fix char MBCS
        Types.CHAR, // 0x3D null Fix char MBCS
        Types.VARCHAR, // 0x3E Var char MBCS
        Types.VARCHAR, // 0x3F null Var char MBCS
        Types.LONGVARCHAR, // 0x40 Long var MBCS
        Types.LONGVARCHAR, // 0x41 null Long var MBCS
        Types.CHAR, // 0x42 Nullterm MBCS
        Types.CHAR, // 0x43 null Nullterm MBCS
        Types.VARBINARY, // 0x44 L String bytes
        Types.VARBINARY, // 0x45 null L String bytes
        Types.VARCHAR, // 0x46 L String SBCS
        Types.VARCHAR, // 0x47 null L String SBCS
        Types.VARCHAR, // 0x48 L String MBCS
        Types.VARCHAR, // 0x49 null L String MBCS
        0x00, // 0x4A Empties
        0x00, // 0x4B Empties
        Types.VARCHAR, // 0x4C SBCS
        Types.VARCHAR, // 0x4D null SBCS
        Types.VARCHAR, // 0x4E MBCS
        Types.VARCHAR, // 0x4F null MBCS
        Types.JAVA_OBJECT, // 0x50 UDT
        Types.JAVA_OBJECT, // 0x51 null UDT
        0x00, // 0x52 Empties
        0x00, // 0x53 Empties
        0x00, // 0x54 Empties
        0x00, // 0x55 Empties
        0x00, // 0x56 Empties
        0x00, // 0x57 Empties
        0x00, // 0x58 Empties
        0x00, // 0x59 Empties
        0x00, // 0x5A Empties
        0x00, // 0x5B Empties
        0x00, // 0x5C Empties
        0x00, // 0x5D Empties
        0x00, // 0x5E Empties
        0x00, // 0x5F Empties
        0x00, // 0x60 Empties
        0x00, // 0x61 Empties
        0x00, // 0x62 Empties
        0x00, // 0x63 Empties
        0x00, // 0x64 Empties
        0x00, // 0x65 Empties
        0x00, // 0x66 Empties
        0x00, // 0x67 Empties
        0x00, // 0x68 Empties
        0x00, // 0x69 Empties
        0x00, // 0x6A Empties
        0x00, // 0x6B Empties
        0x00, // 0x6C Empties
        0x00, // 0x6D Empties
        0x00, // 0x6E Empties
        0x00, // 0x6F Empties
        0x00, // 0x70 Empties
        0x00, // 0x71 Empties
        0x00, // 0x72 Empties
        0x00, // 0x73 Empties
        0x00, // 0x74 Empties
        0x00, // 0x75 Empties
        0x00, // 0x76 Empties
        0x00, // 0x77 Empties
        0x00, // 0x78 Empties
        0x00, // 0x79 Empties
        0x00, // 0x7A Empties
        0x00, // 0x7B Empties
        0x00, // 0x7C Empties
        0x00, // 0x7D Empties
        0x00, // 0x7E Empties
        0x00, // 0x7F Empties
        0x00, // 0x80 Empties
        0x00, // 0x81 Empties
        0x00, // 0x82 Empties
        0x00, // 0x83 Empties
        0x00, // 0x84 Empties
        0x00, // 0x85 Empties
        0x00, // 0x86 Empties
        0x00, // 0x87 Empties
        0x00, // 0x88 Empties
        0x00, // 0x89 Empties
        0x00, // 0x8A Empties
        0x00, // 0x8B Empties
        0x00, // 0x8C Empties
        0x00, // 0x8D Empties
        0x00, // 0x8E Empties
        0x00, // 0x8F Empties
        0x00, // 0x90 Empties
        0x00, // 0x91 Empties
        0x00, // 0x92 Empties
        0x00, // 0x93 Empties
        0x00, // 0x94 Empties
        0x00, // 0x95 Empties
        0x00, // 0x96 Empties
        0x00, // 0x97 Empties
        0x00, // 0x98 Empties
        0x00, // 0x99 Empties
        0x00, // 0x9A Empties
        0x00, // 0x9B Empties
        0x00, // 0x9C Empties
        0x00, // 0x9D Empties
        0x00, // 0x9E Empties
        0x00, // 0x9F Empties
        0x00, // 0xA0 Empties
        0x00, // 0xA1 Empties
        0x00, // 0xA2 Empties
        0x00, // 0xA3 Empties
        0x00, // 0xA4 Empties
        0x00, // 0xA5 Empties
        0x00, // 0xA6 Empties
        0x00, // 0xA7 Empties
        0x00, // 0xA8 Empties
        0x00, // 0xA9 Empties
        0x00, // 0xAA Empties
        0x00, // 0xAB Empties
        0x00, // 0xAC Empties
        0x00, // 0xAD Empties
        0x00, // 0xAE Empties
        0x00, // 0xAF Empties
        0x00, // 0xB0 Empties
        0x00, // 0xB1 Empties
        0x00, // 0xB2 Empties
        0x00, // 0xB3 Empties
        0x00, // 0xB4 Empties
        0x00, // 0xB5 Empties
        0x00, // 0xB6 Empties
        0x00, // 0xB7 Empties
        0x00, // 0xB8 Empties
        0x00, // 0xB9 Empties
        0x00, // 0xBA Empties
        0x00, // 0xBB Empties
        0x00, // 0xBC Empties
        0x00, // 0xBD Empties
        Types.BOOLEAN, // 0xBE Boolean
        Types.BOOLEAN, // 0xBF null Boolean
        0x00, // 0xC0 Empties
        0x00, // 0xC1 Empties
        0x00, // 0xC2 Empties
        0x00, // 0xC3 Empties
        0x00, // 0xC4 Empties
        0x00, // 0xC5 Empties
        0x00, // 0xC6 Empties
        0x00, // 0xC7 Empties
        Types.BLOB, // 0xC8 Large object bytes
        Types.BLOB, // 0xC9 null Large object bytes
        Types.CLOB, // 0xCA Large object char SBCS
        Types.CLOB, // 0xCB null Large object char SBCS
        0x00,
        0x00,
        Types.CLOB, // 0xCE Large object char MBCS
        Types.CLOB, // 0xCF null Large object char MBCS
    };


    private String typdefnam_;

    private int ccsidSbc_;
    private boolean ccsidSbcSet_;
    private Charset ccsidSbcEncoding_;

    private int ccsidDbc_;
    private boolean ccsidDbcSet_;
    private Charset ccsidDbcEncoding_;


    private int ccsidMbc_;
    private boolean ccsidMbcSet_;
    private Charset ccsidMbcEncoding_;


    private boolean mddOverride_ = false;
    private FdocaSimpleDataArray overrideTable_[] = new FdocaSimpleDataArray[OVERRIDE_TABLE_SIZE];

    //---------------------constructors/finalizer---------------------------------


    Typdef() {
        this.initialize(0, false, 0, false, 0, false, null);
    }

    Typdef(int ccsidSbc,
           String typdefnam,
           int ccsidDbc,
           int ccsidMbc) {

        this.initialize(ccsidSbc, true, ccsidMbc, true, ccsidDbc, true, typdefnam);
    }

    private void initialize(int ccsidSbc,
                            boolean ccsidSbcSet,
                            int ccsidMbc,
                            boolean ccsidMbcSet,
                            int ccsidDbc,
                            boolean ccsidDbcSet,
                            String typdefnam) {
        ccsidSbc_ = ccsidSbc;
        ccsidSbcSet_ = ccsidSbcSet;
        ccsidSbcEncoding_ = null;
        ccsidMbc_ = ccsidMbc;
        ccsidMbcSet_ = ccsidMbcSet;
        ccsidMbcEncoding_ = null;
        ccsidDbc_ = ccsidDbc;
        ccsidDbcSet_ = ccsidDbcSet;
        ccsidDbcEncoding_ = null;
        setTypdefnam(typdefnam);
    }

    //-------------------------private and package friendly methods---------------

    String getTypdefnam() {
        return typdefnam_;
    }

    void setTypdefnam(String typdefnam) {
        typdefnam_ = typdefnam;
        if (typdefnam_ == null) {
            return;
        }

    }

    int getCcsidSbc() {
        return ccsidSbc_;
    }

    void setCcsidSbc(int ccsid) {
        ccsidSbc_ = ccsid;
        ccsidSbcSet_ = true;
        ccsidSbcEncoding_ = null;
    }

    boolean isCcsidSbcSet() {
        return ccsidSbcSet_;
    }

    // analyze exception handling some more here
    Charset getCcsidSbcEncoding() {
        if (ccsidSbcEncoding_ == null) {
            ccsidSbcEncoding_ = CCSIDConstants.UTF8;
        }
        return ccsidSbcEncoding_;
    }

    int getCcsidDbc() {
        return ccsidDbc_;
    }

    void setCcsidDbc(int ccsid) {
        ccsidDbc_ = ccsid;
        ccsidDbcSet_ = true;
        ccsidDbcEncoding_ = null;
    }

    boolean isCcsidDbcSet() {
        return ccsidDbcSet_;
    }

    // analyze exception handling some more here
    private Charset getCcsidDbcEncoding() {
        if (ccsidDbcEncoding_ == null) {
            ccsidDbcEncoding_ = CCSIDConstants.UTF8;
        }
        return ccsidDbcEncoding_;
    }

    int getCcsidMbc() {
        return ccsidMbc_;
    }

    void setCcsidMbc(int ccsid) {
        ccsidMbc_ = ccsid;
        ccsidMbcSet_ = true;
        ccsidMbcEncoding_ = null;
    }

    boolean isCcsidMbcSet() {
        return ccsidMbcSet_;
    }

    // analyze exception handling some more here
    Charset getCcsidMbcEncoding() {
        if (ccsidMbcEncoding_ == null) {
            ccsidMbcEncoding_ = CCSIDConstants.UTF8;
        }
        return ccsidMbcEncoding_;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // this shouldn't happen since we implement the cloneable interface
        }
    }

    // Populates netCursor descriptors, rename this populateCursorDescriptors()
    void updateColumn(Cursor netCursor,
                      ConnectionMetaData metadata,
                      int columnIndex,
                      int protocolLid,
                      int protocolLength) {
        FdocaSimpleDataArray sda = environmentTables_[protocolLid];

        if ((mddOverride_) && (overrideTable_[protocolLid] != null)) {
            sda = overrideTable_[protocolLid];
        }

        if (sda == null) {
            throw new IllegalStateException("SQLState.NET_INVALID_FDOCA_ID");
//            netAgent_.accumulateChainBreakingReadExceptionAndThrow(
//                new DisconnectException(netAgent_,
//                    new ClientMessageId(SQLState.NET_INVALID_FDOCA_ID)));
        }

        // 2. Set Null indicator based on PROTOCOL Type.
        //    Nullable SQL and PROTOCOL types are all odd numbers and the nullable
        //    type is one number higher than the related non-nullable type.
        netCursor.nullable_[columnIndex] = ((sda.protocolType_ % 2) == 1);

        // 3. Update CCSID
        //    The typdef object should store the java encoding,
        switch (sda.ccsid_) {
        case CCSIDSBC:
            netCursor.charset_[columnIndex] = getCcsidSbcEncoding();
            netCursor.ccsid_[columnIndex] = this.ccsidSbc_;
            break;
        case CCSIDMBC:
            if (isCcsidMbcSet() && (ccsidMbc_ != 0)) {
                netCursor.charset_[columnIndex] = getCcsidMbcEncoding();
                netCursor.ccsid_[columnIndex] = ccsidMbc_;
            } else {
                // if the server didn't return a mixed byte ccsid, set both the
                // encoding and the btc reference to null. see CCSIDDBC comment below.
                netCursor.charset_[columnIndex] = null;
                netCursor.ccsid_[columnIndex] = 0;
            }
            break;
        case CCSIDDBC:
            if (isCcsidDbcSet() && (ccsidDbc_ != 0)) {
                netCursor.charset_[columnIndex] = getCcsidDbcEncoding();
                netCursor.ccsid_[columnIndex] = this.ccsidDbc_;
            } else {
                // if the server didn't return a double byte ccsid, set both the
                // encoding and the btc reference to null.  later an exception will
                // be thrown on the getXXX method.  calling the getCcsidDbcEncoding method
                // will throw the exception here and this is not desirable.
                netCursor.charset_[columnIndex] = null;
                netCursor.ccsid_[columnIndex] = 0;
            }
            break;

        default:   // This default case is used for mdd override ccsids.
            // In this case the sda.ccsid_ is the actual native ccsid,
            // otherwise the sda.ccsid_ is a placeholder:
            //  CCSIDMBC, CCSIDDDBC, CCSIDSBC to indicate that
            // the actual ccsid is the connection's ccsid (in protocol lingo the connection's typdef ccsid).
            netCursor.charset_[columnIndex] = metadata.isZos() ? CCSIDConstants.EBCDIC : CCSIDConstants.UTF8;
            netCursor.ccsid_[columnIndex] = sda.ccsid_;
            break;
        }

        // 5. Set the length fdoca length.
        //    For some types this may be a precision and a scale.
        //    this includes FIXED DECIMAL (PROTOCOL type 0x0E, 0X0F),
        // retain the high order placeholder for lobs)
        // this includes LARGE OBJECT BYTES (PROTOCOL type 0xC8, 0xC9)
        //               LARGE OBJECT CHAR SBCS (PROTOCOL type 0xCA, 0xCB)
        //               LARGE OBJECT CHAR DBCS (PROTOCOL type 0xCC, 0xCD)
        //               LARGE OBJECT CHAR MIXED (PROTOCOL type 0xCE, 0xCF)
        // also set the hasLobs_ flag to true
        if ((sda.protocolType_ >= 0xC8) && (sda.protocolType_ <= 0xCF)) {
            // retain placeholder information
            // right now just set the length...
            // probably need to accomodate for the high order placeholder bit
            netCursor.fdocaLength_[columnIndex] = protocolLength;
            netCursor.hasLobs_ = true;
        } else {
            netCursor.fdocaLength_[columnIndex] = protocolLength;
        }

        // 6. Set jdbc type.
        netCursor.jdbcTypes_[columnIndex] = protocolToJdbcTypes_[sda.protocolType_];
        if (netCursor.jdbcTypes_[columnIndex] == 0x00) {
          // TODO: Set up logging framework for DRDA codebase
//          System.out.println("WARN: Found unknown protocol type: " + sda.protocolType_);
        }

        // 7. Get the number of bytes to read for variable length data.
        netCursor.typeToUseForComputingDataLength_[columnIndex] = sda.typeToUseForComputingDataLength_;

        // 8. Update the maximumRowSize
        // Count : column null indicator +
        //         column length +
        //         posibly a 2-byte length for varchar columns
        switch (netCursor.typeToUseForComputingDataLength_[columnIndex]) {
        case DECIMALLENGTH:
            netCursor.maximumRowSize_ += 1 + 16;
            break;
        case LOBLENGTH:
            netCursor.maximumRowSize_ += 1 + (netCursor.fdocaLength_[columnIndex] & 0x7fff);
            break;
        default:
            netCursor.maximumRowSize_ += 1 + netCursor.fdocaLength_[columnIndex] + 2; // 2 is the length indicator for varchar
            break;
        }

        // 9. Get the character size.
        if (sda.characterSize_ == 2) {
            netCursor.isGraphic_[columnIndex] = true;
        }

    }

    // Called before the code parses a descriptor
    void clearMddOverrides() {
        if (mddOverride_ == false) {
            return;
        }

        mddOverride_ = false;
        for (int i = 0; i < OVERRIDE_TABLE_SIZE; i++) {
            overrideTable_[i] = null;
        }
    }

    // Called after the code parses a descriptor
    void setMddOverride(int protocolType,
                        int fdocaTripletLid,
                        int fdocaFieldType,
                        int ccsid,
                        int characterSize,
                        int mode,
                        int length) {
        mddOverride_ = true;


        if (overrideTable_[fdocaTripletLid] == null) {
            overrideTable_[fdocaTripletLid] = new FdocaSimpleDataArray
                    (protocolType,
                            ccsid,
                            characterSize,
                            mapFdocaTypeToTypeToUseForComputingDataLength(fdocaFieldType));
        } else {
            overrideTable_[fdocaTripletLid].update(protocolType,
                    ccsid,
                    characterSize,
                    mapFdocaTypeToTypeToUseForComputingDataLength(fdocaFieldType));
        }
    }

    private int mapFdocaTypeToTypeToUseForComputingDataLength(int fdocaFieldType) {
        switch (fdocaFieldType & 0x7f) { // &0x7f masks out the null indicator
        case FdocaConstants.FDOCA_TYPE_VARCHAR:
        case FdocaConstants.FDOCA_TYPE_VARBYTES:
            return Typdef.TWOBYTELENGTH;
        case FdocaConstants.FDOCA_TYPE_DECIMAL:
        case FdocaConstants.FDOCA_TYPE_NUMERIC_CHAR:
            return Typdef.DECIMALLENGTH;
        case FdocaConstants.FDOCA_TYPE_LOBBYTES:
        case FdocaConstants.FDOCA_TYPE_LOBCHAR:
            return Typdef.LOBLENGTH;
        case FdocaConstants.FDOCA_TYPE_PSCLBYTE:
        case FdocaConstants.FDOCA_TYPE_PSCLCHAR:
            return Typdef.ONEBYTELENGTH;
        default:
            return 0;
        }
    }

}
