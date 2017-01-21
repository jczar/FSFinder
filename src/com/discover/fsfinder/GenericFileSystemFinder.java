package com.discover.fsfinder;

import java.util.ArrayList;
import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.target.FSTarget;
import com.discover.fsfinder.target.FSTargetManager;
import com.discover.fsfinder.target.FSTargetValidator;

public class GenericFileSystemFinder extends FSFinder {

    public GenericFileSystemFinder(FSTraverser traverser, FSPathProvider hintProvider, FSPathProvider ignorePathProvider)
            throws FSFinderException {
        	
            super(traverser, hintProvider, ignorePathProvider);
            defineTargets();
    }
 
    // TODO: Use a FSTargetPriorityQueue instead of a FSTarget/String array
    public void defineTargets() {
        fsTargets = new ArrayList<FSTarget>();
        
        fsTargets.addAll(FSTargetManager.createFSTarget(ExecutionContext.getInstance().getUserInput().getTargetFile(), FSTarget.TYPE.FILE, 
        		new FSTargetValidator() {
        	
        	        public boolean isValid(String absolutFilePath) {
        	        	return true;
        	        }
        	
                }
        ));
    }

    public List<String> postFindProcess(List<String> targetsFound) {
        return targetsFound; // Explore all oraInst.loc files and find inventory.xml pointed by them
        // As a next step, we need to parse the resultant inventory.xml files and get all Oracle homes
    }
}
