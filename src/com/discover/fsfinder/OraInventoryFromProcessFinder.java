package com.discover.fsfinder;

import java.util.ArrayList;
import java.util.List;

import com.discover.fsfinder.exception.FSFinderException;
import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.target.FSTarget;
import com.discover.fsfinder.target.FSTargetManager;
import com.discover.fsfinder.target.FSTargetValidator;

public class OraInventoryFromProcessFinder extends FSFinder {

    public OraInventoryFromProcessFinder(FSTraverser traverser, FSPathProvider hintProvider, FSPathProvider ignorePathProvider)
        throws FSFinderException {
    	
        super(traverser, hintProvider, ignorePathProvider);
        defineTargets();
    }

    // TODO: Use a FSTargetPriorityQueue instead of a FSTarget/String array
    public void defineTargets() {
        fsTargets = new ArrayList<FSTarget>();
        
        fsTargets.addAll(FSTargetManager.createFSTarget("oraInst.loc", com.discover.fsfinder.target.FSTarget.TYPE.FILE, 
        		new FSTargetValidator() {

        	        // No specific post-validation after this file is found
                    public boolean isValid(String absoluteFilePath) {
                        return true;
                    }
                }
        ));

        // Tests with ContextXML/inventory.xml
        fsTargets.addAll(FSTargetManager.createFSTarget("inventory.xml", com.discover.fsfinder.target.FSTarget.TYPE.FILE, 
        		new FSTargetValidator() {

                    public boolean isValid(String absoluteFilePath) {
                        return absoluteFilePath != null && absoluteFilePath.indexOf("ContentsXML") >= 0;
                    }
                }
        ));
    }

    public List<String> postFindProcess(List<String> targetsFound) {
        return targetsFound; // Explore all oraInst.loc files and find inventory.xml pointed by them
        // As a next step, we need to parse the resultant inventory.xml files and get all Oracle homes
    }
}
