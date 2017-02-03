package com.discover.fsfinder;

import java.io.File;
import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.target.FSTargetFilter;
import com.discover.fsfinder.util.StringUtil;

public class MultiThreadFSTraverseWorkerBottomUp extends MultiThreadFSTraverseWorker {

	private static final String NO_PATH = "None";
	
	private boolean singleLevelMatchAll = false;
	private boolean multiLevelMatchAll = false;

	private boolean isTargetDirNameFound = false;
	private boolean beyondStartPath = false;
	private boolean offStartPath = false;
	
    private String dirNameToFind;
	
    // spcIdx (start path index) refers to the current position of the path component
    // being processed within the initial start path (e.g. 2 for /u01/app/*/config) means "*"
    private int spcIdx = -1;
	
    public MultiThreadFSTraverseWorkerBottomUp(List<String> paths, MultiThreadFSHandler fsHandler, MultiThreadFSContext context, String[] args) {
    	super(paths, fsHandler, context);

    	processInputArgs(args);
    }

    /*
     * Expects the following arguments based on the indexes:
     * 
     *  0: Current Start Path Component index
     *  1: Directory name expected to be found in the current directory level
     *  Discarded --2: Previous wildcard, which will indicate any wildcard processed by a previous worker  
     *  Discarded --   It will be used to control the behaviour of the next path component evaluation
     *  2: OffStartPath. Indicates if the current path has been forked out of the main start path
     *     (e.g. start path: /u01/app/fa/ and current path searches /u01/fusionapps/ onwards)
     */
    private void processInputArgs(String[] args) {
    	try {
    		spcIdx = Integer.parseInt(args[0]);    		
    		dirNameToFind = args[1];
    		offStartPath = Boolean.parseBoolean(args[2]);    	
    		
    		if (!offStartPath) {
    			if (spcIdx < fsContext.getSingleStartPathsComps().length) {    				
    				singleLevelMatchAll = (fsContext.SINGLE_LEVEL_WILDCARD.equals(fsContext.getSingleStartPathsComps()[spcIdx]));
    				multiLevelMatchAll = (fsContext.MULTI_LEVEL_WILDCARD.equals(fsContext.getSingleStartPathsComps()[spcIdx]));

    				// If current path component is a wildcard, then set dirname to find the next directory path component
    				// to emulate zero or 1 regex behaviour ("?") for *, or zero or many ("*") for **
    				// Start Path component index also is set to the next component
    				// IMPORTANT: It is assumed that all wildcards have a subsequent path component
    				if (singleLevelMatchAll || multiLevelMatchAll) {
    					dirNameToFind = fsContext.getSingleStartPathsComps()[spcIdx + 1];
    				}

    			} else {
    				beyondStartPath = true;
    			}
    		}
    		
    	} catch (Exception e) {
    		throw new FSFinderException("Wrong/missing arguments passed to Bottom Up worker constructor: " + args, e);
    	}
    	
    	if (spcIdx < 0) {
    		throw new FSFinderException("Bottom up initialization problem. Start path initial index < 0");
    	}
    	    	
        filter = new FSTargetFilter(fsContext.getTargetManager(), null );
    }
    
    /*
     * Sets expected arguments for subsequently forked workers, which should be the same as the ones documented 
     * in method processInputArgs
     * 
     * (non-Javadoc)
     * @see com.discover.fsfinder.MultiThreadFSTraverseWorker#getChildWorkerArgsForQueue()
     */
    @Override
    public String[] getChildWorkerArgsForQueue(boolean topPriorityChildren) {
    	boolean _offStartPath = false;
    	
    	// Empty string means we'll be past beyond the last start path component
    	// in the next worker, hence there is no next path to look for
    	String nextDirNameToFind = new String();
    	
    	int nextSpcIdx = spcIdx;

    	// Increase spcIdx if requesting argument for non topPriorityChildren 
    	// (which always should increase since they would be offStartPath)
    	if (!topPriorityChildren) {
    		nextSpcIdx++;
    	} else {
    		// If arguments are requested for topPriorityChildren,
    		// then increase spcIdx only in the following cases:
    		// 1. If current worker works beyond start path components (increase by 1)
    		// 2. Current path comp is Wildcard AND target dir name was found (increase by 2)
    		// 3. Current path comp is Not Wildcard (increase by 1, we assume that topPriorityChildren
    		//    is requested for target directory(ies) that matched target dirname already). 
    		if (beyondStartPath) {
    			nextSpcIdx++;
    		} else {
    			if (singleLevelMatchAll || multiLevelMatchAll) {
    				if (isTargetDirNameFound) {
    					// If current path comp is a wildcard and target dir name was found,
    					// move spcIdx 2 positions ahead, as (spcIdx + 1) should be the current
    					// targetDirName and we need now to look for the next path comp (in case there is one)
    					nextSpcIdx += 2;
    				} else {
    					// If current path comp is a wildcard but not target was found, then 
    					// do not increase spcIdx (stay at the same position)
    			    }
    			} else {
    				nextSpcIdx++;
    			}
    		}    	
    	}
    		
        if (topPriorityChildren) {
    		if (nextSpcIdx < fsContext.getSingleStartPathsComps().length)  {
    			nextDirNameToFind = fsContext.getSingleStartPathsComps()[nextSpcIdx];
    		}
    	} else {
    		// If we need arguments for directories that don't match target dirname,
    		// we don't need to pass a next dirname to find.
    		// Leave nextTargetDirName as empty string
    	}
    	    	
        _offStartPath = !topPriorityChildren;
    	
    	return new String[] { String.valueOf(nextSpcIdx), nextDirNameToFind, String.valueOf(_offStartPath) }; 
    }
    
    @Override
    public void processChildrenPath(String dfName, String dfAbsolutePath) {
    	if (StringUtil.isNotNullOrEmpty(dfName)) {
    		if (!offStartPath && !beyondStartPath && 
    				(singleLevelMatchAll || multiLevelMatchAll || dfName.equals(dirNameToFind))) {
    			
    			topChildrenPaths.add(dfAbsolutePath);
   				skipCurrentLevel = (spcIdx == 0);
    			
    			isTargetDirNameFound = true;
    		} else {
    			if (!offStartPath && beyondStartPath) {
    				topChildrenPaths.add(dfAbsolutePath);
    			} else {
    				// No DirNameToFind was found, but scIdx is within
    				// the start patch components range and current path component is NOT wildcard
    				// or current path component is off path
    				childrenPaths.add(dfAbsolutePath);
    			}
    		}
    	}    	
    }    
    
    @Override
    public boolean preDistributionValidate() {
    	// No pre validation needed right now
    	return true;
    }
}
