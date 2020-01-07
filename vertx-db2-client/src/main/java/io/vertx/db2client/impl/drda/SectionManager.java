/*

   Derby - Class org.apache.derby.client.am.SectionManager

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package io.vertx.db2client.impl.drda;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Stack;

public class SectionManager {
    
    public static final SectionManager INSTANCE = new SectionManager();

    // The following stack of available sections is
    // for pooling and recycling previously used sections.
    // For performance, the section objects themselves are pooled,
    // rather than just keeping track of free section numbers;
    // this way, we don't have to new-up a section if one is available in the pool.
    private final Stack<Section> freeSectionsNonHold_ = new Stack<Section>();
    private final Stack<Section> freeSectionsHold_ = new Stack<Section>();

    private int nextAvailableSectionNumber_ = 1;

    // store package consistency token information and initialized in
    // setPKGNAMCBytes
    // holdPKGNAMCBytes stores PKGNAMCBytes when holdability is hold
    // noHoldPKGNAMCBytes stores PKGNAMCBytes when holdability is no hold
    byte[] holdPKGNAMCBytes = null;
    byte[] noHoldPKGNAMCBytes = null;


    private final static String packageNameWithHold__ = "SYSSH200";//"SYSLH000";
    private final static String packageNameWithNoHold__ = "SYSLN000";

    private final static String cursorNamePrefixWithHold__ = "SQL_CURSH000C";//"SQL_CURLH000C";
    private final static String cursorNamePrefixWithNoHold__ = "SQL_CURLN000C";
    
    private SectionManager() {}

    // Jdbc 1 positioned updates are implemented via
    // sql scan for "...where current of <users-cursor-name>",
    // the addition of mappings from cursor names to query sections,
    // and the subtitution of <users-cursor-name> with <canned-cursor-name> in the pass-thru sql string
    // "...where current of <canned-cursor-name>" when user-defined cursor names are used.
    // Both "canned" cursor names (from our jdbc package set) and user-defined cursor names are mapped.
    // Statement.cursorName_ is initialized to null until the cursor name is requested or set.
    // When set (s.setCursorName()) with a user-defined name, then it is added to the cursor map at that time;
    // When requested (rs.getCursorName()), if the cursor name is still null,
    // then is given the canned cursor name as defined by our jdbc package set and added to the cursor map.
    // Still need to consider how positioned updates should interact with multiple result sets from a stored.
    private final Hashtable<String, Section> positionedUpdateCursorNameToQuerySection_ = new Hashtable<String, Section>();

    private static final int maxNumSections_ = 32768;

    /**
     * Store the Packagename and consistency token information This is called from Section.setPKGNAMCBytes
     *
     * @param b                    bytearray that has the PKGNAMC information to be stored
     * @param resultSetHoldability depending on the holdability store it in the correct byte array packagename and
     *                             consistency token information for when holdability is set to HOLD_CURSORS_OVER_COMMIT
     *                             is stored in holdPKGNAMCBytes and in noHoldPKGNAMCBytes when holdability is set to
     *                             CLOSE_CURSORS_AT_COMMIT
     */
    void setPKGNAMCBytes(byte[] b, int resultSetHoldability) {
        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
            INSTANCE.holdPKGNAMCBytes = b;
        } else if (resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            INSTANCE.noHoldPKGNAMCBytes = b;
        }
    }


    //------------------------entry points----------------------------------------
    
    public Section getDynamicSection() {
        //return Section.DEFAULT;
        return getDynamicSection(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    // Get a section for either a jdbc update or query statement.
    Section getDynamicSection(int resultSetHoldability) {
        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
            return getSection(freeSectionsHold_, packageNameWithHold__, cursorNamePrefixWithHold__, resultSetHoldability);
        } else if (resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            return getSection(freeSectionsNonHold_, packageNameWithNoHold__, cursorNamePrefixWithNoHold__, resultSetHoldability);
        } else {
            throw new UnsupportedOperationException("SQLState.UNSUPPORTED_HOLDABILITY_PROPERTY " + resultSetHoldability);
        }
    }

    private Section getSection(
            Stack<Section> freeSections,
            String packageName,
            String cursorNamePrefix,
            int resultSetHoldability) {

        if (!freeSections.empty()) {
            return freeSections.pop();
        } else if (nextAvailableSectionNumber_ < (maxNumSections_ - 1)) {
            String cursorName = cursorNamePrefix + nextAvailableSectionNumber_;
            Section section = new Section(packageName, nextAvailableSectionNumber_, cursorName, resultSetHoldability);
            nextAvailableSectionNumber_++;
            return section;
        } else
        // unfortunately we have run out of sections
        {
            throw new IllegalStateException("SQLState.EXCEEDED_MAX_SECTIONS 32000");
        }
    }

    void freeSection(Section section, int resultSetHoldability) {
        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
            this.freeSectionsHold_.push(section);
        } else if (resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            this.freeSectionsNonHold_.push(section);
        }
    }

    // Get a section for a jdbc 2 positioned update/delete for the corresponding query.
    // A positioned update section must come from the same package as its query section.
    Section getPositionedUpdateSection(Section querySection) {
//        ClientConnection connection = agent_.connection_;
//        return getDynamicSection(connection.holdability());
        return getDynamicSection(ResultSet.HOLD_CURSORS_OVER_COMMIT); // @AGG assume HOLD_CURSORS_OVER_COMMIT
    }

    // Get a section for a jdbc 1 positioned update/delete for the corresponding query.
    // A positioned update section must come from the same package as its query section.
    Section getPositionedUpdateSection(String cursorName, boolean useExecuteImmediateSection) {
        Section querySection = (Section) positionedUpdateCursorNameToQuerySection_.get(cursorName);

        // If querySection is null, then the user must have provided a bad cursor name.
        // Otherwise, get a new section and save the client's cursor name and the server's
        // cursor name to the new section.
        if (querySection != null) {
            Section section = getPositionedUpdateSection(querySection);
            // getPositionedUpdateSection gets the next available section from the query
            // package, and it has a different cursor name associated with the section.
            // We need to save the client's cursor name and server's cursor name to the
            // new section.
            section.setClientCursorName(querySection.getClientCursorName());
            section.serverCursorNameForPositionedUpdate_ = querySection.getServerCursorName();
            return section;
        } else {
            return null;
        }
    }

    void mapCursorNameToQuerySection(String cursorName, Section section) {
        positionedUpdateCursorNameToQuerySection_.put(cursorName, section);
    }

    void removeCursorNameToQuerySectionMapping(String clientCursorName,
                                               String serverCursorName) {
        if (clientCursorName != null) {
            positionedUpdateCursorNameToQuerySection_.remove(clientCursorName);
        }
        if (serverCursorName != null) {
            positionedUpdateCursorNameToQuerySection_.remove(serverCursorName);
        }
    }

}

