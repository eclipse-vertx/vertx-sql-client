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

public class CCSIDConstants {
    
    public static final int CCSID_EBCDIC = 500; // 0x01F4
    public static final int CCSID_UTF8 = 1208; // 0x04B8
    public static final int TARGET_UNICODE_MGR = CCSID_UTF8;
    
    public static final Charset EBCDIC = Charset.forName("CP1047");
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    
    static Charset currentCCSID = EBCDIC;
    
    public static Charset getCCSID() {
        return currentCCSID;
    }
    
    private CCSIDConstants() {}
    
}
