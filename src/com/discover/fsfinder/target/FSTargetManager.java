package com.discover.fsfinder.target;

import java.util.*;

import com.discover.fsfinder.ExecutionContext;
import com.discover.fsfinder.target.FSTarget.EXPR_TYPE;

public class FSTargetManager {

    private Map<String, FSTarget> regularTargets = new LinkedHashMap<String, FSTarget>();
    private List<FSTarget> exprTargets = new ArrayList<FSTarget>();
    
    private boolean isThereExprTarget = false; 
    
    public void setTargets(List<FSTarget> targetList) {
    	
    	for (FSTarget target: targetList) {
    		if (target.getTargetExprType() == EXPR_TYPE.REGULAR) {
    		    regularTargets.put(target.getName(), target);
    		} else {
    			isThereExprTarget = true;
    			exprTargets.add(target);
    		}
    	}
    }

    public FSTarget getRegularTarget(String targetName) {
        return (FSTarget) regularTargets.get(targetName);
    }

    public Iterator<String> getRegularTargetNames() {
        if (regularTargets != null && !regularTargets.isEmpty())
            return regularTargets.keySet().iterator();
        
        return null;
    }
    
    public FSTarget matchTarget(String fileName) {
    	if (!isThereExprTarget) {
    		return getRegularTarget(fileName);
    	} else {
    		for (FSTarget exprTarget: exprTargets) {
    			if (exprTarget.getExprValidator().doesFilenameMatch(fileName)) { 
    				return exprTarget; 
    			}
    		}
    	}
    	
    	return null;
    }
    
    public static List<FSTarget> createFSTarget(String filePath, FSTarget.TYPE type, FSTargetValidator validator) {    	
        if (null == filePath)
            return null;
        
        List<FSTarget> targetList = new ArrayList<FSTarget>();
        
        // There are preceding directories in the absolute file path
        // Note: This use case of passing an absolute path target has not been tested as of 2017-01
        // Probably it's pointless, since we already pass a -s source path parameter 
        if (filePath.indexOf("/") > 0) {
        	// Break and generate preceding targets in a list
        	
            String filePathDirs[] = filePath.split("/");
            
            if (null != filePathDirs && filePathDirs.length > 0) {
            	FSTarget tmpFSTarget = null;
                for (int i = filePathDirs.length - 1; i >= 0; i--) {
                	tmpFSTarget = new FSTarget(filePathDirs[i], 
							(i != filePathDirs.length - 1), 
							(i != filePathDirs.length - 1) ? FSTarget.TYPE.DIRECTORY : type, 
							(i != filePathDirs.length - 1) ? null : validator);
                	tmpFSTarget.getExprValidator().loadTargetRegex(filePathDirs[i], ExecutionContext.getInstance().getUserInput().isRegex());
                	                	
                    targetList.add(tmpFSTarget);
                }

                return targetList;
                
            } else {
                return null; // This should not happen (root directory should not be provided as a target)
            }
        } 
        
        FSTarget newFSTarget = new FSTarget(filePath, false, type, validator);
        newFSTarget.getExprValidator().loadTargetRegex(filePath, ExecutionContext.getInstance().getUserInput().isRegex());
        
        targetList.add(newFSTarget);
        
        return targetList;
    }
}
