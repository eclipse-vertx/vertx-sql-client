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
import java.nio.charset.StandardCharsets;

import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;

public class CCSIDConstants {

	  private static final Logger LOG = LoggerFactory.getLogger(CCSIDConstants.class);

    public static final int CCSID_EBCDIC = 500; // 0x01F4
    public static final int CCSID_UTF8 = 1208; // 0x04B8
    public static final int TARGET_UNICODE_MGR = CCSID_UTF8;

    public static final Charset EBCDIC = Charset.forName("CP1047");
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
    public static final Charset UTF16BE = StandardCharsets.UTF_16BE;

    private static final Charset CP037 = Charset.isSupported("CP037") ? Charset.forName("CP037") : null;
    private static final Charset WINDOWS_1252 = Charset.isSupported("windows-1252") ? Charset.forName("windows-1252") : null;
    private static final Charset GBK = Charset.isSupported("GBK") ? Charset.forName("GBK") : null;
    private static final Charset CP935 = Charset.isSupported("Cp935") ? Charset.forName("Cp935") : null;

    private CCSIDConstants() {}

    public static Charset getCharsetForCCSID(int ccsid) {
        switch (ccsid) {
            case CCSID_UTF8:
                return UTF8;
            case 1386:
            case 5488:
            case 1114:
              return GBK != null ? GBK : UTF8;
            case 935:
              return CP935 != null ? CP935 : UTF8;
            case 819:
                return ISO_8859_1;
            case 1200:
                return UTF16BE;
            case 37:
              return CP037 != null ? CP037 : UTF8;
            case CCSID_EBCDIC:
            case 1047:
                return EBCDIC;
            case 1252:
              return WINDOWS_1252 != null ? WINDOWS_1252 : UTF8;
            case 0:
                return UTF8;
            default:
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Unmapped DB2 CCSID code: " + ccsid + ". Falling back to UTF-8.");
                }
                return UTF8;
        }
    }
}
