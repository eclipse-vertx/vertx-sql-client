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

/**
 * Constants for FD:OCA (Formatted Data: Object Content Architecture) 
 */
class FdocaConstants {

    // PROTOCOL FD:OCA constants used (subset)
    static final int FDOCA_TYPE_VARBYTES = 0x02;
    static final int FDOCA_TYPE_PSCLBYTE = 0x07;
    static final int FDOCA_TYPE_VARCHAR = 0x11;
    static final int FDOCA_TYPE_PSCLCHAR = 0x19;
    static final int FDOCA_TYPE_DECIMAL = 0x30;
    static final int FDOCA_TYPE_NUMERIC_CHAR = 0x32;
    static final int FDOCA_TYPE_LOBBYTES = 0x50;
    static final int FDOCA_TYPE_LOBCHAR = 0x51;

    static final int CPT_TRIPLET_TYPE = 0x7F;      // CPT triplet type
    static final int MDD_TRIPLET_TYPE = 0x78;      // MDD triplet type
    static final int NGDA_TRIPLET_TYPE = 0x76;     // N-GDA triplet type
    static final int RLO_TRIPLET_TYPE = 0x71;      // RLO triplet type
    static final int SDA_TRIPLET_TYPE = 0x70;      // SDA triplet type

    static final int SQLDTARD_LID = 0xF0;
    static final int SQLCADTA_LID = 0xE0;
    static final int SQLDTAGRP_LID = 0xD0;         // SQLDTAGRP LID
    static final int NULL_LID = 0x00;

    static final int INDICATOR_NULLABLE = 0x00;
    static final int NULL_DATA = 0xFF;

    static final int MAX_VARS_IN_NGDA = 84;        // Number of SQLVARs in full SQLDTAGRP
    // N-GDA or CPT
    static final int MDD_TRIPLET_SIZE = 7;         // Size of MDD triplet
    static final int SQLDTARD_RLO_SIZE = 6;        // size of SQLDTARD minus MDD
    static final int SQLCADTA_RLO_SIZE = 9;        // size of SQLCDTA minus MDD
    static final int SDA_TRIPLET_SIZE = 12;        // Size of SDA triplet

    static final byte[] MDD_SQLDTAGRP_TOSEND = {
        (byte) 0x07, (byte) 0x78, (byte) 0x00, (byte) 0x05,
        (byte) 0x02, (byte) 0x01, (byte) 0xD0
    };

    static final byte[] MDD_SQLDTA_TOSEND = {
        (byte) 0x07, (byte) 0x78, (byte) 0x00, (byte) 0x05,
        (byte) 0x03, (byte) 0x01, (byte) 0xE4
    };

    // Hard-coded SQLDTA RLO
    static final byte[] SQLDTA_RLO_TOSEND = {
        (byte) 0x06, (byte) 0x71, (byte) 0xE4,
        (byte) 0xD0, (byte) 0x00, (byte) 0x01
    };

    // Hard-coded SQLCADTA RLO
    static final byte[] SQLCADTA_RLO_TOSEND = {
        (byte) 0x09, (byte) 0x71, (byte) 0xE0,
        (byte) 0x54, (byte) 0x00, (byte) 0x01,
        (byte) 0xD0, (byte) 0x00, (byte) 0x01
    };

    // Hard-coded SQLDTARD RLO
    static final byte[] SQLDTARD_RLO_TOSEND = {
        (byte) 0X06, (byte) 0X71, (byte) 0xF0,
        (byte) 0xE0, (byte) 0x00, (byte) 0x00
    };

    // following code added for parseSQLDTARD prototype
    static final int SQLDTARD_TRIPLET_TYPE_START = 0;
    static final int SQLDTARD_TRIPLET_TYPE_END = 1;
    static final int SQLDTARD_TRIPLET_TYPE_MDD = 2;
    static final int SQLDTARD_TRIPLET_TYPE_SDA = 3;
    static final int SQLDTARD_TRIPLET_TYPE_RLO = 4;
    static final int SQLDTARD_TRIPLET_TYPE_GDA = 5;
    static final int SQLDTARD_TRIPLET_TYPE_CPT = 6;

    static final boolean[][] SQLDTARD_TRIPLET_TYPES = {
        //   /*START*/, /*END*/, /*MDD*/, /*SDA*/, /*RLO*/, /*GDA*/, /*CPT*/    // next ->
/*START*/ {false, false, true, false, false, true, false},
/* END */ {false, false, false, false, false, false, false},
/* MDD */ {false, false, false, true, true, true, false}, //           |
/* SDA */ {false, false, true, false, false, false, false}, // previous  |
/* RLO */ {false, true, true, false, true, false, false}, //          \ /
/* GDA */ {false, false, true, false, true, false, true},
/* CPT */ {false, false, true, false, true, false, true}};

    static final int SQLDTARD_TRIPLET_ID_START = 0;
    static final int SQLDTARD_TRIPLET_ID_END = 1;
    static final int SQLDTARD_TRIPLET_ID_SDA = 2;
    static final int SQLDTARD_TRIPLET_ID_0 = 3;
    static final int SQLDTARD_TRIPLET_ID_D0 = 4;
    static final int SQLDTARD_TRIPLET_ID_E0 = 5;
    static final int SQLDTARD_TRIPLET_ID_F0 = 6;

    static final boolean[][] SQLDTARD_TRIPLET_IDS = {
        //   /*START*/, /*END*/, /*SDA*/, /*0*/, /*D0*/, /*E0*/, /*F4*/ // next ->
/*START*/  {false, false, false, true, true, false, false},
/* END */  {false, false, false, false, false, false, false},
/* SDA */  {false, false, false, true, false, false, false},
/* 0   */  {false, false, true, true, true, true, true},
/* D0 */   {false, false, false, true, false, true, false},
/* E0 */   {false, false, false, true, false, false, true},
/* F4 */   {false, true, false, false, false, false, false}};

    static final int RLO_GROUP_LID = 0;
    static final int RLO_ELEMENT_TAKEN = 1;
    static final int RLO_REP_FACTOR = 2;

    static final int[][] RLO_SQLCADTA = {// GROUP LID , ELEM TAKEN,  REP_FACTOR
        {0x54, 0, 1},
        {0xD0, 0, 1}};

    static final int[][] RLO_SQLDTARD = {// GROUP LID , ELEM TAKEN,  REP_FACTOR
        {0xE0, 0, 0}};

}