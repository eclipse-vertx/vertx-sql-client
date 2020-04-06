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
    
    private final List<DB2Package> pkgs = new ArrayList<>(6);
    private final Section staticSection;
    
    SectionManager() {
    	// by default there are 3 small and 3 large packages
        for (int i = 0; i < 3; i++)
          pkgs.add(new DB2Package(true, i));
        for (int i = 0; i < 3; i++)
          pkgs.add(new DB2Package(false, i));
        staticSection = new Section.ImmediateSection(pkgs.get(3));
    }
    
    void configureForZOS() {
      // DB2/Z doesn't have small packages by default -- remove them
      pkgs.removeIf(DB2Package::isSmallPackage);
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder("SectionManager info:\n");
    	for (DB2Package p : pkgs)
    		sb.append("  ").append(p).append("\n");
    	sb.append(staticSection);
    	return sb.toString();
    }
    
    public int sectionsInUse() {
    	return pkgs.stream().mapToInt(DB2Package::sectionsInUse).sum();
    }
    
    public Section getSection(String sql) {
      if (DRDAQueryRequest.isQuery(sql) ||
          "COMMIT".equalsIgnoreCase(sql) ||
          "ROLLBACK".equalsIgnoreCase(sql)) {
        return getDynamicSection();
      } else {
        return staticSection;
      }
    }
    
    private Section getDynamicSection() {
    	for (DB2Package p : pkgs) {
    		Section s = p.getFreeSection();
    		if (s != null)
    			return s;
    	}
    	throw new IllegalStateException("All sections are in use: " + this);
    }

}

