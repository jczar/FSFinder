package com.discover.fsfinder.target;

import java.util.HashSet;
import java.util.Set;

import com.discover.fsfinder.util.StringUtil;

public class FSTargetFilter {

    private FSTargetManager targetManager;
    private Set<String> ignoreDirSet = new HashSet<String>();

    public FSTargetFilter(FSTargetManager targetManager, String ignoreDirList[]) {
    	
        this.targetManager = targetManager;
        
        if (null != ignoreDirList) {
            for (String ignoreFile: ignoreDirList) {
            	if (StringUtil.isNotNullOrEmpty(ignoreFile)) {
            	    ignoreDirSet.add(ignoreFile);
            	}
            }
        }
    }

    public boolean shouldIgnore(String directoryName) {
        if (null == ignoreDirSet || ignoreDirSet.isEmpty()) {
            return false;
        } else {
            return ignoreDirSet.contains(directoryName);
        }
    }

    public boolean isTarget(String fileName, String absolutePath) {
        FSTarget fsTarget = null;
        
        if ((fsTarget = targetManager.matchTarget(fileName)) != null) {
            return fsTarget.isValid(absolutePath);
        } else {
            return false;
        }
    }
    
}
