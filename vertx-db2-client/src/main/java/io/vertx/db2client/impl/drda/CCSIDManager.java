package io.vertx.db2client.impl.drda;

import java.nio.charset.Charset;

public class CCSIDManager {
    
    public static final int CCSID_EBCDIC = 500; // 0x01F4
    public static final int CCSID_UTF8 = 1208; // 0x04B8
    public static final int TARGET_UNICODE_MGR = CCSID_UTF8;
    
    public static final Charset EBCDIC = Charset.forName("CP1047");
    public static final Charset UTF8 = Charset.forName("UTF-8");
    
    private Charset currentCCSID = EBCDIC;
    
    public Charset getCCSID() {
        return currentCCSID;
    }
    
    public void setCCSID(Charset ccsid) {
        if (!ccsid.equals(UTF8) && !ccsid.equals(EBCDIC))
            throw new IllegalArgumentException("Unsupported CCSID: " + ccsid);
        this.currentCCSID = ccsid;
    }
    
    public int getCCSIDNumber() {
        if (currentCCSID.equals(UTF8))
            return CCSID_UTF8;
        else
            return CCSID_EBCDIC;
    }

}
