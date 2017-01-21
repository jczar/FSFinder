package com.discover.fsfinder;

import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.path.DummyIgnorePathProvider;
import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.path.hint.ProcessBasedHintPathProvider;
import com.discover.fsfinder.path.hint.ProcessCwdHintPathProvider;
import com.discover.fsfinder.path.hint.UserInputHintPathProvider;
import com.discover.fsfinder.util.LoggingUtil;

public class ExecutionContext {

    private FSTraverser mtTraverser;

    private FSPathProvider decoratedHintProvider;
    private FSFinder finder;

    //private static IterableHintPathProvider iterableHintProvider;
    //private static IterableOracleInventoryFinder iterableFinder;
    
    private UserCmdLineInput userInput = null;
    
    private static ExecutionContext ctx = null;
    
    private ExecutionContext() {
    }
    
    public static ExecutionContext getInstance() {
    	if (ctx == null) {
    		ctx = new ExecutionContext();
    	}
    	return ctx;
    }
    
    //{
        //iterableHintProvider = new IterableHintPathProvider();
        //iterableFinder = new IterableOracleInventoryFinder();
        
        // #################################################
        // Define list of hint providers
        //List<FSPathProvider> hintPathProviders = new ArrayList<FSPathProvider>();
        //hintPathProviders.add(new ProcessCwdHintPathProvider());
        //hintPathProviders.add(new ProcessBasedHintPathProvider());
        // #################################################
        
        // #################################################
        // Define a decorated set of hint providers
        //iterableHintProvider.setProviderCollection(hintPathProviders);
        
        // ################################################# 
        // Define list of finders to execute sequentially
        //List<FSFinder> finders = new ArrayList<FSFinder>();
        //finders.add(
        		//new OraInventoryFromProcessFinder(mtTraverser, decoratedHintProvider, new DummyIgnorePathProvider())
        //	);
        //iterableFinder.setFinderCollection(finders);
        // #################################################
        
    //}
    
    private void initializeFinder() {
    	if (null == getUserInput()) {
    		throw new FSFinderException("No valid user input has been provided");
    	}
    	
    	mtTraverser = new MultiThreadFSTraverserV6(false);
    	
        decoratedHintProvider = new ProcessCwdHintPathProvider(new ProcessBasedHintPathProvider(new UserInputHintPathProvider()));
        finder = new GenericFileSystemFinder(mtTraverser, decoratedHintProvider, new DummyIgnorePathProvider());    	
    }
    
    public FSFinder getFSFinder() {
    	if (null == finder) {
    		initializeFinder();
    	}
        return finder;
    }
    
    public void setUserInput(UserCmdLineInput userCmdLineInput) {
    	userInput = userCmdLineInput;
    }
    
    public UserCmdLineInput getUserInput() {
    	return userInput;
    }
    
    public void printExecutionInfo(List<String> targetsFound, List<String> failedPaths) {
    	if (getUserInput().isVerbose()) {
    		if (null != targetsFound && !targetsFound.isEmpty()) {
    			LoggingUtil.printlnMessage("\n\n========================================\n");
    			LoggingUtil.printlnMessage("\nTargets found: ");

    			for (String target: targetsFound) {
    				LoggingUtil.printlnMessage(target);
    			}
    		}

    		if (null != failedPaths && !failedPaths.isEmpty()) {
    			LoggingUtil.printlnMessage("\n========================================\n");
    			LoggingUtil.printlnMessage("List of paths that could not be traversed: ");

    			for (String failedPath: failedPaths) {
    				LoggingUtil.printlnMessage(failedPath);
    			}
    		}

    		LoggingUtil.printlnMessage("- Search completed -");
    	}
    }
}
