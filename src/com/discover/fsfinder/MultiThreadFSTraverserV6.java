package com.discover.fsfinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.target.FSTarget;
import com.discover.fsfinder.util.FSUtil;
import com.discover.fsfinder.util.LoggingUtil;
import com.discover.fsfinder.util.StringUtil;

public class MultiThreadFSTraverserV6 extends FSTraverser implements MultiThreadFSHandler {
	
	// Timeout in seconds
    private static int WAIT_FOR_NEW_TASKS_TIMEOUT = 10;
    private static int WAIT_TO_CHECK_FOR_COMPLETION = 2;
    private ThreadPoolExecutor threadPoolExecutor;
    private MultiThreadFSContext fsTraverseContext;
    
    private List<RejectedPaths> rejectedPaths = new ArrayList<RejectedPaths>();
    
    public MultiThreadFSTraverserV6(boolean regexExpand) {
        super(regexExpand);
        fsTraverseContext = new MultiThreadFSContext();
        
        LoggingUtil.printlnVerboseMessage("Degree of parallelism of this system: " + MultiThreadFSContext.getParallelismDegree());
        
        threadPoolExecutor = new ThreadPoolExecutor(
        		MultiThreadFSContext.getParallelismDegree(), 
        		MultiThreadFSContext.getParallelismDegree(), 
        		0L, 
        		TimeUnit.MILLISECONDS, 
        		new LinkedBlockingQueue<Runnable>()
        	);        
        
        LoggingUtil.printlnDebugMessage("Starting Traverser.. ThreadPoolExecutor info: Active count = " + threadPoolExecutor.getActiveCount() + 
				". Pool size = " + threadPoolExecutor.getPoolSize() + ". Task Count = " + threadPoolExecutor.getTaskCount() + 
				". Core pool size = " + threadPoolExecutor.getCorePoolSize());
    }

    @Override
    public List<String> traverse(List<FSTarget> targets, List<String> startPaths) throws FSFinderException {
        if (null == targets || targets.isEmpty()) {
            throw new FSFinderException("No targets had been specified for this traverser [" + this.getClass().getName() + "]");
        }
        
        if (ExecutionContext.getInstance().getUserInput().isVerbose()) {
        	LoggingUtil.printlnMessage("\n========================================\n");
        	LoggingUtil.printlnMessage("Traversing the paths:");

        	for (String _tmp: startPaths) {
        		LoggingUtil.printlnMessage(_tmp);	
        	}
        	LoggingUtil.printlnMessage("\n========================================\n");
        }
               
        startPaths.removeAll(Collections.singleton(null));
        validateStartPaths(startPaths);        
        
        fsTraverseContext.setTargets(targets);
        
        // Note: Leaving this line just to keep reference of the previous usage of the timer
        //MultiThreadTimedBarrier.startTimer(WAIT_FOR_NEW_TASKS_TIMEOUT);
        
        for (String startPath: startPaths) {
            if (!fsTraverseContext.getIgnoreCache().containsPath(startPath) && !fsTraverseContext.getPathCache().containsImplicitPath(startPath)) {
                traversePath(startPath);
                fsTraverseContext.getPathCache().addPath(startPath);
            }
        }

        LoggingUtil.printlnVerboseMessage("Beginning to wait for tasks to complete...");
        waitForTasksToComplete(WAIT_TO_CHECK_FOR_COMPLETION);

        LoggingUtil.printlnDebugMessage("Starting to process rejected paths...");
        processRejectedPaths();
        
        LoggingUtil.printlnDebugMessage("Shutting down tasks.....");
        shutDownTasks();
                
        LoggingUtil.printlnDebugMessage("\nExiting traverser...");
        
        return fsTraverseContext.getTargetsFound();
    }

    private void traversePath(String path) {
    	switch (this.getTraverseDirection()) {
    	    case BOTTOM_UP:
    	        traversePathBottomUp(path);
    	        break;
    	        
    	    case TOP_DOWN:
    	    	traversePathTopDown(path, ExecutionContext.getInstance().getUserInput().isBacktrace());
    	        break;
    	}    	
    }

