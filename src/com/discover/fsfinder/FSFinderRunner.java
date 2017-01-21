package com.discover.fsfinder;

import java.util.List;

import com.discover.fsfinder.util.LoggingUtil;

public class FSFinderRunner {

    public static void main(String args[]) {
    	UserCmdLineInput userInput = null;
    	
    	if (null == (userInput = UserCmdLineInput.createUserCmdLineInput(args))) {
    		UserCmdLineInput.printCmdLineUsage();
    		return;
    	}    	
    	ExecutionContext.getInstance().setUserInput(userInput);
    	
    	LoggingUtil.printlnVerboseMessage(userInput.toString());
    	
        FSFinder fsFinder = ExecutionContext.getInstance().getFSFinder();
        
        List<String> targetsFound = fsFinder.findTargets();
        List<String> failedPaths = fsFinder.getTraverseFailedPaths();
        
        ExecutionContext.getInstance().printExecutionInfo(targetsFound, failedPaths);
    }
    
}
