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

class FdocaSimpleDataArray {
    //---------------------navigational members-----------------------------------

    //-----------------------------state------------------------------------------

    // store the protocol type. this is needed to know
    // which protocol type the mdd override is for.
    int protocolType_;

    // the ccsid identifies the encoding of the character data.  converting the
    // ccsid into binary form generates the four byte representation.  The
    // FD:OCA rules state that if the high order 16 bits of the CCSID field
    // are zero, then the low order 16 bits are to be interpreted as a CCSID
    int ccsid_;

    // indicates the number of bytes each character takes in storage.
    // 1 is used for character, date, time, timestamp, and numeric character fields.
    // it must be 0 for all other types.
    int characterSize_;

    // this is a group of types which indicates how the data length are computed.
    int typeToUseForComputingDataLength_;

    //---------------------constructors/finalizer---------------------------------

    FdocaSimpleDataArray(int protocolType,
                         int ccsid,
                         int characterSize,
                         int typeToUseForComputingDataLength) {
        protocolType_ = protocolType;
        ccsid_ = ccsid;
        characterSize_ = characterSize;
        typeToUseForComputingDataLength_ = typeToUseForComputingDataLength;
    }

    public void update(int protocolType,
                       int ccsid,
                       int characterSize,
                       int typeToUseForComputingDataLength) {
        protocolType_ = protocolType;
        ccsid_ = ccsid;
        characterSize_ = characterSize;
        typeToUseForComputingDataLength_ = typeToUseForComputingDataLength;
    }

}