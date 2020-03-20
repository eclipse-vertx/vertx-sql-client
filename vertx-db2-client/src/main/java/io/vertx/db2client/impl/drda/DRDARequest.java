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

import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Hashtable;

import io.netty.buffer.ByteBuf;

public abstract class DRDARequest {
    
    // Product Identifiers as defined by the Open Group.
    public  static  final   String  DB2_DRDA_SERVER_ID = "CSS";
    public  static  final   String  DB2_DRDA_CLIENT_ID = "DNC";
    
    final ByteBuf buffer;
    
    Deque<Integer> markStack = new ArrayDeque<>(4);

    //  This Object tracks the location of the current
    //  Dss header length bytes.  This is done so
    //  the length bytes can be automatically
    //  updated as information is added to this stream.
    private int dssLengthLocation_ = 0;

    // tracks the request correlation ID to use for commands and command objects.
    // this is automatically updated as commands are built and sent to the server.
    private int correlationID_ = 0;

    private boolean simpleDssFinalize = false;
    
    public DRDARequest(ByteBuf buffer) {
        this.buffer = buffer;
    }
    
    String getHostname() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "localhost";
        }
    }
    
    // creates an request dss in the buffer to contain a ddm command
    // object.  calling this method means any previous dss objects in
    // the buffer are complete and their length and chaining bytes can
    // be updated appropriately.
    protected final void createCommand() {
        buildDss(false, false, false, DssConstants.GDSFMT_RQSDSS, ++correlationID_, false);
    }
    
    private final void buildDss(boolean dssHasSameCorrelator, boolean chainedToNextStructure,
            boolean nextHasSameCorrelator, int dssType, int corrId, boolean simpleFinalizeBuildingNextDss) {
        if (doesRequestContainData()) {
            if (simpleDssFinalize) {
                completeCommand();
            } else {
                finalizePreviousChainedDss(dssHasSameCorrelator);
            }
        }

        // RQSDSS header is 6 bytes long: (ll)(Cf)(rc)
        buffer.ensureWritable(6);

        // Save the position of the length bytes, so they can be updated with a
        // different value at a later time.
        dssLengthLocation_ = buffer.writerIndex();
        // Dummy values for the DSS length (token ll above).
        // The correct length will be inserted when the DSS is finalized.
        buffer.writeShort((short) 0xFFFF);

        // Insert the mandatory 0xD0 (token C).
        buffer.writeByte((byte) 0xD0);

        // Insert the dssType (token f), which also tells if the DSS is chained
        // or not. See DSSFMT in the DRDA specification for details.
        if (chainedToNextStructure) {
            dssType |= DssConstants.GDSCHAIN;
            if (nextHasSameCorrelator) {
                dssType |= DssConstants.GDSCHAIN_SAME_ID;
            }
        }
        buffer.writeByte((byte) dssType);

        // Write the request correlation id (two bytes, token rc).
        // use method that writes a short
        buffer.writeShort((short) corrId);

        simpleDssFinalize = simpleFinalizeBuildingNextDss;
    }
    
    final void createEncryptedCommandData() {
        // TODO: @AGG encryption here
//        if (netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRIDDTA ||
//                netAgent_.netConnection_.getSecurityMechanism() == NetConfiguration.SECMEC_EUSRPWDDTA) {
//            buildDss(true, false, false, DssConstants.GDSFMT_ENCOBJDSS, correlationID_, false);
//        } else {
            buildDss(true,
                    false,
                    false,
                    DssConstants.GDSFMT_OBJDSS,
                    correlationID_,
                    false);
//        }
    }
    
    // used to finialize a dss which is already in the buffer
    // before another dss is built.  this includes updating length
    // bytes and chaining bits.
    private final void finalizePreviousChainedDss(boolean dssHasSameCorrelator) {

        completeCommand();
        int pos = dssLengthLocation_ + 3;
        byte value = buffer.getByte(pos);
        value |= 0x40;
        if (dssHasSameCorrelator) // for blobs
        {
            value |= 0x10;
        }
        buffer.setByte(pos, value);
    }
    
    /**
     * Signal the completion of a DSS Layer A object.
     * <p>
     * The length of the DSS object will be calculated based on the difference
     * between the start of the DSS, saved in the variable
     * {@link #dssLengthLocation_}, and the current offset into the buffer which
     * marks the end of the data.
     * <p>
     * In the event the length requires the use of continuation DSS headers,
     * one for each 32k chunk of data, the data will be shifted and the
     * continuation headers will be inserted with the correct values as needed.
     * Note: In the future, we may try to optimize this approach
     * in an attempt to avoid these shifts.
     */
    public final void completeCommand() {
        // calculate the total size of the dss and the number of bytes which would
        // require continuation dss headers.  The total length already includes the
        // the 6 byte dss header located at the beginning of the dss.  It does not
        // include the length of any continuation headers.
        int totalSize = buffer.writerIndex() - dssLengthLocation_;
        int bytesRequiringContDssHeader = totalSize - 32767;

        // determine if continuation headers are needed
        if (bytesRequiringContDssHeader > 0) {

            // the continuation headers are needed, so calculate how many.
            // after the first 32767 worth of data, a continuation header is
            // needed for every 32765 bytes (32765 bytes of data + 2 bytes of
            // continuation header = 32767 Dss Max Size).
            int contDssHeaderCount = bytesRequiringContDssHeader / 32765;
            if (bytesRequiringContDssHeader % 32765 != 0) {
                contDssHeaderCount++;
            }

            // right now the code will shift to the right.  In the future we may want
            // to try something fancier to help reduce the copying (maybe keep
            // space in the beginning of the buffer??).
            // the offset points to the next available offset in the buffer to place
            // a piece of data, so the last dataByte is at offset -1.
            // various bytes will need to be shifted by different amounts
            // depending on how many dss headers to insert so the amount to shift
            // will be calculated and adjusted as needed.  ensure there is enough room
            // for all the conutinuation headers and adjust the offset to point to the
            // new end of the data.
            int dataByte = buffer.writerIndex() - 1;
            int shiftOffset = contDssHeaderCount * 2;
            buffer.ensureWritable(shiftOffset);
            buffer.writerIndex(buffer.writerIndex() + shiftOffset);

            // mark passOne to help with calculating the length of the final (first or
            // rightmost) continuation header.
            boolean passOne = true;
            do {
                // calculate chunk of data to shift
                int dataToShift = bytesRequiringContDssHeader % 32765;
                if (dataToShift == 0) {
                    dataToShift = 32765;
                }

                // perform the shift
                dataByte -= dataToShift;
                byte[] array = buffer.array();
                System.arraycopy(array, dataByte + 1,
                        array, dataByte + shiftOffset + 1, dataToShift);

                // calculate the value the value of the 2 byte continuation dss header which
                // includes the length of itself.  On the first pass, if the length is 32767
                // we do not want to set the continuation dss header flag.
                int twoByteContDssHeader = dataToShift + 2;
                if (passOne) {
                    passOne = false;
                } else {
                    if (twoByteContDssHeader == 32767) {
                        twoByteContDssHeader = 0xFFFF;
                    }
                }

                // insert the header's length bytes
                buffer.setShort(dataByte + shiftOffset - 1,
                                (short) twoByteContDssHeader);

                // adjust the bytesRequiringContDssHeader and the amount to shift for
                // data in upstream headers.
                bytesRequiringContDssHeader -= dataToShift;
                shiftOffset -= 2;

                // shift and insert another header for more data.
            } while (bytesRequiringContDssHeader > 0);

            // set the continuation dss header flag on for the first header
            totalSize = 0xFFFF;

        }

        // insert the length bytes in the 6 byte dss header.
        buffer.setShort(dssLengthLocation_, (short) totalSize);
    }
    
    // Called to update the last ddm length bytes marked (lengths are updated
    // in the reverse order that they are marked).  It is up to the caller
    // to make sure length bytes were marked before calling this method.
    // If the length requires ddm extended length bytes, the data will be
    // shifted as needed and the extended length bytes will be automatically
    // inserted.
    protected final void updateLengthBytes() {
        // remove the top length location offset from the mark stack\
        // calculate the length based on the marked location and end of data.
        int lengthLocation = markStack.pop();
        int length = buffer.writerIndex() - lengthLocation;

        // determine if any extended length bytes are needed.  the value returned
        // from calculateExtendedLengthByteCount is the number of extended length
        // bytes required. 0 indicates no exteneded length.
        int extendedLengthByteCount = calculateExtendedLengthByteCount(length);
        if (extendedLengthByteCount != 0) {

            // ensure there is enough room in the buffer for the extended length bytes.
            ensureLength(extendedLengthByteCount);

            // calculate the length to be placed in the extended length bytes.
            // this length does not include the 4 byte llcp.
            int extendedLength = length - 4;

            // shift the data to the right by the number of extended length bytes needed.
            int extendedLengthLocation = lengthLocation + 4;
            byte[] array = buffer.array();
            System.arraycopy(array,
                    extendedLengthLocation,
                    array,
                    extendedLengthLocation + extendedLengthByteCount,
                    extendedLength);

            // write the extended length
            int shiftSize = (extendedLengthByteCount - 1) * 8;
            for (int i = 0; i < extendedLengthByteCount; i++) {
                buffer.setByte(extendedLengthLocation++,
                           (byte) (extendedLength >>> shiftSize));
                shiftSize -= 8;
            }
            // adjust the offset to account for the shift and insert
            buffer.writerIndex(buffer.writerIndex() + extendedLengthByteCount);

            // the two byte length field before the codepoint contains the length
            // of itself, the length of the codepoint, and the number of bytes used
            // to hold the extended length.  the 2 byte length field also has the first
            // bit on to indicate extended length bytes were used.
            length = extendedLengthByteCount + 4;
            length |= 0x8000;
        }

        // write the 2 byte length field (2 bytes before codepoint).
        buffer.setShort(lengthLocation, (short) length);
    }
    
    // this method writes a 4 byte length/codepoint pair plus the bytes contained
    // in array buff to the buffer.
    // the 2 length bytes in the llcp will contain the length of the data plus
    // the length of the llcp.  This method does not handle scenarios which
    // require extended length bytes.
    final void writeScalarBytes(int codePoint, byte[] buff) {
        writeScalarBytes(codePoint, buff, 0, buff.length);
    }

    // this method inserts a 4 byte length/codepoint pair plus length number of bytes
    // from array buff starting at offset start.
    // Note: no checking will be done on the values of start and length with respect
    // the actual length of the byte array.  The caller must provide the correct
    // values so an array index out of bounds exception does not occur.
    // the length will contain the length of the data plus the length of the llcp.
    // This method does not handle scenarios which require extended length bytes.
    final void writeScalarBytes(int codePoint, byte[] buff, int start, int length) {
        writeLengthCodePoint(length + 4, codePoint);
        ensureLength(length);
        buffer.writeBytes(buff, start, length);
    }
    
    // this method inserts binary data into the buffer and pads the
    // data with the padByte if the data length is less than the paddedLength.
    // Not: this method is not to be used for truncation and buff.length
    // must be <= paddedLength.
    final void writeScalarPaddedBytes(byte[] buff, int paddedLength, byte padByte) {
        //writeBytes(buff);
        buffer.writeBytes(buff);
        padBytes(padByte, paddedLength - buff.length);
    }
    
    final void writeScalarString(int codePoint, String string) {
        writeScalarString(codePoint, string, 0,Integer.MAX_VALUE,null);
    } 
    
    /**
     *  insert a 4 byte length/codepoint pair plus ddm character data into
     * the buffer.  This method assumes that the String argument can be
     * converted by the ccsid manager.  This should be fine because usually
     * there are restrictions on the characters which can be used for ddm
     * character data. 
     * The two byte length field will contain the length of the character data
     * and the length of the 4 byte llcp.  This method does not handle
     * scenarios which require extended length bytes.
     * 
     * @param codePoint  codepoint to write 
     * @param string     value
     * @param byteMinLength minimum length. String will be padded with spaces 
     * if value is too short. Assumes space character is one byte.
     * @param byteLengthLimit  Limit to string length. SQLException will be 
     * thrown if we exceed this limit.
     * @param sqlState  SQLState to throw with string as param if byteLengthLimit
     * is exceeded.
     * @throws SQLException if string exceeds byteLengthLimit
     */
    final void writeScalarString(int codePoint, String string, int byteMinLength,
            int byteLengthLimit, String sqlState) {
        // We don't know the length of the string yet, so set it to 0 for now.
        // Will be updated later.
        int lengthPos = buffer.writerIndex();
        writeLengthCodePoint(0, codePoint);

        int stringByteLength = encodeString(string);
        if (stringByteLength > byteLengthLimit) {
            throw new IllegalArgumentException("SQLState=" + sqlState + " " + string);
        }

        // pad if we don't reach the byteMinLength limit
        if (stringByteLength < byteMinLength) {
            padBytes(CCSIDConstants.getCCSID().encode(" ").get(), byteMinLength - stringByteLength);
            stringByteLength = byteMinLength;
        }

        // Update the length field. The length includes two bytes for the
        // length field itself and two bytes for the codepoint.
        buffer.setShort(lengthPos, (short) (stringByteLength + 4));
    }
    
    private void writeLidAndLengths(int[][] lidAndLengthOverrides, int count, int offset) {
        ensureLength(count * 3);
        for (int i = 0; i < count; i++, offset++) {
            buffer.writeByte((byte) lidAndLengthOverrides[offset][0]);
            buffer.writeShort((short) lidAndLengthOverrides[offset][1]);
        }
    }
    
    // if mdd overrides are not required, lids and lengths are copied straight into the
    // buffer.
    // otherwise, lookup the protocolType in the map.  if an entry exists, substitute the
    // protocolType with the corresponding override lid.
    final void writeLidAndLengths(int[][] lidAndLengthOverrides,
                                  int count,
                                  int offset,
                                  boolean mddRequired,
                                  Hashtable map) {
        if (!mddRequired) {
            writeLidAndLengths(lidAndLengthOverrides, count, offset);
        }
        // if mdd overrides are required, lookup the protocolType in the map, and substitute
        // the protocolType with the override lid.
        else {
            ensureLength(count * 3);
            int protocolType, overrideLid;
            Object entry;
            for (int i = 0; i < count; i++, offset++) {
                protocolType = lidAndLengthOverrides[offset][0];
                // lookup the protocolType in the protocolType->overrideLid map
                // if an entry exists, replace the protocolType with the overrideLid
                entry = map.get(protocolType);
                overrideLid = (entry == null) ? protocolType : ((Integer) entry).intValue();
                buffer.writeByte((byte) overrideLid);
                buffer.writeShort((short) lidAndLengthOverrides[offset][1]);
            }
        }
    }
    
    // helper method to calculate the minimum number of extended length bytes needed
    // for a ddm.  a return value of 0 indicates no extended length needed.
    private final int calculateExtendedLengthByteCount(long ddmSize) //throws SQLException
    {
        // according to Jim and some tests perfomred on Lob data,
        // the extended length bytes are signed.  Assume that
        // if this is the case for Lobs, it is the case for
        // all extended length scenarios.
        if (ddmSize <= 0x7FFF) {
            return 0;
        } else if (ddmSize <= 0x7FFFFFFFL) {
            return 4;
        } else if (ddmSize <= 0x7FFFFFFFFFFFL) {
            return 6;
        } else {
            return 8;
        }
    }
    
    /**
     * Encode a string and put it into the buffer. A larger buffer will be
     * allocated if the current buffer is too small to hold the entire string.
     *
     * @param string the string to encode
     * @return the number of bytes in the encoded representation of the string
     */
    private int encodeString(String string) {
        int startPos = buffer.writerIndex();
        buffer.writeCharSequence(string, CCSIDConstants.getCCSID());
        return buffer.writerIndex() - startPos;
    }
    
    // insert a 4 byte length/codepoint pair into the buffer.
    // total of 4 bytes inserted in buffer.
    // Note: the length value inserted in the buffer is the same as the value
    // passed in as an argument (this value is NOT incremented by 4 before being
    // inserted).
    final void writeLengthCodePoint(int length, int codePoint) {
        buffer.writeShort((short) length);
        buffer.writeShort((short) codePoint);
    }
    
    // insert 3 unsigned bytes into the buffer.  this was
    // moved up from NetStatementRequest for performance
    final void buildTripletHeader(int tripletLength,
                                  int tripletType,
                                  int tripletId) {
        ensureLength(3);
        buffer.writeByte((byte) tripletLength);
        buffer.writeByte((byte) tripletType);
        buffer.writeByte((byte) tripletId);
    }
    
    /**
     * Writes a long into the buffer, using six bytes.
     */
    final void writeLong6Bytes(long v) {
        ensureLength(6);
        buffer.writeShort((short) (v >> 32));
        buffer.writeInt((int) v);
    }
    
    /**
     * Writes a LocalDate to the buffer in the standard SQL format
     * of <code>YYYY-MM-DD</code> using UTF8 encoding
     */
    final void writeDate(LocalDate date) {
    	ensureLength(10);
    	String d = String.format("%04d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    	buffer.writeCharSequence(d, StandardCharsets.UTF_8);
    }
    
    final void writeDate(java.sql.Date date) {
    	writeDate(date.toLocalDate());
    }
    
    /**
     * Writes a LocalTime to the buffer in the standard SQL format
     * of <code>hh:mm:ss</code> using UTF8 encoding
     */
    final void writeTime(LocalTime time) {
    	ensureLength(8);
    	String d = String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
    	buffer.writeCharSequence(d, StandardCharsets.UTF_8);
    }
    
    final void writeTime(java.sql.Time time) {
    	writeTime(time.toLocalTime());
    }
    
    // insert a java.math.BigDecimal into the buffer.
    final void writeBigDecimal(BigDecimal v,
                               int declaredPrecision,
                               int declaredScale) {
        ensureLength(16);
        // @AGG simply write bytes normally here instead of manually incrementing index after?
        int length = Decimal.bigDecimalToPackedDecimalBytes(
                buffer, buffer.writerIndex(),
                v, declaredPrecision, declaredScale);
        buffer.writerIndex(buffer.writerIndex() + length);
    }
    
    // follows the TYPDEF rules (note: don't think ddm char data is ever length
    // delimited)
    // Will write a varchar mixed or single
    //  this was writeLDString
    final void writeSingleorMixedCcsidLDString(String s, Charset encoding) {
        byte[] b = s.getBytes(encoding);
        if (b.length > 0x7FFF) {
            throw new IllegalArgumentException("SQLState.LANG_STRING_TOO_LONG 32767");
        }
        writeLDBytes(b);
    }
    
    final void writeLDBytes(byte[] bytes) {
        buffer.writeShort(bytes.length);
        buffer.writeBytes(bytes);
//        writeLDBytesX(bytes.length, bytes);
    }
    
    // insert a 4 byte length/codepoint pair and a 1 byte unsigned value into the buffer.
    // total of 5 bytes inserted in buffer.
    final void writeScalar1Byte(int codePoint, int value) {
        ensureLength(5);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x05);
        buffer.writeShort((short) codePoint);
        buffer.writeByte((byte) value);
    }
    
    final void writeScalar2Bytes(int codePoint, int value) {
        ensureLength(6);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x06);
        buffer.writeShort((short) codePoint);
        buffer.writeShort((short) value);
    }
    
    // insert a 4 byte length/codepoint pair and a 4 byte unsigned value into the
    // buffer.  total of 8 bytes inserted in the buffer.
    final void writeScalar4Bytes(int codePoint, long value) {
        ensureLength(8);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x08);
        buffer.writeShort((short) codePoint);
        buffer.writeInt((int) value);
    }

    // insert a 4 byte length/codepoint pair and a 8 byte unsigned value into the
    // buffer.  total of 12 bytes inserted in the buffer.
    final void writeScalar8Bytes(int codePoint, long value) {
        ensureLength(12);
        buffer.writeByte((byte) 0x00);
        buffer.writeByte((byte) 0x0C);
        buffer.writeShort((short) codePoint);
        buffer.writeLong(value);
    }
    
    // mark the location of a two byte ddm length field in the buffer,
    // skip the length bytes for later update, and insert a ddm codepoint
    // into the buffer.  The value of the codepoint is not checked.
    // this length will be automatically updated when construction of
    // the ddm object is complete (see updateLengthBytes method).
    // Note: this mechanism handles extended length ddms.
    protected final void markLengthBytes(int codePoint) {
        ensureLength(4);

        // save the location of length bytes in the mark stack.
        markStack.push(buffer.writerIndex());

        // skip the length bytes and insert the codepoint
        buffer.writerIndex(buffer.writerIndex() + 2);
        buffer.writeShort((short) codePoint);
    }
    
    // insert the padByte into the buffer by length number of times.
    private final void padBytes(byte padByte, int length) {
        ensureLength(length);
        for (int i = 0; i < length; i++) {
            buffer.writeByte(padByte);
        }
    }
    
    void ensureLength(int length) {
        buffer.ensureWritable(length);
    }
    
    // method to determine if any data is in the request.
    // this indicates there is a dss object already in the buffer.
    private final boolean doesRequestContainData() {
        return buffer.writerIndex() != 0;
    }

}
