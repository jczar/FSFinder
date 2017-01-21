package com.discover.fsfinder.target;

import java.util.*;

public class FSTargetManager {

    private Map<String, FSTarget> targets = new LinkedHashMap<String, FSTarget>();
    
    public void setTargets(List<FSTarget> targetList) {
    	
    	for (FSTarget target: targetList) {
    		targets.put(target.getName(), target);
    	}
    }

    public FSTarget getTarget(String targetName) {
        return (FSTarget) targets.get(targetName);
    }

    public Iterator<String> getTargetNames() {
        if (targets != null && !targets.isEmpty())
            return targets.keySet().iterator();
        
        return null;
    }

    public static List<FSTarget> createFSTarget(String filePath, FSTarget.TYPE type, FSTargetValidator validator) {    	
        if (null == filePath)
            return null;
        
        List<FSTarget> targetList = new ArrayList<FSTarget>();
        
        // There are preceding directories in the absolute file path
        if (filePath.indexOf("/") > 0) {
        	// Break and generate preceding targets in a list
        	
            String filePathDirs[] = filePath.split("/");
            
            if (null != filePathDirs && filePathDirs.length > 0) {
                for (int i = filePathDirs.length - 1; i >= 0; i--) {
                    targetList.add(new FSTarget(filePathDirs[i], 
                    							(i != filePathDirs.length - 1), 
                    							(i != filePathDirs.length - 1) ? FSTarget.TYPE.DIRECTORY : type, 
                    							(i != filePathDirs.length - 1) ? null : validator));
                }

                return targetList;
                
            } else {
                return null; // This should not happen (root directory should not be provided as a target)
            }
        } 
        
        targetList.add(new FSTarget(filePath, false, type, validator));
        
        return targetList;
    }
}