    private void traversePathTopDown(String path, boolean isBackTrace) {
        String pathElements[] = FSUtil.getPathElements(path);
        String _path = new String(path);
        
        if (null == pathElements || pathElements.length <= 0) {
            return;
        }
        
        for (int idx = pathElements.length - 1; idx > 0; idx--) {
            if (idx == pathElements.length - 1) {
                queuePath(_path, null);
                
                // Don't like this hack, but somehow the main thread needs to hold 
                // for the subtasks to start queuing. Otherwise it fails.
                pause(5);
                
                if (!isBackTrace) break;
            }
            
            int pathElmIdx = _path.lastIndexOf(pathElements[idx]);
            
            if (pathElmIdx < 0) {
                throw new FSFinderException("Could not properly traverse path [" + path + "]");
            }
            
            _path = _path.substring(0, pathElmIdx);
            
            queuePath(_path, new String[] { pathElements[idx] });
        }    	
    }
    
    private void traversePathBottomUp(String path) {
    	fsTraverseContext.setSingleStartPathComps(FSUtil.getPathElements(path));
    	
    	queuePath("/", new String[] { String.valueOf(0), 
    					       fsTraverseContext.getSingleStartPathsComps()[0], 
    					       String.valueOf(false) });
    }

    @Override
    protected void validateStartPaths(List<String> startPaths) throws FSFinderException {
        
        if (null == startPaths || startPaths.isEmpty()) {
        	throw new FSFinderException("No start paths were provided by user or found by the hint providers");
        }
    	
    	boolean isSingleStartPath = (startPaths.size() == 1);
    	boolean isBottomUp = false;
    	
    	// Validates that, if a wildcard is passed in a path, there is only a single path being passed.
    	for (String startPath: startPaths) {
    		if (StringUtil.isNotNullOrEmpty(startPath)) {
    			if (startPath.indexOf("*") >= 0) {
    				setTraverseDirection(TRAVERSE_DIRECTION.BOTTOM_UP);
    				startPath = FSUtil.formatBottomUpPath(startPath);
    				
    				if (!isSingleStartPath) {
    					throw new FSFinderException("If a wildcard is used (*), only one path should be passed");
    				}

    				isBottomUp = true;
    			}
    		}
    	}

    	if (!isBottomUp) {
    		// If no wildcards were used, the search is process based, or if user indicated it explicitly, 
    		// approach should be TOP_DOWN. 
    		// However, ONLY IF backtrace was indicated, the app will move back to parent directories in the path.
    		// Otherwise, it will only span threads searching from the very last path component
    		// (which is how a regular find command works in Unix)
    		// For process based searches, backtrace is TRUE implicitly.
    		setTraverseDirection(TRAVERSE_DIRECTION.TOP_DOWN);    		
    	}

    	
        if(!validateAbsolutePaths(startPaths))
            throw new FSFinderException("All provided start paths must be absolute");
    }
    
