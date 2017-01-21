package com.discover.fsfinder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.discover.fsfinder.target.FSTarget;
import com.discover.fsfinder.target.FSTargetManager;
import com.discover.fsfinder.util.LoggingUtil;

public class MultiThreadFSContext {

    public static final int MAX_DIRS_PER_WORKER = 100;
    
	public final static String SINGLE_LEVEL_WILDCARD = "*";
	public final static String MULTI_LEVEL_WILDCARD = "**";
    
    private FSFinderCache ignoreCache = new FSFinderCache();
    private FSFinderCache pathCache = new FSFinderCache();
    private FSTargetManager targetManager = new FSTargetManager();
    private List<String> targetsFound = new LinkedList<String>(); 
    private List<String> traverseFailedPaths = new LinkedList<String>();
    
    private String[] singleStartPathComps;
   
    private int numberOfOccurrences = 0; 
    private static volatile boolean keepSearching = true;
    
    private static AtomicInteger numberOfWorkers = new AtomicInteger(0);

    public static int getParallelismDegree() {
        return Runtime.getRuntime().availableProcessors();
    }

    public FSFinderCache getPathCache() {
        return pathCache;
    }

    public FSFinderCache getIgnoreCache() {
        return ignoreCache;
    }

    public void setTargets(List<FSTarget> targetList) {
        targetManager.setTargets(targetList);
    }

    public FSTargetManager getTargetManager() {
        return targetManager;
    }

    public void setIgnoreCacheFromList(List<String> ignorePathList) {
    	for (String ignorePath: ignorePathList) {
    		ignoreCache.addPath(ignorePath);
    	}
    }

    public synchronized void addTargetFound(String targetName) {
    	
    	UserCmdLineInput ui = ExecutionContext.getInstance().getUserInput();
    	
    	if (ui.isSearchAll() || getNumberOfOccurrences() < ui.getMaxOccurrences()) {
    		targetsFound.add(targetName);
    		addOneMoreOccurrence();  
    		
    		LoggingUtil.printlnMessage(targetName);

    		if (!ui.isSearchAll() && getNumberOfOccurrences() >= ui.getMaxOccurrences()) {
    			disableSearch();
    		}
    	}
    }

    public List<String> getTargetsFound() {
        return targetsFound;
    }

    public List<String> getTraverseFailedPaths() {
        return traverseFailedPaths;
    }

    public void addToTraverseFailedPaths(String directory) {
        if(traverseFailedPaths == null) {
            traverseFailedPaths = new LinkedList<String>();
        }
        
        traverseFailedPaths.add(directory);
    }
    
    public void setSingleStartPathComps(String[] startPathComps) {
    	singleStartPathComps = startPathComps;
    }
    
    public String[] getSingleStartPathsComps() {
    	return singleStartPathComps;
    }
    
    public int getNumberOfOccurrences() {
    	return numberOfOccurrences;
    }
    
    public void addOneMoreOccurrence() {
    	numberOfOccurrences++;
    }
    
    public int increaseNumberOfWorkers() {
    	int nWorkers = numberOfWorkers.incrementAndGet();
    	
    	LoggingUtil.printlnDebugMessage("Incremented number of workers to: " + nWorkers);
    	
    	return nWorkers;
    }
    
    public int getNumberOfWorkers() {
    	return numberOfWorkers.get();
    }
    
    public int decreaseNumberOfWorkers() {
    	int nWorkers = numberOfWorkers.decrementAndGet();
    	
    	LoggingUtil.printlnDebugMessage("Decremented number of workers to: " + nWorkers);    	
    	
    	return nWorkers;
    }    
    
    public static void disableSearch() {
    	keepSearching = false;
    }
    
    public static boolean isKeepSearching() {
    	return keepSearching;
    }
}
