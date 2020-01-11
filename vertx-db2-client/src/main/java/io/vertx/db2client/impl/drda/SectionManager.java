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

import java.util.ArrayList;
import java.util.List;

public class SectionManager {
    
    public static final SectionManager INSTANCE = new SectionManager();
    
    private final List<DB2Package> pkgs = new ArrayList<>(6);
    
    private SectionManager() {
    	// by default there are 3 small and 3 large packages
    	for (int i = 0; i < 3; i++)
    		pkgs.add(new DB2Package(true, i));
    	for (int i = 0; i < 3; i++)
    		pkgs.add(new DB2Package(false, i));
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder("SectionManager info:\n");
    	for (DB2Package p : pkgs)
    		sb.append("  ").append(p).append("\n");
    	return sb.toString();
    }
    
    public int sectionsInUse() {
    	return pkgs.stream().mapToInt(DB2Package::sectionsInUse).sum();
    }

//    // Jdbc 1 positioned updates are implemented via
//    // sql scan for "...where current of <users-cursor-name>",
//    // the addition of mappings from cursor names to query sections,
//    // and the subtitution of <users-cursor-name> with <canned-cursor-name> in the pass-thru sql string
//    // "...where current of <canned-cursor-name>" when user-defined cursor names are used.
//    // Both "canned" cursor names (from our jdbc package set) and user-defined cursor names are mapped.
//    // Statement.cursorName_ is initialized to null until the cursor name is requested or set.
//    // When set (s.setCursorName()) with a user-defined name, then it is added to the cursor map at that time;
//    // When requested (rs.getCursorName()), if the cursor name is still null,
//    // then is given the canned cursor name as defined by our jdbc package set and added to the cursor map.
//    // Still need to consider how positioned updates should interact with multiple result sets from a stored.
//    private final Hashtable<String, Section> positionedUpdateCursorNameToQuerySection_ = new Hashtable<String, Section>();

//    /**
//     * Store the Packagename and consistency token information This is called from Section.setPKGNAMCBytes
//     *
//     * @param b                    bytearray that has the PKGNAMC information to be stored
//     * @param resultSetHoldability depending on the holdability store it in the correct byte array packagename and
//     *                             consistency token information for when holdability is set to HOLD_CURSORS_OVER_COMMIT
//     *                             is stored in holdPKGNAMCBytes and in noHoldPKGNAMCBytes when holdability is set to
//     *                             CLOSE_CURSORS_AT_COMMIT
//     */
//    void setPKGNAMCBytes(byte[] b, int resultSetHoldability) {
//        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
//            INSTANCE.holdPKGNAMCBytes = b;
//        } else if (resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
//            INSTANCE.noHoldPKGNAMCBytes = b;
//        }
//    }

    public Section getDynamicSection() {
    	for (DB2Package p : pkgs) {
    		Section s = p.getFreeSection();
    		if (s != null)
    			return s;
    	}
    	throw new IllegalStateException("All sections are in use: " + this);
    }

//    // Get a section for either a jdbc update or query statement.
//    Section getDynamicSection(int resultSetHoldability) {
//        if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
//            return getSection(freeSectionsHold_, packageNameWithHold__, cursorNamePrefixWithHold__, resultSetHoldability);
//        } else if (resultSetHoldability == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
//            return getSection(freeSectionsNonHold_, packageNameWithNoHold__, cursorNamePrefixWithNoHold__, resultSetHoldability);
//        } else {
//            throw new UnsupportedOperationException("SQLState.UNSUPPORTED_HOLDABILITY_PROPERTY " + resultSetHoldability);
//        }
//    }

//    private Section getSection(
//            ConcurrentLinkedDeque<Section> freeSections,
//            String packageName,
//            String cursorNamePrefix,
//            int resultSetHoldability) {
//        Section freeSection = freeSections.poll();
//        if (freeSection != null)
//            return freeSection;
//        
//        // No free sections to reuse, create a new one
//        if (nextAvailableSectionNumber_ < (maxNumSections_ - 1)) {
//            String cursorName = cursorNamePrefix + nextAvailableSectionNumber_;
//            Section section = new Section(packageName, nextAvailableSectionNumber_, cursorName, resultSetHoldability);
//            nextAvailableSectionNumber_++;
//            return section;
//        } 
//        
//        // unfortunately we have run out of sections
//        throw new IllegalStateException("SQLState.EXCEEDED_MAX_SECTIONS 32000");
//    }

//    // Get a section for a jdbc 2 positioned update/delete for the corresponding query.
//    // A positioned update section must come from the same package as its query section.
//    Section getPositionedUpdateSection(Section querySection) {
////        ClientConnection connection = agent_.connection_;
////        return getDynamicSection(connection.holdability());
//        return getDynamicSection(ResultSet.HOLD_CURSORS_OVER_COMMIT); // @AGG assume HOLD_CURSORS_OVER_COMMIT
//    }
//
//    // Get a section for a jdbc 1 positioned update/delete for the corresponding query.
//    // A positioned update section must come from the same package as its query section.
//    Section getPositionedUpdateSection(String cursorName, boolean useExecuteImmediateSection) {
//        Section querySection = (Section) positionedUpdateCursorNameToQuerySection_.get(cursorName);
//
//        // If querySection is null, then the user must have provided a bad cursor name.
//        // Otherwise, get a new section and save the client's cursor name and the server's
//        // cursor name to the new section.
//        if (querySection != null) {
//            Section section = getPositionedUpdateSection(querySection);
//            // getPositionedUpdateSection gets the next available section from the query
//            // package, and it has a different cursor name associated with the section.
//            // We need to save the client's cursor name and server's cursor name to the
//            // new section.
//            section.setClientCursorName(querySection.getClientCursorName());
//            section.serverCursorNameForPositionedUpdate_ = querySection.getServerCursorName();
//            return section;
//        } else {
//            return null;
//        }
//    }

//    void mapCursorNameToQuerySection(String cursorName, Section section) {
//        positionedUpdateCursorNameToQuerySection_.put(cursorName, section);
//    }
//
//    void removeCursorNameToQuerySectionMapping(String clientCursorName,
//                                               String serverCursorName) {
//        if (clientCursorName != null) {
//            positionedUpdateCursorNameToQuerySection_.remove(clientCursorName);
//        }
//        if (serverCursorName != null) {
//            positionedUpdateCursorNameToQuerySection_.remove(serverCursorName);
//        }
//    }

}

