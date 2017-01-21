package com.discover.fsfinder;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.target.FSTargetFilter;
import com.discover.fsfinder.util.LoggingUtil;

import java.io.File;
import java.util.*;

public abstract class MultiThreadFSTraverseWorker
    implements Runnable {
	
    protected MultiThreadFSHandler fsHandler;
    protected MultiThreadFSContext fsContext;
    protected FSTargetFilter filter;
    
    protected List<String> childrenPaths = new LinkedList<String>();
    protected List<String> topChildrenPaths = new LinkedList<String>();
    
    protected boolean skipCurrentLevel = false;
    
    private List<String> paths;
    
    public MultiThreadFSTraverseWorker(List<String> paths, MultiThreadFSHandler fsHandler, MultiThreadFSContext context) {
    	this.paths = paths;
        this.fsHandler = fsHandler;
        this.fsContext = context;
    }

    @Override
    public void run() {
        //MultiThreadTimedBarrier.resetTimer();
    	
    	fsContext.increaseNumberOfWorkers();
        processPaths();
        fsContext.decreaseNumberOfWorkers();
    }

    /*
     * The default work distribution behavior is as follows:
     *
     * 1. Each worker receives a set of paths (initially just root)
     * 2. For each given path:
     * 3.    Check IgnoreCache. If the path matches or is contained in any of the paths in the cache, skip
     * 4.    Check VisitedCache. If the path has been visited, skip
     * 3.    Search the targets in the path
     * 4.    Add each path children directories to a unique queue for all paths being iterated
     * 5.    If a non-zero depth is specified, then:
     * 6.                   Add each children directory in the queue, to the set of paths to iterate in the next cycle
     * 7.                   Increase current worker depth  and repeat since step #2
     * 8.                   If depth is >= than max-depth then exit loop and continue to divide work (#10)
     * 9.
     * 10.   Divide the paths in the queue given a specific criteria (initially divide them by a specific number)
     * 11.   Resultant subsections will be delivered to new threads for processing
     */
    private void processPaths() {
        if (null == paths || paths.size() <= 0) {
            return;
        }
        
        if (paths.size() > 100) {
            throw new FSFinderException("Unable to work on so many directories [" + paths.size() + "]");
        }
        
        if (ExecutionContext.getInstance().getUserInput().isDebug()) {                
        	StringBuilder sbr = new StringBuilder();
        	sbr.append("************************** Current Worker paths: **************************\n");
        	for (String p: paths) {
        		sbr.append(p + ", ");
        	}
        	sbr.append("\n");

        	LoggingUtil.printDebugMessage(sbr.toString());
        }
        
        for (String path: paths) {
        	processPath(path);
        }
        
        if (!preDistributionValidate()) {
        	return;
        }
        
        if (MultiThreadFSContext.isKeepSearching()) {
            distributeChildrenPaths();
        }
    }

    protected void distributeChildrenPaths() {        
    	distributeChildrenPaths(topChildrenPaths, true);
    	distributeChildrenPaths(childrenPaths, false);
    }
    
    private void distributeChildrenPaths(List<String> _paths, boolean topPriorityChildren) {
        if (_paths == null || _paths.isEmpty()) {
            return;
        }
   
        if (ExecutionContext.getInstance().getUserInput().isDebug()) {
        	LoggingUtil.printlnMessage("===================");
        	LoggingUtil.printlnMessage("Children to distribute with topPriorityChildren = " + topPriorityChildren + ". Size: " + _paths.size() + ": ");
        	for (String p: _paths) {
        		LoggingUtil.printMessage(p + ", ");
        	}
        	LoggingUtil.printlnMessage("===================");
        }
        
        if (_paths.size() > MultiThreadFSContext.MAX_DIRS_PER_WORKER) {
            for (int c = 0; c <= _paths.size(); c += MultiThreadFSContext.MAX_DIRS_PER_WORKER) {
                int _tmpSize = ((c + MultiThreadFSContext.MAX_DIRS_PER_WORKER) > _paths.size() ? _paths.size() - c : MultiThreadFSContext.MAX_DIRS_PER_WORKER);
                
                List<String> _tmpPathList = _paths.subList(c, c + _tmpSize);
                
                fsHandler.queuePaths(_tmpPathList, getChildWorkerArgsForQueue(topPriorityChildren));
            }

        } else {
            fsHandler.queuePaths(_paths, getChildWorkerArgsForQueue(topPriorityChildren));
        }
    }    

    protected void processPath(String path) {
        try {
            File filePath = new File(path);
            if (filePath.isDirectory()) {
                processDirectory(filePath);
            } else {
                // Ideally, no files should be provided to a worker, but directories.
            	// Just ignore
            }
        } catch(Exception e) {
            // Catch any exception while managing the Directory File and adds
            // the directory to a failedDirs list
            addToFailedDirs(path);
        }
    }    
    
    protected void processDirectory(File directory) {
        File[] directoryFiles = directory.listFiles();
        	
        for (File dirFile: directoryFiles) {
            String dfAbsolutePath = dirFile.getAbsolutePath();
            String dfName = dirFile.getName();
            
            if (dirFile.isFile()) {
                processFile(dfName, dfAbsolutePath);
            } else if (dirFile.isDirectory() && !filter.shouldIgnore(dfName)) {
            	processChildrenPath(dfName, dfAbsolutePath);
            	
            	if (skipCurrentLevel) {
            		break;
            	}
            }
        }
    }    

    protected void processFile(String fileName, String dfAbsolutePath) {
        if (filter.isTarget(fileName, dfAbsolutePath)) {
            fsContext.addTargetFound(dfAbsolutePath);
        }
    }

    protected abstract String[] getChildWorkerArgsForQueue(boolean topPriorityChildren);
    protected abstract void processChildrenPath(String dfName, String absolutePath);
    
    protected abstract boolean preDistributionValidate();

    protected void addToFailedDirs(String directory) {
        fsContext.addToTraverseFailedPaths(directory);
    }

}