    private boolean validateAbsolutePaths(List<String> paths) {
        for (String path: paths) {
            File pathFile = new File(path);
            
            if (!pathFile.isAbsolute()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setIgnorePaths(List<String> ignorePaths) {
        for (String ignorePath: ignorePaths) {
            fsTraverseContext.getIgnoreCache().addPath(ignorePath);
        }
    }

    @Override
    public void queuePath(String path, String[] args) {
        List<String> pathList = new LinkedList<String>();
        pathList.add(path);
        
        queuePaths(pathList, args);
    }

    @Override
    public void queuePaths(List<String> paths, String[] args) {
    	try {
    		if (ExecutionContext.getInstance().getUserInput().isDebug()) {
    			LoggingUtil.printlnMessage("Attempting to queue one more worker. Size of paths: " + paths.size()); 
        		LoggingUtil.printlnMessage("Execution info: Active count = " + threadPoolExecutor.getActiveCount() + 
        				". Pool size = " + threadPoolExecutor.getPoolSize() + ". Task Count = " + threadPoolExecutor.getTaskCount() + 
        				". Core pool size = " + threadPoolExecutor.getCorePoolSize());
    		}
    	
            threadPoolExecutor.execute(createFSTraverseWorker(this.getTraverseDirection(), paths, args));
            
    	} catch (RejectedExecutionException ree) {
    		LoggingUtil.printlnVerboseMessage("Some workers were rejected by the thread pool. "
    				+ "Will attempt to resubmit them, but if no target is found you may want to rerun the search");
    		
    		if (ExecutionContext.getInstance().getUserInput().isDebug()) {
    			// If the search has been disabled by any worker(s), attempts to queue new workers may fail
    			// Add failed paths to rejectedPaths list and try to submit them again later
    			LoggingUtil.printlnDebugMessage("[WARNING] Could not queue more workers due to the following exception: " + ree.getMessage() + 
    					"\n" + ree.getCause());    		
    			LoggingUtil.printlnDebugMessage("Execution info: Active count = " + threadPoolExecutor.getActiveCount() + 
    					". Pool size = " + threadPoolExecutor.getPoolSize() + ". Task Count = " + threadPoolExecutor.getTaskCount() + 
    					". Core pool size = " + threadPoolExecutor.getCorePoolSize());
    			
        		LoggingUtil.printlnDebugMessage("Adding new rejected paths: ");
        		for (String pt: paths) {
        			LoggingUtil.printDebugMessage(pt + ", ");
        		}
    		}
    		
    		rejectedPaths.add(new RejectedPaths(paths, args));
    		
    		LoggingUtil.printlnDebugMessage("Added new rejected path. rejected paths size: " + rejectedPaths.size());    		
    	}
    }
    
    @Override
    public List<String> getTraverseFailedPaths() {
        return fsTraverseContext.getTraverseFailedPaths();
    }
    
    private MultiThreadFSTraverseWorker createFSTraverseWorker(
    		TRAVERSE_DIRECTION direction, 
    		List<String> paths, 
    		String... args) {
    	
    	MultiThreadFSTraverseWorker worker = null;
    	
    	switch (direction) {
	        case BOTTOM_UP:
	            worker = new MultiThreadFSTraverseWorkerBottomUp(paths, this, fsTraverseContext, args);
	            break;
	        
	        case TOP_DOWN:
	        	worker = new MultiThreadFSTraverseWorkerTopDown(paths, this, fsTraverseContext, args);
	            break;
    	}
    	
   		LoggingUtil.printlnDebugMessage("Created new worker with direction " + direction.name() + ": " + worker.toString());
    	
    	return worker;
    }

    private void waitForTasksToComplete(int checkTimeout) {
    	
        //while (!MultiThreadTimedBarrier.isDone()) {
    	while (fsTraverseContext.getNumberOfWorkers() > 0) {
            try {
                Thread.sleep(checkTimeout * 1000);
                continue;
            }
            catch(InterruptedException ie) { }
            
            break;
        }
        
        LoggingUtil.printlnDebugMessage("Tasks completed!!!!!!!");
    }
    
    private void shutDownTasks() {
    	
    	LoggingUtil.printlnDebugMessage("Will shutdown tasks!!!!!!!!!!!!!");
        try {
            threadPoolExecutor.shutdownNow();
            if (!threadPoolExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                LoggingUtil.printlnVerboseMessage("Some traverser workers may be stuck or were not able to complete. It's possible that some paths in the file system ended up untraversed");            	
                ExecutionContext.getInstance().printExecutionInfo(null, null);
                
                System.exit(0);
            }
        } catch (InterruptedException ie) {
            LoggingUtil.printlnVerboseMessage("Internal executor service could not be shutdown gracefully. Some paths may have not been traversed.");
        }
    }
    
    private void pause(int millisecs) {
        try {
            Thread.sleep(millisecs);
        } catch (InterruptedException ie) {
    	    throw new FSFinderException(ie);
        }
    }

    @Override
    public void processRejectedPaths() {
    	  
    	LoggingUtil.printlnDebugMessage("Checking RejectedPaths........");
        while (null != rejectedPaths && rejectedPaths.size()  > 0) {
        	LoggingUtil.printlnDebugMessage("Found RejectedPaths, resubmitting.........");
        	List<RejectedPaths> _rejectedPaths = new ArrayList<RejectedPaths>(rejectedPaths);    	
        	rejectedPaths.clear();

        	for (RejectedPaths rpaths: _rejectedPaths) {
        		LoggingUtil.printlnDebugMessage("Attempting to submit rejected paths again. Paths list size: " + rpaths._paths.size());
        		
        		for (String pth: rpaths._paths) {
        			System.out.print(pth + ", ");
        		}
        		queuePaths(rpaths._paths, rpaths._args);
        	}
        	
        	waitForTasksToComplete(WAIT_TO_CHECK_FOR_COMPLETION);
        }
    }
    
    private static class RejectedPaths {
    	List<String> _paths;
    	String[] _args;
    	
    	RejectedPaths(List<String> _paths, String[] _args) {
    		this._paths = _paths;
    		this._args = _args;
    	}
    }
}
