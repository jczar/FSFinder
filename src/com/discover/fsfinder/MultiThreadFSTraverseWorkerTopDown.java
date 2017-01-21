package com.discover.fsfinder;

import java.io.File;
import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.target.FSTargetFilter;
import com.discover.fsfinder.util.StringUtil;

public class MultiThreadFSTraverseWorkerTopDown extends MultiThreadFSTraverseWorker {

    // Only relevant in the context of a Top Down traverse
    // where previous/child path already traversed should be ignored. 
	// Only the main traverse handler can pass this argument, as it orchestrates
	// the top down traversal of a given path
    private String previousTraversedDir;
	
    public MultiThreadFSTraverseWorkerTopDown(List<String> paths, MultiThreadFSHandler fsHandler, MultiThreadFSContext context, String[] args) {
    	super(paths, fsHandler, context);
        
    	processInputArgs(context, args);
    }


    /*
     * Expected input arguments by index:
     * 
     * 0: Previous traversed directory. Since this is a top down traversal,
     *    we should ignore the directory we just came from.
     */
    private void processInputArgs(MultiThreadFSContext fsContext, String[] args) {
        
       	if (args != null && args.length > 0) {
            this.previousTraversedDir = args[0];
      	}
                    
        filter = new FSTargetFilter(fsContext.getTargetManager(), StringUtil.isNotNullOrEmpty(previousTraversedDir) ? 
        		new String[] {
                    previousTraversedDir
                } : null 
            );
    }
        
    /*
     * No parent directory to ignore is expected for workers submitted from a worker
     * 
     * (non-Javadoc)
     * @see com.discover.fsfinder.MultiThreadFSTraverseWorker#getChildWorkerArgsForQueue()
     */
    @Override
    public String[] getChildWorkerArgsForQueue(boolean topPriorityChildren) {
    	return new String[] { }; 
    }
    
    @Override
    public void processChildrenPath(String dfName, String dfAbsolutePath) {
    	topChildrenPaths.add(dfAbsolutePath);
    }
    
    @Override
    public boolean preDistributionValidate() {
    	// No pre validation needed right now
    	return true;
    }
}
